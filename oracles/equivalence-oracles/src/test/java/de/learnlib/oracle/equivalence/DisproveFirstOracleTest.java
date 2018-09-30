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
package de.learnlib.oracle.equivalence;

import java.util.Collection;

import com.google.common.collect.Lists;
import de.learnlib.api.oracle.BlackBoxOracle;
import de.learnlib.api.oracle.PropertyOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.concepts.Output;
import net.automatalib.words.Alphabet;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DisproveFirstOracleTest {

    @Mock
    private PropertyOracle<Boolean, Output<Boolean, Boolean>, Boolean, Boolean> po1;

    @Mock
    private PropertyOracle<Boolean, Output<Boolean, Boolean>, Boolean, Boolean> po2;

    private BlackBoxOracle<Output<Boolean, Boolean>, Boolean, Boolean> oracle;

    @Mock
    private DefaultQuery<Boolean, Boolean> query;

    @Mock
    private Output<Boolean, Boolean> automaton;

    @Mock
    private Alphabet<Boolean> inputs;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        // make sure the assertion check for InclusionOracle.isCounterExample passes
        Mockito.when(query.getInput()).thenReturn(null);
        Mockito.when(query.getOutput()).thenReturn(Boolean.TRUE);
        Mockito.when(automaton.computeOutput(Mockito.any())).thenReturn(Boolean.FALSE);

        oracle = new DisproveFirstOracle<>(Lists.newArrayList(po1, po2));
        Mockito.when(po1.findCounterExample(automaton, inputs)).thenCallRealMethod();
        Mockito.when(po1.doFindCounterExample(automaton, inputs)).thenReturn(query);
        Mockito.when(po2.findCounterExample(automaton, inputs)).thenCallRealMethod();
        Mockito.when(po2.doFindCounterExample(automaton, inputs)).thenReturn(query);
    }

    @Test
    public void testGetPropertyOracles() throws Exception {
        Assert.assertEquals(oracle.getPropertyOracles().size(), 2);
    }

    /**
     * Tests:
     *  1. whether the correct counterexample is given by the {@link DisproveFirstOracle}, and
     *  2. whether {@link PropertyOracle#disprove(Output, Collection)} is called only on {@link #po2}.
     */
    @Test
    public void testFindCounterExample() throws Exception {
        final DefaultQuery<Boolean, Boolean> ce = oracle.findCounterExample(automaton, inputs);

        Assert.assertEquals(ce, query);

        Mockito.verify(po1, Mockito.times(1)).disprove(automaton, inputs);
        Mockito.verify(po2).disprove(automaton, inputs);
        Mockito.verify(po2, Mockito.never()).doFindCounterExample(automaton, inputs);
    }
}