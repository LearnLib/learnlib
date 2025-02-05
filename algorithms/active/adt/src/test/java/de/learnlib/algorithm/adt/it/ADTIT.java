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
package de.learnlib.algorithm.adt.it;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

import de.learnlib.algorithm.adt.api.ADTExtender;
import de.learnlib.algorithm.adt.api.LeafSplitter;
import de.learnlib.algorithm.adt.api.SubtreeReplacer;
import de.learnlib.algorithm.adt.config.ADTExtenders;
import de.learnlib.algorithm.adt.config.LeafSplitters;
import de.learnlib.algorithm.adt.config.SubtreeReplacers;
import de.learnlib.algorithm.adt.learner.ADTLearner;
import de.learnlib.algorithm.adt.learner.ADTLearnerBuilder;
import de.learnlib.counterexample.LocalSuffixFinder;
import de.learnlib.counterexample.LocalSuffixFinders;
import de.learnlib.driver.simulator.MealySimulatorSUL;
import de.learnlib.filter.statistic.oracle.CounterAdaptiveQueryOracle;
import de.learnlib.oracle.AdaptiveMembershipOracle;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.oracle.equivalence.MealyEQOracleChain;
import de.learnlib.oracle.equivalence.MealyRandomWpMethodEQOracle;
import de.learnlib.oracle.equivalence.MealySimulatorEQOracle;
import de.learnlib.oracle.membership.MealySimulatorOracle;
import de.learnlib.oracle.membership.SULAdaptiveOracle;
import de.learnlib.sul.SUL;
import de.learnlib.testsupport.MQ2AQWrapper;
import de.learnlib.testsupport.it.learner.AbstractMealyLearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList;
import de.learnlib.util.Experiment.MealyExperiment;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.dot.DOTParsers;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ADTIT extends AbstractMealyLearnerIT {

    private static final List<LeafSplitter> LEAF_SPLITTERS =
            Arrays.asList(LeafSplitters.DEFAULT_SPLITTER, LeafSplitters.EXTEND_PARENT);
    private static final List<ADTExtender> ADT_EXTENDERS =
            Arrays.asList(ADTExtenders.NOP, ADTExtenders.EXTEND_BEST_EFFORT);
    private static final List<SubtreeReplacer> SUBTREE_REPLACERS = Arrays.asList(SubtreeReplacers.NEVER_REPLACE,
                                                                                 SubtreeReplacers.EXHAUSTIVE_BEST_EFFORT,
                                                                                 SubtreeReplacers.LEVELED_BEST_EFFORT,
                                                                                 SubtreeReplacers.LEVELED_MIN_LENGTH,
                                                                                 SubtreeReplacers.LEVELED_MIN_SIZE,
                                                                                 SubtreeReplacers.SINGLE_BEST_EFFORT);

    @Override
    protected <I, O> void addLearnerVariants(Alphabet<I> alphabet,
                                             int targetSize,
                                             MealyMembershipOracle<I, O> mqOracle,
                                             LearnerVariantList.MealyLearnerVariantList<I, O> variants) {

        final ADTLearnerBuilder<I, O> builder = new ADTLearnerBuilder<>();
        builder.setAlphabet(alphabet);
        builder.setOracle(new MQ2AQWrapper<>(mqOracle));

        final Random useCacheGenerator = new Random(42);

        for (int i = 0; i < LEAF_SPLITTERS.size(); i++) {
            final LeafSplitter leafSplitter = LEAF_SPLITTERS.get(i);
            builder.setLeafSplitter(leafSplitter);

            for (int j = 0; j < ADT_EXTENDERS.size(); j++) {
                final ADTExtender adtExtender = ADT_EXTENDERS.get(j);
                builder.setAdtExtender(adtExtender);

                for (int k = 0; k < SUBTREE_REPLACERS.size(); k++) {
                    final SubtreeReplacer subtreeReplacer = SUBTREE_REPLACERS.get(k);
                    builder.setSubtreeReplacer(subtreeReplacer);

                    for (LocalSuffixFinder<@Nullable Object, @Nullable Object> suffixFinder : LocalSuffixFinders.values()) {
                        builder.setSuffixFinder(suffixFinder);
                        builder.setUseObservationTree(useCacheGenerator.nextBoolean());

                        variants.addLearnerVariant(i + "," + j + "," + k, builder.create());
                    }

                }
            }
        }
    }

    /**
     * Integration test for <a href="https://github.com/LearnLib/learnlib/issues/137">issue 137</a>. This test checks
     * whether the algorithm behaves deterministically. Since the ADS computations traverse a lot of maps, not
     * maintaining a certain order (e.g. by using {@link HashMap}s instead of {@link LinkedHashMap}s) may result in
     * different query performance based on the internal objects' hash value. This is something that we want to avoid.
     *
     * @throws IOException
     *         if example model cannot be parsed
     * @throws FormatException
     *         if example model cannot be parsed
     */
    @Test
    public void testIssue137() throws IOException, FormatException {
        final CompactMealy<String, String> model = loadModel();
        final Alphabet<String> alphabet = model.getInputAlphabet();

        final SUL<String, String> sul = new MealySimulatorSUL<>(model);
        final AdaptiveMembershipOracle<String, String> aqo = new SULAdaptiveOracle<>(sul);
        final MealyMembershipOracle<String, String> mqo = new MealySimulatorOracle<>(model);

        for (int i = 0; i < LEAF_SPLITTERS.size(); i++) {
            for (int j = 0; j < ADT_EXTENDERS.size(); j++) {
                for (int k = 0; k < SUBTREE_REPLACERS.size(); k++) {
                    for (int seed = 0; seed < 50; seed++) {
                        long last = 0;
                        for (int iter = 0; iter < 5; iter++) {
                            final CounterAdaptiveQueryOracle<String, String> counter =
                                    new CounterAdaptiveQueryOracle<>(aqo);
                            final ADTLearner<String, String> learner = new ADTLearner<>(alphabet,
                                                                                        counter,
                                                                                        LEAF_SPLITTERS.get(i),
                                                                                        ADT_EXTENDERS.get(j),
                                                                                        SUBTREE_REPLACERS.get(j));

                            final MealyEQOracleChain<String, String> eqo =
                                    new MealyEQOracleChain<>(new MealyRandomWpMethodEQOracle<>(mqo,
                                                                                               0,
                                                                                               1,
                                                                                               100,
                                                                                               new Random(seed),
                                                                                               1),
                                                             new MealySimulatorEQOracle<>(model));
                            final MealyExperiment<String, String> exp =
                                    new MealyExperiment<>(learner, eqo, model.getInputAlphabet());

                            exp.run();

                            final long count = counter.getResetCounter().getCount();

                            if (iter == 0) {
                                last = count;
                            }

                            Assert.assertEquals(count,
                                                last,
                                                String.format("i: %d, j: %d, k: %d, seed: %d, last: %d, count: %d",
                                                              i,
                                                              j,
                                                              k,
                                                              seed,
                                                              last,
                                                              count));
                        }
                    }
                }
            }
        }
    }

    private static CompactMealy<String, String> loadModel() throws IOException, FormatException {
        try (InputStream is = ADTIT.class.getResourceAsStream("/tcp_client_ubuntu.dot")) {
            return DOTParsers.mealy().readModel(is).model;
        }
    }

}
