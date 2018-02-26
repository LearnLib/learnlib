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
package de.learnlib.api.modelchecking.counterexample;

import java.util.SortedSet;
import java.util.TreeSet;

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests whether lassos are constructed correctly for any automaton.
 *
 * @param <L> the {@link Lasso} type to test.
 *
 * @author Jeroen Meijer
 */
public abstract class AbstractLassoTest<L extends Lasso<?, ?, String, ?>> {

    private Alphabet<String> alphabet = Alphabets.fromArray("a");

    private L lasso1;
    private L lasso2;
    private L lasso3;

    protected abstract L getLasso(Word<String> prefix, Word<String> loop, int unfoldTimes);

    public Alphabet<String> getAlphabet() {
        return alphabet;
    }

    @BeforeClass
    public void setUp() {
        lasso1 = getLasso(Word.epsilon(), Word.fromSymbols("a"), 1);
        lasso2 = getLasso(Word.fromSymbols("a"), Word.fromSymbols("a"), 1);
        lasso3 = getLasso(Word.fromSymbols("a"), Word.fromSymbols("a", "a"), 1);
    }

    @Test
    public void testGetWord() {
        Assert.assertEquals(lasso1.getWord(), Word.fromSymbols("a"));
        Assert.assertEquals(lasso2.getWord(), Word.fromSymbols("a", "a"));
        Assert.assertEquals(lasso3.getWord(), Word.fromSymbols("a", "a", "a"));
    }

    @Test
    public void testGetLoop() {
        Assert.assertEquals(lasso1.getLoop(), Word.fromSymbols("a"));
        Assert.assertEquals(lasso2.getLoop(), Word.fromSymbols("a"));
        Assert.assertEquals(lasso3.getLoop(), Word.fromSymbols("a", "a"));
    }

    @Test
    public void testGetPrefix() {
        Assert.assertEquals(lasso1.getPrefix(), Word.epsilon());
        Assert.assertEquals(lasso2.getPrefix(), Word.fromSymbols("a"));
        Assert.assertEquals(lasso3.getPrefix(), Word.fromSymbols("a"));
    }

    @Test
    public void testGetLoopBeginIndices() {
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

}
