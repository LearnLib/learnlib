/* Copyright (C) 2013-2023 TU Dortmund
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

import de.learnlib.buildtool.refinement.annotation.GenerateRefinement;
import de.learnlib.buildtool.refinement.annotation.Generic;
import de.learnlib.buildtool.refinement.annotation.Interface;
import de.learnlib.buildtool.refinement.annotation.Map;
import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.oracle.EquivalenceOracle.DFAEquivalenceOracle;
import de.learnlib.oracle.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.oracle.EquivalenceOracle.MooreEquivalenceOracle;
import de.learnlib.query.DefaultQuery;
import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.automaton.concept.Output;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.MooreMachine;
import net.automatalib.util.automaton.Automata;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

@GenerateRefinement(name = "DFASimulatorEQOracle",
                    generics = "I",
                    parentGenerics = {@Generic(clazz = DFA.class, generics = {"?", "I"}),
                                      @Generic("I"),
                                      @Generic(clazz = Boolean.class)},
                    parameterMapping = @Map(from = UniversalDeterministicAutomaton.class,
                                            to = DFA.class,
                                            withGenerics = {"?", "I"}),
                    interfaces = @Interface(clazz = DFAEquivalenceOracle.class, generics = "I"))
@GenerateRefinement(name = "MealySimulatorEQOracle",
                    generics = {"I", "O"},
                    parentGenerics = {@Generic(clazz = MealyMachine.class, generics = {"?", "I", "?", "O"}),
                                      @Generic("I"),
                                      @Generic(clazz = Word.class, generics = "O")},
                    parameterMapping = @Map(from = UniversalDeterministicAutomaton.class,
                                            to = MealyMachine.class,
                                            withGenerics = {"?", "I", "?", "O"}),
                    interfaces = @Interface(clazz = MealyEquivalenceOracle.class, generics = {"I", "O"}))
@GenerateRefinement(name = "MooreSimulatorEQOracle",
                    generics = {"I", "O"},
                    parentGenerics = {@Generic(clazz = MooreMachine.class, generics = {"?", "I", "?", "O"}),
                                      @Generic("I"),
                                      @Generic(clazz = Word.class, generics = "O")},
                    parameterMapping = @Map(from = UniversalDeterministicAutomaton.class,
                                            to = MooreMachine.class,
                                            withGenerics = {"?", "I", "?", "O"}),
                    interfaces = @Interface(clazz = MooreEquivalenceOracle.class, generics = {"I", "O"}))
public class SimulatorEQOracle<A extends UniversalDeterministicAutomaton<?, I, ?, ?, ?>, I, D>
        implements EquivalenceOracle<A, I, D> {

    private final UniversalDeterministicAutomaton<?, I, ?, ?, ?> reference;
    private final Output<I, D> output;

    public <R extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & Output<I, D>> SimulatorEQOracle(R reference) {
        this.reference = reference;
        this.output = reference;
    }

    @Override
    public @Nullable DefaultQuery<I, D> findCounterExample(A hypothesis, Collection<? extends I> inputs) {
        final Word<I> sep = Automata.findSeparatingWord(reference, hypothesis, inputs);

        if (sep == null) {
            return null;
        }

        return new DefaultQuery<>(sep, output.computeOutput(sep));
    }
}
