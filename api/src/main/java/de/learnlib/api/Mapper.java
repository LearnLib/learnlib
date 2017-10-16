/* Copyright (C) 2013-2017 TU Dortmund
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
}
