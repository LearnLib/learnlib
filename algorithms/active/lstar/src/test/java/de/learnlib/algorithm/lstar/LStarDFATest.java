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
package de.learnlib.algorithm.lstar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.algorithm.lstar.ce.ObservationTableCEXHandler;
import de.learnlib.algorithm.lstar.closing.ClosingStrategy;
import de.learnlib.algorithm.lstar.dfa.ExtensibleLStarDFA;
import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.oracle.equivalence.SimulatorEQOracle;
import de.learnlib.oracle.equivalence.WMethodEQOracle;
import de.learnlib.oracle.equivalence.WpMethodEQOracle;
import de.learnlib.oracle.membership.DFASimulatorOracle;
import de.learnlib.testsupport.example.dfa.ExamplePaulAndMary;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.word.Word;
import org.testng.annotations.Test;

@Test
public class LStarDFATest extends LearningTest {

    @Test
    public void testLStar() {
        ExamplePaulAndMary pmExample = ExamplePaulAndMary.createExample();
        DFA<?, String> targetDFA = pmExample.getReferenceAutomaton();
        Alphabet<String> alphabet = pmExample.getAlphabet();

        DFAMembershipOracle<String> dfaOracle = new DFASimulatorOracle<>(targetDFA);

        // Empty set of suffixes => minimum compliant set
        List<Word<String>> suffixes = Collections.emptyList();

        List<EquivalenceOracle<? super DFA<?, String>, String, Boolean>> eqOracles = new ArrayList<>();

        eqOracles.add(new SimulatorEQOracle<>(targetDFA));
        eqOracles.add(new WMethodEQOracle<>(dfaOracle, 3));
        eqOracles.add(new WpMethodEQOracle<>(dfaOracle, 3));

        for (ObservationTableCEXHandler<? super String, ? super Boolean> handler : LearningTest.CEX_HANDLERS) {
            for (ClosingStrategy<? super String, ? super Boolean> strategy : LearningTest.CLOSING_STRATEGIES) {

                for (EquivalenceOracle<? super DFA<?, String>, String, Boolean> eqOracle : eqOracles) {
                    LearningAlgorithm<? extends DFA<?, String>, String, Boolean> learner =
                            new ExtensibleLStarDFA<>(alphabet, dfaOracle, suffixes, handler, strategy);

                    testLearnModel(targetDFA, alphabet, learner, eqOracle);
                }
            }
        }
    }

}
