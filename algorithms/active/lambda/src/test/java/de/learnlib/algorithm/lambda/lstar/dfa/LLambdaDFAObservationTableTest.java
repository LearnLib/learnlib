/* Copyright (C) 2013-2026 TU Dortmund University
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
package de.learnlib.algorithm.lambda.lstar.dfa;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import de.learnlib.algorithm.lambda.AbstractCounterexampleQueueTest;
import de.learnlib.algorithm.lambda.lstar.LLambdaDFA;
import de.learnlib.datastructure.observationtable.Row;
import de.learnlib.datastructure.observationtable.writer.ObservationTableASCIIWriter;
import de.learnlib.oracle.equivalence.DFASimulatorEQOracle;
import de.learnlib.oracle.membership.DFASimulatorOracle;
import de.learnlib.util.Experiment;
import net.automatalib.common.util.IOUtil;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LLambdaDFAObservationTableTest {

    @Test
    public void testOT() throws IOException {
        final var sul = AbstractCounterexampleQueueTest.DFA;
        final var alphabet = sul.getInputAlphabet();
        final var mqo = new DFASimulatorOracle<>(sul);
        final var eqo = new DFASimulatorEQOracle<>(sul);

        final var learner = new LLambdaDFA<>(alphabet, mqo);

        final var exp = new Experiment<>(learner, eqo, alphabet);

        exp.run();

        final var ot = learner.getObservationTable();

        final int spRows = ot.numberOfShortPrefixRows();
        final int lpRows = ot.numberOfLongPrefixRows();
        final int rows = ot.numberOfDistinctRows();

        // test sizes
        Assert.assertEquals(rows, exp.getFinalHypothesis().size() * alphabet.size() + 1); // all transitions + ε
        Assert.assertEquals(spRows, exp.getFinalHypothesis().size());
        Assert.assertEquals(lpRows, rows - spRows);

        // test alphabet
        Assert.assertEquals(ot.getInputAlphabet(), alphabet);

        // test rows
        for (int i = 0; i < spRows; i++) {
            Row<Character> row = ot.getRow(i);
            Word<Character> label = row.getLabel();
            Assert.assertTrue(row.isShortPrefixRow());
            Assert.assertEquals(ot.transformAccessSequence(label), label);
            for (int j = 0; j < alphabet.size(); j++) {
                Assert.assertNotNull(row.getSuccessor(j));
            }
        }
        final var sps = ot.getShortPrefixes();
        for (int i = spRows; i < rows; i++) {
            Row<Character> row = ot.getRow(i);
            Word<Character> label = row.getLabel();
            Assert.assertFalse(row.isShortPrefixRow());
            Assert.assertTrue(sps.contains(ot.transformAccessSequence(label)));
            for (int j = 0; j < alphabet.size(); j++) {
                Assert.assertNull(row.getSuccessor(j));
            }
        }

        // test rendering
        final var sb = new StringBuilder();
        new ObservationTableASCIIWriter<>().write(ot, sb);

        try (InputStream is = LLambdaDFAObservationTableTest.class.getResourceAsStream("/ot_dfa.txt");
             Reader r = IOUtil.asBufferedUTF8Reader(is)) {

            final var expected = IOUtil.toString(r);
            Assert.assertEquals(sb.toString(), expected);
        }
    }
}
