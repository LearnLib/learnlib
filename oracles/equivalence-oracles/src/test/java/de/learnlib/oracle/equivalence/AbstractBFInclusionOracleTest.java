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
package de.learnlib.oracle.equivalence;

import de.learnlib.query.DefaultQuery;
import de.learnlib.testsupport.AbstractBFOracleTest;
import de.learnlib.util.AbstractBFOracle;
import net.automatalib.automaton.concept.DetOutputAutomaton;
import net.automatalib.ts.simple.SimpleDTS;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests any breadth-first inclusion oracle.
 *
 * @param <D> the output type
 */
public abstract class AbstractBFInclusionOracleTest<A extends DetOutputAutomaton<?, Character, ?, D>, D>
        extends AbstractBFOracleTest<D> {

    private DefaultQuery<Character, D> query;

    private AbstractBFInclusionOracle<A, Character, D> bfio;

    private A automaton;

    protected abstract DefaultQuery<Character, D> createQuery();

    protected abstract AbstractBFInclusionOracle<A, Character, D> createBreadthFirstInclusionOracle();

    protected abstract A createAutomaton();

    @Override
    protected AbstractBFOracle<? extends SimpleDTS<?, Character>, Character, D> createBreadthFirstOracle(double multiplier) {
        return createBreadthFirstInclusionOracle();
    }

    @BeforeMethod
    public void setUp() {
        super.setUp();

        query = createQuery();
        bfio = createBreadthFirstInclusionOracle();
        automaton = createAutomaton();
    }

    @Test
    public void testIsCounterExample() {
        Assert.assertTrue(bfio.isCounterExample(automaton, query.getInput(), query.getOutput()));
    }

    @Test
    public void testFindCounterExample() {
        final DefaultQuery<Character, D> cex = bfio.findCounterExample(automaton, ALPHABET);
        Assert.assertEquals(cex, query);
    }
}
