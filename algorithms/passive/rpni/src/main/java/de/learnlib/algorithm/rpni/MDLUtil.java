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
package de.learnlib.algorithm.rpni;

import java.util.List;

import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.common.smartcollection.IntSeq;

final class MDLUtil {

    private MDLUtil() {}

    static <S, I> double score(UniversalDeterministicAutomaton<S, Integer, ?, Boolean, ?> merged,
                               int alphabetSize,
                               List<IntSeq> positiveSamples) {
        double sampleScore = 0;

        for (IntSeq w : positiveSamples) {
            sampleScore += countWordChoices(merged, alphabetSize, w);
        }

        return merged.size() * alphabetSize + sampleScore;
    }

    private static <S, I> double countWordChoices(UniversalDeterministicAutomaton<S, Integer, ?, Boolean, ?> merged,
                                                  int alphabetSize,
                                                  IntSeq word) {
        S currentState = merged.getInitialState();
        assert currentState != null;
        double result = Math.log(countStateChoices(merged, alphabetSize, currentState)) /
                        Math.log(2); // log_2 x = log_e x / log_e 2

        for (Integer i : word) {
            currentState = merged.getSuccessor(currentState, i);
            assert currentState != null;
            result += Math.log(countStateChoices(merged, alphabetSize, currentState)) / Math.log(2);
        }

        return result;
    }

    private static <S, I> int countStateChoices(UniversalDeterministicAutomaton<S, Integer, ?, Boolean, ?> pta,
                                                int alphabetSize,
                                                S state) {
        int choices = Boolean.TRUE.equals(pta.getStateProperty(state)) ? 1 : 0;

        for (int i = 0; i < alphabetSize; i++) {
            if (pta.getSuccessor(state, i) != null) {
                choices++;
            }
        }

        return choices;
    }

}
