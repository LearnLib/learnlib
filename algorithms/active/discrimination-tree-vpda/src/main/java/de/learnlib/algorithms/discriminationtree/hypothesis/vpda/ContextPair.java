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
package de.learnlib.algorithms.discriminationtree.hypothesis.vpda;

import net.automatalib.words.Word;

/**
 * @param <I>
 *         input symbol type
 *
 * @author Malte Isberner
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
