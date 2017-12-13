/* Copyright (C) 2013-2017 TU Dortmund
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
package de.learnlib.api.modelchecking.counterexample;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.learnlib.api.modelchecking.counterexample.Lasso.DFALasso;
import de.learnlib.api.modelchecking.counterexample.Lasso.MealyLasso;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealyTransition;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests whether lassos are constructed correctly for any automaton.
 *
 * @param <L> the {@link Lasso} type to test.
 *
 * @author Jeroen Meijer
 */
@Ignore
public abstract class LassoTest<L extends Lasso<?, ?, String, ?>> {

    private Alphabet<String> alphabet = Alphabets.fromArray("a");

    private L lasso1;
    private L lasso2;
    private L lasso3;

    protected abstract L getLasso(Word<String> prefix, Word<String> loop, int unfoldTimes);

    public Alphabet<String> getAlphabet() {
        return alphabet;
    }

    @Before
    public void setUp() {
        lasso1 = getLasso(Word.epsilon(), Word.fromSymbols("a"), 1);
        lasso2 = getLasso(Word.fromSymbols("a"), Word.fromSymbols("a"), 1);
        lasso3 = getLasso(Word.fromSymbols("a"), Word.fromSymbols("a", "a"), 1);
    }

    @Test
    public void testGetWord() throws Exception {
        Assert.assertEquals(lasso1.getWord(), Word.fromSymbols("a"));
        Assert.assertEquals(lasso2.getWord(), Word.fromSymbols("a", "a"));
        Assert.assertEquals(lasso3.getWord(), Word.fromSymbols("a", "a", "a"));
    }

    @Test
    public void testGetLoop() throws Exception {
        Assert.assertEquals(lasso1.getLoop(), Word.fromSymbols("a"));
        Assert.assertEquals(lasso2.getLoop(), Word.fromSymbols("a"));
        Assert.assertEquals(lasso3.getLoop(), Word.fromSymbols("a", "a"));
    }

    @Test
    public void testGetPrefix() throws Exception {
        Assert.assertEquals(lasso1.getPrefix(), Word.epsilon());
        Assert.assertEquals(lasso2.getPrefix(), Word.fromSymbols("a"));
        Assert.assertEquals(lasso3.getPrefix(), Word.fromSymbols("a"));
    }

    @Test
    public void testGetLoopBeginIndices() throws Exception {
        final SortedSet<Integer> indices = new TreeSet<>();
        indices.add(0);
        indices.add(1);
        Assert.assertEquals(lasso1.getLoopBeginIndices(), indices);

        indices.clear();
        indices.add(1);
        indices.add(2);
        Assert.assertEquals(lasso2.getLoopBeginIndices(), indices);

        indices.clear();
        indices.add(1);
        indices.add(3);
        Assert.assertEquals(lasso3.getLoopBeginIndices(), indices);
    }

    public static class DFALassoTest extends LassoTest<DFALasso<?, String>> {

        @Override
        protected DFALasso<?, String> getLasso(Word<String> prefix, Word<String> loop, int unfoldTimes) {
            return new DFALasso<>(new DFAMock(prefix, loop), getAlphabet(), unfoldTimes);
        }

        @Test
        public void testGetOutput() throws Exception {
            final DFALasso<?, String> lasso = getLasso(Word.epsilon(), Word.fromSymbols("a"), 1);
            Assert.assertTrue(lasso.getOutput());
        }

        class DFAMock implements DFA<Integer, String> {

            private Word<String> prefix;
            private Word<String> word;

            DFAMock(Word<String> prefix, Word<String> loop) {
                this.prefix = prefix;
                word = prefix.concat(loop);
            }

            @Nonnull
            @Override
            public Collection<Integer> getStates() {
                final Collection<Integer> states =
                        IntStream.range(0, word.length()).boxed().collect(Collectors.toSet());
                return states;
            }

            @Nullable
            @Override
            public Integer getTransition(Integer state, @Nullable String input) {
                final Integer result;

                if (word.getSymbol(state).equals(input)) {
                    if (state < word.length() - 1) {
                        result = state + 1;
                    } else {
                        result = prefix.length();
                    }
                } else {
                    result = null;
                }

                return result;
            }

            @Override
            public boolean isAccepting(Integer state) {
                // dfa is prefix-closed; always return true
                return true;
            }

            @Nullable
            @Override
            public Integer getInitialState() {
                return 0;
            }
        }
    }

    public static class MealyLassoTest extends LassoTest<MealyLasso<?, String, String>> {

        @Override
        protected MealyLasso<?, String, String> getLasso(Word<String> prefix, Word<String> loop, int unfoldTimes) {
            return new MealyLasso<>(new MealyMachineMock(prefix, loop), getAlphabet(), 1);
        }

        @Test
        public void testGetOutput() throws Exception {
            final MealyLasso<?, String, String> lasso = getLasso(Word.epsilon(), Word.fromSymbols("a"), 1);
            Assert.assertEquals(lasso.getOutput(), Word.fromSymbols(MealyMachineMock.OUTPUT));
        }

        class MealyMachineMock implements MealyMachine<Integer, String, CompactMealyTransition<String>, String> {

            public static final String OUTPUT = "test";

            private final Word<String> prefix;
            private final Word<String> word;

            MealyMachineMock(Word<String> prefix, Word<String> loop) {
                this.prefix = prefix;
                word = prefix.concat(loop);
            }

            @Nullable
            @Override
            public String getTransitionOutput(CompactMealyTransition<String> transition) {
                return OUTPUT;
            }

            @Nonnull
            @Override
            public Collection<Integer> getStates() {
                final Collection<Integer> states =
                        IntStream.range(0, word.length()).boxed().collect(Collectors.toSet());
                return states;
            }

            @Nullable
            @Override
            public CompactMealyTransition<String> getTransition(Integer state, @Nullable String input) {
                final CompactMealyTransition<String> result;
                if (word.getSymbol(state).equals(input)) {
                    if (state < word.length() - 1) {
                        result = new CompactMealyTransition<>(state + 1, OUTPUT);
                    } else {
                        result = new CompactMealyTransition<>(prefix.length(), OUTPUT);
                    }
                } else {
                    result = null;
                }

                return result;
            }

            @Nonnull
            @Override
            public Integer getSuccessor(CompactMealyTransition<String> transition) {
                return transition.getSuccId();
            }

            @Nullable
            @Override
            public Integer getInitialState() {
                return 0;
            }
        }
    }
}
