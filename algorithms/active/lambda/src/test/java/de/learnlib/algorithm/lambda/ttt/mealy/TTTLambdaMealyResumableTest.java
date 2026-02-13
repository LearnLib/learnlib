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
package de.learnlib.algorithm.lambda.ttt.mealy;

import de.learnlib.algorithm.lambda.ttt.TTTLambdaState;
import de.learnlib.algorithm.lambda.ttt.dfa.TTTLambdaDFA;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.oracle.SingleQueryOracle;
import de.learnlib.testsupport.AbstractResumableLearnerMealyTest;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TTTLambdaMealyResumableTest
        extends AbstractResumableLearnerMealyTest<TTTLambdaMealy<Character, Character>, TTTLambdaState<Character, Word<Character>>> {

    @Override
    protected TTTLambdaMealy<Character, Character> getLearner(MealyMembershipOracle<Character, Character> oracle,
                                                              Alphabet<Character> alphabet) {
        return new TTTLambdaMealy<>(alphabet, oracle);
    }

    @Override
    protected int getRounds() {
        return 2;
    }

    @Test
    public void testIncorrectState() {
        final Alphabet<Character> alphabet = Alphabets.fromArray();
        final SingleQueryOracle<Character, Word<Character>> oracleMealy = (pre, suff) -> suff;
        final SingleQueryOracle<Character, Boolean> oracleDFA = (pre, suff) -> false;
        final var learnerMealy = new TTTLambdaMealy<>(alphabet, oracleMealy);
        final var learnerDFA = new TTTLambdaDFA<>(alphabet, oracleDFA);

        final TTTLambdaState<?, ?> origState = learnerDFA.suspend();
        @SuppressWarnings("unchecked")
        final TTTLambdaState<Character, Word<Character>> castState =
                (TTTLambdaState<Character, Word<Character>>) origState;

        Assert.assertThrows(IllegalArgumentException.class, () -> learnerMealy.resume(castState));
    }
}
