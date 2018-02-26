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
package de.learnlib.oracle.membership;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import de.learnlib.api.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.oracle.OmegaMembershipOracle;
import de.learnlib.api.oracle.OmegaQueryAnswerer;
import de.learnlib.api.oracle.SingleQueryOmegaOracle;
import de.learnlib.api.query.OmegaQuery;
import de.learnlib.oracle.membership.SimulatorOracle.DFASimulatorOracle;
import de.learnlib.oracle.membership.SimulatorOracle.MealySimulatorOracle;
import de.learnlib.util.MQUtil;
import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.commons.util.Pair;
import net.automatalib.ts.simple.SimpleDTS;
import net.automatalib.words.Word;

/**
 * Answers {@link OmegaQuery}s by simulating an automaton.
 *
 * @author Jeroen Meijer
 *
 * @see SimulatorOracle
 *
 * @param <S> the state type.
 * @param <I> the input type.
 * @param <D> the output type.
 * @param <SO> the SimulatorOracle type.
 */
public class SimulatorOmegaOracle<S, I, D, SO extends SimulatorOracle<I, D>>
        implements SingleQueryOmegaOracle<S, I, D> {

    /**
     * The automaton to simulate.
     */
    private final SimpleDTS<S, I> simpleDTS;

    /**
     * @see #getSimulatorOracle()
     */
    private final SO simulatorOracle;

    /**
     * Constructs a new {@link SimulatorOmegaOracle}.
     *
     * @param automaton the automaton to simulate.
     * @param simulatorOracle the {@link SimulatorOracle} used to answer {@link de.learnlib.api.query.Query}s.
     * @param <A> the automaton type.
     */
    public <A extends SuffixOutput<I, D> & SimpleDTS<S, I>> SimulatorOmegaOracle(A automaton, SO simulatorOracle) {
        this.simpleDTS = automaton;
        this.simulatorOracle = simulatorOracle;
    }

    /**
     * Gets the {@link SimulatorOracle} used to answer {@link de.learnlib.api.query.Query}s.
     *
     * @return the SimulatorOracle.
     */
    public SO getSimulatorOracle() {
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

    @Override
    public void processQueries(Collection<? extends OmegaQuery<S, I, D>> queries) {
        MQUtil.answerOmegaQueriesAuto(this, queries);
    }

    /**
     * Returns an answer for an {@link OmegaQuery}.
     *
     * The output is obtained through the {@link SimulatorOracle}, while the states are obtained by means of creating
     * several access sequences to states in the simulated automaton.
     *
     * @see OmegaQueryAnswerer#answerQuery(Word, Word, Set)
     */
    @Override
    public Pair<D, List<S>> answerQuery(Word<I> prefix, Word<I> suffix, Set<Integer> indices) {
        final List<S> states = new ArrayList<>();

        for (int i : indices) {
            states.add(simpleDTS.getState(prefix.concat(suffix).prefix(i)));
        }

        return Pair.of(simulatorOracle.answerQuery(prefix, suffix), states);
    }

    public static class DFASimulatorOmegaOracle<S, I>
            extends SimulatorOmegaOracle<S, I, Boolean, DFASimulatorOracle<I>>
            implements DFAOmegaMembershipOracle<S, I> {

        public DFASimulatorOmegaOracle(DFA<S, I> automaton) {
            super(automaton, new DFASimulatorOracle<>(automaton));
        }

        @Override
        public DFAMembershipOracle<I> getDFAMembershipOracle() {
            return getSimulatorOracle();
        }
    }

    public static class MealySimulatorOmegaOracle<S, I, O>
            extends SimulatorOmegaOracle<S, I, Word<O>, MealySimulatorOracle<I, O>>
            implements MealyOmegaMembershipOracle<S, I, O> {

        public MealySimulatorOmegaOracle(MealyMachine<S, I, ?, O> automaton) {
            super(automaton, new MealySimulatorOracle<>(automaton));
        }

        @Override
        public MealyMembershipOracle<I, O> getMealyMembershipOracle() {
            return getSimulatorOracle();
        }
    }
}
