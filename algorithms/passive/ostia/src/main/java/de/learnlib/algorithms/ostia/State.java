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

class State {

    Out out;
    Edge[] transitions;

    State(int alphabetSize) {
        transitions = new Edge[alphabetSize];
    }

    State(State copy) {
        transitions = OSTIA.copyTransitions(copy.transitions);
        out = copy.out == null ? null : new Out(IntQueue.copyAndConcat(copy.out.str, null));
    }

    public void assign(State other) {
        out = other.out;
        transitions = other.transitions;
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

    /**
     * The IntQueue is consumed and should not be reused after calling this method.
     */
    void prependButIgnoreMissingStateOutput(IntQueue prefix) {
        for (Edge edge : transitions) {
            if (edge != null) {
                edge.out = IntQueue.copyAndConcat(prefix, edge.out);
            }
        }
        if (out != null) {
            out.str = IntQueue.copyAndConcat(prefix, out.str);
        }
    }

}
