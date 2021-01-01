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
class State {

    private State(){

    }
    static class Original extends State {
        public Original(int alphabetSize) {
            transitions = new Edge[alphabetSize];
        }


    }
    static class  Copy extends State {
        public final Original original;

        public Copy(Original original) {
            this.original = original;
            transitions = copyTransitions(original.transitions);
            out = original.out == null ? null : new Out(IntQueue.copyAndConcat(original.out.str, null));
        }

        private Edge[] copyTransitions(Edge[] transitions) {
            final Edge[] copy = new Edge[transitions.length];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = transitions[i] == null ? null : new Edge(transitions[i]);
            }
            return copy;
        }

        public void assign() {
            original.out = out;
            original.transitions = transitions;
        }
    }

    Out out;
    Edge[] transitions;


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

    @Override
    public String toString() {
        return String.valueOf(out);
    }
}
