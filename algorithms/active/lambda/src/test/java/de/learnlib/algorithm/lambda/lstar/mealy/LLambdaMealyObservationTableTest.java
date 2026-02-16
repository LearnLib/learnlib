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
package de.learnlib.algorithm.lambda.lstar.mealy;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collection;

import de.learnlib.algorithm.lambda.AbstractCounterexampleQueueTest;
import de.learnlib.algorithm.lambda.lstar.LLambdaMealy;
import de.learnlib.algorithm.lambda.lstar.dfa.LLambdaDFAObservationTableTest;
import de.learnlib.datastructure.observationtable.Row;
import de.learnlib.datastructure.observationtable.writer.ObservationTableASCIIWriter;
import de.learnlib.oracle.equivalence.MealySimulatorEQOracle;
import de.learnlib.oracle.membership.MealySimulatorOracle;
import de.learnlib.util.Experiment;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.common.util.IOUtil;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LLambdaMealyObservationTableTest {

    @Test
    public void testRender() throws IOException {

        final var dfa = AbstractCounterexampleQueueTest.DFA;
        final var sul = new DFA2MealyWrapper<>(dfa);
        final var alphabet = dfa.getInputAlphabet();
        final var mqo = new MealySimulatorOracle<>(sul);
        final var eqo = new MealySimulatorEQOracle<>(sul);

        final var learner = new LLambdaMealy<>(alphabet, mqo);

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

        try (InputStream is = LLambdaDFAObservationTableTest.class.getResourceAsStream("/ot_mealy.txt");
             Reader r = IOUtil.asBufferedUTF8Reader(is)) {

            final var expected = IOUtil.toString(r);
            Assert.assertEquals(sb.toString(), expected);
        }
    }

    private static final class DFA2MealyWrapper<S, I> implements MealyMachine<S, I, S, Boolean> {

        private final DFA<S, I> dfa;

        private DFA2MealyWrapper(DFA<S, I> dfa) {
            this.dfa = dfa;
        }

        @Override
        public Collection<S> getStates() {
            return dfa.getStates();
        }

        @Override
        public Boolean getTransitionOutput(S transition) {
            return dfa.isAccepting(transition);
        }

        @Override
        public @Nullable S getTransition(S state, I input) {
            return dfa.getSuccessor(state, input);
        }

        @Override
        public S getSuccessor(S transition) {
            return transition;
        }

        @Override
        public @Nullable S getInitialState() {
            return dfa.getInitialState();
        }
    }
}
