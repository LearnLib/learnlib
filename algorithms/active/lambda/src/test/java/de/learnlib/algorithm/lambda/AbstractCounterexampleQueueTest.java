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
package de.learnlib.algorithm.lambda;

import de.learnlib.algorithm.LearningAlgorithm.DFALearner;
import de.learnlib.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.oracle.equivalence.SampleSetEQOracle;
import de.learnlib.oracle.membership.DFASimulatorOracle;
import de.learnlib.util.Experiment.DFAExperiment;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.util.automaton.Automata;
import net.automatalib.util.automaton.builder.AutomatonBuilders;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases for testing counterexample management.
 */
public abstract class AbstractCounterexampleQueueTest {

    private static final CompactDFA<Character> DFA;

    static {
        final Alphabet<Character> alphabet = Alphabets.characters('a', 'b');
        // @formatter:off
        DFA = AutomatonBuilders.newDFA(alphabet)
                               .withInitial("q0")
                               .from("q0")
                                   .on('a').to("q1")
                                   .on('b').to("q1'")
                               .from("q1")
                                   .on('a').to("q2")
                                   .on('b').loop()
                               .from("q1'")
                                   .on('a').loop()
                                   .on('b').to("q2'")
                               .from("q2")
                                   .on('a').to("q3")
                                   .on('b').loop()
                               .from("q2'")
                                .on('a', 'b').loop()
                               .from("q3")
                                   .on('a').to("q0")
                                   .on('b').loop()
                               .withAccepting("q0", "q2'")
                               .create();
        //@formatter:on
    }

    /**
     * Simulates a learning setup in which intermediate counterexamples are discarded (popped from the stack) and the
     * main counterexample needs to be re-evaluated twice.
     */
    @Test
    public void testPop() {
        final Alphabet<Character> alphabet = DFA.getInputAlphabet();
        final DFAMembershipOracle<Character> mqOracle = new DFASimulatorOracle<>(DFA);
        final DFALearner<Character> learner = getLearner(alphabet, mqOracle);

        final SampleSetEQOracle<Character, Boolean> eqOracle = new SampleSetEQOracle<>();
        final Word<Character> a = Word.fromLetter('a');
        final Word<Character> b = new WordBuilder<>('b', 9).toWord();
        eqOracle.addAll(mqOracle, Word.fromWords(b, a, b, a, b, a, b, a));

        final DFAExperiment<Character> experiment = new DFAExperiment<>(learner, eqOracle, alphabet);
        experiment.run();

        final DFA<?, Character> result = experiment.getFinalHypothesis();

        Assert.assertTrue(Automata.testEquivalence(DFA, result, alphabet));
    }

    protected abstract <I> DFALearner<I> getLearner(Alphabet<I> alphabet, DFAMembershipOracle<I> oracle);
}
