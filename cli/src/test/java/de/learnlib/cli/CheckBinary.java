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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import net.automatalib.common.util.process.ProcessUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * This is an integration test for invoking the final binary. It does not match the regular naming conventions of the
 * surefire- or failsafe-plugin, because it should only be explicitly called by the failsafe-plugin when the "cli"
 * profile is active and the native binary is actually built.
 */
public class CheckBinary {

    @Test
    public void testInvokeBinary() throws IOException, InterruptedException {

        final File bin = Path.of(System.getProperty("learnlib.binary.path", ""))
                             .resolve("bin")
                             .resolve("learnlib")
                             .toAbsolutePath()
                             .toFile();
        final String sul = ApplicationIT.STATELESS;
        final File snapshot = Files.createTempDirectory("learnlib-snapshot").toFile();
        snapshot.deleteOnExit();

        final List<String> argLine = Arrays.asList(bin.getAbsolutePath(),
                                                   "-tDFA",
                                                   "-lL_STAR",
                                                   "-sa",
                                                   "-sb",
                                                   "-eSAMPLE",
                                                   "--eqo-sample=a a",
                                                   "--eqo-sample=a b",
                                                   "--eqo-sample=b",
                                                   "--cache",
                                                   "--stats",
                                                   "--snapshot-dir",
                                                   snapshot.toString(),
                                                   sul);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final int exitCode = ProcessUtil.invokeProcess(argLine, null, OutputStream.nullOutputStream(), baos);

        Assert.assertEquals(exitCode, 0, baos.toString(StandardCharsets.UTF_8));
    }
}
