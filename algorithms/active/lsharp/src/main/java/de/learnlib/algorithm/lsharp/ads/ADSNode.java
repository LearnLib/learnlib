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

import java.util.HashMap;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.Nullable;

public class ADSNode<I, O> {

    private final @Nullable I input;
    private final Map<O, ADSNode<I, O>> children;
    private final int score;

    public ADSNode() {
        this.input = null;
        this.children = new HashMap<>();
        this.score = 0;
    }

    public ADSNode(I input, Map<O, ADSNode<I, O>> children, int score) {
        this.input = input;
        this.children = children;
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public @Nullable I getInput() {
        return input;
    }

    public @Nullable ADSNode<I, O> getChildNode(O lastOutput) {
        return this.children.get(lastOutput);
    }
}
