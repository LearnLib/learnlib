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

import de.learnlib.oracle.SingleQueryOracle;
import de.learnlib.oracle.SingleQueryOracle.SingleQueryOracleDFA;
import de.learnlib.oracle.SingleQueryOracle.SingleQueryOracleMealy;
import de.learnlib.oracle.SingleQueryOracle.SingleQueryOracleMoore;
import de.learnlib.tooling.annotation.refinement.GenerateRefinement;
import de.learnlib.tooling.annotation.refinement.Generic;
import de.learnlib.tooling.annotation.refinement.Interface;
import de.learnlib.tooling.annotation.refinement.Mapping;
import net.automatalib.automaton.concept.SuffixOutput;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.NFA;
import net.automatalib.automaton.procedural.SBA;
import net.automatalib.automaton.procedural.SPA;
import net.automatalib.automaton.procedural.SPMM;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.MooreMachine;
import net.automatalib.automaton.vpa.SEVPA;
import net.automatalib.word.Word;

/**
 * A membership oracle backed by an automaton. The automaton must implement the {@link SuffixOutput} concept, allowing
 * to identify a suffix part in the output (relative to a prefix/suffix subdivision in the input).
 * <p>
 * <b>Implementation note</b>: Under the assumption that read-operations do not alter the internal state of the
 * automaton, this oracle is thread-safe.
 *
 * @param <I>
 *         input symbol type
 * @param <D>
 *         (suffix) output domain type
 */
@GenerateRefinement(name = "DFASimulatorOracle",
                    generics = @Generic(value = "I", desc = "input symbol type"),
                    parentGenerics = {@Generic("I"), @Generic(clazz = Boolean.class)},
                    typeMappings = @Mapping(from = SuffixOutput.class,
                                            to = DFA.class,
                                            generics = {@Generic("?"), @Generic("I")}),
                    interfaces = @Interface(clazz = SingleQueryOracleDFA.class, generics = @Generic("I")))
@GenerateRefinement(name = "MealySimulatorOracle",
                    generics = {@Generic(value = "I", desc = "input symbol type"),
                                @Generic(value = "O", desc = "output symbol type")},
                    parentGenerics = {@Generic("I"), @Generic(clazz = Word.class, generics = "O")},
                    typeMappings = @Mapping(from = SuffixOutput.class,
                                            to = MealyMachine.class,
                                            generics = {@Generic("?"), @Generic("I"), @Generic("?"), @Generic("O")}),
                    interfaces = @Interface(clazz = SingleQueryOracleMealy.class,
                                            generics = {@Generic("I"), @Generic("O")}))
@GenerateRefinement(name = "MooreSimulatorOracle",
                    generics = {@Generic(value = "I", desc = "input symbol type"),
                                @Generic(value = "O", desc = "output symbol type")},
                    parentGenerics = {@Generic("I"), @Generic(clazz = Word.class, generics = "O")},
                    typeMappings = @Mapping(from = SuffixOutput.class,
                                            to = MooreMachine.class,
                                            generics = {@Generic("?"), @Generic("I"), @Generic("?"), @Generic("O")}),
                    interfaces = @Interface(clazz = SingleQueryOracleMoore.class,
                                            generics = {@Generic("I"), @Generic("O")}))
@GenerateRefinement(name = "NFASimulatorOracle",
                    generics = @Generic(value = "I", desc = "input symbol type"),
                    parentGenerics = {@Generic("I"), @Generic(clazz = Boolean.class)},
                    typeMappings = @Mapping(from = SuffixOutput.class,
                                            to = NFA.class,
                                            generics = {@Generic("?"), @Generic("I")}),
                    interfaces = @Interface(clazz = SingleQueryOracleDFA.class, generics = @Generic("I")))
@GenerateRefinement(name = "SBASimulatorOracle",
                    generics = @Generic(value = "I", desc = "input symbol type"),
                    parentGenerics = {@Generic("I"), @Generic(clazz = Boolean.class)},
                    typeMappings = @Mapping(from = SuffixOutput.class,
                                            to = SBA.class,
                                            generics = {@Generic("?"), @Generic("I")}),
                    interfaces = @Interface(clazz = SingleQueryOracleDFA.class, generics = @Generic("I")))
@GenerateRefinement(name = "SEVPASimulatorOracle",
                    generics = @Generic(value = "I", desc = "input symbol type"),
                    parentGenerics = {@Generic("I"), @Generic(clazz = Boolean.class)},
                    typeMappings = @Mapping(from = SuffixOutput.class,
                                            to = SEVPA.class,
                                            generics = {@Generic("?"), @Generic("I")}),
                    interfaces = @Interface(clazz = SingleQueryOracleDFA.class, generics = @Generic("I")))
@GenerateRefinement(name = "SPASimulatorOracle",
                    generics = @Generic(value = "I", desc = "input symbol type"),
                    parentGenerics = {@Generic("I"), @Generic(clazz = Boolean.class)},
                    typeMappings = @Mapping(from = SuffixOutput.class,
                                            to = SPA.class,
                                            generics = {@Generic("?"), @Generic("I")}),
                    interfaces = @Interface(clazz = SingleQueryOracleDFA.class, generics = @Generic("I")))
@GenerateRefinement(name = "SPMMSimulatorOracle",
                    generics = {@Generic(value = "I", desc = "input symbol type"),
                                @Generic(value = "O", desc = "output symbol type")},
                    parentGenerics = {@Generic("I"), @Generic(clazz = Word.class, generics = "O")},
                    typeMappings = @Mapping(from = SuffixOutput.class,
                                            to = SPMM.class,
                                            generics = {@Generic("?"), @Generic("I"), @Generic("?"), @Generic("O")}),
                    interfaces = @Interface(clazz = SingleQueryOracleMealy.class,
                                            generics = {@Generic("I"), @Generic("O")}))
public class SimulatorOracle<I, D> implements SingleQueryOracle<I, D> {

    private final SuffixOutput<I, D> automaton;

    /**
     * Constructor.
     *
     * @param automaton
     *         the suffix-observable automaton
     */
    public SimulatorOracle(SuffixOutput<I, D> automaton) {
        this.automaton = automaton;
    }

    @Override
    public D answerQuery(Word<I> prefix, Word<I> suffix) {
        return automaton.computeSuffixOutput(prefix, suffix);
    }
}
