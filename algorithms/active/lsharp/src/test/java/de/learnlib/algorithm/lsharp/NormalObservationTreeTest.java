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
package de.learnlib.algorithm.lsharp;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.common.util.Pair;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.InputModelData;
import net.automatalib.serialization.InputModelDeserializer;
import net.automatalib.serialization.dot.DOTParsers;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

public class NormalObservationTreeTest {

    private static final int RAND = 42;
    private static final int LOW_INPUTS = 20;
    private static final int HIGH_INPUTS = 100;

    private InputModelData<String, CompactMealy<String, String>> readMealy(String filename)
            throws IOException, FormatException {
        final InputModelDeserializer<String, CompactMealy<String, String>> parser = DOTParsers.mealy();

        try (InputStream res = NormalObservationTreeTest.class.getResourceAsStream(filename)) {
            return parser.readModel(res);
        }
    }

    private List<Pair<Word<String>, Word<String>>> tryGenInputs(MealyMachine<?, String, ?, String> mealy,
                                                                Alphabet<String> alphabet,
                                                                int count) {
        Random random = new Random(RAND);
        List<Pair<Word<String>, Word<String>>> pairs = new ArrayList<>();

        for (int pair = 0; pair < count; pair++) {
            final int length = random.nextInt(101);
            final WordBuilder<String> result = new WordBuilder<>(length);

            for (int j = 0; j < length; ++j) {
                int symidx = random.nextInt(alphabet.size());
                String sym = alphabet.getSymbol(symidx);
                result.append(sym);
            }

            Word<String> word = result.toWord();

            pairs.add(Pair.of(word, mealy.computeOutput(word)));
        }

        return pairs;
    }

    @Test
    public void xferSeqMantained() throws IOException, FormatException {
        InputModelData<String, CompactMealy<String, String>> model = readMealy("/BitVise.dot");
        CompactMealy<String, String> fsm = model.model;
        Alphabet<String> alphabet = model.alphabet;
        List<Pair<Word<String>, Word<String>>> tests = tryGenInputs(fsm, alphabet, LOW_INPUTS);
        NormalObservationTree<String, String> ret = new NormalObservationTree<>(fsm.getInputAlphabet());

        for (int testIndex = 0; testIndex < tests.size(); testIndex++) {
            Word<String> is = tests.get(testIndex).getFirst();
            Word<String> os = tests.get(testIndex).getSecond();

            ret.insertObservation(null, is, os);
            Assert.assertNotNull(ret.getSucc(ret.defaultState(), is));
            for (int inIndex = 0; inIndex < testIndex; inIndex++) {
                Word<String> iis = tests.get(inIndex).getFirst();
                Integer ds = ret.getSucc(ret.defaultState(), iis);
                Assert.assertNotNull(ds);
                Word<String> rxAcc = ret.getAccessSeq(ds);
                Assert.assertEquals(rxAcc,
                                    iis,
                                    "Failed at testIndex " + testIndex + "and inINdex " + inIndex +
                                    ", after inserting" + is);
                for (int i = 0; i < iis.length(); i++) {
                    Word<String> pref = iis.prefix(i);
                    Word<String> suff = iis.suffix(iis.length() - i);

                    Integer prefDest = ret.getSucc(ret.defaultState(), pref);
                    Assert.assertNotNull(prefDest);
                    Word<String> xferSeq = ret.getTransferSeq(ds, prefDest);
                    Assert.assertEquals(xferSeq, suff);
                }
            }
        }
    }

    @Test
    public void accessSeqMantained() throws IOException, FormatException {
        InputModelData<String, CompactMealy<String, String>> model = readMealy("/BitVise.dot");
        CompactMealy<String, String> fsm = model.model;
        Alphabet<String> alphabet = model.alphabet;
        List<Pair<Word<String>, Word<String>>> tests = tryGenInputs(fsm, alphabet, HIGH_INPUTS);
        NormalObservationTree<String, String> ret = new NormalObservationTree<>(alphabet);

        for (int testIndex = 0; testIndex < tests.size(); testIndex++) {
            Word<String> is = tests.get(testIndex).getFirst();
            Word<String> os = tests.get(testIndex).getSecond();

            ret.insertObservation(null, is, os);
            Assert.assertNotNull(ret.getSucc(ret.defaultState(), is));
            for (int inIndex = 0; inIndex < testIndex; inIndex++) {
                Word<String> iis = tests.get(inIndex).getFirst();
                Integer ds = ret.getSucc(ret.defaultState(), iis);
                Assert.assertNotNull(ds);
                Word<String> rxAcc = ret.getAccessSeq(ds);
                Assert.assertEquals(rxAcc,
                                    iis,
                                    "Failed at testIndex " + testIndex + "and inINdex " + inIndex +
                                    ", after inserting" + is);
            }
        }

        for (int testIndex = 0; testIndex < tests.size(); testIndex++) {
            Word<String> is = tests.get(testIndex).getFirst();
            Integer dest = ret.getSucc(ret.defaultState(), is);
            Assert.assertNotNull(dest, "Seq number " + testIndex + " : " + is + " is not in tree?!");
            Word<String> accSeq = ret.getAccessSeq(dest);
            Assert.assertEquals(accSeq, is);
        }

    }

}
