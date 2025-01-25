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

import java.util.ArrayList;
import java.util.List;

import de.learnlib.oracle.membership.SimulatorOmegaOracle.DFASimulatorOmegaOracle;
import de.learnlib.query.OmegaQuery;
import de.learnlib.testsupport.example.dfa.ExamplePaulAndMary;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests the same functionality as in {@link SimulatorOracleTest}, but also tests whether the correct states are
 * visited.
 */
public class SimulatorOmegaOracleTest {

    @Test
    public void testDFASimulatorOmegaOracle() {

        DFA<Integer, String> dfa = ExamplePaulAndMary.constructMachine();

        DFASimulatorOmegaOracle<Integer, String> oracle = new DFASimulatorOmegaOracle<>(dfa);

        List<OmegaQuery<String, Boolean>> queries = new ArrayList<>();

        OmegaQuery<String, Boolean> q1 = new OmegaQuery<>(Word.epsilon(),
                                                          Word.fromSymbols(ExamplePaulAndMary.IN_PAUL,
                                                                           ExamplePaulAndMary.IN_LOVES,
                                                                           ExamplePaulAndMary.IN_MARY),
                                                          1);
        OmegaQuery<String, Boolean> q2 = new OmegaQuery<>(Word.fromSymbols(ExamplePaulAndMary.IN_MARY),
                                                          Word.fromSymbols(ExamplePaulAndMary.IN_MARY,
                                                                           ExamplePaulAndMary.IN_LOVES,
                                                                           ExamplePaulAndMary.IN_PAUL),
                                                          1);
        queries.add(q1);
        queries.add(q2);

        Assert.assertEquals(queries.get(0).getLoop().size(), 3);
        Assert.assertEquals(queries.get(1).getLoop().size(), 3);

        oracle.processQueries(queries);

        // Paul loves Mary...
        Assert.assertFalse(queries.get(0).isUltimatelyPeriodic());

        // ... but Mary does not love Paul :-(
        Assert.assertTrue(queries.get(1).isUltimatelyPeriodic());
        Assert.assertEquals(queries.get(1).getOutput(), Boolean.FALSE);
    }
}
