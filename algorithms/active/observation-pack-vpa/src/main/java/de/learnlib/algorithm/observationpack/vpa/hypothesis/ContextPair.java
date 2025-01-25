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
package de.learnlib.algorithm.observationpack.vpa.hypothesis;

import net.automatalib.word.Word;

/**
 * A context pair of prefix and suffix to discriminate a hypothesis state.
 *
 * @param <I>
 *         input symbol type
 */
public class ContextPair<I> {

    private final Word<I> prefix;
    private final Word<I> suffix;

    public ContextPair(Word<I> prefix, Word<I> suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public Word<I> getPrefix() {
        return prefix;
    }

    public Word<I> getSuffix() {
        return suffix;
    }

    public int getLength() {
        return prefix.length() + suffix.length();
    }

    @Override
    public String toString() {
        return "<" + prefix + ", " + suffix + ">";
    }
}
