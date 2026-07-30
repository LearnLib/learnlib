/* Copyright (C) 2013-2026 TU Dortmund University
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
package de.learnlib.cli.factory;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import de.learnlib.cli.Application;
import de.learnlib.cli.ApplicationIT;
import de.learnlib.cli.option.EQOracle;
import de.learnlib.cli.option.Options;
import de.learnlib.cli.util.AcceptorNullOracle;
import de.learnlib.cli.util.AdaptiveNullOracle;
import de.learnlib.cli.util.TransducerNullOracle;
import de.learnlib.cli.util.Util;
import de.learnlib.oracle.equivalence.EQOracleChain;
import de.learnlib.oracle.equivalence.KWayStateCoverEQOracle;
import de.learnlib.oracle.equivalence.KWayTransitionCoverEQOracle;
import de.learnlib.oracle.equivalence.RandomWMethodEQOracle;
import de.learnlib.oracle.equivalence.RandomWordsEQOracle;
import de.learnlib.oracle.equivalence.RandomWpMethodEQOracle;
import de.learnlib.oracle.equivalence.SampleSetEQOracle;
import de.learnlib.oracle.equivalence.WMethodEQOracle;
import de.learnlib.oracle.equivalence.WpMethodEQOracle;
import de.learnlib.oracle.equivalence.vpa.RandomWellMatchedWordsEQOracle;
import net.automatalib.automaton.UniversalDeterministicAutomaton.RegularAutomaton;
import net.automatalib.automaton.fsa.NFA;
import net.automatalib.automaton.procedural.SBA;
import net.automatalib.automaton.procedural.SPA;
import net.automatalib.automaton.procedural.SPMM;
import net.automatalib.automaton.vpa.OneSEVPA;
import net.automatalib.common.util.random.RandomUtil;
import net.automatalib.util.automaton.conformance.KWayStateCoverTestsIterator.CombinationMethod;
import net.automatalib.util.automaton.conformance.KWayTransitionCoverTestsIterator.GenerationMethod;
import net.automatalib.util.automaton.conformance.KWayTransitionCoverTestsIterator.OptimizationMetric;
import net.automatalib.word.Word;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import picocli.CommandLine;

public class EQOFactoryTest {

    private final CommandLine cmd;

    public EQOFactoryTest() {
        cmd = new CommandLine(new Application());
        cmd.setErr(new PrintWriter(OutputStream.nullOutputStream()));
    }

    @Test
    public void testRegularEQOs() {
        final Set<EQOracle> remaining = EnumSet.allOf(EQOracle.class);
        final Options options = buildOptions(remaining,
                                             EQOracle.SAMPLE,
                                             EQOracle.RANDOM,
                                             EQOracle.KWAY_S,
                                             EQOracle.W,
                                             EQOracle.RANDOM_WP,
                                             EQOracle.KWAY_T,
                                             EQOracle.WP,
                                             EQOracle.RANDOM_W);

        final EQOracleChain<? extends RegularAutomaton<?, String, ?, ?, ?>, String, Boolean> chain =
                EQOFactory.getRegularOracles(options, new AcceptorNullOracle());
        var oracles = chain.getOracles();

        Assert.assertEquals(oracles.size(), 8);

        Assert.assertTrue(oracles.get(0) instanceof SampleSetEQOracle);
        Assert.assertTrue(oracles.get(1) instanceof RandomWordsEQOracle);
        Assert.assertTrue(oracles.get(2) instanceof KWayStateCoverEQOracle);
        Assert.assertTrue(oracles.get(3) instanceof WMethodEQOracle);
        Assert.assertTrue(oracles.get(4) instanceof RandomWpMethodEQOracle);
        Assert.assertTrue(oracles.get(5) instanceof KWayTransitionCoverEQOracle);
        Assert.assertTrue(oracles.get(6) instanceof WpMethodEQOracle);
        Assert.assertTrue(oracles.get(7) instanceof RandomWMethodEQOracle);

        Assert.assertTrue(remaining.isEmpty());
    }

    @Test
    public void testAdaptiverEQOs() {
        final Set<EQOracle> remaining = EnumSet.allOf(EQOracle.class);
        final Options options = buildOptions(remaining,
                                             EQOracle.SAMPLE,
                                             EQOracle.RANDOM,
                                             EQOracle.KWAY_S,
                                             EQOracle.W,
                                             EQOracle.RANDOM_WP,
                                             EQOracle.KWAY_T,
                                             EQOracle.WP,
                                             EQOracle.RANDOM_W);

        final EQOracleChain<? extends RegularAutomaton<?, String, ?, ?, ?>, String, Word<String>> chain =
                EQOFactory.getAdaptiveOracles(options, new AdaptiveNullOracle());
        var oracles = chain.getOracles();

        Assert.assertEquals(oracles.size(), 8);

        Assert.assertTrue(oracles.get(0) instanceof SampleSetEQOracle);
        Assert.assertTrue(oracles.get(1) instanceof RandomWordsEQOracle);
        Assert.assertTrue(oracles.get(2) instanceof KWayStateCoverEQOracle);
        Assert.assertTrue(oracles.get(3) instanceof WMethodEQOracle);
        Assert.assertTrue(oracles.get(4) instanceof RandomWpMethodEQOracle);
        Assert.assertTrue(oracles.get(5) instanceof KWayTransitionCoverEQOracle);
        Assert.assertTrue(oracles.get(6) instanceof WpMethodEQOracle);
        Assert.assertTrue(oracles.get(7) instanceof RandomWMethodEQOracle);

        Assert.assertTrue(remaining.isEmpty());
    }

    @Test
    public void testNFAEQOs() {
        final Set<EQOracle> remaining = EnumSet.allOf(EQOracle.class);
        final Options options = buildOptions(remaining,
                                             EQOracle.SAMPLE,
                                             EQOracle.RANDOM,
                                             EQOracle.KWAY_S,
                                             EQOracle.W,
                                             EQOracle.RANDOM_WP,
                                             EQOracle.KWAY_T,
                                             EQOracle.WP,
                                             EQOracle.RANDOM_W);

        final EQOracleChain<NFA<?, String>, String, Boolean> chain =
                EQOFactory.getNFAOracles(options, new AcceptorNullOracle());
        var oracles = chain.getOracles();

        Assert.assertEquals(oracles.size(), 8);
        Assert.assertTrue(remaining.isEmpty());
    }

    @Test
    public void testSBAEQOs() {
        final Set<EQOracle> remaining = EnumSet.allOf(EQOracle.class);
        final Options options = buildOptions(remaining, EQOracle.SAMPLE, EQOracle.RANDOM, EQOracle.W);

        final EQOracleChain<SBA<?, String>, String, Boolean> chain =
                EQOFactory.getSBAOracles(options, new AcceptorNullOracle());
        var oracles = chain.getOracles();

        Assert.assertEquals(oracles.size(), 3);
        Assert.assertTrue(oracles.get(0) instanceof SampleSetEQOracle);
        Assert.assertTrue(oracles.get(1) instanceof RandomWellMatchedWordsEQOracle);
        Assert.assertTrue(oracles.get(2) instanceof de.learnlib.oracle.equivalence.sba.WMethodEQOracle);

        for (EQOracle oracle : remaining) {
            final Options opt = buildOptions(remaining, oracle);
            Assert.assertThrows(() -> EQOFactory.getSBAOracles(opt, new AcceptorNullOracle()));
        }
    }

    @Test
    public void testSPAEQOs() {
        final Set<EQOracle> remaining = EnumSet.allOf(EQOracle.class);
        final Options options = buildOptions(remaining, EQOracle.SAMPLE, EQOracle.WP, EQOracle.RANDOM, EQOracle.W);

        final EQOracleChain<SPA<?, String>, String, Boolean> chain =
                EQOFactory.getSPAOracles(options, new AcceptorNullOracle());
        var oracles = chain.getOracles();

        Assert.assertEquals(oracles.size(), 4);
        Assert.assertTrue(oracles.get(0) instanceof SampleSetEQOracle);
        Assert.assertTrue(oracles.get(1) instanceof de.learnlib.oracle.equivalence.spa.WpMethodEQOracle);
        Assert.assertTrue(oracles.get(2) instanceof RandomWellMatchedWordsEQOracle);
        Assert.assertTrue(oracles.get(3) instanceof de.learnlib.oracle.equivalence.spa.WMethodEQOracle);

        for (EQOracle oracle : remaining) {
            final Options opt = buildOptions(remaining, oracle);
            Assert.assertThrows(() -> EQOFactory.getSPAOracles(opt, new AcceptorNullOracle()));
        }
    }

    @Test
    public void testSPMMEQOs() {
        final Set<EQOracle> remaining = EnumSet.allOf(EQOracle.class);
        final Options options = buildOptions(remaining, EQOracle.SAMPLE, EQOracle.RANDOM, EQOracle.W);

        final EQOracleChain<SPMM<?, String, ?, String>, String, Word<String>> chain =
                EQOFactory.getSPMMOracles(options, new TransducerNullOracle());
        var oracles = chain.getOracles();

        Assert.assertEquals(oracles.size(), 3);
        Assert.assertTrue(oracles.get(0) instanceof SampleSetEQOracle);
        Assert.assertTrue(oracles.get(1) instanceof RandomWellMatchedWordsEQOracle);
        Assert.assertTrue(oracles.get(2) instanceof de.learnlib.oracle.equivalence.spmm.WMethodEQOracle);

        for (EQOracle oracle : remaining) {
            final Options opt = buildOptions(remaining, oracle);
            Assert.assertThrows(() -> EQOFactory.getSPMMOracles(opt, new TransducerNullOracle()));
        }
    }

    @Test
    public void testVPAEQOs() {
        final Set<EQOracle> remaining = EnumSet.allOf(EQOracle.class);
        final Options options = buildOptions(remaining, EQOracle.SAMPLE, EQOracle.RANDOM);

        final EQOracleChain<OneSEVPA<?, String>, String, Boolean> chain =
                EQOFactory.getVPAOracles(options, new AcceptorNullOracle());
        var oracles = chain.getOracles();

        Assert.assertEquals(oracles.size(), 2);
        Assert.assertTrue(oracles.get(0) instanceof SampleSetEQOracle);
        Assert.assertTrue(oracles.get(1) instanceof RandomWellMatchedWordsEQOracle);

        for (EQOracle oracle : remaining) {
            final Options opt = buildOptions(remaining, oracle);
            Assert.assertThrows(() -> EQOFactory.getVPAOracles(opt, new AcceptorNullOracle()));
        }
    }

    @Test
    public void testRegularParameters() {

        // Set up randomized options
        final Random r = new Random(42);
        final int randomBound = 1_000;
        final List<String> args = new ArrayList<>();

        args.add(ApplicationIT.STATEFUL);
        args.add(ApplicationIT.STATEFUL);
        args.add("-sa");
        Arrays.stream(EQOracle.values()).map(EQOracle::name).forEach(n -> args.add("-e" + n));

        args.add("--eqo-kways-combinationMethod=" + RandomUtil.choose(r, CombinationMethod.values()));
        args.add("--eqo-kways-k=" + r.nextInt(randomBound));
        args.add("--eqo-kways-randomWalkLen=" + r.nextInt(randomBound));
        args.add("--eqo-kways-seed=" + r.nextInt(randomBound));

        args.add("--eqo-kwayt-randomWalkLen=" + r.nextInt(randomBound));
        args.add("--eqo-kwayt-numGeneratePaths=" + r.nextInt(randomBound));
        args.add("--eqo-kwayt-maxPathLen=" + r.nextInt(randomBound));
        args.add("--eqo-kwayt-maxNumberOfSteps=" + r.nextInt(randomBound));
        args.add("--eqo-kwayt-k=" + r.nextInt(randomBound));
        args.add("--eqo-kwayt-optimizationMetric=" + RandomUtil.choose(r, OptimizationMetric.values()));
        args.add("--eqo-kwayt-generationMethod=" + RandomUtil.choose(r, GenerationMethod.values()));
        args.add("--eqo-kwayt-seed=" + r.nextInt(randomBound));

        args.add("--eqo-random-minLength=" + r.nextInt(randomBound));
        args.add("--eqo-random-maxLength=" + r.nextInt(randomBound));
        args.add("--eqo-random-maxTests=" + r.nextInt(randomBound));
        args.add("--eqo-random-seed=" + r.nextInt(randomBound));

        args.add("--eqo-randomw-minimalSize=" + r.nextInt(randomBound));
        args.add("--eqo-randomw-rndLength=" + r.nextInt(randomBound));
        args.add("--eqo-randomw-bound=" + r.nextInt(randomBound));
        args.add("--eqo-randomw-seed=" + r.nextInt(randomBound));

        args.add("--eqo-randomwp-minimalSize=" + r.nextInt(randomBound));
        args.add("--eqo-randomwp-rndLength=" + r.nextInt(randomBound));
        args.add("--eqo-randomwp-bound=" + r.nextInt(randomBound));
        args.add("--eqo-randomwp-seed=" + r.nextInt(randomBound));

        args.add("--eqo-w-lookahead=" + r.nextInt(randomBound));
        args.add("--eqo-w-expectedSize=" + r.nextInt(randomBound));

        args.add("--eqo-wp-lookahead=" + r.nextInt(randomBound));
        args.add("--eqo-wp-expectedSize=" + r.nextInt(randomBound));

        final Options options = Util.parseOptions(cmd, args.toArray(new String[0]));

        // Mock constructors and spy on calls
        final AcceptorNullOracle oracle = new AcceptorNullOracle();

        final List<Object> kWaySArgs = new ArrayList<>();
        final List<Object> kWayTArgs = new ArrayList<>();
        final List<Object> randomArgs = new ArrayList<>();
        final List<Object> randomWArgs = new ArrayList<>();
        final List<Object> randomWpArgs = new ArrayList<>();
        final List<Object> wArgs = new ArrayList<>();
        final List<Object> wpArgs = new ArrayList<>();

        try (MockedConstruction<?> kWayS = Mockito.mockConstruction(KWayStateCoverEQOracle.class,
                                                                    (mock, context) -> kWaySArgs.addAll(context.arguments()));
             MockedConstruction<?> kWayT = Mockito.mockConstruction(KWayTransitionCoverEQOracle.class,
                                                                    (mock, context) -> kWayTArgs.addAll(context.arguments()));
             MockedConstruction<?> random = Mockito.mockConstruction(RandomWordsEQOracle.class,
                                                                     (mock, context) -> randomArgs.addAll(context.arguments()));
             MockedConstruction<?> randomW = Mockito.mockConstruction(RandomWMethodEQOracle.class,
                                                                      (mock, context) -> randomWArgs.addAll(context.arguments()));
             MockedConstruction<?> randomWp = Mockito.mockConstruction(RandomWpMethodEQOracle.class,
                                                                       (mock, context) -> randomWpArgs.addAll(context.arguments()));
             MockedConstruction<?> sample = Mockito.mockConstruction(SampleSetEQOracle.class);
             MockedConstruction<?> w = Mockito.mockConstruction(WMethodEQOracle.class,
                                                                (mock, context) -> wArgs.addAll(context.arguments()));
             MockedConstruction<?> wp = Mockito.mockConstruction(WpMethodEQOracle.class,
                                                                 (mock, context) -> wpArgs.addAll(context.arguments()))) {

            EQOFactory.getRegularOracles(options, oracle);

            Assert.assertEquals(kWayS.constructed().size(), 1);
            Assert.assertEquals(kWayT.constructed().size(), 1);
            Assert.assertEquals(random.constructed().size(), 1);
            Assert.assertEquals(randomW.constructed().size(), 1);
            Assert.assertEquals(randomWp.constructed().size(), 1);
            Assert.assertEquals(sample.constructed().size(), 1);
            Assert.assertEquals(w.constructed().size(), 1);
            Assert.assertEquals(wp.constructed().size(), 1);
        }

        // Validate
        final int batchSize = 2 * EQOFactory.BATCH_SIZE;

        // KWayStateCoverEQOracle
        Assert.assertEquals(kWaySArgs.size(), 6);
        Assert.assertSame(kWaySArgs.get(0), oracle);
        // compare seeds by sampling a value from the random object
        Assert.assertEquals(((Random) kWaySArgs.get(1)).nextInt(randomBound),
                            new Random(options.eqoParams.kWayState.seed).nextInt(randomBound));
        Assert.assertEquals(kWaySArgs.get(2), options.eqoParams.kWayState.randomWalkLen);
        Assert.assertEquals(kWaySArgs.get(3), options.eqoParams.kWayState.k);
        Assert.assertEquals(kWaySArgs.get(4), options.eqoParams.kWayState.combinationMethod);
        Assert.assertEquals(kWaySArgs.get(5), batchSize);

        // KWayTransitionCoverEQOracle
        Assert.assertEquals(kWayTArgs.size(), 10);
        Assert.assertSame(kWayTArgs.get(0), oracle);
        // compare seeds by sampling a value from the random object
        Assert.assertEquals(((Random) kWayTArgs.get(1)).nextInt(randomBound),
                            new Random(options.eqoParams.kWayTransition.seed).nextInt(randomBound));
        Assert.assertEquals(kWayTArgs.get(2), options.eqoParams.kWayTransition.randomWalkLen);
        Assert.assertEquals(kWayTArgs.get(3), options.eqoParams.kWayTransition.numGeneratePaths);
        Assert.assertEquals(kWayTArgs.get(4), options.eqoParams.kWayTransition.maxPathLen);
        Assert.assertEquals(kWayTArgs.get(5), options.eqoParams.kWayTransition.maxNumberOfSteps);
        Assert.assertEquals(kWayTArgs.get(6), options.eqoParams.kWayTransition.k);
        Assert.assertEquals(kWayTArgs.get(7), options.eqoParams.kWayTransition.optimizationMetric);
        Assert.assertEquals(kWayTArgs.get(8), options.eqoParams.kWayTransition.generationMethod);
        Assert.assertEquals(kWayTArgs.get(9), batchSize);

        // RandomWordsEQOracle
        Assert.assertEquals(randomArgs.size(), 6);
        Assert.assertSame(randomArgs.get(0), oracle);
        Assert.assertEquals(randomArgs.get(1), options.eqoParams.random.minLength);
        Assert.assertEquals(randomArgs.get(2), options.eqoParams.random.maxLength);
        Assert.assertEquals(randomArgs.get(3), options.eqoParams.random.maxTests);
        // compare seeds by sampling a value from the random object
        Assert.assertEquals(((Random) randomArgs.get(4)).nextInt(randomBound),
                            new Random(options.eqoParams.random.seed).nextInt(randomBound));
        Assert.assertEquals(randomArgs.get(5), batchSize);

        // RandomWMethodEQOracle
        Assert.assertEquals(randomWArgs.size(), 6);
        Assert.assertSame(randomWArgs.get(0), oracle);
        Assert.assertEquals(randomWArgs.get(1), options.eqoParams.randomWMethod.minimalSize);
        Assert.assertEquals(randomWArgs.get(2), options.eqoParams.randomWMethod.rndLength);
        Assert.assertEquals(randomWArgs.get(3), options.eqoParams.randomWMethod.bound);
        // compare seeds by sampling a value from the random object
        Assert.assertEquals(((Random) randomWArgs.get(4)).nextInt(randomBound),
                            new Random(options.eqoParams.randomWMethod.seed).nextInt(randomBound));
        Assert.assertEquals(randomWArgs.get(5), batchSize);

        // RandomWpMethodEQOracle
        Assert.assertEquals(randomWpArgs.size(), 6);
        Assert.assertSame(randomWArgs.get(0), oracle);
        Assert.assertEquals(randomWpArgs.get(1), options.eqoParams.randomWpMethod.minimalSize);
        Assert.assertEquals(randomWpArgs.get(2), options.eqoParams.randomWpMethod.rndLength);
        Assert.assertEquals(randomWpArgs.get(3), options.eqoParams.randomWpMethod.bound);
        // compare seeds by sampling a value from the random object
        Assert.assertEquals(((Random) randomWpArgs.get(4)).nextInt(randomBound),
                            new Random(options.eqoParams.randomWpMethod.seed).nextInt(randomBound));
        Assert.assertEquals(randomWpArgs.get(5), batchSize);

        // WMethodEQOracle
        Assert.assertEquals(wArgs.size(), 4);
        Assert.assertSame(wArgs.get(0), oracle);
        Assert.assertEquals(wArgs.get(1), options.eqoParams.wMethod.lookahead);
        Assert.assertEquals(wArgs.get(2), options.eqoParams.wMethod.expectedSize);
        Assert.assertEquals(wArgs.get(3), batchSize);

        // WpMethodEQOracle
        Assert.assertEquals(wpArgs.size(), 4);
        Assert.assertSame(wpArgs.get(0), oracle);
        Assert.assertEquals(wpArgs.get(1), options.eqoParams.wpMethod.lookahead);
        Assert.assertEquals(wpArgs.get(2), options.eqoParams.wpMethod.expectedSize);
        Assert.assertEquals(wpArgs.get(3), batchSize);
    }

    @Test
    public void testSBAParameters() {

        // Set up randomized options
        final Random r = new Random(42);
        final int randomBound = 1_000;
        final List<String> args = new ArrayList<>();

        args.add(ApplicationIT.STATEFUL);
        args.add(ApplicationIT.STATEFUL);
        args.add("-sa");
        Stream.of(EQOracle.RANDOM, EQOracle.SAMPLE, EQOracle.W).map(EQOracle::name).forEach(n -> args.add("-e" + n));

        args.add("--eqo-random-minLength=" + r.nextInt(randomBound));
        args.add("--eqo-random-maxLength=" + r.nextInt(randomBound));
        args.add("--eqo-random-maxTests=" + r.nextInt(randomBound));
        args.add("--eqo-random-seed=" + r.nextInt(randomBound));

        args.add("--eqo-w-lookahead=" + r.nextInt(randomBound));
        args.add("--eqo-w-expectedSize=" + r.nextInt(randomBound));

        final Options options = Util.parseOptions(cmd, args.toArray(new String[0]));

        // Mock constructors and spy on calls
        final AcceptorNullOracle oracle = new AcceptorNullOracle();

        final List<Object> randomArgs = new ArrayList<>();
        final List<Object> wArgs = new ArrayList<>();

        try (MockedConstruction<?> random = Mockito.mockConstruction(RandomWellMatchedWordsEQOracle.class,
                                                                     (mock, context) -> randomArgs.addAll(context.arguments()));
             MockedConstruction<?> sample = Mockito.mockConstruction(SampleSetEQOracle.class);
             MockedConstruction<?> w = Mockito.mockConstruction(de.learnlib.oracle.equivalence.sba.WMethodEQOracle.class,
                                                                (mock, context) -> wArgs.addAll(context.arguments()))) {

            EQOFactory.getSBAOracles(options, oracle);

            Assert.assertEquals(random.constructed().size(), 1);
            Assert.assertEquals(sample.constructed().size(), 1);
            Assert.assertEquals(w.constructed().size(), 1);
        }

        // Validate
        final int batchSize = 2 * EQOFactory.BATCH_SIZE;

        // RandomWellMatchedWordsEQOracle
        Assert.assertEquals(randomArgs.size(), 7);
        // compare seeds by sampling a value from the random object
        Assert.assertEquals(((Random) randomArgs.get(0)).nextInt(randomBound),
                            new Random(options.eqoParams.random.seed).nextInt(randomBound));
        Assert.assertSame(randomArgs.get(1), oracle);
        Assert.assertEquals(randomArgs.get(2), EQOFactory.RANDOM_CALL_PROB);
        Assert.assertEquals(randomArgs.get(3), options.eqoParams.random.maxTests);
        Assert.assertEquals(randomArgs.get(4), options.eqoParams.random.minLength);
        Assert.assertEquals(randomArgs.get(5), options.eqoParams.random.maxLength);
        Assert.assertEquals(randomArgs.get(6), batchSize);

        // WMethodEQOracle
        Assert.assertEquals(wArgs.size(), 4);
        Assert.assertSame(wArgs.get(0), oracle);
        Assert.assertEquals(wArgs.get(1), options.eqoParams.wMethod.lookahead);
        Assert.assertEquals(wArgs.get(2), options.eqoParams.wMethod.expectedSize);
        Assert.assertEquals(wArgs.get(3), batchSize);
    }

    @Test
    public void testSPAParameters() {

        // Set up randomized options
        final Random r = new Random(42);
        final int randomBound = 1_000;
        final List<String> args = new ArrayList<>();

        args.add(ApplicationIT.STATEFUL);
        args.add(ApplicationIT.STATEFUL);
        args.add("-sa");
        Stream.of(EQOracle.RANDOM, EQOracle.SAMPLE, EQOracle.W, EQOracle.WP)
              .map(EQOracle::name)
              .forEach(n -> args.add("-e" + n));

        args.add("--eqo-random-minLength=" + r.nextInt(randomBound));
        args.add("--eqo-random-maxLength=" + r.nextInt(randomBound));
        args.add("--eqo-random-maxTests=" + r.nextInt(randomBound));
        args.add("--eqo-random-seed=" + r.nextInt(randomBound));

        args.add("--eqo-w-lookahead=" + r.nextInt(randomBound));
        args.add("--eqo-w-expectedSize=" + r.nextInt(randomBound));

        args.add("--eqo-wp-lookahead=" + r.nextInt(randomBound));
        args.add("--eqo-wp-expectedSize=" + r.nextInt(randomBound));

        final Options options = Util.parseOptions(cmd, args.toArray(new String[0]));

        // Mock constructors and spy on calls
        final AcceptorNullOracle oracle = new AcceptorNullOracle();

        final List<Object> randomArgs = new ArrayList<>();
        final List<Object> wArgs = new ArrayList<>();
        final List<Object> wpArgs = new ArrayList<>();

        try (MockedConstruction<?> random = Mockito.mockConstruction(RandomWellMatchedWordsEQOracle.class,
                                                                     (mock, context) -> randomArgs.addAll(context.arguments()));
             MockedConstruction<?> sample = Mockito.mockConstruction(SampleSetEQOracle.class);
             MockedConstruction<?> w = Mockito.mockConstruction(de.learnlib.oracle.equivalence.spa.WMethodEQOracle.class,
                                                                (mock, context) -> wArgs.addAll(context.arguments()));
             MockedConstruction<?> wp = Mockito.mockConstruction(de.learnlib.oracle.equivalence.spa.WpMethodEQOracle.class,
                                                                 (mock, context) -> wpArgs.addAll(context.arguments()))) {

            EQOFactory.getSPAOracles(options, oracle);

            Assert.assertEquals(random.constructed().size(), 1);
            Assert.assertEquals(sample.constructed().size(), 1);
            Assert.assertEquals(w.constructed().size(), 1);
            Assert.assertEquals(wp.constructed().size(), 1);
        }

        // Validate
        final int batchSize = 2 * EQOFactory.BATCH_SIZE;

        // RandomWellMatchedWordsEQOracle
        Assert.assertEquals(randomArgs.size(), 7);
        // compare seeds by sampling a value from the random object
        Assert.assertEquals(((Random) randomArgs.get(0)).nextInt(randomBound),
                            new Random(options.eqoParams.random.seed).nextInt(randomBound));
        Assert.assertSame(randomArgs.get(1), oracle);
        Assert.assertEquals(randomArgs.get(2), EQOFactory.RANDOM_CALL_PROB);
        Assert.assertEquals(randomArgs.get(3), options.eqoParams.random.maxTests);
        Assert.assertEquals(randomArgs.get(4), options.eqoParams.random.minLength);
        Assert.assertEquals(randomArgs.get(5), options.eqoParams.random.maxLength);
        Assert.assertEquals(randomArgs.get(6), batchSize);

        // WMethodEQOracle
        Assert.assertEquals(wArgs.size(), 4);
        Assert.assertSame(wArgs.get(0), oracle);
        Assert.assertEquals(wArgs.get(1), options.eqoParams.wMethod.lookahead);
        Assert.assertEquals(wArgs.get(2), options.eqoParams.wMethod.expectedSize);
        Assert.assertEquals(wArgs.get(3), batchSize);

        // WpMethodEQOracle
        Assert.assertEquals(wpArgs.size(), 4);
        Assert.assertSame(wpArgs.get(0), oracle);
        Assert.assertEquals(wpArgs.get(1), options.eqoParams.wpMethod.lookahead);
        Assert.assertEquals(wpArgs.get(2), options.eqoParams.wpMethod.expectedSize);
        Assert.assertEquals(wpArgs.get(3), batchSize);
    }

    @Test
    public void testSPMMParameters() {
        // Set up randomized options
        final Random r = new Random(42);
        final int randomBound = 1_000;
        final List<String> args = new ArrayList<>();

        args.add(ApplicationIT.STATEFUL);
        args.add(ApplicationIT.STATEFUL);
        args.add("-sa");
        Stream.of(EQOracle.RANDOM, EQOracle.SAMPLE, EQOracle.W).map(EQOracle::name).forEach(n -> args.add("-e" + n));

        args.add("--eqo-random-minLength=" + r.nextInt(randomBound));
        args.add("--eqo-random-maxLength=" + r.nextInt(randomBound));
        args.add("--eqo-random-maxTests=" + r.nextInt(randomBound));
        args.add("--eqo-random-seed=" + r.nextInt(randomBound));

        args.add("--eqo-w-lookahead=" + r.nextInt(randomBound));
        args.add("--eqo-w-expectedSize=" + r.nextInt(randomBound));

        final Options options = Util.parseOptions(cmd, args.toArray(new String[0]));

        // Mock constructors and spy on calls
        final TransducerNullOracle oracle = new TransducerNullOracle();

        final List<Object> randomArgs = new ArrayList<>();
        final List<Object> wArgs = new ArrayList<>();

        try (MockedConstruction<?> random = Mockito.mockConstruction(RandomWellMatchedWordsEQOracle.class,
                                                                     (mock, context) -> randomArgs.addAll(context.arguments()));
             MockedConstruction<?> sample = Mockito.mockConstruction(SampleSetEQOracle.class);
             MockedConstruction<?> w = Mockito.mockConstruction(de.learnlib.oracle.equivalence.spmm.WMethodEQOracle.class,
                                                                (mock, context) -> wArgs.addAll(context.arguments()))) {

            EQOFactory.getSPMMOracles(options, oracle);

            Assert.assertEquals(random.constructed().size(), 1);
            Assert.assertEquals(sample.constructed().size(), 1);
            Assert.assertEquals(w.constructed().size(), 1);
        }

        // Validate
        final int batchSize = 2 * EQOFactory.BATCH_SIZE;

        // RandomWellMatchedWordsEQOracle
        Assert.assertEquals(randomArgs.size(), 7);
        // compare seeds by sampling a value from the random object
        Assert.assertEquals(((Random) randomArgs.get(0)).nextInt(randomBound),
                            new Random(options.eqoParams.random.seed).nextInt(randomBound));
        Assert.assertSame(randomArgs.get(1), oracle);
        Assert.assertEquals(randomArgs.get(2), EQOFactory.RANDOM_CALL_PROB);
        Assert.assertEquals(randomArgs.get(3), options.eqoParams.random.maxTests);
        Assert.assertEquals(randomArgs.get(4), options.eqoParams.random.minLength);
        Assert.assertEquals(randomArgs.get(5), options.eqoParams.random.maxLength);
        Assert.assertEquals(randomArgs.get(6), batchSize);

        // WMethodEQOracle
        Assert.assertEquals(wArgs.size(), 4);
        Assert.assertSame(wArgs.get(0), oracle);
        Assert.assertEquals(wArgs.get(1), options.eqoParams.wMethod.lookahead);
        Assert.assertEquals(wArgs.get(2), options.eqoParams.wMethod.expectedSize);
        Assert.assertEquals(wArgs.get(3), batchSize);
    }

    private Options buildOptions(Set<EQOracle> remaining, EQOracle... oracles) {

        final String[] args = new String[oracles.length + 2];

        args[0] = ApplicationIT.STATELESS;
        args[1] = "-sa";

        for (int i = 0; i < oracles.length; i++) {
            EQOracle oracle = oracles[i];
            remaining.remove(oracle);
            args[i + 2] = "-e" + oracle.name();
        }

        return Util.parseOptions(cmd, args);
    }
}
