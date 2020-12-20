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
package net.automatalib.commons.smartcollections;

import java.util.Arrays;
import java.util.List;
import java.util.PrimitiveIterator.OfInt;

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * An {@link IntSeq} is an abstract view on a finite, random-access data-structure for primitive integer values. It
 * allows for a unified view on integer arrays, {@link List lists} of integers, {@link Word words} with an accompanying
 * {@link Alphabet#getSymbolIndex(Object) alphabet index function}, etc.
 *
 * @author Aleksander Mendoza-Drosik
 * @author frohme
 */
public interface IntSeq extends Iterable<Integer> {

    int size();

    int get(int index);

    @Override
    default OfInt iterator() {
        return new OfInt() {

            int curr;

            {
                this.curr = 0;
            }

            @Override
            public int nextInt() {
                return get(curr++);
            }

            @Override
            public boolean hasNext() {
                return curr < size();
            }
        };
    }

    static <I> IntSeq of(Word<I> word, Alphabet<I> alphabet) {
        return new IntSeq() {

            @Override
            public int size() {
                return word.size();
            }

            @Override
            public int get(int index) {
                return alphabet.getSymbolIndex(word.getSymbol(index));
            }

            @Override
            public String toString() {
                return word.toString();
            }
        };
    }

    static IntSeq of(int... ints) {
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

    static IntSeq of(List<Integer> ints) {
        return new IntSeq() {

            @Override
            public int size() {
                return ints.size();
            }

            @Override
            public int get(int index) {
                return ints.get(index);
            }

            @Override
            public String toString() {
                return ints.toString();
            }
        };
    }
}
