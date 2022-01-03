/* Copyright (C) 2013-2022 TU Dortmund
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
package de.learnlib.datastructure.pta;

import de.learnlib.datastructure.pta.pta.BlueFringePTA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.words.Alphabet;

public final class PTAUtil {

    private PTAUtil() {
        // prevent instantiation
    }

    public static <I> CompactDFA<I> toDFA(BlueFringePTA<Boolean, Void> pta, Alphabet<I> alphabet) {
        CompactDFA<I> dfa = new CompactDFA<>(alphabet, pta.getNumRedStates());
        pta.toAutomaton(dfa, alphabet, b -> b, x -> x);

        return dfa;
    }

    public static <I, O> MealyMachine<?, I, ?, O> toMealy(BlueFringePTA<Void, O> pta, Alphabet<I> alphabet) {
        CompactMealy<I, O> mealy = new CompactMealy<>(alphabet, pta.getNumRedStates());
        pta.toAutomaton(mealy, alphabet);
        return mealy;
    }
}
