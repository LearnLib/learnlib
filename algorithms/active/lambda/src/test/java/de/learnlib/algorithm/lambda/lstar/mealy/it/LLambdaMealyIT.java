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
package de.learnlib.algorithm.lambda.lstar.mealy.it;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Random;

import de.learnlib.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.algorithm.lambda.lstar.LLambdaMealy;
import de.learnlib.driver.simulator.MealySimulatorSUL;
import de.learnlib.oracle.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.oracle.equivalence.MealyRandomWpMethodEQOracle;
import de.learnlib.oracle.membership.SULOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.testsupport.it.learner.AbstractMealyLearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList.MealyLearnerVariantList;
import de.learnlib.util.Experiment.MealyExperiment;
import de.learnlib.util.mealy.MealyUtil;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.dot.DOTInputModelData;
import net.automatalib.serialization.dot.DOTParsers;
import net.automatalib.util.automaton.Automata;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class LLambdaMealyIT extends AbstractMealyLearnerIT {

    @Override
    protected <I, O> void addLearnerVariants(Alphabet<I> alphabet,
                                             int targetSize,
                                             MealyMembershipOracle<I, O> mqOracle,
                                             MealyLearnerVariantList<I, O> variants) {
        variants.addLearnerVariant("LLambdaMealy", new LLambdaMealy<>(alphabet, mqOracle));
    }

    /**
     * Checks that the number of prefixes is equal to the number of hypothesis states. For details, see <a
     * href="https://github.com/LearnLib/learnlib/issues/144">issue 144</a>.
     */
    @Test
    public void testIssue144() throws IOException, FormatException {

        try (InputStream is = LLambdaMealyIT.class.getClassLoader()
                                                  .getResourceAsStream("mosquitto__two_client_will_retain.dot")) {
            final DOTInputModelData<Integer, String, CompactMealy<String, String>> model =
                    DOTParsers.mealy().readModel(is);
            final CompactMealy<String, String> mealy = model.model;
            final Alphabet<String> alphabet = model.alphabet;

            final int seed = -1177788003;

            final MealyMembershipOracle<String, String> mqo = new SULOracle<>(new MealySimulatorSUL<>(mealy));
            final MealyLearner<String, String> learner = new LLambdaMealy<>(alphabet, mqo);
            final MealyEquivalenceOracle<String, String> eqo =
                    new ShorteningEQO<>(new MealyRandomWpMethodEQOracle<>(mqo, 0, 4, 10_000, new Random(seed), 1));

            final MealyExperiment<String, String> experiment = new MealyExperiment<>(learner, eqo, alphabet);
            final MealyMachine<?, String, ?, String> hyp = experiment.run();

            Assert.assertTrue(Automata.testEquivalence(mealy, hyp, alphabet));
        }
    }

    private static final class ShorteningEQO<I, O> implements MealyEquivalenceOracle<I, O> {

        private final MealyEquivalenceOracle<I, O> delegate;

        private ShorteningEQO(MealyEquivalenceOracle<I, O> delegate) {
            this.delegate = delegate;
        }

        @Override
        public @Nullable DefaultQuery<I, Word<O>> findCounterExample(MealyMachine<?, I, ?, O> hypothesis,
                                                                     Collection<? extends I> inputs) {
            DefaultQuery<I, Word<O>> ce = delegate.findCounterExample(hypothesis, inputs);
            return ce == null ? null : MealyUtil.shortenCounterExample(hypothesis, ce);
        }
    }
}
