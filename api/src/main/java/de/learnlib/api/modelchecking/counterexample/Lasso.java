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
package de.learnlib.api.modelchecking.counterexample;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.automata.concepts.InputAlphabetHolder;
import net.automatalib.automata.concepts.Output;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.simple.SimpleAutomaton;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealyTransition;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.ts.simple.SimpleDTS;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import net.automatalib.words.impl.Alphabets;

/**
 * A lasso is an single infinite word.
 *
 * The implementation is an automaton such that its singleton language is the infinite word. Also, the implementation
 * is actually the finite representation (by unrolling the loop) of the infinite word, including information how many
 * times the loop of the lasso is unrolled.
 *
 * @param <S> the state type of the automaton that contains the infinite word.
 * @param <A> the automaton type which contains the lasso.
 * @param <I> the input type
 * @param <D> the output type
 */
@ParametersAreNonnullByDefault
public abstract class Lasso<S, A extends SimpleDTS<S, I> & Output<I, D>, I, D>
        implements SimpleDTS<Integer, I>, Output<I, D>, SimpleAutomaton<Integer, I>, InputAlphabetHolder<I> {

    public static final String NO_LASSO = "Automaton is not lasso shaped";

    /**
     * @see #getWord()
     */
    private final Word<I> word;

    /**
     * @see #getLoop()
     */
    private final Word<I> loop;

    /**
     * @see #getPrefix()
     */
    private final Word<I> prefix;

    /**
     * @see #getOutput()
     */
    private final D output;

    /**
     * @see #getAutomaton()
     */
    private final A automaton;

    /**
     * @see #getInputAlphabet()
     */
    private final Alphabet<I> inputAlphabet;

    /**
     * @see #getLoopBeginIndices()
     */
    private final SortedSet<Integer> loopBeginIndices = new TreeSet<>();

    /**
     * Constructs a finite representation of a given automaton (that contains a lasso), by unrolling the loop {@code
     * unfoldTimes}.
     *
     * @param automaton the automaton containing the lasso.
     * @param inputs the input alphabet.
     * @param unfoldTimes the number of times the loop needs to be unrolled, must be {@code > 0}.
     */
    public Lasso(A automaton, Collection<? extends I> inputs, int unfoldTimes) {
        assert unfoldTimes > 0;

        // save the original automaton
        this.automaton = automaton;

        // construct the input alphabet
        inputAlphabet = Alphabets.fromCollection(inputs);

        // create a map for the visited states
        final Map<S, Integer> states = new HashMap<>();

        // create a WordBuilder, for the finite representation of the lasso
        final WordBuilder<I> wb = new WordBuilder<>();

        // start visiting the initial state
        S current = automaton.getInitialState();

        // index for the current state
        int i = 0;
        do {
            // create a mapping from the current state to the state index
            states.put(current, i++);

            // find the input that leads to the next state
            final S c = current;
            final I input = inputAlphabet.stream().filter(in -> automaton.getSuccessor(c, in) != null).
                    findAny().orElseThrow(() -> new IllegalArgumentException(NO_LASSO));

            // append the input to the finite representation
            wb.append(input);

            // continue with the next state.
            current = automaton.getSuccessor(current, input);
        } while (!states.containsKey(current));

        // save the state index at which the loop begins
        final int loopBegin = states.get(current);

        // determine the loop of the lasso
        loop = wb.toWord(loopBegin, wb.size());

        // determine the prefix of the lasso
        prefix = wb.toWord(0, loopBegin);

        // append the loop several times to the finite representation
        for (int u = 1; u < unfoldTimes; u++) {
            wb.append(loop);
        }

        // store the entire finite representation of the lasso
        word = wb.toWord();

        // store the finite representation of output of the lasso
        output = automaton.computeOutput(word);

        // store all the symbol indices after which the beginning of the loop is visited.
        for (int l = prefix.length(); l <= word.length(); l += loop.length()) {
            loopBeginIndices.add(l);
        }
    }

    /**
     * Gets the finite representation of the lasso.
     *
     * @return the Word.
     */
    public Word<I> getWord() {
        return word;
    }

    /**
     * Gets the loop of the lasso.
     *
     * @return the Word.
     */
    public Word<I> getLoop() {
        return loop;
    }

    /**
     * Gets the prefix of the lasso.
     *
     * @return the Word.
     */
    public Word<I> getPrefix() {
        return prefix;
    }

    /**
     * Gets the finite representation of the output of the lasso.
     *
     * @return the output type D.
     */
    public D getOutput() {
        return output;
    }

    /**
     * Gets the automaton containing the lasso.
     *
     * @return the automaton type a.
     */
    public A getAutomaton() {
        return automaton;
    }

    /**
     * The sorted set containing some symbol indices after which the begin state of the loop is visited.
     */
    public SortedSet<Integer> getLoopBeginIndices() {
        return loopBeginIndices;
    }

    @Nullable
    @Override
    public Integer getInitialState() {
        return 0;
    }

    /**
     * Get the successor state of a given state, or {@code null} when no such successor exists.
     *
     * @see SimpleDTS#getSuccessor(Object, Object)
     */
    @Nullable
    @Override
    public Integer getSuccessor(Integer state, @Nullable I input) {
        final Integer result;
        if (state < word.length() && input.equals(word.getSymbol(state))) {
            result = state + 1;
        } else {
            result = null;
        }

        return result;
    }

    @Nonnull
    @Override
    public Collection<Integer> getStates() {
        return CollectionsUtil.intRange(0, word.length());
    }

    /**
     * Gets the input alphabet of this automaton.
     *
     * @return the Alphabet.
     */
    @Nonnull
    @Override
    public Alphabet<I> getInputAlphabet() {
        return inputAlphabet;
    }

    /**
     * A DFALasso is a lasso for {@link DFA}s.
     *
     * @param <S> the state type of the DFA that contains the lasso.
     * @param <I> the input type
     */
    public static class DFALasso<S, I> extends Lasso<S, DFA<S, I>, I, Boolean> implements DFA<Integer, I> {

        public DFALasso(DFA<S, I> automaton, Collection<? extends I> inputs, int unfoldTimes) {
            super(automaton, inputs, unfoldTimes);
        }

        @Nullable
        @Override
        public Integer getTransition(Integer state, @Nullable I input) {
            return getSuccessor(state, input);
        }

        /**
         * Returns whether the given state is accepting.
         *
         * The current state is only accepting iff it is precisely the state after the last symbol index in the
         * finite representation of the lasso.
         *
         * @param state to compute whether it is accepting.
         *
         * @return whether the given {@code state} is accepting.
         */
        @Override
        public boolean isAccepting(Integer state) {
            return state == getWord().length();
        }
    }

    /**
     * A MealyLasso is a lasso for {@link MealyMachine}s.
     *
     * @param <S> the state type of the Mealy machine that contains the lasso.
     * @param <I> the input type
     * @param <O> the output type
     */
    public static class MealyLasso<S, I, O> extends Lasso<S, MealyMachine<S, I, ?, O>, I, Word<O>>
            implements MealyMachine<Integer, I, CompactMealyTransition<O>, O> {

        public MealyLasso(MealyMachine<S, I, ?, O> automaton, Collection<? extends I> inputs, int unfoldTimes) {
            super(automaton, inputs, unfoldTimes);
        }

        @Nullable
        @Override
        public O getTransitionOutput(CompactMealyTransition<O> transition) {
            return transition.getOutput();
        }

        /**
         * Returns the transition from a given {@code state}, and {@code input}, or {@code null} if such a transition
         * does not exist.
         *
         * @see net.automatalib.ts.DeterministicTransitionSystem#getTransition(Object, Object)
         */
        @Nullable
        @Override
        public CompactMealyTransition<O> getTransition(Integer state, @Nullable I input) {
            final CompactMealyTransition<O> result;
            if (getWord().getSymbol(state).equals(input)) {
                result = new CompactMealyTransition<>(state + 1, getOutput().getSymbol(state));
            } else {
                result = null;
            }
            return result;
        }

        @Nonnull
        @Override
        public Integer getSuccessor(CompactMealyTransition<O> transition) {
            return transition.getSuccId();
        }

        /**
         * Computes the output of the given input sequence.
         *
         * Only returns a word ({@code null} otherwise) when the input sequence is precisely the finite representation
         * of the input word of the lasso.
         *
         * @see Output#computeOutput(Iterable)
         */
        @Override
        public Word<O> computeOutput(Iterable<? extends I> input) {
            final Word<O> output = getAutomaton().computeOutput(input);
            final Word<O> result;
            if (output.equals(getOutput())) {
                result = output;
            } else {
                result = Word.epsilon();
            }

            return result;
        }
    }
}
