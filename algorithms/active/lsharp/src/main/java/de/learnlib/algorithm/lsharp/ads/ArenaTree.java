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
package de.learnlib.algorithm.lsharp.ads;

import java.util.ArrayList;
import java.util.List;

import net.automatalib.common.util.Pair;

public class ArenaTree<T, P> {

    public final List<ArenaNode<T, P>> arena;

    public ArenaTree() {
        this.arena = new ArrayList<>();
    }

    public int size() {
        return this.arena.size();
    }

    public int node(T value) {
        int idx = this.size();
        this.arena.add(new ArenaNode<>(value));
        return idx;
    }

    public int nodeWithParent(T value, int pIndex, P input) {
        int idx = this.arena.size();
        this.arena.add(new ArenaNode<>(Pair.of(input, pIndex), value));
        return idx;
    }

    public T get(int index) {
        return this.arena.get(index).value;
    }

}
