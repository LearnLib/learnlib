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
import java.util.Iterator;
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
public class CExFirstBBOracleTest extends AbstractBlackBoxOracleTest {

    private CExFirstBBOracle<AutomatonMock, Void, Void, BlackBoxProperty<?, AutomatonMock, Void, Void>> oracle;

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
        Mockito.when(blackBoxProperty1.findCounterExample(automaton, alphabet)).thenReturn(defaultQuery);
        Mockito.when(blackBoxProperty2.findCounterExample(automaton, alphabet)).thenReturn(defaultQuery);

        final Set<BlackBoxProperty<?, AutomatonMock, Void, Void>> bbps = new HashSet<>();
        bbps.add(blackBoxProperty1);
        bbps.add(blackBoxProperty2);
        oracle = new CExFirstBBOracle<>(bbps);
    }

    @Override
    public AbstractBlackBoxOracle<AutomatonMock, Void, Void, BlackBoxProperty<?, AutomatonMock, Void, Void>>
            getBaseBlackBoxOracle() {
        return oracle;
    }

    /**
     * Tests:
     *  1. whether the correct counterexample is given by the {@link CExFirstBBOracle}, and
     *  2. whether {@link BlackBoxProperty#disprove(Object, Collection)} is called only on {@link #blackBoxProperty2}.
     *
     * This method assumes an order on the {@link BlackBoxProperty}s, i.e.:
     * {@code oracle.getProperties().iterator().next().equals(blackBoxProperty2}.
     */
    @Test
    public void testFindCounterExample() {
        final DefaultQuery cex = oracle.findCounterExample(automaton, alphabet);

        Assert.assertEquals(cex, defaultQuery);

        final Iterator<BlackBoxProperty<?, AutomatonMock, Void, Void>> it = oracle.getProperties().iterator();

        final BlackBoxProperty<?, AutomatonMock, Void, Void> p1 = it.next();
        final BlackBoxProperty<?, AutomatonMock, Void, Void> p2 = it.next();

        Mockito.verify(p1).disprove(automaton, alphabet);
        Mockito.verify(p2, Mockito.never()).disprove(automaton, alphabet);
    }
}