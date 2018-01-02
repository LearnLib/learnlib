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
package de.learnlib.algorithms.lstar.mealy;

import java.util.ArrayList;
import java.util.List;

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public final class LStarMealyUtil {

    private LStarMealyUtil() {
        // prevent instantiation
    }

    public static <I> List<Word<I>> ensureSuffixCompliancy(List<Word<I>> suffixes,
                                                           Alphabet<I> alphabet,
                                                           boolean needsConsistencyCheck) {
        List<Word<I>> compSuffixes = new ArrayList<>();
        if (needsConsistencyCheck) {
            for (int i = 0; i < alphabet.size(); i++) {
                compSuffixes.add(Word.fromLetter(alphabet.getSymbol(i)));
            }
        }

        for (Word<I> w : suffixes) {
            if (w.isEmpty()) {
                continue;
            }
            if (needsConsistencyCheck && w.length() == 1) {
                continue;
            }
            compSuffixes.add(w);
        }

        return compSuffixes;
    }

}
