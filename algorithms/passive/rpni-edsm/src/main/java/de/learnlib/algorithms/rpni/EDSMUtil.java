/* Copyright (C) 2013-2018 TU Dortmund
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
package de.learnlib.algorithms.rpni;

import java.util.List;

import com.google.common.primitives.Ints;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.StateIDs;

/**
 * @author frohme
 */
final class EDSMUtil {

    private EDSMUtil() {
    }

    static <S> long score(UniversalDeterministicAutomaton<S, Integer, ?, Boolean, ?> pta,
                          List<int[]> positiveSamples,
                          List<int[]> negativeSamples) {

        final StateIDs<S> stateIDs = pta.stateIDs();

        final int[] tp = new int[pta.size()];
        final int[] tn = new int[pta.size()];

        for (final int[] w : positiveSamples) {
            int index = stateIDs.getStateId(pta.getState(Ints.asList(w)));
            tp[index]++;
        }

        for (final int[] w : negativeSamples) {
            int index = stateIDs.getStateId(pta.getState(Ints.asList(w)));
            tn[index]++;
        }

        int score = 0;

        for (final S s : pta.getStates()) {
            final int indexOfCurrentState = stateIDs.getStateId(s);
            if (tn[indexOfCurrentState] > 0) {
                if (tp[indexOfCurrentState] > 0) {
                    return Long.MIN_VALUE;
                } else {
                    score += tn[indexOfCurrentState] - 1;
                }
            } else {
                if (tp[indexOfCurrentState] > 0) {
                    score += tp[indexOfCurrentState] - 1;
                }
            }
        }

        return score;
    }

}
