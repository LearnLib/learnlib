/* Copyright (C) 2013-2021 TU Dortmund
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
package de.learnlib.algorithms.spa;

import de.learnlib.algorithms.spa.manager.DefaultATRManager;
import de.learnlib.algorithms.spa.manager.OptimizingATRManager;
import net.automatalib.words.Alphabet;
import net.automatalib.words.SPAAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import net.automatalib.words.impl.DefaultSPAAlphabet;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ATRManagerTest {

    private static final SPAAlphabet<Character> ALPHABET;

    static {
        final Alphabet<Character> callAlphabet = Alphabets.characters('A', 'C');
        final Alphabet<Character> internalAlphabet = Alphabets.characters('a', 'b');
        final char returnSymbol = 'R';

        ALPHABET = new DefaultSPAAlphabet<>(internalAlphabet, callAlphabet, returnSymbol);
    }

    @DataProvider
    public static Object[] atrManager() {
        return new Object[] {new DefaultATRManager<>(ALPHABET), new OptimizingATRManager<>(ALPHABET)};
    }

    @Test(dataProvider = "atrManager")
    public void testScanning(ATRManager<Character> manager) {

        final Word<Character> word = Word.fromCharSequence("ABaRCbRR");

        manager.scanPositiveCounterexample(word);

        Assert.assertEquals(manager.getAccessSequence('A'), Word.fromCharSequence("A"));
        Assert.assertEquals(manager.getAccessSequence('B'), Word.fromCharSequence("AB"));
        Assert.assertEquals(manager.getAccessSequence('C'), Word.fromCharSequence("ABaRC"));

        Assert.assertEquals(manager.getTerminatingSequence('A'), Word.fromCharSequence("BaRCbR"));
        Assert.assertEquals(manager.getTerminatingSequence('B'), Word.fromCharSequence("a"));
        Assert.assertEquals(manager.getTerminatingSequence('C'), Word.fromCharSequence("b"));

        Assert.assertEquals(manager.getReturnSequence('A'), Word.fromCharSequence("R"));
        Assert.assertEquals(manager.getReturnSequence('B'), Word.fromCharSequence("RCbRR"));
        Assert.assertEquals(manager.getReturnSequence('C'), Word.fromCharSequence("RR"));
    }

}
