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
package de.learnlib.examples.dfa;

import de.learnlib.examples.DefaultLearningExample.DefaultDFALearningExample;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

public class ExampleKeylock extends DefaultDFALearningExample<Integer> {

    private final boolean cyclical;

    public ExampleKeylock(int size, boolean cyclical, int additionalSymbols) {
        super(createDFA(size, cyclical, additionalSymbols));
        this.cyclical = cyclical;
    }

    public static CompactDFA<Integer> createDFA(int size, boolean cyclical, int additionalSymbols) {
        if (size < 2) {
            throw new IllegalArgumentException("Minimum keylock DFA size is 2");
        }

        Alphabet<Integer> alphabet = Alphabets.integers(0, additionalSymbols);
        CompactDFA<Integer> result = new CompactDFA<>(alphabet, size);

        int init = result.addIntInitialState(false);
        for (int sym = 1; sym <= additionalSymbols; sym++) {
            result.setTransition(init, sym, init);
        }

        int prev = init;
        for (int i = 2; i < size; i++) {
            int curr = result.addIntState(false);
            for (int sym = 1; sym <= additionalSymbols; sym++) {
                result.setTransition(curr, sym, curr);
            }

            result.setTransition(prev, 0, curr);
            prev = curr;
        }

        int end = result.addIntState(true);
        result.setTransition(prev, 0, end);

        if (cyclical) {
            result.setTransition(end, 0, init);
        }
        for (int sym = cyclical ? 1 : 0; sym <= additionalSymbols; sym++) {
            result.setTransition(end, sym, end);
        }

        return result;
    }

    public static ExampleKeylock createExample(int size, boolean cyclical) {
        return createExample(size, cyclical, 0);
    }

    public static ExampleKeylock createExample(int size, boolean cyclical, int additionalSymbols) {
        return new ExampleKeylock(size, cyclical, additionalSymbols);
    }

    @Override
    public String toString() {
        return "Keylock[size=" + getReferenceAutomaton().size() + ",alphabetSize=" + getAlphabet().size() +
               ",cyclical=" + cyclical + "]";
    }

}
