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
package de.learnlib.algorithms.oml.ttt.st;

import java.util.LinkedHashMap;
import java.util.Map;

import net.automatalib.words.Word;

/**
 * @author fhowar
 */
class STNodeImpl<I> implements STNode<I> {

    private final STNodeImpl<I> parent;
    private final I symbol;
    private final Map<I, STNodeImpl<I>> children;

    STNodeImpl(STNodeImpl<I> parent, I symbol) {
        this.parent = parent;
        this.symbol = symbol;
        this.children = new LinkedHashMap<>();
    }

    @Override
    public Word<I> word() {
        return toWord(Word.epsilon());
    }

    private Word<I> toWord(Word<I> prefix) {
        if (symbol == null) {
            return prefix;
        }
        return parent.toWord(prefix.append(symbol));
    }

    @Override
    public STNodeImpl<I> prepend(I a) {
        STNodeImpl<I> n = children.get(a);
        if (n == null) {
            n = new STNodeImpl<>(this, a);
            children.put(a, n);
        }
        return n;
    }
}

