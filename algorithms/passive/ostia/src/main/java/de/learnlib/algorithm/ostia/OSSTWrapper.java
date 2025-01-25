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

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.Set;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.SubsequentialTransducer;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

class OSSTWrapper<I, O> implements SubsequentialTransducer<State, I, Edge, O> {

    private final State root;
    private final Alphabet<I> inputAlphabet;
    private final Alphabet<O> outputAlphabet;

    OSSTWrapper(State root, Alphabet<I> inputAlphabet, Alphabet<O> outputAlphabet) {
        this.root = root;
        this.inputAlphabet = inputAlphabet;
        this.outputAlphabet = outputAlphabet;
    }

    @Override
    public Collection<State> getStates() {
        final Set<State> cache = new LinkedHashSet<>();
        final Queue<State> queue = new ArrayDeque<>();

        queue.add(root);

        while (!queue.isEmpty()) {
            @SuppressWarnings("nullness") // false positive https://github.com/typetools/checker-framework/issues/399
            @NonNull State s = queue.poll();
            cache.add(s);

            for (Edge transition : s.transitions) {
                if (transition != null) {
                    State succ = transition.target;

                    if (succ != null && !cache.contains(succ)) {
                        queue.add(succ);
                        cache.add(succ);
                    }
                }
            }
        }

        return cache;
    }

    @Override
    public @Nullable Edge getTransition(State state, I input) {
        return state.transitions[inputAlphabet.getSymbolIndex(input)];
    }

    @Override
    public State getInitialState() {
        return root;
    }

    @Override
    public Word<O> getStateProperty(State state) {
        return outToWord(state.out);
    }

    @Override
    public Word<O> getTransitionProperty(Edge transition) {
        return outToWord(transition.out);
    }

    @Override
    public State getSuccessor(Edge transition) {
        return transition.target;
    }

    private Word<O> outToWord(@Nullable Out out) {
        return outToWord(out == null ? null : out.str);
    }

    private Word<O> outToWord(@Nullable IntQueue out) {
        if (out == null) {
            return Word.epsilon();
        }

        final WordBuilder<O> wb = new WordBuilder<>();

        IntQueue outIter = out;
        while (outIter != null) {
            wb.add(outputAlphabet.getSymbol(outIter.value));
            outIter = outIter.next;
        }

        return wb.toWord();
    }
}
