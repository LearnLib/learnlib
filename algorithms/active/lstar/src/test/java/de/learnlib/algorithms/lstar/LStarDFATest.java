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
package de.learnlib.algorithms.lstar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.learnlib.algorithms.lstar.ce.ObservationTableCEXHandler;
import de.learnlib.algorithms.lstar.closing.ClosingStrategy;
import de.learnlib.algorithms.lstar.dfa.ExtensibleLStarDFA;
import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.examples.dfa.ExamplePaulAndMary;
import de.learnlib.oracle.equivalence.SimulatorEQOracle;
import de.learnlib.oracle.equivalence.WMethodEQOracle;
import de.learnlib.oracle.equivalence.WpMethodEQOracle;
import de.learnlib.oracle.membership.SimulatorOracle.DFASimulatorOracle;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Symbol;
import org.testng.annotations.Test;

@Test
public class LStarDFATest extends LearningTest {

    @Test
    public void testLStar() {
        ExamplePaulAndMary pmExample = ExamplePaulAndMary.createExample();
        DFA<?, Symbol> targetDFA = pmExample.getReferenceAutomaton();
        Alphabet<Symbol> alphabet = pmExample.getAlphabet();

        DFAMembershipOracle<Symbol> dfaOracle = new DFASimulatorOracle<>(targetDFA);

        // Empty set of suffixes => minimum compliant set
        List<Word<Symbol>> suffixes = Collections.emptyList();

        List<EquivalenceOracle<? super DFA<?, Symbol>, Symbol, Boolean>> eqOracles = new ArrayList<>();

        eqOracles.add(new SimulatorEQOracle<>(targetDFA));
        eqOracles.add(new WMethodEQOracle<>(dfaOracle, 3));
        eqOracles.add(new WpMethodEQOracle<>(dfaOracle, 3));

        for (ObservationTableCEXHandler<? super Symbol, ? super Boolean> handler : LearningTest.CEX_HANDLERS) {
            for (ClosingStrategy<? super Symbol, ? super Boolean> strategy : LearningTest.CLOSING_STRATEGIES) {

                for (EquivalenceOracle<? super DFA<?, Symbol>, Symbol, Boolean> eqOracle : eqOracles) {
                    LearningAlgorithm<? extends DFA<?, Symbol>, Symbol, Boolean> learner =
                            new ExtensibleLStarDFA<>(alphabet, dfaOracle, suffixes, handler, strategy);

                    testLearnModel(targetDFA, alphabet, learner, dfaOracle, eqOracle);
                }
            }
        }
    }

}
