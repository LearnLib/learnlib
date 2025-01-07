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
package de.learnlib.oracle;

import java.util.Collection;
import java.util.Queue;
import java.util.Stack;

import de.learnlib.query.DefaultQuery;
import net.automatalib.automaton.DeterministicAutomaton;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Finds counterexamples (to particular claims) to a hypothesis, while generating words that are in the given
 * hypothesis.
 *
 * @param <A>
 *         the automaton type
 * @param <I>
 *         the input type
 * @param <D>
 *         the output type
 */
public interface AutomatonOracle<A extends DeterministicAutomaton<?, I, ?>, I, D> {

    /**
     * Returns whether the given input and output is a counter example for the given hypothesis.
     *
     * @param hypothesis
     *         the hypothesis
     * @param inputs
     *         the input sequence
     * @param output
     *         the output corresponding to the input.
     *
     * @return whether the given input and output is a counter example.
     */
    boolean isCounterExample(A hypothesis, Iterable<? extends I> inputs, D output);

    /**
     * Returns the next input word, or {@code null} if there is no next input.
     * <p>
     * Implementations could for example return words in a breadth-first, or depth-first manner.
     *
     * @return the next input word, or {@code null} if there is no next input.
     */
    @Nullable Word<I> nextInput();

    /**
     * Add a new input word.
     * <p>
     * Implementations could add words to e.g. a {@link Stack}, or {@link Queue}.
     *
     * @param input
     *         the input word to add.
     */
    void addWord(Word<I> input);

    /**
     * Setup method which is called immediately before
     * {@link #findCounterExample(DeterministicAutomaton, Collection, int)} is called.
     */
    void pre();

    /**
     * Returns the multiplier used to compute the number of queries this automaton oracle should perform to decide
     * whether a given hypothesis is a counter example.
     *
     * @return the multiplier
     */
    double getMultiplier();

    /**
     * Sets the multiplier value. See {@link #getMultiplier()}.
     *
     * @param multiplier
     *         the multiplier
     */
    void setMultiplier(double multiplier);

    /**
     * Processes the given input. Implementations could use membership oracles to process the query.
     *
     * @param hypothesis
     *         the hypothesis.
     * @param input
     *         the input to process.
     *
     * @return the processed query.
     */
    DefaultQuery<I, D> processInput(A hypothesis, Word<I> input);

    /**
     * Adds words to a datastructure. The key part of the implementation is that undefined inputs will be skipped.
     *
     * @param hypothesis
     *         the automaton to add words for.
     * @param inputs
     *         the input alphabet.
     * @param prefix
     *         the current prefix to extend.
     */
    default void addWords(A hypothesis, Collection<? extends I> inputs, Word<I> prefix) {
        for (I i : inputs) {
            final Word<I> word = prefix.append(i);

            // skip undefined inputs
            if (!hypothesis.getStates(word).isEmpty()) {
                addWord(word);
            }
        }
    }

    /**
     * Returns whether the given input is accepted by the given hypothesis.
     *
     * @param hypothesis
     *         the hypothesis automaton.
     * @param input
     *         the input.
     *
     * @return whether the given input is accepted.
     */
    boolean accepts(A hypothesis, Iterable<? extends I> input);

    /**
     * Find a counterexample for a given {@code hypothesis}.
     *
     * @param hypothesis
     *         the hypothesis to find a counter example to.
     * @param inputs
     *         the alphabet.
     * @param maxQueries
     *         the maximum number of queries.
     *
     * @return the counterexample, or {@code null} if a counter example does not exist.
     */
    default @Nullable DefaultQuery<I, D> findCounterExample(A hypothesis,
                                                            Collection<? extends I> inputs,
                                                            int maxQueries) {
        pre();
        DefaultQuery<I, D> ce = null;

        int queries = 0;
        for (Word<I> input = nextInput(); input != null && ce == null && queries != maxQueries; input = nextInput()) {
            addWords(hypothesis, inputs, input);
            if (accepts(hypothesis, input)) {
                final DefaultQuery<I, D> query = processInput(hypothesis, input);
                ce = isCounterExample(hypothesis, query.getInput(), query.getOutput()) ? query : null;
                queries++;
            }
        }

        return ce;
    }

    /**
     * Finds a counter example to the given hypothesis. By default, the maximum number of queries performed are
     * {@code hypothesis.size() * getMultiplier()}.
     *
     * @param hypothesis
     *         the hypothesis automaton.
     * @param inputs
     *         the input alphabet.
     *
     * @return the counter example, or {@code null} if a counter example does not exist
     */
    default @Nullable DefaultQuery<I, D> findCounterExample(A hypothesis, Collection<? extends I> inputs) {
        return findCounterExample(hypothesis, inputs, (int) (hypothesis.size() * getMultiplier()));
    }

    interface DFAOracle<I> extends AutomatonOracle<DFA<?, I>, I, Boolean> {

        @Override
        default boolean accepts(DFA<?, I> hypothesis, Iterable<? extends I> input) {
            return hypothesis.accepts(input);
        }
    }

    interface MealyOracle<I, O> extends AutomatonOracle<MealyMachine<?, I, ?, O>, I, Word<O>> {

        @Override
        default boolean accepts(MealyMachine<?, I, ?, O> hypothesis, Iterable<? extends I> input) {
            return hypothesis.computeOutput(input) != null;
        }
    }
}
