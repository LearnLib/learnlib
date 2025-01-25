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
package de.learnlib.filter.cache;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import de.learnlib.oracle.SingleQueryOracle.SingleQueryOracleMealy;
import de.learnlib.query.DefaultQuery;
import net.automatalib.common.util.Pair;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

public class InterningMembershipOracleTest {

    @Test
    public void testInterning() {
        final DefaultQuery<Character, Word<Character>> q1 = new DefaultQuery<>(Word.epsilon(), Word.fromString("qwe"));
        final DefaultQuery<Character, Word<Character>> q2 = new DefaultQuery<>(Word.fromString("asd"), Word.epsilon());

        final Oracle<Character> oracle = new Oracle<>();
        final InterningMembershipOracle<Character, Word<Character>> interning = new InterningMembershipOracle<>(oracle);

        interning.processQueries(Arrays.asList(q1, q2));

        final Word<Character> o1 = q1.getOutput();

        Assert.assertSame(q1.getOutput(), o1);
        Assert.assertSame(q2.getOutput(), o1);

        // repeated queries
        interning.processQueries(Arrays.asList(q1, q2));

        Assert.assertSame(q1.getOutput(), o1);
        Assert.assertSame(q2.getOutput(), o1);

        // check executed queries
        Assert.assertEquals(oracle.count, 4);
        Assert.assertEquals(oracle.queries,
                            Set.of(Pair.of(Word.epsilon(), Word.fromString("qwe")),
                                   Pair.of(Word.fromString("asd"), Word.epsilon())));
        // check that oracle actually returns different objects
        Assert.assertNotSame(oracle.answerQuery(q1.getInput()), o1);
        Assert.assertEquals(oracle.answerQuery(q1.getInput()), o1);
    }

    private static class Oracle<I> implements SingleQueryOracleMealy<I, Character> {

        private int count;
        private final Set<Pair<Word<I>, Word<I>>> queries;

        Oracle() {
            this.count = 0;
            this.queries = new HashSet<>();
        }

        @Override
        public Word<Character> answerQuery(Word<I> prefix, Word<I> suffix) {
            count++;
            queries.add(Pair.of(prefix, suffix));
            return Word.fromString("abcdef");
        }
    }

}
