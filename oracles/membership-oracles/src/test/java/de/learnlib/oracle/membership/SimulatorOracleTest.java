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

import de.learnlib.query.DefaultQuery;
import de.learnlib.testsupport.example.dfa.ExamplePaulAndMary;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SimulatorOracleTest {

    @Test
    public void testDFASimulatorOracle() {

        DFA<?, String> dfa = ExamplePaulAndMary.constructMachine();

        SimulatorOracle<String, Boolean> oracle = new SimulatorOracle<>(dfa);

        List<DefaultQuery<String, Boolean>> queries = new ArrayList<>();

        DefaultQuery<String, Boolean> q1 = new DefaultQuery<>(Word.fromSymbols(ExamplePaulAndMary.IN_PAUL,
                                                                               ExamplePaulAndMary.IN_LOVES,
                                                                               ExamplePaulAndMary.IN_MARY));
        DefaultQuery<String, Boolean> q2 = new DefaultQuery<>(Word.fromSymbols(ExamplePaulAndMary.IN_MARY,
                                                                               ExamplePaulAndMary.IN_LOVES,
                                                                               ExamplePaulAndMary.IN_PAUL));
        queries.add(q1);
        queries.add(q2);

        Assert.assertEquals(queries.get(0).getInput().size(), 3);
        Assert.assertEquals(queries.get(1).getInput().size(), 3);

        oracle.processQueries(queries);

        // Paul loves Mary...
        Assert.assertEquals(queries.get(0).getOutput(), Boolean.TRUE);

        // ... but Mary does not love Paul :-(
        Assert.assertEquals(queries.get(1).getOutput(), Boolean.FALSE);

    }

}
