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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;
import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;

public class SnapshotIT {

    private final CommandLine cmd;

    public SnapshotIT() {
        cmd = new CommandLine(new Application());
        prepareCommandLine(cmd);
    }

    private void prepareCommandLine(CommandLine cmd) {
        final IExecutionExceptionHandler defaultHandler = cmd.getExecutionExceptionHandler();

        cmd.setExecutionExceptionHandler((ex, commandLine, fullParseResult) -> {
            Assert.assertTrue(ex instanceof IllegalArgumentException, ex.toString());
            return defaultHandler.handleExecutionException(ex, commandLine, fullParseResult);
        });
        cmd.setParameterExceptionHandler((ex, args) -> {
            Assert.fail("The application should not fail because of wrong parameters");
            return 0;
        });
        cmd.setErr(new PrintWriter(OutputStream.nullOutputStream()));
    }

    @Test
    public void testNonResumableLearner() throws IOException {
        final File resume = Files.createTempFile("learnlib-resume", "").toFile();
        final File snapshot = Files.createTempDirectory("learnlib-snapshot").toFile();

        resume.deleteOnExit();
        snapshot.deleteOnExit();
        int exitCode;

        exitCode = cmd.execute(ApplicationIT.STATELESS,
                               "-sa",
                               "-sb",
                               "-tMEALY",
                               "-lSPARSE",
                               "--resume-from",
                               resume.toString());
        Assert.assertTrue(exitCode > 0);

        exitCode = cmd.execute(ApplicationIT.STATELESS,
                               "-sa",
                               "-sb",
                               "-tMEALY",
                               "-lSPARSE",
                               "--snapshot-dir",
                               snapshot.toString());
        Assert.assertTrue(exitCode > 0);

        exitCode = cmd.execute(ApplicationIT.STATELESS,
                               "-sa",
                               "-sb",
                               "-tMEALY",
                               "-lSPARSE",
                               "--resume-from",
                               resume.toString(),
                               "--snapshot-dir",
                               snapshot.toString());
        Assert.assertTrue(exitCode > 0);
    }

    @Test
    public void testInvalidSnapshotProperties() throws IOException {
        final File resume = Files.createTempDirectory("learnlib-resume").toFile();
        final File snapshot = Files.createTempFile("learnlib-snapshot", "").toFile();

        resume.deleteOnExit();
        snapshot.deleteOnExit();

        int exitCode;

        exitCode = cmd.execute(ApplicationIT.STATELESS, "-sa", "-sb", "--resume-from", resume.toString());
        Assert.assertTrue(exitCode > 0);

        exitCode = cmd.execute(ApplicationIT.STATELESS, "-sa", "-sb", "--snapshot-dir", snapshot.toString());
        Assert.assertTrue(exitCode > 0);

        exitCode = cmd.execute(ApplicationIT.STATELESS,
                               "-sa",
                               "-sb",
                               "--resume-from",
                               resume.toString(),
                               "--snapshot-dir",
                               snapshot.toString());
        Assert.assertTrue(exitCode > 0);
    }

    @Test
    public void testSuspendResume() throws IOException {
        final File snapshot = Files.createTempDirectory("learnlib-snapshot").toFile();
        final File output = Files.createTempFile("learnlib-output", "").toFile();

        snapshot.deleteOnExit();
        output.deleteOnExit();
        int exitCode;

        // run regular scenario with snapshotting
        exitCode = cmd.execute(ApplicationIT.STATELESS_LARGE,
                               "-sa",
                               "-sb",
                               "-tMEALY",
                               "-eSAMPLE",
                               "--eqo-sample=a a a a",
                               "--snapshot-dir",
                               snapshot.toString());
        Assert.assertEquals(exitCode, 0);

        final List<Path> files = Files.list(snapshot.toPath()).toList();

        Assert.assertEquals(files.size(), 1);
        final File resume = files.get(0).toFile();
        resume.deleteOnExit();

        // use cmd once https://github.com/remkop/picocli/issues/2066 is fixed
        final CommandLine cmd2 = new CommandLine(new Application());
        prepareCommandLine(cmd2);
        // resume from snapshot with broken SUL
        // with an empty SampleSet oracle, no equivalence queries should be posed
        // the final hypothesis should be completely constructed from resuming
        exitCode = cmd2.execute(ApplicationIT.STATELESS_BROKEN,
                                "-sa",
                                "-sb",
                                "-tMEALY",
                                "-eSAMPLE",
                                "--resume-from",
                                resume.toString(),
                                "-o",
                                output.toString());
        Assert.assertEquals(exitCode, 0);

        ApplicationIT.checkOutputs(output.toPath(), "/ser/mealy_large.dot");
    }

    @Test
    public void testRegularExecution() throws IOException {
        final File output = Files.createTempFile("learnlib-output", "").toFile();
        output.deleteOnExit();

        // run regular scenario with snapshotting
        final int exitCode = cmd.execute(ApplicationIT.STATELESS_LARGE,
                                         "-sa",
                                         "-sb",
                                         "-tMEALY",
                                         "-eSAMPLE",
                                         "--eqo-sample=a a a a",
                                         "-o",
                                         output.toString());
        Assert.assertEquals(exitCode, 0);
        ApplicationIT.checkOutputs(output.toPath(), "/ser/mealy_large.dot");
    }
}
