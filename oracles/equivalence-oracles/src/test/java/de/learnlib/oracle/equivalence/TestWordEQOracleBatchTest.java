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
package de.learnlib.oracle.equivalence;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.query.Query;
import net.automatalib.automaton.concept.Output;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test for size of batch generation of {@link AbstractTestWordEQOracle}.
 */
public class TestWordEQOracleBatchTest {

    private static final int BATCH_SIZE = 20;
    private static final int EXPECTED_NUM_OF_QUERIES = 60;
    private static final int MAX_QUERIES = 100;
    private static final int THRESHOLD = 47;

    @Test
    public void testBatchMode() {
        final DummyMQOracle<Character> mOracle = new DummyMQOracle<>();
        final DummyEQOracle<Character> eqOracle = new DummyEQOracle<>(mOracle);
        final DummyHypothesis<Character> hyp = new DummyHypothesis<>();

        eqOracle.findCounterExample(hyp, Collections.singleton('a'));

        Assert.assertEquals(mOracle.getQueryCounter(), EXPECTED_NUM_OF_QUERIES);
        Assert.assertEquals(eqOracle.getGeneratedWordsCounter(), EXPECTED_NUM_OF_QUERIES);
        Assert.assertEquals(hyp.getQueryCounter(), THRESHOLD);
    }

    private static final class DummyHypothesis<I> implements Output<I, Boolean> {

        private int queryCounter;

        int getQueryCounter() {
            return queryCounter;
        }

        @Override
        public @Nullable Boolean computeOutput(Iterable<? extends I> input) {
            queryCounter++;

            return queryCounter >= THRESHOLD ? Boolean.TRUE : null;
        }
    }

    private static final class DummyMQOracle<I> implements DFAMembershipOracle<I> {

        private int queryCounter;

        int getQueryCounter() {
            return queryCounter;
        }

        @Override
        public void processQueries(Collection<? extends Query<I, Boolean>> queries) {
            queryCounter += queries.size();
        }
    }

    private static final class DummyEQOracle<I> extends AbstractTestWordEQOracle<Output<I, Boolean>, I, Boolean> {

        private int generatedWordsCounter;

        DummyEQOracle(MembershipOracle<I, Boolean> membershipOracle) {
            super(membershipOracle, BATCH_SIZE);
        }

        int getGeneratedWordsCounter() {
            return generatedWordsCounter;
        }

        @Override
        protected Stream<Word<I>> generateTestWords(Output<I, Boolean> hypothesis, Collection<? extends I> inputs) {
            final I sym = inputs.iterator().next();
            return Stream.generate(() -> Word.fromLetter(sym)).peek(w-> generatedWordsCounter++).limit(MAX_QUERIES);
        }
    }

}
