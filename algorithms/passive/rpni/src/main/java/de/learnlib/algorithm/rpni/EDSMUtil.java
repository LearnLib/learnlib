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
import net.automatalib.automaton.concept.StateIDs;
import net.automatalib.common.smartcollection.IntSeq;

final class EDSMUtil {

    private EDSMUtil() {}

    static <S> long score(UniversalDeterministicAutomaton<S, Integer, ?, Boolean, ?> merge,
                          List<IntSeq> positiveSamples,
                          List<IntSeq> negativeSamples) {

        final int numStates = merge.size();
        final StateIDs<S> stateIDs = merge.stateIDs();

        final int[] tp = new int[numStates];
        final int[] tn = new int[numStates];

        for (IntSeq w : positiveSamples) {
            int index = stateIDs.getStateId(merge.getState(w));
            tp[index]++;
        }

        for (IntSeq w : negativeSamples) {
            int index = stateIDs.getStateId(merge.getState(w));
            tn[index]++;
        }

        int score = 0;

        for (int i = 0; i < numStates; i++) {
            // note that we can't run into conflicts because we don't even consider violating merges
            if (tn[i] > 0) {
                score += tn[i] - 1;
            } else if (tp[i] > 0) {
                score += tp[i] - 1;
            }
        }

        return score;
    }

}
