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

/**
 * @author Aleksander Mendoza-Drosik
 */
class StateCopy extends StateParent {

    final State original;

    StateCopy(State original) {
        super.out = original.out == null ? null : new Out(IntQueue.copyAndConcat(original.out.str, null));
        super.transitions = copyTransitions(original.transitions);
        this.original = original;
    }

    private Edge[] copyTransitions(Edge[] transitions) {
        final Edge[] copy = new Edge[transitions.length];
        for (int i = 0; i < copy.length; i++) {
            copy[i] = transitions[i] == null ? null : new Edge(transitions[i]);
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
    void prepend(IntQueue prefix) {
        for (Edge edge : transitions) {
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
