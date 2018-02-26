/* Copyright (C) 2013-2018 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
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
package de.learnlib.oracle.blackbox;

import java.util.Set;

import de.learnlib.api.oracle.BlackBoxOracle.BlackBoxProperty;
import net.automatalib.automata.concepts.Output;
import net.automatalib.ts.simple.SimpleDTS;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Jeroen Meijer
 */
public abstract class AbstractBlackBoxOracleTest {

    public abstract AbstractBlackBoxOracle<AutomatonMock, Void, Void, BlackBoxProperty<?, AutomatonMock, Void, Void>>
        getBaseBlackBoxOracle();

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAmount() {
        final Set<BlackBoxProperty<?, AutomatonMock, Void, Void>> bbps = getBaseBlackBoxOracle().getProperties();
        Assert.assertEquals(bbps.size(), 2);
    }

    @Test
    public void testCache() {
        final Set<BlackBoxProperty<?, AutomatonMock, Void, Void>> bbps = getBaseBlackBoxOracle().getProperties();
        for (BlackBoxProperty bbp : bbps) {
            Mockito.verify(bbp).useCache();
        }
    }

    interface AutomatonMock extends SimpleDTS<Void, Void>, Output<Void, Void> {}
}
