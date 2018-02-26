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
package de.learnlib.oracle.membership;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import de.learnlib.api.query.OmegaQuery;
import de.learnlib.examples.dfa.ExamplePaulAndMary;
import de.learnlib.oracle.membership.SimulatorOmegaOracle.DFASimulatorOmegaOracle;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Symbol;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests the same functionality as in {@link SimulatorOracleTest}, but also tests whether the correct states are
 * visited.
 *
 * @author Jeroen Meijer
 */
public class SimulatorOmegaOracleTest {

    @Test
    public void testDFASimulatorOmegaOracle() {

        DFA<Integer, Symbol> dfa = ExamplePaulAndMary.constructMachine();

        DFASimulatorOmegaOracle<Integer, Symbol> oracle = new DFASimulatorOmegaOracle<>(dfa);

        List<OmegaQuery<Integer, Symbol, Boolean>> queries = new ArrayList<>();

        OmegaQuery<Integer, Symbol, Boolean> q1 = new OmegaQuery<>(Word.fromSymbols(ExamplePaulAndMary.IN_PAUL,
                                                                                    ExamplePaulAndMary.IN_LOVES,
                                                                                    ExamplePaulAndMary.IN_MARY),
                                                                   new HashSet<>(Arrays.asList(0, 1, 2, 3)));
        OmegaQuery<Integer, Symbol, Boolean> q2 = new OmegaQuery<>(Word.fromSymbols(ExamplePaulAndMary.IN_MARY,
                                                                                    ExamplePaulAndMary.IN_LOVES,
                                                                                    ExamplePaulAndMary.IN_PAUL),
                                                                   new HashSet<>(Arrays.asList(0, 1, 2, 3)));
        queries.add(q1);
        queries.add(q2);

        Assert.assertEquals(queries.get(0).getInput().size(), 3);
        Assert.assertEquals(queries.get(0).getIndices().size(), 4);
        Assert.assertEquals(queries.get(1).getInput().size(), 3);
        Assert.assertEquals(queries.get(1).getIndices().size(), 4);

        oracle.processQueries(queries);

        // Paul loves Mary...
        Assert.assertEquals(queries.get(0).getOutput(), Boolean.TRUE);
        Assert.assertEquals(queries.get(0).getStates(), Arrays.asList(0, 1, 3, 4));

        // ... but Mary does not love Paul :-(
        Assert.assertEquals(queries.get(1).getOutput(), Boolean.FALSE);
        Assert.assertEquals(queries.get(1).getStates(), Arrays.asList(0, 2, 2, 2));
    }
}
