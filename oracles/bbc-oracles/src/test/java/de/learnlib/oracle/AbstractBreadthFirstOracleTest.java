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
package de.learnlib.oracle;

import java.util.NoSuchElementException;

import de.learnlib.api.query.Query;
import net.automatalib.ts.simple.SimpleDTS;
import net.automatalib.words.Word;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Class to test any {@link AbstractBreadthFirstOracle}.
 *
 * @author Jeroen Meijer
 */
public abstract class AbstractBreadthFirstOracleTest<D> {

    public static final int MAX_WORDS = 1;

    private AbstractBreadthFirstOracle<? extends SimpleDTS<?, Character>, Character, D, ? extends Query<Character, D>>
            bfo;

    protected abstract AbstractBreadthFirstOracle<? extends SimpleDTS<?, Character>, Character, D, ? extends Query<Character, D>> createBreadthFirstOracle(
            int maxWords);

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        bfo = createBreadthFirstOracle(MAX_WORDS);
    }

    @Test
    public void testGetMaxWords() {
        Assert.assertEquals(bfo.getMaxWords(), MAX_WORDS);
    }

    /**
     * Tests breadth-first order.
     */
    @Test
    public void testNextInput() {
        bfo.pre();
        bfo.addWord(Word.fromLetter('a'));
        bfo.addWord(Word.fromLetter('b'));
        Assert.assertEquals(bfo.nextInput(), Word.fromLetter('a'));
        Assert.assertEquals(bfo.nextInput(), Word.fromLetter('b'));
    }

    @Test
    public void testAddWord() {
        bfo.pre();
        bfo.addWord(Word.epsilon());
        Assert.assertEquals(bfo.nextInput(), Word.epsilon());
    }

    @Test(expectedExceptions = NoSuchElementException.class)
    public void testPre() {
        bfo.pre();
        bfo.addWord(Word.epsilon());
        bfo.pre();
        bfo.nextInput();
    }
}