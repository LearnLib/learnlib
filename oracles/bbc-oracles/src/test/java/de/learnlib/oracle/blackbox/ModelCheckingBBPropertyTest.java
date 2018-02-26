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

import de.learnlib.api.modelchecking.modelchecker.ModelChecker;
import de.learnlib.api.oracle.EmptinessOracle;
import de.learnlib.api.oracle.InclusionOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.concepts.Output;
import net.automatalib.ts.simple.SimpleDTS;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the cached, and uncached implementations of {@link ModelCheckingBBProperty#disprove(Output, Collection)}, and
 * {@link ModelCheckingBBProperty#findCounterExample(Output, Collection)}.
 *
 * @author Jeroen Meijer
 */
public class ModelCheckingBBPropertyTest {

    private Alphabet<String> alphabet = Alphabets.fromArray("a");

    @Mock
    private AutomatonMock automaton;

    @Mock
    private ResultMock result;

    @Mock
    private ModelChecker<String, AutomatonMock, String, ResultMock> modelChecker;

    @Mock
    private EmptinessOracle<ResultMock, String, String, DefaultQuery<String, String>> emptinessOracle;

    @Mock
    private InclusionOracle<AutomatonMock, String, String, DefaultQuery<String, String>> inclusionOracle;

    private ModelCheckingBBProperty<String, AutomatonMock, String, String, ResultMock> mcbbp;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mcbbp = new ModelCheckingBBProperty<>(modelChecker, emptinessOracle, inclusionOracle, "");
        Mockito.when(modelChecker.findCounterExample(automaton, alphabet, "")).thenReturn(result);
    }

    @Test
    public void testDisproveUncached() {
        mcbbp.disprove(automaton, alphabet);
        Mockito.verify(modelChecker, Mockito.times(1)).findCounterExample(Mockito.eq(automaton), Mockito.any(), Mockito.eq(""));
        Mockito.verify(emptinessOracle, Mockito.times(1)).findCounterExample(result, alphabet);
        mcbbp.disprove(automaton, alphabet);
        Mockito.verify(modelChecker, Mockito.times(2)).findCounterExample(Mockito.eq(automaton), Mockito.any(), Mockito.eq(""));
    }

    @Test
    public void testDisproveCached() {
        mcbbp.useCache();

        mcbbp.disprove(automaton, alphabet);
        Mockito.verify(modelChecker, Mockito.times(1)).findCounterExample(Mockito.eq(automaton), Mockito.any(), Mockito.eq(""));
        Mockito.verify(emptinessOracle, Mockito.times(1)).findCounterExample(result, alphabet);

        mcbbp.disprove(automaton, alphabet);
        Mockito.verify(modelChecker, Mockito.times(1)).findCounterExample(Mockito.eq(automaton), Mockito.any(), Mockito.eq(""));

        mcbbp.disprove(automaton, Alphabets.singleton("no-cache"));
        Mockito.verify(modelChecker, Mockito.times(2)).findCounterExample(Mockito.eq(automaton), Mockito.any(), Mockito.eq(""));

        mcbbp.clearCache();

        mcbbp.disprove(automaton, alphabet);
        Mockito.verify(modelChecker, Mockito.times(3)).findCounterExample(Mockito.eq(automaton), Mockito.any(), Mockito.eq(""));
    }

    @Test
    public void testFindCounterExampleUncached() {
        mcbbp.findCounterExample(automaton, alphabet);
        Mockito.verify(modelChecker, Mockito.times(1)).findCounterExample(Mockito.eq(automaton), Mockito.any(), Mockito.eq(""));
        Mockito.verify(inclusionOracle, Mockito.times(1)).findCounterExample(result, alphabet);
        mcbbp.findCounterExample(automaton, alphabet);
        Mockito.verify(modelChecker, Mockito.times(2)).findCounterExample(Mockito.eq(automaton), Mockito.any(), Mockito.eq(""));
    }

    @Test
    public void testFindCounterExampleCached() {
        mcbbp.useCache();

        mcbbp.findCounterExample(automaton, alphabet);
        Mockito.verify(modelChecker, Mockito.times(1)).findCounterExample(Mockito.eq(automaton), Mockito.any(), Mockito.eq(""));
        Mockito.verify(inclusionOracle, Mockito.times(1)).findCounterExample(result, alphabet);

        mcbbp.findCounterExample(automaton, alphabet);
        Mockito.verify(modelChecker, Mockito.times(1)).findCounterExample(Mockito.eq(automaton), Mockito.any(), Mockito.eq(""));

        mcbbp.findCounterExample(automaton, Alphabets.singleton("no-cache"));
        Mockito.verify(modelChecker, Mockito.times(2)).findCounterExample(Mockito.eq(automaton), Mockito.any(), Mockito.eq(""));

        mcbbp.clearCache();

        mcbbp.findCounterExample(automaton, alphabet);
        Mockito.verify(modelChecker, Mockito.times(3)).findCounterExample(Mockito.eq(automaton), Mockito.any(), Mockito.eq(""));
    }

    interface AutomatonMock extends SimpleDTS<Integer, String>, Output<String, String> {}

    interface ResultMock extends AutomatonMock {}
}