package de.learnlib.algorithm.lsharp;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.Test;

import net.automatalib.automaton.transducer.CompactMealy;
import net.automatalib.common.util.Pair;
import net.automatalib.serialization.InputModelDeserializer;
import net.automatalib.serialization.dot.DOTParsers;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;

public class NormalObservationTreeTest {

    private LSMealyMachine<String, String> readMealy(String filename) throws IOException {
        InputModelDeserializer<String, CompactMealy<String, String>> parser = DOTParsers
                .mealy(new CompactMealy.Creator<String, String>(), DOTParsers.DEFAULT_MEALY_EDGE_PARSER);
        InputStream res = this.getClass().getResourceAsStream(filename);
        CompactMealy<String, String> target = parser.readModel(res).model;
        return new LSMealyMachine<>(target.getInputAlphabet(), target);
    }

    private List<Pair<Word<String>, Word<String>>> tryGenInputs(LSMealyMachine<String, String> mealy, Integer count) {
        Random random = new Random(42);
        List<Pair<Word<String>, Word<String>>> pairs = new LinkedList<>();

        for (int pair = 0; pair < count; pair++) {
            final int length = random.nextInt(101);
            final WordBuilder<String> result = new WordBuilder<>(length);

            for (int j = 0; j < length; ++j) {
                int symidx = random.nextInt(mealy.getInputAlphabet().size());
                String sym = mealy.getInputAlphabet().getSymbol(symidx);
                result.append(sym);
            }

            Word<String> word = result.toWord();

            pairs.add(Pair.of(word, mealy.computeOutput(word)));
        }

        return pairs;
    }

    @Test
    public void xferSeqMantained() throws IOException {
        LSMealyMachine<String, String> fsm = readMealy("/BitVise.dot");
        List<Pair<Word<String>, Word<String>>> tests = tryGenInputs(fsm, 20);
        NormalObservationTree<String, String> ret = new NormalObservationTree<>(fsm.getInputAlphabet());

        for (int testIndex = 0; testIndex < tests.size(); testIndex++) {
            Word<String> is = tests.get(testIndex).getFirst();
            Word<String> os = tests.get(testIndex).getSecond();

            ret.insertObservation(null, is, os);
            Assert.assertNotNull(ret.getSucc(ret.defaultState(), is));
            for (int inIndex = 0; inIndex < testIndex; inIndex++) {
                Word<String> iis = tests.get(inIndex).getFirst();
                LSState ds = ret.getSucc(ret.defaultState(), iis);
                Assert.assertNotNull(ds);
                Word<String> rxAcc = ret.getAccessSeq(ds);
                Assert.assertTrue(iis.equals(rxAcc), "Failed at testIndex " + testIndex + "and inINdex " + inIndex
                        + ", \n after inserting" + is.toString());
                for (int i = 0; i < iis.length(); i++) {
                    Word<String> pref = iis.prefix(i);
                    Word<String> suff = iis.suffix(iis.length() - i);

                    LSState prefDest = ret.getSucc(ret.defaultState(), pref);
                    Assert.assertNotNull(prefDest);
                    Word<String> xferSeq = ret.getTransferSeq(ds, prefDest);
                    Assert.assertTrue(suff.equals(xferSeq));
                }
            }
        }
    }

    @Test
    public void accessSeqMantained() throws IOException {
        LSMealyMachine<String, String> fsm = readMealy("/BitVise.dot");
        List<Pair<Word<String>, Word<String>>> tests = tryGenInputs(fsm, 100);
        NormalObservationTree<String, String> ret = new NormalObservationTree<>(fsm.getInputAlphabet());

        for (int testIndex = 0; testIndex < tests.size(); testIndex++) {
            Word<String> is = tests.get(testIndex).getFirst();
            Word<String> os = tests.get(testIndex).getSecond();

            ret.insertObservation(null, is, os);
            Assert.assertNotNull(ret.getSucc(ret.defaultState(), is));
            for (int inIndex = 0; inIndex < testIndex; inIndex++) {
                Word<String> iis = tests.get(inIndex).getFirst();
                LSState ds = ret.getSucc(ret.defaultState(), iis);
                Assert.assertNotNull(ds);
                Word<String> rxAcc = ret.getAccessSeq(ds);
                Assert.assertTrue(iis.equals(rxAcc), "Failed at testIndex " + testIndex + "and inINdex " + inIndex
                        + ", \n after inserting" + is.toString());
            }
        }

        for (int testIndex = 0; testIndex < tests.size(); testIndex++) {
            Word<String> is = tests.get(testIndex).getFirst();
            LSState dest = ret.getSucc(ret.defaultState(), is);
            Assert.assertNotNull(dest, "Seq number " + testIndex + " : " + is + " is not in tree?!");
            Word<String> accSeq = ret.getAccessSeq(dest);
            Assert.assertTrue(is.equals(accSeq));
        }

    }

}
