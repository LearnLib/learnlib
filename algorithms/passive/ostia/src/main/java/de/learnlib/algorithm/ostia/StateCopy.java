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
package de.learnlib.algorithm.ostia;

import org.checkerframework.checker.nullness.qual.Nullable;

class StateCopy extends StateParent {

    final State original;

    StateCopy(State original) {
        super.out = original.out == null ? null : new Out(IntQueue.copyAndConcat(original.out.str, null));
        super.transitions = copyTransitions(original.transitions);
        this.original = original;
    }

    private static @Nullable Edge[] copyTransitions(@Nullable Edge[] transitions) {
        final @Nullable Edge[] copy = new Edge[transitions.length];
        for (int i = 0; i < copy.length; i++) {
            @Nullable Edge edge = transitions[i];
            copy[i] = edge == null ? null : new Edge(edge);
        }
        return copy;
    }

    void assign() {
        original.out = out;
        original.transitions = transitions;
    }

    /**
     * The IntQueue is consumed and should not be reused after calling this method.
     */
    void prepend(@Nullable IntQueue prefix) {
        for (@Nullable Edge edge : transitions) {
            if (edge != null) {
                edge.out = IntQueue.copyAndConcat(prefix, edge.out);
            }
        }
        if (out == null) {
            out = new Out(prefix);
        } else {
            out.str = IntQueue.copyAndConcat(prefix, out.str);
        }
    }
}
