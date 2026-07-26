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
package de.learnlib.cli;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import de.learnlib.cli.util.AbstractRunner;
import de.learnlib.filter.statistic.oracle.CounterAdaptiveQueryOracle;
import de.learnlib.filter.statistic.oracle.CounterOracle;
import de.learnlib.statistic.Statistics;
import de.learnlib.statistic.StatisticsService;
import net.automatalib.common.util.IOUtil;
import net.automatalib.common.util.process.ProcessUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import picocli.CommandLine;

public class ApplicationIT {

    public static final String STATELESS = getPathToScript("/sul/stateless.py");
    public static final String STATELESS_SPA = getPathToScript("/sul/spa.py");
    public static final String STATELESS_SBA = getPathToScript("/sul/sba.py");
    public static final String STATEFUL = getPathToScript("/sul/stateful.py");

    private static String getPathToScript(String script) {
        final URL resource = Objects.requireNonNull(ApplicationIT.class.getResource(script));
        try {
            return Paths.get(resource.toURI()).toFile().getAbsolutePath();
        } catch (URISyntaxException e) {
            throw new SkipException("Error while loading script " + script);
        }
    }

    @BeforeTest
    public void setUp() {
        try {
            if (ProcessUtil.invokeProcess(new String[] {"python", "--version"}) != 0) {
                throw new SkipException("python not supported");
            }
        } catch (IOException | InterruptedException e) {
            throw new SkipException("python not supported");
        }
    }

    @Test
    public void testVerbosity() {
        final Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        final Level oldLevel = root.getLevel();

        final Application app = new Application();
        final CommandLine cmd = new CommandLine(app);

        int exitCode = cmd.execute("-v", STATELESS, "-sa", "-eSAMPLE", "--eqo-sample=a b");

        Assert.assertEquals(exitCode, 0);
        Assert.assertEquals(root.getLevel(), Level.DEBUG);


        exitCode = cmd.execute("-vv", STATELESS, "-sa", "-eSAMPLE", "--eqo-sample=a b");

        Assert.assertEquals(exitCode, 0);
        Assert.assertEquals(root.getLevel(), Level.TRACE);

        root.setLevel(oldLevel);
    }

    @Test
    public void testStatelessDFA() throws IOException {
        checkRegularExecution(new String[] {"-lLSTAR", "-eSAMPLE", "--eqo-sample=a b", STATELESS}, "/ser/dfa.dot");
    }

    @Test
    public void testMealyPreset() throws IOException {
        checkRegularExecution(new String[] {"-lLSTAR", "-tMEALY", "-eSAMPLE", "--eqo-sample=a b", STATELESS},
                              "/ser/mealy.dot");
    }

    @Test
    public void testMealyAdaptive() throws IOException {
        checkRegularExecution(new String[] {"-tMEALY",
                                            "-lLSHARP",
                                            "-eSAMPLE",
                                            "--eqo-sample=a b",
                                            "--reset=reset",
                                            STATEFUL}, "/ser/mealy.dot");
    }

    @Test
    public void testStatelessNFA() throws IOException {
        checkRegularExecution(new String[] {"-tNFA", "-lNLSTAR", "-eSAMPLE", "--eqo-sample=a b", STATELESS},
                              "/ser/dfa.dot");
    }

    @Test
    public void testStatelessSBA() throws IOException {
        checkProceduralExecution(new String[] {"-lLSTAR", "-tSBA", "-eSAMPLE", "--eqo-sample=S a R", STATELESS_SBA},
                                 null);
    }

    @Test
    public void testStatelessSPA() throws IOException {
        checkProceduralExecution(new String[] {"-lLSTAR", "-tSPA", "-eSAMPLE", "--eqo-sample=S a R", STATELESS_SPA},
                                 "/ser/spa.dot");
    }

    @Test
    public void testStatelessSPMM() throws IOException {
        checkProceduralExecution(new String[] {"-lLSTAR",
                                               "-tSPMM",
                                               "-eSAMPLE",
                                               "--eqo-sample=S a R",
                                               "-d=\\s",
                                               STATELESS_SBA}, "/ser/spmm.dot");
    }

    @Test
    public void testStatelessVPA() throws IOException {
        checkProceduralExecution(new String[] {"-tVPA", "-eSAMPLE", "--eqo-sample=S a R", STATELESS_SPA}, null);
    }

    @Test
    public void testStatistics() throws IOException {
        checkRegularExecution(new String[] {"-lLSTAR", "-eSAMPLE", "--eqo-sample=abab", "--stats", STATELESS}, null);

        StatisticsService statistics = Statistics.getService();
        Assert.assertTrue(statistics.getCount(CounterOracle.KEY_SYMBOL.withId(AbstractRunner.EQO_KEY)).isPresent());
        Assert.assertTrue(statistics.getCount(CounterOracle.KEY_SYMBOL.withId(AbstractRunner.LEARNER_KEY)).isPresent());
        statistics.clear();
    }

    @Test
    public void testAdaptiveStatistics() throws IOException {
        checkRegularExecution(new String[] {"-tMEALY",
                                            "-lLSHARP",
                                            "-eSAMPLE",
                                            "--eqo-sample=abab",
                                            "--reset=reset",
                                            "--stats",
                                            STATEFUL}, null);

        StatisticsService statistics = Statistics.getService();
        Assert.assertTrue(statistics.getCount(CounterAdaptiveQueryOracle.KEY_SYMBOL.withId(AbstractRunner.EQO_KEY))
                                    .isPresent());
        Assert.assertTrue(statistics.getCount(CounterAdaptiveQueryOracle.KEY_SYMBOL.withId(AbstractRunner.LEARNER_KEY))
                                    .isPresent());
        statistics.clear();
    }

    private void checkRegularExecution(String[] params, @Nullable String resource) throws IOException {
        Path out = Files.createTempFile("automatalib", "");
        out.toFile().deleteOnExit();

        final String[] args = Arrays.copyOf(params, params.length + 4);
        args[args.length - 4] = "-sa";
        args[args.length - 3] = "-sb";
        args[args.length - 2] = "-o";
        args[args.length - 1] = out.toAbsolutePath().toString();

        final Application app = new Application();
        final CommandLine cmd = new CommandLine(app);

        final int exitCode = cmd.execute(args);

        Assert.assertEquals(exitCode, 0);

        if (resource != null) {
            checkOutputs(out, resource);
        }
    }

    private void checkProceduralExecution(String[] params, String resource) throws IOException {
        Path out = Files.createTempFile("automatalib", "");
        out.toFile().deleteOnExit();

        final String[] args = Arrays.copyOf(params, params.length + 6);
        args[args.length - 6] = "--call=S";
        args[args.length - 5] = "--int=a";
        args[args.length - 4] = "--int=b";
        args[args.length - 3] = "--ret=R";
        args[args.length - 2] = "-o";
        args[args.length - 1] = out.toAbsolutePath().toString();

        final Application app = new Application();
        final CommandLine cmd = new CommandLine(app);

        final int exitCode = cmd.execute(args);

        Assert.assertEquals(exitCode, 0);
        if (resource != null) {
            checkOutputs(out, resource);
        }
    }

    private void checkOutputs(Path output, String resource) throws IOException {
        final StringWriter expectedWriter = new StringWriter();

        try (Reader reader = IOUtil.asBufferedUTF8Reader(ApplicationIT.class.getResourceAsStream(resource))) {
            reader.transferTo(expectedWriter);
            Assert.assertEquals(Files.readString(output), expectedWriter.toString());
        }
    }

}
