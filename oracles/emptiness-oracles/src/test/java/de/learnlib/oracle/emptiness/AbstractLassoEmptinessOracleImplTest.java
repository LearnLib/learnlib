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

import com.google.common.collect.Lists;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.api.query.OmegaQuery;
import de.learnlib.oracle.AbstractBFOracleTest;
import de.learnlib.util.AbstractBFOracle;
import net.automatalib.modelchecking.Lasso;
import net.automatalib.ts.simple.SimpleDTS;
import net.automatalib.words.Word;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the {@link LassoEmptinessOracleImpl} and all its subtypes.
 *
 * @author Jeroen Meijer
 *
 * @param <L> the lasso type
 * @param <D> the output type
 */
public abstract class AbstractLassoEmptinessOracleImplTest<L extends Lasso<Character, D>, D>
        extends AbstractBFOracleTest<D> {

    private LassoEmptinessOracleImpl<L, Integer, Character, D> leo;

    private final Word<Character> prefix = Word.epsilon();

    private final Word<Character> loop = Word.fromSymbols('a');

    private D output;

    private L automaton;

    private DefaultQuery<Character, D> query;

    protected abstract LassoEmptinessOracleImpl<L, Integer, Character, D> createLassoEmptinessOracleImpl();

    protected abstract D createOutput();

    protected abstract L createAutomaton();

    protected abstract DefaultQuery<Character, D> createQuery();

    @BeforeMethod
    public void setUp() {
        super.setUp();
        leo = createLassoEmptinessOracleImpl();
        automaton = createAutomaton();
        query = createQuery();
        output = createOutput();
    }

    @Override
    @Test
    public void testGetMultiplier() {
        Assert.assertEquals(leo.getMultiplier(), -1.0);
    }

    @Test
    public void testProcessOmegaQuery() throws Exception {
        final OmegaQuery<Integer, Character, D> test = new OmegaQuery<>(prefix, loop, 1);

        Mockito.doAnswer(invocation -> {
            final OmegaQuery<Integer, Character, D> q = invocation.getArgument(0);
            if (q.getLoop().equals(Word.fromSymbols('a'))) {
                q.answer(output);
                q.setStates(Lists.newArrayList(1, 1));
            } else {
                q.answer(null);
                q.setStates(Lists.newArrayList(-1, -1));
            }
            return null;
        }).when(leo.getOmegaMembershipOracle()).processQuery(ArgumentMatchers.any());

        leo.processOmegaQuery(test);

        Assert.assertEquals(test.getOutput(), output);
        Assert.assertEquals(test.getStates(), Lists.newArrayList(1, 1));
    }

    @Test
    public void testIsSameState() throws Exception {

        Mockito.when(leo.getOmegaMembershipOracle().isSameState(prefix, 1, prefix.concat(loop), 1)).thenReturn(true);
        Mockito.when(leo.getOmegaMembershipOracle().isSameState(prefix, 1, prefix.concat(loop), 2)).thenReturn(false);

        Assert.assertTrue(leo.isSameState(prefix, 1, prefix.concat(loop), 1));
        Assert.assertFalse(leo.isSameState(prefix, 1, prefix.concat(loop), 2));
    }

    @Override
    protected AbstractBFOracle<? extends SimpleDTS<?, Character>, Character, D> createBreadthFirstOracle(double multiplier) {
        return createLassoEmptinessOracleImpl();
    }

    @Test
    public void testFindCounterExample() {
        final DefaultQuery cex = leo.findCounterExample(automaton, ALPHABET);
        Assert.assertEquals(cex, query);
    }

    @Test
    public void testIsCounterExample() throws Exception {
        leo.isCounterExample(automaton, query.getInput(), query.getOutput());
    }
}