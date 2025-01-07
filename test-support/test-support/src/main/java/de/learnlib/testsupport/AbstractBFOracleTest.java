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
package de.learnlib.testsupport;

import de.learnlib.util.AbstractBFOracle;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.ts.simple.SimpleDTS;
import net.automatalib.word.Word;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Class to test any {@link AbstractBFOracle}.
 */
public abstract class AbstractBFOracleTest<D> {

    public static final Alphabet<Character> ALPHABET = Alphabets.singleton('a');
    public static final double MULTIPLIER = 2.0;

    private AutoCloseable mock;

    private AbstractBFOracle<? extends SimpleDTS<?, Character>, Character, D> bfo;

    protected abstract AbstractBFOracle<? extends SimpleDTS<?, Character>, Character, D> createBreadthFirstOracle(double multiplier);

    @BeforeMethod
    public void setUp() {
        mock = MockitoAnnotations.openMocks(this);
        bfo = createBreadthFirstOracle(MULTIPLIER);
    }

    @AfterMethod
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void tearDown() throws Exception {
        this.mock.close();
    }

    @Test
    public void testGetMultiplier() {
        Assert.assertEquals(bfo.getMultiplier(), MULTIPLIER);
    }

    /**
     * Tests breadth-first order.
     */
    @Test
    public void testNextInput() {
        bfo.pre();
        bfo.addWord(Word.fromLetter('a'));
        bfo.addWord(Word.fromLetter('b'));
        Assert.assertEquals(bfo.nextInput(), Word.epsilon());
        Assert.assertEquals(bfo.nextInput(), Word.fromLetter('a'));
        Assert.assertEquals(bfo.nextInput(), Word.fromLetter('b'));
    }

    @Test
    public void testAddWord() {
        bfo.pre();
        bfo.addWord(Word.epsilon());
        Assert.assertEquals(bfo.nextInput(), Word.epsilon());
    }

    @Test
    public void testPre() {
        bfo.pre();
        bfo.addWord(Word.epsilon());
        bfo.pre();
        Assert.assertEquals(bfo.nextInput(), Word.epsilon());
    }
}
