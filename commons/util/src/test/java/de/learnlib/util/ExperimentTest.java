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
package de.learnlib.util;

import java.util.Collection;
import java.util.Random;

import de.learnlib.algorithm.LearningAlgorithm.DFALearner;
import de.learnlib.oracle.EquivalenceOracle.DFAEquivalenceOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.statistic.Statistics;
import de.learnlib.statistic.StatsContainer;
import de.learnlib.util.Experiment.DFAExperiment;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ExperimentTest {

    private static final Random RANDOM = new Random(42);
    private static final int REFINEMENT_STEPS = 3;

    @Test
    public void testExperiment() {

        final Alphabet<Character> alphabet = Alphabets.characters('a', 'c');
        final CompactDFA<Character> target = RandomAutomata.randomDFA(RANDOM, 5, alphabet);
        final CompactDFA<Character> intermediateTarget = RandomAutomata.randomDFA(RANDOM, target.size() - 1, alphabet);

        final MockUpLearner<Character> learner = new MockUpLearner<>(target, intermediateTarget);
        final DFAEquivalenceOracle<Character> eq = new MockUpOracle<>(intermediateTarget);

        final StatsContainer statMock = Mockito.mock(StatsContainer.class);

        try (MockedStatic<Statistics> statistics = Mockito.mockStatic(Statistics.class)) {
            statistics.when(Statistics::getContainer).thenReturn(statMock);

            DFAExperiment<Character> experiment = new DFAExperiment<>(learner, eq, alphabet);

            Assert.assertThrows(experiment::getFinalHypothesis);

            experiment.run();

            Assert.assertThrows(experiment::run);

            DFA<?, Character> finalModel = experiment.getFinalHypothesis();

            Assert.assertNotNull(experiment.getFinalHypothesis());
            Assert.assertSame(finalModel, target);

            Assert.assertTrue(learner.startLearningCalled);
            Assert.assertEquals(learner.refinementSteps, REFINEMENT_STEPS);

            Mockito.verify(statMock, Mockito.atLeastOnce())
                   .startOrResumeClock(ArgumentMatchers.eq(Experiment.LEARNING_PROFILE_KEY),
                                       ArgumentMatchers.anyString());
            Mockito.verify(statMock, Mockito.atLeastOnce())
                   .pauseClock(ArgumentMatchers.eq(Experiment.LEARNING_PROFILE_KEY));
            Mockito.verify(statMock, Mockito.atLeastOnce())
                   .startOrResumeClock(ArgumentMatchers.eq(Experiment.COUNTEREXAMPLE_PROFILE_KEY),
                                       ArgumentMatchers.anyString());
            Mockito.verify(statMock, Mockito.atLeastOnce())
                   .pauseClock(ArgumentMatchers.eq(Experiment.COUNTEREXAMPLE_PROFILE_KEY));
            Mockito.verify(statMock, Mockito.atLeastOnce())
                   .increaseCounter(ArgumentMatchers.eq(Experiment.LEARNING_ROUNDS_KEY), ArgumentMatchers.anyString());
        }
    }

    private static final class MockUpLearner<I> implements DFALearner<I> {

        private final DFA<?, I> targetModel;
        private final DFA<?, I> intermediateModel;
        private boolean startLearningCalled;
        private int refinementSteps;

        MockUpLearner(CompactDFA<I> target, CompactDFA<I> intermediateTarget) {
            targetModel = target;
            intermediateModel = intermediateTarget;
        }

        @Override
        public void startLearning() {
            startLearningCalled = true;
        }

        @Override
        public boolean refineHypothesis(DefaultQuery<I, Boolean> ceQuery) {
            return refinementSteps++ < REFINEMENT_STEPS;
        }

        @Override
        public DFA<?, I> getHypothesisModel() {
            if (refinementSteps < REFINEMENT_STEPS) {
                return intermediateModel;
            }

            return targetModel;
        }
    }

    private static final class MockUpOracle<I> implements DFAEquivalenceOracle<I> {

        private final DFA<?, I> intermediateTarget;
        private int counterexamples;

        MockUpOracle(DFA<?, I> intermediateTarget) {
            this.intermediateTarget = intermediateTarget;
        }

        @Override
        public @Nullable DefaultQuery<I, Boolean> findCounterExample(DFA<?, I> hypothesis,
                                                                     Collection<? extends I> inputs) {
            if (counterexamples < REFINEMENT_STEPS) {
                Assert.assertSame(hypothesis, intermediateTarget);

                counterexamples++;
                return new DefaultQuery<>(Word.epsilon(), true);
            }
            return null;
        }
    }

}
