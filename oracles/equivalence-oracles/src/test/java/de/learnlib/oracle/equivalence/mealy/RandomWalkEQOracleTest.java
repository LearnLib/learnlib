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
package de.learnlib.oracle.equivalence.mealy;

import java.util.Random;

import de.learnlib.oracle.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.sul.SUL;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.testng.Assert;
import org.testng.annotations.Test;

public class RandomWalkEQOracleTest {

    private static final int MAX_LENGTH;
    private static final Alphabet<Character> ALPHABET;

    static {
        MAX_LENGTH = 100;
        ALPHABET = Alphabets.characters('a', 'f');
    }

    @Test
    public void testOracle() {

        final DummySUL dummySUL = new DummySUL();

        final MealyEquivalenceOracle<Character, Character> mOracle =
                new RandomWalkEQOracle<>(dummySUL, 0.01, MAX_LENGTH, new Random(42));

        final DefaultQuery<Character, Word<Character>> ce =
                mOracle.findCounterExample(new DummyMealy(ALPHABET), ALPHABET);

        Assert.assertNull(ce);
        Assert.assertTrue(dummySUL.isCalledPost());
    }

    private static final class DummySUL implements SUL<Character, Character> {

        private boolean calledPre;
        private boolean calledPost;
        private int inputLength;

        private boolean firstInvocation = true;

        @Override
        public void pre() {
            calledPre = true;
            inputLength = 0;

            if (firstInvocation) {
                firstInvocation = false;
            } else {
                Assert.assertTrue(calledPost);
                calledPost = false;
            }
        }

        @Override
        public void post() {
            calledPost = true;
            Assert.assertTrue(inputLength <= MAX_LENGTH);
            calledPre = false;
        }

        public boolean isCalledPost() {
            return this.calledPost;
        }

        @Override
        public @Nullable Character step(@Nullable Character in) {
            Assert.assertTrue(this.calledPre);
            inputLength++;

            return null;
        }
    }

    private static final class DummyMealy extends CompactMealy<Character, Character> {

        DummyMealy(Alphabet<Character> alphabet) {
            super(alphabet);
            final Integer init = super.addInitialState();

            alphabet.forEach(s -> super.addTransition(init, s, init, null));
        }
    }

}
