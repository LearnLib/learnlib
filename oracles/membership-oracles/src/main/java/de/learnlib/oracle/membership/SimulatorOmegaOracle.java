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
package de.learnlib.oracle.membership;

import java.util.ArrayList;
import java.util.List;

import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.oracle.OmegaMembershipOracle;
import de.learnlib.oracle.OmegaQueryAnswerer;
import de.learnlib.oracle.SingleQueryOmegaOracle;
import de.learnlib.query.OmegaQuery;
import de.learnlib.query.Query;
import net.automatalib.automaton.concept.SuffixOutput;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.common.util.Pair;
import net.automatalib.ts.simple.SimpleDTS;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Answers {@link OmegaQuery}s by simulating an automaton.
 * <p>
 * <b>Implementation note</b>: Under the assumption that read-operations do not alter the internal state of the
 * automaton, this oracle is thread-safe.
 *
 * @see SimulatorOracle
 *
 * @param <S> the state type.
 * @param <I> the input type.
 * @param <D> the output type.
 */
public class SimulatorOmegaOracle<S extends Object, I, D> implements SingleQueryOmegaOracle<S, I, D> {

    /**
     * The automaton to simulate.
     */
    private final SimpleDTS<S, I> simpleDTS;

    /**
     * The oracle to answer queries.
     */
    private final SimulatorOracle<I, D> simulatorOracle;

    /**
     * Constructs a new {@link SimulatorOmegaOracle}.
     *
     * @param automaton the automaton to simulate.
     * @param simulatorOracle the {@link SimulatorOracle} used to answer {@link Query}s.
     * @param <A> the automaton type.
     */
    public <A extends SuffixOutput<I, D> & SimpleDTS<S, I>> SimulatorOmegaOracle(A automaton, SimulatorOracle<I, D> simulatorOracle) {
        this.simpleDTS = automaton;
        this.simulatorOracle = simulatorOracle;
    }

    /**
     * Gets the {@link SimulatorOracle} used to answer {@link Query}s.
     *
     * @return the SimulatorOracle.
     */
    @Override
    public MembershipOracle<I, D> getMembershipOracle() {
        return simulatorOracle;
    }

    /**
     * Test for state equivalence by simply invoking {@link Object#equals(Object)}.
     *
     * @see OmegaMembershipOracle#isSameState(Word, Object, Word, Object)
     */
    @Override
    public boolean isSameState(Word<I> input1, S s1, Word<I> input2, S s2) {
        return s1.equals(s2);
    }

    /**
     * Returns an answer for an {@link OmegaQuery}.
     * <p>
     * The output is obtained through the {@link SimulatorOracle}, while the states are obtained by means of creating
     * two access sequences to states in the simulated automaton.
     *
     * @see OmegaQueryAnswerer#answerQuery(Word, Word, int)
     */
    @Override
    public Pair<@Nullable D, Integer> answerQuery(Word<I> prefix, Word<I> loop, int repeat) {
        assert repeat > 0;

        final WordBuilder<I> wb = new WordBuilder<>(prefix.length() + loop.length() * repeat, prefix);

        S stateIter = simpleDTS.getState(wb);

        if (stateIter == null) {
            return Pair.of(null, -1);
        }

        final List<S> states = new ArrayList<>(repeat + 1);
        states.add(stateIter);

        for (int i = 0; i < repeat; i++) {
            final S nextState = simpleDTS.getSuccessor(stateIter, loop);

            if (nextState == null) {
                return Pair.of(null, -1);
            }

            wb.append(loop);

            int prefixLength = prefix.length();
            for (S s : states) {
                if (isSameState(wb.toWord(0, prefixLength), s, wb.toWord(), nextState)) {
                    return Pair.of(simulatorOracle.answerQuery(wb.toWord()), i + 1);
                }
                prefixLength += loop.length();
            }

            states.add(nextState);
            stateIter = nextState;
        }

        return Pair.of(null, -1);
    }

    public static class DFASimulatorOmegaOracle<S extends Object, I>
            extends SimulatorOmegaOracle<S, I, Boolean>
            implements SingleQueryOmegaOracleDFA<S, I> {

        private final DFA<S, I> automaton;

        public DFASimulatorOmegaOracle(DFA<S, I> automaton) {
            super(automaton, new DFASimulatorOracle<>(automaton));
            this.automaton = automaton;
        }

        @Override
        public DFAMembershipOracle<I> getMembershipOracle() {
            return new DFASimulatorOracle<>(automaton);
        }
    }

    public static class MealySimulatorOmegaOracle<S extends Object, I, O>
            extends SimulatorOmegaOracle<S, I, Word<O>>
            implements SingleQueryOmegaOracleMealy<S, I, O> {

        private final MealyMachine<?, I, ?, O> automaton;

        public MealySimulatorOmegaOracle(MealyMachine<S, I, ?, O> automaton) {
            super(automaton, new MealySimulatorOracle<>(automaton));
            this.automaton = automaton;
        }

        @Override
        public MealyMembershipOracle<I, O> getMembershipOracle() {
            return new MealySimulatorOracle<>(automaton);
        }
    }
}
