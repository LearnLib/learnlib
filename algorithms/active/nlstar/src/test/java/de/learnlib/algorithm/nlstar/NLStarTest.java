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
package de.learnlib.algorithm.nlstar;

import de.learnlib.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.oracle.equivalence.SampleSetEQOracle;
import de.learnlib.oracle.membership.NFASimulatorOracle;
import de.learnlib.util.Experiment;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.NFA;
import net.automatalib.automaton.fsa.impl.CompactNFA;
import net.automatalib.util.automaton.Automata;
import net.automatalib.util.automaton.builder.AutomatonBuilders;
import net.automatalib.util.automaton.fsa.NFAs;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

public class NLStarTest {

    /**
     * Test case for the bug described in issue <a href="https://github.com/LearnLib/learnlib/issues/70">#70</a>.
     */
    @Test
    public void testIssue70() {
        final Alphabet<Character> alphabet = Alphabets.characters('a', 'b');

        // @formatter:off
        final CompactNFA<Character> nfa = AutomatonBuilders.newNFA(alphabet)
                                                           .withInitial("q0")
                                                           .from("q0")
                                                                .on('a').to("q1")
                                                                .on('b').to("q2")
                                                           .from("q1").on('b').to("q1")
                                                           .from("q2").on('a').to("q2")
                                                           .withAccepting("q1", "q2")
                                                           .create();
        // @formatter:on

        final DFAMembershipOracle<Character> mqOracle = new NFASimulatorOracle<>(nfa);

        final SampleSetEQOracle<Character, Boolean> eqOracle = new SampleSetEQOracle<>();
        eqOracle.addAll(mqOracle,
                        Word.fromLetter('a'),
                        Word.fromString("ab"),
                        Word.fromString("aa"),
                        Word.fromString("bab"));

        final NLStarLearner<Character> learner = new NLStarLearner<>(alphabet, mqOracle);

        final Experiment<NFA<?, Character>> experiment = new Experiment<>(learner, eqOracle, alphabet);
        experiment.run();
        final NFA<?, Character> hyp = experiment.getFinalHypothesis();

        Assert.assertEquals(nfa.size(), hyp.size());
        Assert.assertTrue(Automata.testEquivalence(NFAs.determinize(nfa, false, false),
                                                   NFAs.determinize(hyp, alphabet, false, false),
                                                   alphabet));
    }
}
