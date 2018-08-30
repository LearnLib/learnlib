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
package de.learnlib.oracle.emptiness;

import de.learnlib.api.query.DefaultQuery;
import de.learnlib.oracle.AbstractBFOracleTest;
import de.learnlib.util.AbstractBFOracle;
import net.automatalib.automata.concepts.DetOutputAutomaton;
import net.automatalib.ts.simple.SimpleDTS;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests any breadth-first emptiness oracle.
 *
 * @author Jeroen Meijer
 */
public abstract class AbstractBFEmptinessOracleTest<A extends DetOutputAutomaton<?, Character, ?, D>, D>
        extends AbstractBFOracleTest<D> {

    private AbstractBFEmptinessOracle<A, Character, D> bfeo;

    private A automaton;

    private DefaultQuery<Character, D> query;

    protected abstract AbstractBFEmptinessOracle<A, Character, D> createBreadthFirstEmptinessOracle();

    protected abstract A createAutomaton();

    protected abstract DefaultQuery<Character, D> createQuery();

    @BeforeMethod
    public void setUp() {
        super.setUp();
        bfeo = createBreadthFirstEmptinessOracle();
        automaton = createAutomaton();
        query = createQuery();
    }

    @Override
    protected AbstractBFOracle<? extends SimpleDTS<?, Character>, Character, D> createBreadthFirstOracle(double multiplier) {
        return createBreadthFirstEmptinessOracle();
    }

    @Test
    public void testFindCounterExample() {
        final DefaultQuery cex = bfeo.findCounterExample(automaton, ALPHABET);
        Assert.assertEquals(cex, query);
    }

    @Test
    public void testIsCounterExample() throws Exception {
        bfeo.isCounterExample(automaton, query.getInput(), query.getOutput());
    }
}