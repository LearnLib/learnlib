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
package de.learnlib.api;

import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.words.Word;

@ParametersAreNonnullByDefault
public interface AccessSequenceTransformer<I> {

    Word<I> transformAccessSequence(Word<I> word);

    default Word<I> longestASPrefix(Word<I> word) {
        int len = word.length();
        Word<I> lastPrefix = Word.epsilon();
        for (int i = 1; i <= len; i++) {
            Word<I> prefix = word.prefix(i);
            if (!isAccessSequence(prefix)) {
                return lastPrefix;
            }
            lastPrefix = prefix;
        }
        return word;
    }

    boolean isAccessSequence(Word<I> word);
}
