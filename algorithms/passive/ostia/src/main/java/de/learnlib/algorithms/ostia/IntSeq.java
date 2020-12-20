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
package de.learnlib.algorithms.ostia;

import java.util.Arrays;

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

interface IntSeq {

    int size();

    int get(int index);

    static <I> IntSeq of(Word<I> word, Alphabet<I> alphabet) {
        return new IntSeq() {

            @Override
            public int size() {
                return word.size();
            }

            @Override
            public int get(int index) {
                return alphabet.applyAsInt(word.getSymbol(index));
            }

            @Override
            public String toString() {
                return word.toString();
            }
        };
    }

    static IntSeq seq(int... ints) {
        return new IntSeq() {

            @Override
            public int size() {
                return ints.length;
            }

            @Override
            public int get(int index) {
                return ints[index];
            }

            @Override
            public String toString() {
                return Arrays.toString(ints);
            }
        };
    }
}
