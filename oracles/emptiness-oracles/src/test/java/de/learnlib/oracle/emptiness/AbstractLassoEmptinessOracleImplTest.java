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
import de.learnlib.api.query.OmegaQuery;
import net.automatalib.modelchecking.Lasso;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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
public abstract class AbstractLassoEmptinessOracleImplTest<L extends Lasso<Character, D>, D> {

    public static final Alphabet<Character> ALPHABET = Alphabets.singleton('a');

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
        MockitoAnnotations.initMocks(this);
        leo = createLassoEmptinessOracleImpl();
        automaton = createAutomaton();
        query = createQuery();
        output = createOutput();
    }

    @Test
    public void testProcessInput() throws Exception {
        Mockito.doAnswer(invocation -> {
            final OmegaQuery<Character, D> q = invocation.getArgument(0);
            if (q.getLoop().equals(Word.fromSymbols('a'))) {
                q.answer(output, 1);
            } else {
                q.answer(null, -1);
            }
            return null;
        }).when(leo.getOmegaMembershipOracle()).processQuery(ArgumentMatchers.any());

        final OmegaQuery<Character, D> test = leo.processInput(prefix, loop, 1);

        Assert.assertEquals(test.getPrefix(), Word.epsilon());
        Assert.assertEquals(test.getLoop(), Word.fromSymbols('a'));
        Assert.assertEquals(test.getRepeat(), 1);
        Assert.assertEquals(test.getOutput(), output);
        Assert.assertEquals(test.getPeriodicity(), 1);
    }

    @Test
    public void testFindCounterExample() {
        final DefaultQuery<Character, D> cex = leo.findCounterExample(automaton, ALPHABET);
        Assert.assertEquals(cex, query);
    }

    @Test
    public void testIsCounterExample() throws Exception {
        leo.isCounterExample(automaton, query.getInput(), query.getOutput());
    }
}