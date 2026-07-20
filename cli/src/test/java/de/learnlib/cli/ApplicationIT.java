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

import net.automatalib.common.util.IOUtil;
import net.automatalib.common.util.process.ProcessUtil;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import picocli.CommandLine;

public class ApplicationIT {

    private static final String STATELESS = getPathToScript("/suls/stateless.py");
    private static final String STATELESS_STDIN = getPathToScript("/suls/stateless_stdin.py");
    private static final String STATEFUL = getPathToScript("/suls/stateful.py");
    private static final String STATEFUL_STDIN = getPathToScript("/suls/stateful_stdin.py");

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
    public void testStatelessMealy() throws IOException {
        checkExecution(new String[] {"-sa", "-sb", "-tMEALY", "--eqo-random-wp-bound", "100", STATELESS}, "/mealy.dot");
    }

    @Test
    public void testStatelessDFA() throws IOException {
        checkExecution(new String[] {"-sa", "-sb", "--eqo-random-wp-bound", "100", STATELESS}, "/dfa.dot");
    }

    @Test
    public void testStatelessStdInMealy() throws IOException {
        checkExecution(new String[] {"-sa", "-sb", "-tMEALY", "--stdin", "-eWP", STATELESS_STDIN}, "/mealy.dot");
    }

    @Test
    public void testStatelessStdInDFA() throws IOException {
        checkExecution(new String[] {"-sa", "-sb", "--stdin", "-eWP", STATELESS_STDIN}, "/dfa.dot");
    }

    @Test
    public void testStatefulMealy() throws IOException {
        checkExecution(new String[] {"-sa",
                                     "-sb",
                                     "-tMEALY",
                                     "--reset",
                                     "reset",
                                     "-eSAMPLE",
                                     "--eqo-sample",
                                     "abab",
                                     STATEFUL}, "/mealy.dot");
    }

    @Test
    public void testStatefulDFA() throws IOException {
        checkExecution(new String[] {"-sa", "-sb", "--reset", "reset", "-eSAMPLE", "--eqo-sample", "abab", STATEFUL},
                       "/dfa.dot");
    }

    @Test
    public void testStatefulStdInMealy() throws IOException {
        checkExecution(new String[] {"-sa",
                                     "-sb",
                                     "-tMEALY",
                                     "--stdin",
                                     "--reset",
                                     "reset",
                                     "-eSAMPLE",
                                     "--eqo-sample",
                                     "abab",
                                     STATEFUL_STDIN}, "/mealy.dot");
    }

    @Test
    public void testStatefulStdInDFA() throws IOException {
        checkExecution(new String[] {"-sa",
                                     "-sb",
                                     "--stdin",
                                     "--reset",
                                     "reset",
                                     "-eSAMPLE",
                                     "--eqo-sample",
                                     "abab",
                                     STATEFUL_STDIN}, "/dfa.dot");
    }

    private void checkExecution(String[] params, String resource) throws IOException {
        Path out = Files.createTempFile("automatalib", "");
        out.toFile().deleteOnExit();

        final String[] args = Arrays.copyOf(params, params.length + 2);
        args[args.length - 2] = "-o";
        args[args.length - 1] = out.toAbsolutePath().toString();

        final Application app = new Application();
        final CommandLine cmd = new CommandLine(app);

        final int exitCode = cmd.execute(args);

        Assert.assertEquals(exitCode, 0);
        checkOutputs(out, resource);
    }

    private void checkOutputs(Path output, String resource) throws IOException {
        final StringWriter expectedWriter = new StringWriter();

        try (Reader reader = IOUtil.asBufferedUTF8Reader(ApplicationIT.class.getResourceAsStream(resource))) {
            reader.transferTo(expectedWriter);
            Assert.assertEquals(Files.readString(output), expectedWriter.toString());
        }
    }

}
