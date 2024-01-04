/* Copyright (C) 2013-2024 TU Dortmund University
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

import de.learnlib.buildtool.refinement.annotation.GenerateRefinement;
import de.learnlib.buildtool.refinement.annotation.Generic;
import de.learnlib.buildtool.refinement.annotation.Interface;
import de.learnlib.buildtool.refinement.annotation.Map;
import de.learnlib.oracle.SingleQueryOracle;
import de.learnlib.oracle.SingleQueryOracle.SingleQueryOracleDFA;
import de.learnlib.oracle.SingleQueryOracle.SingleQueryOracleMealy;
import de.learnlib.oracle.SingleQueryOracle.SingleQueryOracleMoore;
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
                    generics = "I",
                    parentGenerics = {@Generic("I"), @Generic(clazz = Boolean.class)},
                    parameterMapping = @Map(from = SuffixOutput.class, to = DFA.class, withGenerics = {"?", "I"}),
                    interfaces = @Interface(clazz = SingleQueryOracleDFA.class, generics = "I"))
@GenerateRefinement(name = "MealySimulatorOracle",
                    generics = {"I", "O"},
                    parentGenerics = {@Generic("I"), @Generic(clazz = Word.class, generics = "O")},
                    parameterMapping = @Map(from = SuffixOutput.class,
                                            to = MealyMachine.class,
                                            withGenerics = {"?", "I", "?", "O"}),
                    interfaces = @Interface(clazz = SingleQueryOracleMealy.class, generics = {"I", "O"}))
@GenerateRefinement(name = "MooreSimulatorOracle",
                    generics = {"I", "O"},
                    parentGenerics = {@Generic("I"), @Generic(clazz = Word.class, generics = "O")},
                    parameterMapping = @Map(from = SuffixOutput.class,
                                            to = MooreMachine.class,
                                            withGenerics = {"?", "I", "?", "O"}),
                    interfaces = @Interface(clazz = SingleQueryOracleMoore.class, generics = {"I", "O"}))
@GenerateRefinement(name = "NFASimulatorOracle",
                    generics = "I",
                    parentGenerics = {@Generic("I"), @Generic(clazz = Boolean.class)},
                    parameterMapping = @Map(from = SuffixOutput.class, to = NFA.class, withGenerics = {"?", "I"}),
                    interfaces = @Interface(clazz = SingleQueryOracleDFA.class, generics = "I"))
@GenerateRefinement(name = "SBASimulatorOracle",
                    generics = "I",
                    parentGenerics = {@Generic("I"), @Generic(clazz = Boolean.class)},
                    parameterMapping = @Map(from = SuffixOutput.class, to = SBA.class, withGenerics = {"?", "I"}),
                    interfaces = @Interface(clazz = SingleQueryOracleDFA.class, generics = "I"))
@GenerateRefinement(name = "SEVPASimulatorOracle",
                    generics = "I",
                    parentGenerics = {@Generic("I"), @Generic(clazz = Boolean.class)},
                    parameterMapping = @Map(from = SuffixOutput.class, to = SEVPA.class, withGenerics = {"?", "I"}),
                    interfaces = @Interface(clazz = SingleQueryOracleDFA.class, generics = "I"))
@GenerateRefinement(name = "SPASimulatorOracle",
                    generics = "I",
                    parentGenerics = {@Generic("I"), @Generic(clazz = Boolean.class)},
                    parameterMapping = @Map(from = SuffixOutput.class, to = SPA.class, withGenerics = {"?", "I"}),
                    interfaces = @Interface(clazz = SingleQueryOracleDFA.class, generics = "I"))
@GenerateRefinement(name = "SPMMSimulatorOracle",
                    generics = {"I", "O"},
                    parentGenerics = {@Generic("I"), @Generic(clazz = Word.class, generics = "O")},
                    parameterMapping = @Map(from = SuffixOutput.class,
                                            to = SPMM.class,
                                            withGenerics = {"?", "I", "?", "O"}),
                    interfaces = @Interface(clazz = SingleQueryOracleMealy.class, generics = {"I", "O"}))
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
