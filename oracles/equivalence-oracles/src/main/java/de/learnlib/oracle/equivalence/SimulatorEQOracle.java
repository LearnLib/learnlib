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
package de.learnlib.oracle.equivalence;

import java.util.Collection;

import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.Output;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Word;

public class SimulatorEQOracle<I, D>
        implements EquivalenceOracle<UniversalDeterministicAutomaton<?, I, ?, ?, ?>, I, D> {

    private final UniversalDeterministicAutomaton<?, I, ?, ?, ?> reference;
    private final Output<I, D> output;

    public <R extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & Output<I, D>> SimulatorEQOracle(R reference) {
        this.reference = reference;
        this.output = reference;
    }

    @Override
    public DefaultQuery<I, D> findCounterExample(UniversalDeterministicAutomaton<?, I, ?, ?, ?> hypothesis,
                                                 Collection<? extends I> inputs) {
        final Word<I> sep = Automata.findSeparatingWord(reference, hypothesis, inputs);

        if (sep == null) {
            return null;
        }

        return new DefaultQuery<>(sep, output.computeOutput(sep));
    }

    public static class DFASimulatorEQOracle<I> implements DFAEquivalenceOracle<I> {

        private final SimulatorEQOracle<I, Boolean> delegate;

        public DFASimulatorEQOracle(DFA<?, I> dfa) {
            this.delegate = new SimulatorEQOracle<>(dfa);
        }

        @Override
        public DefaultQuery<I, Boolean> findCounterExample(DFA<?, I> hypothesis, Collection<? extends I> inputs) {
            return delegate.findCounterExample(hypothesis, inputs);
        }
    }

    public static class MealySimulatorEQOracle<I, O> implements MealyEquivalenceOracle<I, O> {

        private final SimulatorEQOracle<I, Word<O>> delegate;

        public MealySimulatorEQOracle(MealyMachine<?, I, ?, O> mealy) {
            this.delegate = new SimulatorEQOracle<>(mealy);
        }

        @Override
        public DefaultQuery<I, Word<O>> findCounterExample(MealyMachine<?, I, ?, O> hypothesis,
                                                           Collection<? extends I> inputs) {
            return delegate.findCounterExample(hypothesis, inputs);
        }

    }

}