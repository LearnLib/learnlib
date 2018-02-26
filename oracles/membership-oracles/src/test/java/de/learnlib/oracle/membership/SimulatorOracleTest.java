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
import java.util.List;

import de.learnlib.api.query.DefaultQuery;
import de.learnlib.examples.dfa.ExamplePaulAndMary;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Symbol;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Maik Merten
 */
public class SimulatorOracleTest {

    @Test
    public void testDFASimulatorOracle() {

        DFA<?, Symbol> dfa = ExamplePaulAndMary.constructMachine();

        SimulatorOracle<Symbol, Boolean> oracle = new SimulatorOracle<>(dfa);

        List<DefaultQuery<Symbol, Boolean>> queries = new ArrayList<>();

        DefaultQuery<Symbol, Boolean> q1 = new DefaultQuery<>(Word.fromSymbols(ExamplePaulAndMary.IN_PAUL,
                                                                               ExamplePaulAndMary.IN_LOVES,
                                                                               ExamplePaulAndMary.IN_MARY));
        DefaultQuery<Symbol, Boolean> q2 = new DefaultQuery<>(Word.fromSymbols(ExamplePaulAndMary.IN_MARY,
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
