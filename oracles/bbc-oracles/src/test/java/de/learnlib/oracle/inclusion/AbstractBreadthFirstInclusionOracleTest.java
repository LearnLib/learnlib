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
package de.learnlib.oracle.inclusion;

import de.learnlib.api.query.DefaultQuery;
import de.learnlib.oracle.AbstractBreadthFirstOracleTest;
import net.automatalib.automata.concepts.Output;
import net.automatalib.ts.simple.SimpleDTS;
import net.automatalib.words.Alphabet;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Jeroen Meijer
 */
public abstract class AbstractBreadthFirstInclusionOracleTest<A extends SimpleDTS<?, I> & Output<I, D>, I, D> extends
                                                                                                              AbstractBreadthFirstOracleTest<D> {

    private AbstractBreadthFirstInclusionOracle<A, I, D> bfeo;

    private A automaton;

    private Alphabet<I> alphabet;

    private DefaultQuery<I, ?> query;

    protected abstract AbstractBreadthFirstInclusionOracle<A, I, D> createBreadthFirstInclusionOracle();

    protected abstract A createAutomaton();

    protected abstract Alphabet<I> createAlphabet();

    protected abstract DefaultQuery<I, ?> createQuery();

    @BeforeMethod
    public void setUp() {
        super.setUp();
        bfeo = createBreadthFirstInclusionOracle();
        automaton = createAutomaton();
        alphabet = createAlphabet();
        query = createQuery();
    }

    @Test
    public void testFindCounterExample() {
        final DefaultQuery cex = bfeo.findCounterExample(automaton, alphabet);
        Assert.assertEquals(query, cex);
    }

}
