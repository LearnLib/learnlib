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

public class StdInOutputAdaptiveOracleTest extends AbstractPythonTest {

    @Test
    public void testStatefulCommunication() throws URISyntaxException {
        final String script = getPathToScript("/stateful_stdin_sul.py");
        final String reset = "reset";
        final StdInOutputAdaptiveOracle<Character, Integer> oracle =
                new StdInOutputAdaptiveOracle<>(Arrays.asList(PROGRAM, script), Integer::parseInt, reset);

        final AdaptiveTestQuery<Character, Integer> q1 = new AdaptiveTestQuery<>(Word.fromString("ab"));
        final AdaptiveTestQuery<Character, Integer> q2 =
                new AdaptiveTestQuery<>(Word.fromLetter('a'), Word.fromString("ab"));

        oracle.processQuery(q1);
        oracle.processQuery(q2);

        Assert.assertEquals(q1.getOutputs().size(), 1);
        Assert.assertEquals(q1.getOutputs().get(0).toWord(), Word.fromSymbols(97, 98));
        Assert.assertEquals(q2.getOutputs().size(), 2);
        Assert.assertEquals(q2.getOutputs().get(0).toWord(), Word.fromLetter(97));
        Assert.assertEquals(q2.getOutputs().get(1).toWord(), Word.fromSymbols(97, 98));

        final String brokenScript = script.substring(0, script.length() - 3) + "2.py";
        final StdInOutputAdaptiveOracle<Character, Integer> brokenOracle =
                new StdInOutputAdaptiveOracle<>(Arrays.asList(PROGRAM, brokenScript), Integer::parseInt, reset);

        final AdaptiveTestQuery<Character, Integer> bq1 = new AdaptiveTestQuery<>(Word.fromString("ab"));
        final AdaptiveTestQuery<Character, Integer> bq2 =
                new AdaptiveTestQuery<>(Word.fromLetter('a'), Word.fromString("ab"));

        Assert.assertThrows(() -> brokenOracle.processQuery(bq1));
        Assert.assertThrows(() -> brokenOracle.processQuery(bq2));
    }

}
