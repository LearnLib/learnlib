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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import de.learnlib.cli.ApplicationIT;
import de.learnlib.cli.option.Options;
import de.learnlib.cli.util.AcceptorNullOracle;
import de.learnlib.cli.util.AdaptiveNullOracle;
import de.learnlib.cli.util.TransducerNullOracle;
import de.learnlib.filter.statistic.oracle.CounterAdaptiveQueryOracle;
import de.learnlib.filter.statistic.oracle.CounterOracle;
import de.learnlib.oracle.AdaptiveMembershipOracle;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.membership.CLIOracle;
import de.learnlib.oracle.membership.CLIOutputAdaptiveOracle;
import de.learnlib.oracle.membership.CLIOutputOracle;
import de.learnlib.oracle.membership.StdInOracle;
import de.learnlib.oracle.membership.StdInOutputAdaptiveOracle;
import de.learnlib.oracle.membership.StdInOutputOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.statistic.Statistics;
import de.learnlib.util.mealy.PresetAdaptiveQuery;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.word.Word;
import org.mockito.Answers;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.MockedStatic.Verification;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MQOFactoryTest {

    private final File sulFile;

    public MQOFactoryTest() {
        final URL resource = MQOFactoryTest.class.getResource("/sul/stateless.py");
        Assert.assertNotNull(resource);
        this.sulFile = new File(resource.getFile());
    }

    @Test
    public void testSingleAcceptorOracle() {
        testSingleOracle(MQOFactory::buildSingleAcceptorOracle, CLIOracle.class, StdInOracle.class, 1);
    }

    @Test
    public void testSingleTransducerOracle() {
        testSingleOracle(MQOFactory::buildSingleTransducerOracle, CLIOutputOracle.class, StdInOutputOracle.class, 2);
    }

    @Test
    public void testSingleAdaptiveOracle() {
        testSingleOracle(MQOFactory::buildSingleAdaptiveOracle,
                         CLIOutputAdaptiveOracle.class,
                         StdInOutputAdaptiveOracle.class,
                         2);
    }

    private <OR> void testSingleOracle(BiFunction<Options, File, OR> creator,
                                       Class<?> cliClass,
                                       Class<?> stdinClass,
                                       int resetParamPos) {
        final String reset = "test";

        final Options options = new Options();
        options.delimiter = " ";
        options.reset = reset;

        final List<Object> cliArgs = new ArrayList<>();

        try (MockedConstruction<?> cli = Mockito.mockConstruction(cliClass,
                                                                  (mock, context) -> cliArgs.addAll(context.arguments()))) {
            creator.apply(options, sulFile);
            Assert.assertEquals(cli.constructed().size(), 1);
        }

        Assert.assertEquals(cliArgs.size(), resetParamPos + 1);
        Assert.assertEquals(cliArgs.get(0), Collections.singletonList(sulFile.getPath()));
        Assert.assertEquals(cliArgs.get(resetParamPos), reset);

        final String additionalArgs = "additional";
        options.stdin = true;
        options.additionalArgs = List.of(additionalArgs, additionalArgs);

        final List<Object> stdinArgs = new ArrayList<>();

        try (MockedConstruction<?> stdin = Mockito.mockConstruction(stdinClass,
                                                                    (mock, context) -> stdinArgs.addAll(context.arguments()))) {
            creator.apply(options, sulFile);
            Assert.assertEquals(stdin.constructed().size(), 1);
        }

        Assert.assertEquals(stdinArgs.size(), resetParamPos + 1);
        Assert.assertEquals(stdinArgs.get(0), List.of(sulFile.getPath(), additionalArgs, additionalArgs));
        Assert.assertEquals(stdinArgs.get(resetParamPos), reset);

        File brokenFile = new File(sulFile.getAbsolutePath() + ".brokenSuffix");
        Assert.assertThrows(() -> MQOFactory.buildSingleAdaptiveOracle(options, brokenFile));
        options.stdin = false;
        Assert.assertThrows(() -> MQOFactory.buildSingleAdaptiveOracle(options, brokenFile));
    }

    @Test
    public void testAcceptorOracle() {
        testOracle(() -> MQOFactory.buildSingleAcceptorOracle(Mockito.any(), Mockito.any()),
                   MQOFactory::getAcceptorOracle,
                   AcceptorNullOracle.class);
    }

    @Test
    public void testTransducerOracle() {
        testOracle(() -> MQOFactory.buildSingleTransducerOracle(Mockito.any(), Mockito.any()),
                   MQOFactory::getTransducerOracle,
                   TransducerNullOracle.class);
    }

    private <D, OR extends MembershipOracle<String, D>> void testOracle(Verification mock,
                                                                        BiFunction<Options, Alphabet<String>, OR> oracleFunction,
                                                                        Class<? extends OR> nullOracle) {
        final Options options = new Options();
        options.sul = Arrays.asList(sulFile, sulFile);

        final Alphabet<String> alphabet = Alphabets.fromArray("a", "b");
        final OR oracleMock = Mockito.spy(nullOracle);

        try (MockedStatic<MQOFactory> factoryMock = Mockito.mockStatic(MQOFactory.class, Answers.CALLS_REAL_METHODS)) {
            factoryMock.when(mock).thenReturn(oracleMock);

            // basic invocation
            OR oracle = oracleFunction.apply(options, alphabet);

            oracle.answerQuery(Word.fromSymbols("a", "b"));
            Mockito.verify(oracleMock, Mockito.times(1)).answerQuery(Mockito.any(), Mockito.any());
            oracle.answerQuery(Word.fromSymbols("a", "b"));
            Mockito.verify(oracleMock, Mockito.times(2)).answerQuery(Mockito.any(), Mockito.any());

            Assert.assertTrue(Statistics.getService()
                                        .getCount(CounterOracle.KEY_SYMBOL.withId(MQOFactory.SUL_KEY))
                                        .isEmpty());
            Assert.assertTrue(Statistics.getService()
                                        .getCount(CounterOracle.KEY_SYMBOL.withId(MQOFactory.CACHE_KEY))
                                        .isEmpty());
            Statistics.getService().clear();

            // with cache
            Mockito.reset(new Object[]{oracleMock}); // make compiler happy
            options.cache = true;
            oracle = oracleFunction.apply(options, alphabet);

            oracle.answerQuery(Word.fromSymbols("a", "b"));
            Mockito.verify(oracleMock, Mockito.times(1)).answerQuery(Mockito.any(), Mockito.any());
            oracle.answerQuery(Word.fromSymbols("a", "b"));
            Mockito.verify(oracleMock, Mockito.times(1)).answerQuery(Mockito.any(), Mockito.any());

            Assert.assertTrue(Statistics.getService()
                                        .getCount(CounterOracle.KEY_SYMBOL.withId(MQOFactory.SUL_KEY))
                                        .isEmpty());
            Assert.assertTrue(Statistics.getService()
                                        .getCount(CounterOracle.KEY_SYMBOL.withId(MQOFactory.CACHE_KEY))
                                        .isEmpty());
            Statistics.getService().clear();

            // with stats
            Mockito.reset(new Object[]{oracleMock}); // make compiler happy
            options.cache = false;
            options.statistics = true;
            oracle = oracleFunction.apply(options, alphabet);

            oracle.answerQuery(Word.fromSymbols("a", "b"));
            Mockito.verify(oracleMock, Mockito.times(1)).answerQuery(Mockito.any(), Mockito.any());
            oracle.answerQuery(Word.fromSymbols("a", "b"));
            Mockito.verify(oracleMock, Mockito.times(2)).answerQuery(Mockito.any(), Mockito.any());

            Assert.assertTrue(Statistics.getService()
                                        .getCount(CounterOracle.KEY_SYMBOL.withId(MQOFactory.SUL_KEY))
                                        .isPresent());
            Assert.assertTrue(Statistics.getService()
                                        .getCount(CounterOracle.KEY_SYMBOL.withId(MQOFactory.CACHE_KEY))
                                        .isEmpty());
            Statistics.getService().clear();

            // with stats + cache
            Mockito.reset(new Object[]{oracleMock}); // make compiler happy
            options.cache = true;
            oracle = oracleFunction.apply(options, alphabet);

            oracle.answerQuery(Word.fromSymbols("a", "b"));
            Mockito.verify(oracleMock, Mockito.times(1)).answerQuery(Mockito.any(), Mockito.any());
            oracle.answerQuery(Word.fromSymbols("a", "b"));
            Mockito.verify(oracleMock, Mockito.times(1)).answerQuery(Mockito.any(), Mockito.any());

            Assert.assertTrue(Statistics.getService()
                                        .getCount(CounterOracle.KEY_SYMBOL.withId(MQOFactory.SUL_KEY))
                                        .isPresent());
            Assert.assertTrue(Statistics.getService()
                                        .getCount(CounterOracle.KEY_SYMBOL.withId(MQOFactory.CACHE_KEY))
                                        .isPresent());
            Statistics.getService().clear();
        }
    }

    @Test
    public void testAdaptiveOracle() {
        final Options options = new Options();
        options.sul = Arrays.asList(sulFile, sulFile);

        final Alphabet<String> alphabet = Alphabets.fromArray("a", "b");
        final AdaptiveNullOracle oracleMock = Mockito.spy(AdaptiveNullOracle.class);

        Assert.assertThrows(() -> MQOFactory.getAdaptiveOracle(options, alphabet)); // --reset required

        options.reset = "reset";

        try (MockedStatic<MQOFactory> factoryMock = Mockito.mockStatic(MQOFactory.class, Answers.CALLS_REAL_METHODS)) {
            factoryMock.when(() -> MQOFactory.buildSingleAdaptiveOracle(Mockito.any(), Mockito.any()))
                       .thenReturn(oracleMock);

            // basic invocation
            AdaptiveMembershipOracle<String, String> oracle = MQOFactory.getAdaptiveOracle(options, alphabet);

            oracle.processQuery(new PresetAdaptiveQuery<>(new DefaultQuery<>(Word.fromSymbols("a", "b"))));
            Mockito.verify(oracleMock, Mockito.times(1)).processQuery(Mockito.any());
            oracle.processQuery(new PresetAdaptiveQuery<>(new DefaultQuery<>(Word.fromSymbols("a", "b"))));
            Mockito.verify(oracleMock, Mockito.times(2)).processQuery(Mockito.any());

            Assert.assertTrue(Statistics.getService()
                                        .getCount(CounterAdaptiveQueryOracle.KEY_SYMBOL.withId(MQOFactory.SUL_KEY))
                                        .isEmpty());
            Assert.assertTrue(Statistics.getService()
                                        .getCount(CounterAdaptiveQueryOracle.KEY_SYMBOL.withId(MQOFactory.CACHE_KEY))
                                        .isEmpty());
            Statistics.getService().clear();

            // with cache
            Mockito.reset(oracleMock);
            options.cache = true;
            oracle = MQOFactory.getAdaptiveOracle(options, alphabet);

            oracle.processQuery(new PresetAdaptiveQuery<>(new DefaultQuery<>(Word.fromSymbols("a", "b"))));
            Mockito.verify(oracleMock, Mockito.times(1)).processQuery(Mockito.any());
            oracle.processQuery(new PresetAdaptiveQuery<>(new DefaultQuery<>(Word.fromSymbols("a", "b"))));
            Mockito.verify(oracleMock, Mockito.times(1)).processQuery(Mockito.any());

            Assert.assertTrue(Statistics.getService()
                                        .getCount(CounterAdaptiveQueryOracle.KEY_SYMBOL.withId(MQOFactory.SUL_KEY))
                                        .isEmpty());
            Assert.assertTrue(Statistics.getService()
                                        .getCount(CounterAdaptiveQueryOracle.KEY_SYMBOL.withId(MQOFactory.CACHE_KEY))
                                        .isEmpty());
            Statistics.getService().clear();

            // with stats
            Mockito.reset(oracleMock);
            options.cache = false;
            options.statistics = true;
            oracle = MQOFactory.getAdaptiveOracle(options, alphabet);

            oracle.processQuery(new PresetAdaptiveQuery<>(new DefaultQuery<>(Word.fromSymbols("a", "b"))));
            Mockito.verify(oracleMock, Mockito.times(1)).processQuery(Mockito.any());
            oracle.processQuery(new PresetAdaptiveQuery<>(new DefaultQuery<>(Word.fromSymbols("a", "b"))));
            Mockito.verify(oracleMock, Mockito.times(2)).processQuery(Mockito.any());

            Assert.assertTrue(Statistics.getService()
                                        .getCount(CounterAdaptiveQueryOracle.KEY_SYMBOL.withId(MQOFactory.SUL_KEY))
                                        .isPresent());
            Assert.assertTrue(Statistics.getService()
                                        .getCount(CounterAdaptiveQueryOracle.KEY_SYMBOL.withId(MQOFactory.CACHE_KEY))
                                        .isEmpty());
            Statistics.getService().clear();

            // with stats + cache
            Mockito.reset(oracleMock);
            options.cache = true;
            oracle = MQOFactory.getAdaptiveOracle(options, alphabet);

            oracle.processQuery(new PresetAdaptiveQuery<>(new DefaultQuery<>(Word.fromSymbols("a", "b"))));
            Mockito.verify(oracleMock, Mockito.times(1)).processQuery(Mockito.any());
            oracle.processQuery(new PresetAdaptiveQuery<>(new DefaultQuery<>(Word.fromSymbols("a", "b"))));
            Mockito.verify(oracleMock, Mockito.times(1)).processQuery(Mockito.any());

            Assert.assertTrue(Statistics.getService()
                                        .getCount(CounterAdaptiveQueryOracle.KEY_SYMBOL.withId(MQOFactory.SUL_KEY))
                                        .isPresent());
            Assert.assertTrue(Statistics.getService()
                                        .getCount(CounterAdaptiveQueryOracle.KEY_SYMBOL.withId(MQOFactory.CACHE_KEY))
                                        .isPresent());
            Statistics.getService().clear();
        }
    }

    @Test
    public void testOutputTransformer() {
        File sulFile = new File(ApplicationIT.STATELESS_BROKEN);

        final Options options = new Options();
        options.delimiter = "\\n";

        var mqo = MQOFactory.buildSingleTransducerOracle(options, sulFile);

        Assert.assertEquals(mqo.answerQuery(Word.epsilon()), Word.epsilon());
        Assert.assertThrows(() -> mqo.answerQuery(Word.fromLetter("a")));
        Assert.assertThrows(() -> mqo.answerQuery(Word.fromLetter("a"), Word.fromSymbols("a", "b")));
    }
}
