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
package de.learnlib.testsupport;

import de.learnlib.Resumable;
import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.query.DefaultQuery;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.util.automaton.Automata;
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
public abstract class AbstractResumableLearnerTest<L extends Resumable<T> & LearningAlgorithm<M, I, D>, M extends UniversalDeterministicAutomaton<?, I, ?, ?, ?>, OR, I, D, T> {

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

    protected abstract EquivalenceOracle<M, I, D> getEquivalenceOracle(M target);

    protected abstract L getLearner(OR oracle, Alphabet<I> alphabet);

    protected abstract int getRounds();

    @Test
    public void testSuspendAndResumeLearner() {
        learner.startLearning();

        int roundsPre = 0, roundsPost = 0;
        byte[] data = null;

        EquivalenceOracle<M, I, D> equivalenceOracle = getEquivalenceOracle(target);

        while (true) {
            final M hyp = learner.getHypothesisModel();
            final DefaultQuery<I, D> ce = equivalenceOracle.findCounterExample(hyp, inputAlphabet);
            if (ce == null) {
                break;
            }
            learner.refineHypothesis(ce);
            roundsPre++;

            if (roundsPre == rounds) {
                // serialize the state
                data = ResumeUtils.toBytes(learner.suspend());
            }
        }

        // deserialize the state
        assert data != null;

        // create the learner from the state
        final L learner2 = getLearner(getOracle(target), inputAlphabet);
        learner2.resume(ResumeUtils.fromBytes(data));

        while (true) {
            final M hyp = learner2.getHypothesisModel();
            final DefaultQuery<I, D> ce = equivalenceOracle.findCounterExample(hyp, inputAlphabet);
            if (ce == null) {
                break;
            }
            learner2.refineHypothesis(ce);
            roundsPost++;
        }

        final boolean modelsAreEquivalent =
                Automata.testEquivalence(learner.getHypothesisModel(), learner2.getHypothesisModel(), inputAlphabet);

        Assert.assertTrue(modelsAreEquivalent);
        Assert.assertEquals(roundsPre - roundsPost, rounds);
    }
}
