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
package de.learnlib.algorithm.lambda.ttt.pt;

import java.util.HashMap;
import java.util.Map;

import de.learnlib.algorithm.lambda.ttt.dt.DTLeaf;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

public class PTNodeImpl<I, D> implements PTNode<I, D> {

    private final @Nullable PTNodeImpl<I, D> parent;
    private final @Nullable I symbol;
    private final Map<I, PTNodeImpl<I, D>> children;

    private DTLeaf<I, D> state;

    public PTNodeImpl(@Nullable PTNodeImpl<I, D> parent, @Nullable I symbol) {
        this.parent = parent;
        this.symbol = symbol;
        this.children = new HashMap<>();
    }

    @Override
    public Word<I> word() {
        return toWord(Word.epsilon());
    }

    @Override
    public PTNode<I, D> append(I i) {
        assert !children.containsKey(i);
        PTNodeImpl<I, D> n = new PTNodeImpl<>(this, i);
        children.put(i, n);
        return n;
    }

    @Override
    public void setState(DTLeaf<I, D> node) {
        this.state = node;
    }

    @Override
    public DTLeaf<I, D> state() {
        return state;
    }

    private Word<I> toWord(Word<I> suffix) {
        if (symbol == null || parent == null) {
            return suffix;
        }
        return parent.toWord(suffix.prepend(symbol));
    }

    @Override
    public @Nullable PTNode<I, D> succ(I a) {
        return children.get(a);
    }

    @Override
    public void makeShortPrefix() {
        this.state.makeShortPrefix(this);
    }
}

