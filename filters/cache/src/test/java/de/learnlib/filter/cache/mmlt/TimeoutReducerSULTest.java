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
package de.learnlib.filter.cache.mmlt;

import de.learnlib.driver.simulator.MMLTSimulatorSUL;
import de.learnlib.filter.cache.CacheTestUtils;
import net.automatalib.symbol.time.TimedInput;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TimeoutReducerSULTest {

    @Test
    public void testCaching() {

        var mmlt = CacheTestUtils.MMLT;
        var sul = new MMLTSimulatorSUL<>(mmlt);

        var mock = Mockito.spy(sul);
        var toSUL = new TimeoutReducerSUL<>(mock, 1);

        toSUL.pre();
        toSUL.step(TimedInput.input("p1"));
        var output = toSUL.timeStep();

        Assert.assertNull(output);
        Mockito.verify(mock, Mockito.times(1)).timeoutStep(ArgumentMatchers.anyLong());

        output = toSUL.timeStep();

        Assert.assertNull(output);
        Mockito.verify(mock, Mockito.times(1)).timeoutStep(ArgumentMatchers.anyLong());

        toSUL.step(TimedInput.input("p2"));
        output = toSUL.timeoutStep(4);

        Assert.assertNotNull(output);
        Mockito.verify(mock, Mockito.times(2)).timeoutStep(ArgumentMatchers.anyLong());

        toSUL.post();
    }
}
