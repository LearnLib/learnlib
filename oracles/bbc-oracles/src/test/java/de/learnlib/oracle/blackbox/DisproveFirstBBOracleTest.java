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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import de.learnlib.api.oracle.BlackBoxOracle.BlackBoxProperty;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.words.Alphabet;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Jeroen Meijer
 */
public class DisproveFirstBBOracleTest extends AbstractBlackBoxOracleTest {

    private DisproveFirstBBOracle<AutomatonMock, Void, Void, BlackBoxProperty<?, AutomatonMock, Void, Void>> oracle;

    @Mock
    private BlackBoxProperty<?, AutomatonMock, Void, Void> blackBoxProperty1;

    @Mock
    private BlackBoxProperty<?, AutomatonMock, Void, Void> blackBoxProperty2;

    @Mock
    private DefaultQuery<Void, Void> defaultQuery;

    @Mock
    private AutomatonMock automaton;

    @Mock
    private Alphabet<Void> alphabet;

    @BeforeMethod
    public void setUp() {
        super.setUp();
        Mockito.when(blackBoxProperty1.findCounterExample(automaton, alphabet)).thenReturn(null);
        Mockito.when(blackBoxProperty2.findCounterExample(automaton, alphabet)).thenReturn(defaultQuery);

        final Set<BlackBoxProperty<?, AutomatonMock, Void, Void>> bbps = new HashSet<>();
        bbps.add(blackBoxProperty1);
        bbps.add(blackBoxProperty2);
        oracle = new DisproveFirstBBOracle<>(bbps);
    }

    @Override
    public AbstractBlackBoxOracle<AutomatonMock, Void, Void, BlackBoxProperty<?, AutomatonMock, Void, Void>>
            getBaseBlackBoxOracle() {
        return oracle;
    }

    /**
     * Tests:
     *  1. whether the correct counterexample is given by the {@link DisproveFirstBBOracle}, and
     *  2. whether {@link BlackBoxProperty#disprove(Object, Collection)} is called on all {@link BlackBoxProperty}s.
     */
    @Test
    public void testFindCounterExample() {
        final DefaultQuery cex = oracle.findCounterExample(automaton, alphabet);

        Assert.assertEquals(cex, defaultQuery);

        Mockito.verify(blackBoxProperty1).disprove(automaton, alphabet);
        Mockito.verify(blackBoxProperty2).disprove(automaton, alphabet);
    }
}