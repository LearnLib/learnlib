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
package de.learnlib.oracle.membership;

import java.net.URISyntaxException;
import java.util.Arrays;

import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

public class StdInOracleTest extends AbstractPythonTest {

    @Test
    public void testStatelessCommunication() throws URISyntaxException {
        final String script = getPathToScript("/stateless_stdin_sul.py");
        final StdInOracle<Character> oracle = new StdInOracle<>(Arrays.asList(PROGRAM, script));

        Assert.assertEquals(oracle.answerQuery(Word.epsilon()), false);
        Assert.assertEquals(oracle.answerQuery(Word.epsilon(), Word.fromString("ab")), false);
        Assert.assertEquals(oracle.answerQuery(Word.fromLetter('a'), Word.fromString("ab")), true);

        final String brokenScript = script.substring(0, script.length() - 3) + "2.py";
        final StdInOracle<Character> brokenOracle = new StdInOracle<>(Arrays.asList(PROGRAM, brokenScript));

        Assert.assertEquals(brokenOracle.answerQuery(Word.epsilon(), Word.fromString("ab")), false);
        Assert.assertEquals(brokenOracle.answerQuery(Word.fromLetter('a'), Word.fromString("ab")), false);
    }

    @Test
    public void testStatefulCommunication() throws URISyntaxException {
        final String script = getPathToScript("/stateful_stdin_sul.py");
        final String reset = "reset";
        final StdInOracle<Character> oracle = new StdInOracle<>(Arrays.asList(PROGRAM, script), reset);

        Assert.assertEquals(oracle.answerQuery(Word.epsilon()), false);
        Assert.assertEquals(oracle.answerQuery(Word.epsilon(), Word.fromString("ab")), false);
        Assert.assertEquals(oracle.answerQuery(Word.fromLetter('a'), Word.fromString("ab")), true);

        final String brokenScript = script.substring(0, script.length() - 3) + "2.py";
        final StdInOracle<Character> brokenOracle = new StdInOracle<>(Arrays.asList(PROGRAM, brokenScript), reset);

        Assert.assertEquals(brokenOracle.answerQuery(Word.epsilon(), Word.fromString("ab")), false);
        Assert.assertEquals(brokenOracle.answerQuery(Word.fromLetter('a'), Word.fromString("ab")), false);
    }
}
