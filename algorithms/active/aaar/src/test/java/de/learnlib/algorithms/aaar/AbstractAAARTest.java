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
package de.learnlib.algorithms.aaar;

import java.util.ArrayList;
import java.util.HashSet;

import com.google.common.collect.Lists;
import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.examples.LearningExample;
import de.learnlib.oracle.equivalence.SampleSetEQOracle;
import de.learnlib.oracle.membership.SimulatorOracle;
import de.learnlib.util.Experiment;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.conformance.WpMethodTestsIterator;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
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
        final ArrayList<Word<I>> testCases = Lists.newArrayList(iter);

        final SampleSetEQOracle<I, D> eqo = new SampleSetEQOracle<>(false);
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
