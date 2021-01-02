/* Copyright (C) 2013-2020 TU Dortmund
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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.commons.smartcollections.IntSeq;

/**
 * @author frohme
 */
final class EDSMUtil {

    private EDSMUtil() {}

    static <S> long score(UniversalDeterministicAutomaton<S, Integer, ?, Boolean, ?> merge,
                          List<IntSeq> positiveSamples,
                          List<IntSeq> negativeSamples) {

        final Collection<S> states = merge.getStates();
        final int numStates = states.size();
        // we don't use the regular stateIDs because we only want to collect all states once.
        final Map<S, Integer> stateIDs = Maps.newHashMapWithExpectedSize(numStates);

        int counter = 0;
        for (S s : states) {
            stateIDs.put(s, counter++);
        }

        final int[] tp = new int[numStates];
        final int[] tn = new int[numStates];

        for (final IntSeq w : positiveSamples) {
            int index = stateIDs.get(merge.getState(w));
            tp[index]++;
        }

        for (final IntSeq w : negativeSamples) {
            int index = stateIDs.get(merge.getState(w));
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
