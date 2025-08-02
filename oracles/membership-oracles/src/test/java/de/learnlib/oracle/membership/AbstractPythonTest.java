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
package de.learnlib.oracle.membership;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Objects;

import net.automatalib.common.util.process.ProcessUtil;
import org.testng.SkipException;
import org.testng.annotations.BeforeTest;

public abstract class AbstractPythonTest {

    protected static final String PROGRAM = "python";

    protected static String getPathToScript(String script) throws URISyntaxException {
        final URL resource = Objects.requireNonNull(AbstractPythonTest.class.getResource(script));
        return Paths.get(resource.toURI()).toFile().getAbsolutePath();
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

}
