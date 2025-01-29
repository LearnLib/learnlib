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
package de.learnlib.algorithm.aaar;

import java.util.HashSet;
import java.util.List;

import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.equivalence.SampleSetEQOracle;
import de.learnlib.oracle.membership.SimulatorOracle;
import de.learnlib.testsupport.example.LearningExample;
import de.learnlib.util.Experiment;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.automaton.concept.SuffixOutput;
import net.automatalib.common.util.collection.IteratorUtil;
import net.automatalib.util.automaton.Automata;
import net.automatalib.util.automaton.conformance.WpMethodTestsIterator;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

public abstract class AbstractAAARTest<L extends AbstractAAARLearner<?, A, A, I, I, D>, I, D, A extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & SuffixOutput<I, D>> {

    private final Alphabet<I> alphabet;
    private final A automaton;
    protected final L aaarLearner;

    public AbstractAAARTest(LearningExample<I, A> learningExample) {
        this.alphabet = learningExample.getAlphabet();
        this.automaton = learningExample.getReferenceAutomaton();
        this.aaarLearner = getLearner(alphabet, new SimulatorOracle<>(automaton));
    }

    @Test
    public void testAbstractHypothesisEquivalence() {

        final WpMethodTestsIterator<I> iter = new WpMethodTestsIterator<>(automaton, alphabet);
        final List<Word<I>> testCases = IteratorUtil.list(iter);

        final SampleSetEQOracle<I, D> eqo = new SampleSetEQOracle<>();
        eqo.addAll(new SimulatorOracle<>(automaton), testCases);

        final LearningAlgorithm<A, I, D> learner =
                new TranslatingLearnerWrapper<>((AbstractAAARLearner<?, A, A, I, I, D>) aaarLearner);
        final Experiment<A> exp = new Experiment<>(learner, eqo, alphabet);

        exp.run();

        final A hyp = aaarLearner.getHypothesisModel();

        Assert.assertEquals(new HashSet<>(aaarLearner.getLearnerAlphabet()), new HashSet<>(alphabet));
        Assert.assertTrue(Automata.testEquivalence(automaton, hyp, alphabet));
    }

    protected abstract L getLearner(Alphabet<I> alphabet, MembershipOracle<I, D> oracle);
}
