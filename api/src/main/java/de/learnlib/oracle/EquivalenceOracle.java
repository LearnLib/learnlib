/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of LearnLib <https://learnlib.de>.
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
package de.learnlib.oracle;

import java.util.Collection;

import de.learnlib.query.DefaultQuery;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.MooreMachine;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An equivalence oracle, which checks hypothesis automata against the (possibly unknown) system under learning (SUL).
 * <p>
 * Please note that equivalence oracles are implicitly connected to a SUL, there is no explicit references in terms of a
 * {@link MembershipOracle} or such. However, this might be different in implementing classes.
 * <p>
 * <b>CAVEAT:</b> Equivalence oracles serve as an abstraction to tackle the (generally undecidable) problem of black-box
 * equivalence testing. The contract imposed by this interface is that results returned by the {@link
 * #findCounterExample(Object, Collection)} method are in fact counterexamples, <b>BUT</b> a {@code null} result
 * signalling no counterexample was found does <b>not</b> mean that there can be none.
 *
 * @param <A>
 *         automaton type this equivalence oracle works on
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 */
public interface EquivalenceOracle<A, I, D> {

    /**
     * Searches for a counterexample disproving the subjected hypothesis. A counterexample is query which, when
     * performed on the SUL, yields a different output than what was predicted by the hypothesis. If no counterexample
     * could be found (this does not necessarily mean that none exists), {@code null} is returned.
     *
     * @param hypothesis
     *         the conjecture
     * @param inputs
     *         the set of inputs to consider, this should be a subset of the input alphabet of the provided hypothesis
     *
     * @return a query exposing different behavior, or {@code null} if no counterexample could be found. In case a
     * non-{@code null} value is returned, the output field in the {@link DefaultQuery} contains the SUL output for the
     * respective query.
     */
    @Nullable DefaultQuery<I, D> findCounterExample(A hypothesis, Collection<? extends I> inputs);

    /**
     * A specialization of the {@link EquivalenceOracle} interface for a DFA learning scenario.
     *
     * @param <I>
     *         input symbol class
     */
    interface DFAEquivalenceOracle<I> extends EquivalenceOracle<DFA<?, I>, I, Boolean> {}

    /**
     * A specialization of the {@link EquivalenceOracle} interface for a Mealy learning scenario.
     *
     * @param <I>
     *         input symbol class
     * @param <O>
     *         output symbol class
     */
    interface MealyEquivalenceOracle<I, O> extends EquivalenceOracle<MealyMachine<?, I, ?, O>, I, Word<O>> {}

    /**
     * A specialization of the {@link EquivalenceOracle} interface for a Moore learning scenario.
     *
     * @param <I>
     *         input symbol class
     * @param <O>
     *         output symbol class
     */
    interface MooreEquivalenceOracle<I, O> extends EquivalenceOracle<MooreMachine<?, I, ?, O>, I, Word<O>> {}

}
