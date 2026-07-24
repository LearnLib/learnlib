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

public class CLIOutputOracleTest extends AbstractPythonTest {

    @Test
    public void testStatelessCommunication() throws URISyntaxException {
        final String script = getPathToScript("/stateless_sul.py");
        final CLIOutputOracle<Character, Word<Integer>> oracle =
                new CLIOutputOracle<>(Arrays.asList(PROGRAM, script), CLIOutputOracleTest::parseOutput);

        Assert.assertEquals(oracle.answerQuery(Word.epsilon()), Word.epsilon());
        Assert.assertEquals(oracle.answerQuery(Word.epsilon(), Word.fromString("ab")), Word.fromSymbols(97, 98));
        Assert.assertEquals(oracle.answerQuery(Word.fromLetter('a'), Word.fromString("ab")), Word.fromSymbols(97, 98));

        final String brokenScript = script.substring(0, script.length() - 3) + "2.py";
        final CLIOutputOracle<Character, Word<Integer>> brokenOracle =
                new CLIOutputOracle<>(Arrays.asList(PROGRAM, brokenScript), CLIOutputOracleTest::parseOutput);

        Assert.assertThrows(() -> brokenOracle.answerQuery(Word.epsilon(), Word.fromString("ab")));
        Assert.assertThrows(() -> brokenOracle.answerQuery(Word.fromLetter('a'), Word.fromString("ab")));
    }

    @Test
    public void testStatefulCommunication() throws URISyntaxException {
        final String script = getPathToScript("/stateful_sul.py");
        final String reset = "reset";
        final CLIOutputOracle<Character, Word<Integer>> oracle =
                new CLIOutputOracle<>(Arrays.asList(PROGRAM, script), CLIOutputOracleTest::parseOutput, reset);

        Assert.assertEquals(oracle.answerQuery(Word.epsilon()), Word.epsilon());
        Assert.assertEquals(oracle.answerQuery(Word.epsilon(), Word.fromString("ab")), Word.fromSymbols(97, 98));
        Assert.assertEquals(oracle.answerQuery(Word.fromLetter('a'), Word.fromString("ab")), Word.fromSymbols(97, 98));

        final String brokenScript = script.substring(0, script.length() - 3) + "2.py";
        final CLIOutputOracle<Character, Word<Integer>> brokenOracle =
                new CLIOutputOracle<>(Arrays.asList(PROGRAM, brokenScript), CLIOutputOracleTest::parseOutput, reset);

        Assert.assertThrows(() -> brokenOracle.answerQuery(Word.epsilon(), Word.fromString("ab")));
        Assert.assertThrows(() -> brokenOracle.answerQuery(Word.fromLetter('a'), Word.fromString("ab")));
    }

    static Word<Integer> parseOutput(String input, Integer suffix) {
        if (suffix == 0) {
            return Word.epsilon();
        }

        if (input.isBlank()) {
            throw new IllegalStateException();
        }

        final Word<Integer> result =
                Arrays.stream(input.split(System.lineSeparator())).map(Integer::parseInt).collect(Word.collector());

        return result.subWord(result.size() - suffix);
    }
}
