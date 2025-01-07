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
package de.learnlib.testsupport;

import de.learnlib.Resumable;
import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.query.DefaultQuery;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.automaton.concept.Output;
import net.automatalib.util.automaton.Automata;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class that checks the workflow of a learning algorithm that implements {@link Resumable}.
 *
 * @param <L>
 *         learner type
 * @param <M>
 *         hypothesis type
 * @param <OR>
 *         membership oracle type
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 * @param <T>
 *         state type
 */
public abstract class AbstractResumableLearnerTest<L extends Resumable<T> & LearningAlgorithm<M, I, D>, M extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & Output<I, D>, OR, I, D, T> {

    protected static final int RANDOM_SEED = 42;

    private M target;

    private L learner;

    private Alphabet<I> inputAlphabet;

    private int rounds;

    @BeforeClass
    public void setup() {
        this.inputAlphabet = getInitialAlphabet();

        this.target = getTarget(inputAlphabet);
        final OR oracle = getOracle(target);

        this.learner = getLearner(oracle, inputAlphabet);

        this.rounds = getRounds();
    }

    protected abstract Alphabet<I> getInitialAlphabet();

    protected abstract M getTarget(Alphabet<I> alphabet);

    protected abstract OR getOracle(M target);

    protected abstract L getLearner(OR oracle, Alphabet<I> alphabet);

    protected abstract int getRounds();

    @Test
    public void testSuspendAndResumeLearner() {
        learner.startLearning();

        int roundsPre = 0, roundsPost = 0;
        byte[] data = null;

        while (true) {
            final M hyp = learner.getHypothesisModel();
            final Word<I> ce = Automata.findSeparatingWord(target, hyp, inputAlphabet);
            if (ce == null) {
                break;
            }
            learner.refineHypothesis(new DefaultQuery<>(Word.epsilon(), ce, target.computeOutput(ce)));
            roundsPre++;

            if (roundsPre == rounds) {
                // serialize the state
                final T state = learner.suspend();
                Assert.assertNotNull(state);
                data = ResumeUtils.toBytes(state);
            }
        }

        // deserialize the state
        Assert.assertNotNull(data);

        // create the learner from the state
        final L learner2 = getLearner(getOracle(target), inputAlphabet);
        learner2.resume(ResumeUtils.fromBytes(data));

        while (true) {
            final M hyp = learner2.getHypothesisModel();
            final Word<I> ce = Automata.findSeparatingWord(target, hyp, inputAlphabet);
            if (ce == null) {
                break;
            }
            learner2.refineHypothesis(new DefaultQuery<>(Word.epsilon(), ce, target.computeOutput(ce)));
            roundsPost++;
        }

        final boolean modelsAreEquivalent =
                Automata.testEquivalence(learner.getHypothesisModel(), learner2.getHypothesisModel(), inputAlphabet);

        Assert.assertTrue(modelsAreEquivalent);
        Assert.assertEquals(roundsPre - roundsPost, rounds);
    }
}
