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
package de.learnlib.algorithm.rpni;

import java.util.List;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.word.Word;

final class MDLUtil {

    private MDLUtil() {}

    static <S, I> double score(UniversalDeterministicAutomaton<S, I, ?, Boolean, ?> merged,
                               Alphabet<I> alphabet,
                               List<Word<I>> positiveSamples) {
        double sampleScore = 0;

        for (Word<I> w : positiveSamples) {
            sampleScore += countWordChoices(merged, alphabet, w);
        }

        return merged.size() * alphabet.size() + sampleScore;
    }

    private static <S, I> double countWordChoices(UniversalDeterministicAutomaton<S, I, ?, Boolean, ?> merged,
                                                  Alphabet<I> alphabet,
                                                  Word<I> word) {
        S currentState = merged.getInitialState();
        assert currentState != null;
        double result = Math.log(countStateChoices(merged, alphabet, currentState)) /
                        Math.log(2); // log_2 x = log_e x / log_e 2

        for (I i : word) {
            currentState = merged.getSuccessor(currentState, i);
            assert currentState != null;
            result += Math.log(countStateChoices(merged, alphabet, currentState)) / Math.log(2);
        }

        return result;
    }

    private static <S, I> int countStateChoices(UniversalDeterministicAutomaton<S, I, ?, Boolean, ?> pta,
                                                Alphabet<I> alphabet,
                                                S state) {
        int choices = Boolean.TRUE.equals(pta.getStateProperty(state)) ? 1 : 0;

        for (I i : alphabet) {
            if (pta.getSuccessor(state, i) != null) {
                choices++;
            }
        }

        return choices;
    }

}
