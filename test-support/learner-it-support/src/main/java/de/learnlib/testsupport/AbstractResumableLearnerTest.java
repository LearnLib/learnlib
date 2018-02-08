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
package de.learnlib.testsupport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.algorithm.feature.ResumableLearner;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.Output;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class that checks the workflow of a learning algorithm that implements {@link ResumableLearner}.
 *
 * @author bainczyk
 */
public abstract class AbstractResumableLearnerTest<L extends ResumableLearner<T> & LearningAlgorithm<M, I, D>, M extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & Output<I, D>, OR, I, D, T extends Serializable> {

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
    public void testSuspendAndResumeLearner() throws Exception {
        final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        final ObjectOutputStream objectOut = new ObjectOutputStream(byteOut);

        learner.startLearning();

        int roundsPre = 0, roundsPost = 0;

        while (true) {
            final Word<I> separatingWord =
                    Automata.findSeparatingWord(target, learner.getHypothesisModel(), inputAlphabet);
            if (separatingWord == null) {
                break;
            }
            learner.refineHypothesis(new DefaultQuery<>(separatingWord, target.computeOutput(separatingWord)));
            roundsPre++;

            if (roundsPre == rounds) {
                // serialize the state
                final T state = learner.suspend();
                objectOut.writeObject(state);
                objectOut.close();
            }
        }

        // deserialize the state
        final ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        final ObjectInputStream objectIn = new ObjectInputStream(byteIn);
        @SuppressWarnings("unchecked")
        final T serializedState = (T) objectIn.readObject();
        objectIn.close();

        // create the learner from the state
        final L learner2 = getLearner(getOracle(target), inputAlphabet);
        learner2.resume(serializedState);

        while (true) {
            final Word<I> word = Automata.findSeparatingWord(target, learner2.getHypothesisModel(), inputAlphabet);
            if (word == null) {
                break;
            }
            learner2.refineHypothesis(new DefaultQuery<>(word, target.computeOutput(word)));
            roundsPost++;
        }

        final boolean modelsAreEquivalent =
                Automata.testEquivalence(learner.getHypothesisModel(), learner2.getHypothesisModel(), inputAlphabet);

        Assert.assertTrue(modelsAreEquivalent);
        Assert.assertTrue(roundsPre - roundsPost == rounds);
    }
}
