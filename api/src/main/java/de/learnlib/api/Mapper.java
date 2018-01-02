/* Copyright (C) 2013-2018 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.learnlib.api;

import de.learnlib.api.oracle.MembershipOracle;

/**
 * A mapper that lifts a {@link SUL} or {@link MembershipOracle} from an "abstract" to a "concrete" level.
 * <p>
 * The notion of "abstract" and "concrete" is not universally defined, and mostly depends on the chosen perspective.
 * Generally speaking, the point of a {@code Mapper<AI,AO,CI,CO>} is to translate a {@code SUL<CI,CO>} into a {@code
 * SUL<AI,AO>}, and additionally provide facilities to map exceptions occurring at the concrete level to symbols at the
 * abstract level.
 *
 * @param <AI>
 *         abstract input symbol type.
 * @param <AO>
 *         abstract output symbol type.
 * @param <CI>
 *         concrete input symbol type.
 * @param <CO>
 *         concrete output symbol type.
 *
 * @author Malte Isberner
 */
public interface Mapper<AI, AO, CI, CO> {

    /**
     * Method that is invoked before any translation steps on a word are performed. Usually left un-implemented for
     * stateless mappers.
     */
    default void pre() {}

    /**
     * Method that is invoked after all translation steps on a word are performed. Usually left un-implemented for
     * stateless mappers.
     */
    default void post() {}

    /**
     * Method that maps an abstract input to a corresponding concrete input.
     *
     * @param abstractInput
     *         the abstract input
     *
     * @return the concrete input
     */
    CI mapInput(AI abstractInput);

    /**
     * Method that maps a concrete output to a corresponding abstract output.
     *
     * @param concreteOutput
     *         the concrete output
     *
     * @return the abstract output
     */
    AO mapOutput(CO concreteOutput);

    /**
     * A mapper refinement to establish the contract of a synchronized, symbol-wise translation of input words for
     * reactive systems. This means, after each call to {@link #mapInput(Object)} the next call on {@code this} object
     * will be {@link #mapOutput(Object)} which is passed the immediate answer to the previously mapped input.
     *
     * @param <AI>
     *         abstract input symbol type.
     * @param <AO>
     *         abstract output symbol type.
     * @param <CI>
     *         concrete input symbol type.
     * @param <CO>
     *         concrete output symbol type.
     *
     * @author frohme
     * @see AsynchronousMapper
     */
    interface SynchronousMapper<AI, AO, CI, CO> extends Mapper<AI, AO, CI, CO> {}

    /**
     * A mapper refinement to establish the contract of a asynchronous, query-wise translation of input words. This
     * means, for a sequence of input symbols, {@link #mapInput(Object)} may be called multiple times before any call to
     * {@link #mapOutput(Object)} occurs.
     * <p>
     * Especially in the context of translating {@link de.learnlib.api.query.Query queries} for mealy machines, which
     * support the concept of un-answered prefixes (combined with answered suffixes) this means, the number of {@link
     * #mapInput(Object)} invocations may be larger than the size of the output word passed to the {@link
     * #mapOutput(Object)} function.
     *
     * @param <AI>
     *         abstract input symbol type.
     * @param <AO>
     *         abstract output symbol type.
     * @param <CI>
     *         concrete input symbol type.
     * @param <CO>
     *         concrete output symbol type.
     *
     * @author frohme
     * @see SynchronousMapper
     */
    interface AsynchronousMapper<AI, AO, CI, CO> extends Mapper<AI, AO, CI, CO> {}
}
