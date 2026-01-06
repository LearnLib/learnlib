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
package de.learnlib.oracle.equivalence.mmlt;

import de.learnlib.oracle.TimedQueryOracle;
import de.learnlib.testsupport.example.mmlt.MMLTExamples;
import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.symbol.time.TimedInput;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ResetSearchEQOracleTest {

    @Test
    public void testInsertPercentages() {
        @SuppressWarnings("unchecked")
        TimedQueryOracle<String, String> mock = Mockito.mock(TimedQueryOracle.class);

        var example = MMLTExamples.sensorCollector();
        var mmlt = example.getReferenceAutomaton();
        var alphabet = example.getAlphabet();

        var eqo = new ResetSearchEQOracle<>(mock, 123, 0, 1);
        var cex = eqo.findCounterExample(mmlt, alphabet);

        Assert.assertNull(cex);
        Mockito.verifyNoInteractions(mock);

        eqo = new ResetSearchEQOracle<>(mock, 123, 1, 0);
        cex = eqo.findCounterExample(mmlt, alphabet);

        Assert.assertNull(cex);
        Mockito.verifyNoInteractions(mock);
    }

    @Test
    public void testAlphabetFilter() {
        @SuppressWarnings("unchecked")
        TimedQueryOracle<String, String> mock = Mockito.spy(TimedQueryOracle.class);

        var example = MMLTExamples.sensorCollector();
        var mmlt = example.getReferenceAutomaton();
        var alphabet = example.getUntimedAlphabet().stream().<TimedInput<String>>map(TimedInput::input).toList();

        var eqo = new ResetSearchEQOracle<>(mock, 123, 1, 1);
        var cex = eqo.findCounterExample(mmlt, alphabet);

        Assert.assertNull(cex);
        Mockito.verifyNoInteractions(mock);

        var alphabetWithTimeOut = new GrowingMapAlphabet<>(alphabet);
        alphabetWithTimeOut.add(TimedInput.timeout());
        cex = eqo.findCounterExample(mmlt, alphabetWithTimeOut);

        Assert.assertNull(cex);
        Mockito.verifyNoInteractions(mock);

        var alphabetWithTimestep = new GrowingMapAlphabet<>(alphabet);
        alphabetWithTimestep.add(TimedInput.step());
        cex = eqo.findCounterExample(mmlt, alphabetWithTimestep);

        Assert.assertNull(cex);
        Mockito.verifyNoInteractions(mock);

        var validAlphabe = example.getAlphabet();
        cex = eqo.findCounterExample(mmlt, validAlphabe);

        // mock always returns null which differs from any non-null hypothesis output
        Assert.assertNotNull(cex);
        Mockito.verify(mock, Mockito.atLeastOnce()).processQueries(ArgumentMatchers.anyCollection());
    }
}
