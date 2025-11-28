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

import de.learnlib.driver.simulator.MMLTSimulatorSUL;
import de.learnlib.testsupport.example.mmlt.MMLTExamples;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TimedSULOracleTest {

    @Test
    public void testValidation() {
        var example = MMLTExamples.sensorCollector();
        var mmlt = example.getReferenceAutomaton();
        var params = example.getParams();

        var oracle = new TimedSULOracle<>(new MMLTSimulatorSUL<>(mmlt), params);

        Assert.assertThrows(IllegalArgumentException.class,
                            () -> oracle.queryTimers(Word.epsilon(), params.maxTimeoutWaitingTime() - 1));
        Assert.assertThrows(IllegalArgumentException.class,
                            () -> oracle.queryTimers(Word.fromLetter(TimedInput.timeout()),
                                                     params.maxTimeoutWaitingTime()));
        Assert.assertThrows(IllegalArgumentException.class,
                            () -> oracle.answerQuery(Word.epsilon(), Word.fromLetter(TimedInput.step(2))));

        // assert not throwing
        Assert.assertEquals(oracle.answerQuery(Word.fromLetter(TimedInput.timeout()),
                                               Word.fromLetter(TimedInput.timeout())),
                            Word.fromLetter(mmlt.getSemantics().getSilentOutput()));

    }
}
