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
package de.learnlib.algorithm.procedural.spmm;

import de.learnlib.algorithm.procedural.spmm.manager.DefaultATManager;
import de.learnlib.algorithm.procedural.spmm.manager.OptimizingATManager;
import de.learnlib.query.DefaultQuery;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.alphabet.impl.DefaultProceduralInputAlphabet;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ATManagerTest {

    private static final ProceduralInputAlphabet<Character> ALPHABET;
    private static final Character ERROR_OUTPUT;

    static {
        final Alphabet<Character> callAlphabet = Alphabets.characters('A', 'C');
        final Alphabet<Character> internalAlphabet = Alphabets.characters('a', 'b');
        final char returnSymbol = 'R';

        ALPHABET = new DefaultProceduralInputAlphabet<>(internalAlphabet, callAlphabet, returnSymbol);
        ERROR_OUTPUT = '✗';
    }

    @DataProvider
    public static Object[] atManager() {
        return new Object[] {new DefaultATManager<>(ALPHABET, ERROR_OUTPUT),
                             new OptimizingATManager<>(ALPHABET, ERROR_OUTPUT)};
    }

    @Test(dataProvider = "atManager")
    public void testScanning(ATManager<Character, Character> manager) {

        final Word<Character> inputWord = Word.fromString("ABaRCbRR");
        final Word<Character> outputWord = Word.fromString("✓✓x✓✓y✓✓");

        manager.scanCounterexample(new DefaultQuery<>(Word.epsilon(), inputWord, outputWord));

        Assert.assertEquals(manager.getAccessSequence('A'), Word.fromLetter('A'));
        Assert.assertEquals(manager.getAccessSequence('B'), Word.fromString("AB"));
        Assert.assertEquals(manager.getAccessSequence('C'), Word.fromString("ABaRC"));

        Assert.assertEquals(manager.getTerminatingSequence('A'), Word.fromString("BaRCbR"));
        Assert.assertEquals(manager.getTerminatingSequence('B'), Word.fromLetter('a'));
        Assert.assertEquals(manager.getTerminatingSequence('C'), Word.fromLetter('b'));
    }

}
