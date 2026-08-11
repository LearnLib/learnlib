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
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import net.automatalib.common.util.process.ProcessUtil;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;

public abstract class AbstractPythonTest {

    private static final boolean AVAILABLE;
    public static final String PROGRAM;

    static {
        String path = "";
        boolean available = false;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // if python is available, we can also use it to give us the absolute path to its interpreter
            if (ProcessUtil.invokeProcess(new String[] {"python3", "-c", "import sys; print(sys.executable, end=\"\")"},
                                          null,
                                          baos,
                                          OutputStream.nullOutputStream()) == 0) {
                path = baos.toString(StandardCharsets.UTF_8);
                available = true;
            }
        } catch (IOException | InterruptedException ignored) {
            // use defaults
        }

        AVAILABLE = available;
        PROGRAM = path;
    }

    @BeforeClass
    public void setUp() {
        if (!AVAILABLE) {
            throw new SkipException("python3 not supported");
        }
    }

}
