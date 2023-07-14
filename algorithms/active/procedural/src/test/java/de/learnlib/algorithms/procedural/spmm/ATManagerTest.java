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
package de.learnlib.algorithms.procedural.spmm;

import de.learnlib.algorithms.procedural.spmm.manager.DefaultATManager;
import de.learnlib.algorithms.procedural.spmm.manager.OptimizingATManager;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.words.Alphabet;
import net.automatalib.words.SPAAlphabet;
import net.automatalib.words.SPAOutputAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import net.automatalib.words.impl.DefaultSPAAlphabet;
import net.automatalib.words.impl.DefaultSPAOutputAlphabet;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ATManagerTest {

    private static final SPAAlphabet<Character> INPUT_ALPHABET;
    private static final SPAOutputAlphabet<Character> OUTPUT_ALPHABET;

    static {
        final Alphabet<Character> callAlphabet = Alphabets.characters('A', 'C');
        final Alphabet<Character> internalAlphabet = Alphabets.characters('a', 'b');
        final char returnSymbol = 'R';

        INPUT_ALPHABET = new DefaultSPAAlphabet<>(internalAlphabet, callAlphabet, returnSymbol);
        OUTPUT_ALPHABET = new DefaultSPAOutputAlphabet<>(Alphabets.characters('x', 'z'), '✗');
    }

    @DataProvider
    public static Object[] atManager() {
        return new Object[] {new DefaultATManager<>(INPUT_ALPHABET, OUTPUT_ALPHABET), new OptimizingATManager<>(INPUT_ALPHABET, OUTPUT_ALPHABET)};
    }

    @Test(dataProvider = "atManager")
    public void testScanning(ATManager<Character, Character> manager) {

        final Word<Character> inputWord = Word.fromCharSequence("ABaRCbRR");
        final Word<Character> outputWord = Word.fromCharSequence("✓✓x✓✓y✓✓");

        manager.scanCounterexample(new DefaultQuery<>(Word.epsilon(), inputWord, outputWord));

        Assert.assertEquals(manager.getAccessSequence('A'), Word.fromCharSequence("A"));
        Assert.assertEquals(manager.getAccessSequence('B'), Word.fromCharSequence("AB"));
        Assert.assertEquals(manager.getAccessSequence('C'), Word.fromCharSequence("ABaRC"));

        Assert.assertEquals(manager.getTerminatingSequence('A'), Word.fromCharSequence("BaRCbR"));
        Assert.assertEquals(manager.getTerminatingSequence('B'), Word.fromCharSequence("a"));
        Assert.assertEquals(manager.getTerminatingSequence('C'), Word.fromCharSequence("b"));
    }

}
