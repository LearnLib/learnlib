/* Copyright (C) 2013-2023 TU Dortmund
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
package de.learnlib.algorithms.procedural.sba;

import de.learnlib.algorithms.procedural.sba.manager.DefaultATManager;
import de.learnlib.algorithms.procedural.sba.manager.OptimizingATManager;
import net.automatalib.words.Alphabet;
import net.automatalib.words.ProceduralInputAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import net.automatalib.words.impl.DefaultProceduralInputAlphabet;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ATManagerTest {

    private static final ProceduralInputAlphabet<Character> ALPHABET;

    static {
        final Alphabet<Character> callAlphabet = Alphabets.characters('A', 'C');
        final Alphabet<Character> internalAlphabet = Alphabets.characters('a', 'b');
        final char returnSymbol = 'R';

        ALPHABET = new DefaultProceduralInputAlphabet<>(internalAlphabet, callAlphabet, returnSymbol);
    }

    @DataProvider
    public static Object[] atManager() {
        return new Object[] {new DefaultATManager<>(ALPHABET), new OptimizingATManager<>(ALPHABET)};
    }

    @Test(dataProvider = "atManager")
    public void testScanning(ATManager<Character> manager) {

        final Word<Character> word = Word.fromCharSequence("ABaRCbRR");

        manager.scanPositiveCounterexample(word);

        Assert.assertEquals(manager.getAccessSequence('A'), Word.fromCharSequence("A"));
        Assert.assertEquals(manager.getAccessSequence('B'), Word.fromCharSequence("AB"));
        Assert.assertEquals(manager.getAccessSequence('C'), Word.fromCharSequence("ABaRC"));

        Assert.assertEquals(manager.getTerminatingSequence('A'), Word.fromCharSequence("BaRCbR"));
        Assert.assertEquals(manager.getTerminatingSequence('B'), Word.fromCharSequence("a"));
        Assert.assertEquals(manager.getTerminatingSequence('C'), Word.fromCharSequence("b"));
    }

}
