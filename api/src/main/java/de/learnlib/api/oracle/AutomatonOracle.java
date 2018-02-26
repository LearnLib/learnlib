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
package de.learnlib.api.oracle;

import java.util.Collection;

import javax.annotation.Nullable;

import de.learnlib.api.modelchecking.counterexample.Lasso;
import de.learnlib.api.modelchecking.counterexample.Lasso.DFALasso;
import de.learnlib.api.modelchecking.counterexample.Lasso.MealyLasso;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.api.query.OmegaQuery;
import de.learnlib.api.query.Query;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.ts.simple.SimpleDTS;
import net.automatalib.words.Word;

/**
 * Finds counterexamples (to particular claims), while generating words that are in a given hypothesis.
 *
 * @author Jeroen Meijer
 *
 * @param <A> the automaton type
 * @param <I> the input type
 * @param <D> the output type
 * @param <Q> the Query type
 */
public interface AutomatonOracle<A extends SimpleDTS<?, I>, I, D, Q extends Query<I, D>> {

    /**
     * Returns whether the given {@code query} is a counterexample to the given {@code hypothesis}.
     *
     * @param hypothesis the hypothesis to check.
     * @param query the query to check.
     *
     * @return whether it is a counterexample.
     */
    boolean isCounterExample(A hypothesis, Q query);

    /**
     * Returns whether the given input word is a word in the given {@code hypthesis}.
     *
     * @param hypothesis the hypothesis to check.
     * @param input the input to check.
     *
     * @return whether the {@code input} is a word in the {@code hypothesis}.
     */
    boolean isWord(A hypothesis, Word<I> input);

    /**
     * Converts the given {@code query} to a {@link DefaultQuery}.
     *
     * @param query the {@link Query} to convert.
     *
     * @return the converted query.
     */
    DefaultQuery<I, D> asDefaultQuery(Q query);

    /**
     * Returns the maximum number of words to generate.
     *
     * @return the maximum.
     */
    int getMaxWords();

    /**
     * Process the given {@code input} word by returning an <b>answered</b> {@link Query}.
     *
     * @param hypothesis the hypothesis for which the {@code input} word is generated.
     * @param input the input word for which the query needs to be answered.
     *
     * @return the answered query.
     */
    Q processInput(A hypothesis, Word<I> input);

    /**
     * Returns the next input word.
     *
     * Implementations could for example return words in a breadth-first, or depth-first manner.
     *
     * @return the next input word.
     */
    Word<I> nextInput();

    /**
     * Adds more input words given a {@code prefix} input word.
     *
     * @param hypothesis the hypothesis to add words for.
     * @param inputs the alphabet.
     * @param prefix the prefix to compute more words for.
     */
    default void addWords(A hypothesis, Collection<? extends I> inputs, Word<I> prefix) {
        for (I input: inputs) {
            final Word<I> word = prefix.append(input);

            // skip undefined inputs
            if (hypothesis.getState(word) != null) {
                addWord(word);
            }
        }
    }

    /**
     * Add a new input word.
     *
     * Implementations could add words to e.g. a {@link java.util.Stack}, or {@link java.util.Queue}.
     *
     * @param input the input word to add.
     */
    void addWord(Word<I> input);

    /**
     * Setup method which is called immediately before {@link #findCounterExample(SimpleDTS, Collection)} is called.
     */
    void pre();

    /**
     * Find a counterexample for a given {@code hypothesis}, by trying up to {@link #getMaxWords()} input words.
     *
     * @param hypothesis the hypothesis to find a counterexample for.
     * @param inputs the alphabet.
     *
     * @return the counterexample, or {@code null} if a counterexample does not exist.
     */
    @Nullable
    default DefaultQuery<I, D> findCounterExample(A hypothesis, Collection<? extends I> inputs) {
        pre();
        addWords(hypothesis, inputs, Word.epsilon());

        DefaultQuery<I, D> ce = null;

        for (int i = 0; i != getMaxWords() && ce == null; i++) {

            Word<I> input;
            do {
                input = nextInput();
                addWords(hypothesis, inputs, input);
            } while (!isWord(hypothesis, input));

            final Q query = processInput(hypothesis, input);
            if (isCounterExample(hypothesis, query)) {
                ce = asDefaultQuery(query);
            }
        }

        return ce;
    }


    /**
     * Generates words for a given DFA.
     *
     * @param <A> the type of DFA.
     * @param <I> the type of input.
     * @param <Q> the type of Query.
     */
    interface DFAOracle<A extends DFA<?, I>, I, Q extends Query<I, Boolean>> extends
            AutomatonOracle<A, I, Boolean, Q> {

        /**
         * Returns whether the given {@code input} is a word in the given {@code hypothesis}.
         *
         * In a DFA {@code input} is a word in the {@code hypothesis} if it is an access sequence to an accepting state.
         *
         * @see AutomatonOracle#isWord(SimpleDTS, Word)
         */
        @Override
        default boolean isWord(A hypothesis, Word<I> input) {
            return hypothesis.computeOutput(input);
        }
    }

    /**
     * Generates words for a given Mealy machine.
     *
     * @param <A> the type of MealyMachine.
     * @param <I> the type of input.
     * @param <O> the type of output.
     * @param <Q> the type of Query.
     */
    interface MealyOracle<A extends MealyMachine<?, I, ?, O>, I, O, Q extends Query<I, Word<O>>> extends
            AutomatonOracle<A, I, Word<O>, Q> {

        /**
         * Returns whether the given {@code input} is a word in the given {@code hypothesis}.
         *
         * In a Mealy machine {@code input} is a word in the {@code hypothesis} if the length of the input word is
         * equal to the length of the output word.
         *
         * @see AutomatonOracle#isWord(SimpleDTS, Word)
         */
        @Override
        default boolean isWord(A hypothesis, Word<I> input) {
            return input.length() == hypothesis.computeOutput(input).length();
        }
    }

    /**
     * Processes input words by means of {@link DefaultQuery}.
     *
     * @param <A> the type of automaton.
     * @param <I> the type of input.
     * @param <D> the typoe of output.
     */
    interface DefaultOracle<A extends SimpleDTS<?, I>, I, D>
            extends AutomatonOracle<A, I, D, DefaultQuery<I, D>> {

        /**
         * Returns the {@link MembershipOracle}, that answers {@link DefaultQuery}.
         *
         * @return the {@link MembershipOracle}.
         */
        MembershipOracle<I, D> getMembershipOracle();

        /**
         * Processes the given {@code input}, by returning an answered {@link DefaultQuery}.
         *
         * @see AutomatonOracle#processInput(SimpleDTS, Word)
         */
        @Override
        default DefaultQuery<I, D> processInput(A hypothesis, Word<I> input){
            final DefaultQuery<I, D> query = new DefaultQuery<>(input);
            getMembershipOracle().processQuery(query);
            return query;
        }

        @Override
        default DefaultQuery<I, D> asDefaultQuery(DefaultQuery<I, D> query) {
            return query;
        }
    }

    interface DFADefaultOracle<I> extends
            DFAOracle<DFA<?, I>, I, DefaultQuery<I, Boolean>>,
            DefaultOracle<DFA<?, I>, I, Boolean> {}

    interface MealyDefaultOracle<I, O> extends
            MealyOracle<MealyMachine<?, I, ?, O>, I, O, DefaultQuery<I, Word<O>>>,
            DefaultOracle<MealyMachine<?, I, ?, O>, I, Word<O>> {}

    /**
     * Processes input words by means of {@link OmegaQuery}.
     *
     * @param <L> the type of Lasso.
     * @param <S> the type of state.
     * @param <I> the type of input.
     * @param <D> the type of output.
     */
    interface LassoOracle<L extends Lasso<?, ?, I, D>, S, I, D>
            extends AutomatonOracle<L, I, D, OmegaQuery<S, I, D>> {

        /**
         * Returns the {@link OmegaMembershipOracle} that answers {@link OmegaQuery}.
         *
         * @return the {@link OmegaMembershipOracle}.
         */
        OmegaMembershipOracle<S, I, D> getOmegaMembershipOracle();

        /**
         * Processes input words by returning answered {@link OmegaQuery}.
         *
         * @see AutomatonOracle#processInput(SimpleDTS, Word)
         */
        @Override
        default OmegaQuery<S, I, D> processInput(L lasso, Word<I> input) {
            final OmegaQuery<S, I, D> query = new OmegaQuery<>(input, lasso.getLoopBeginIndices());
            getOmegaMembershipOracle().processQuery(query);
            return query;
        }

        @Override
        default DefaultQuery<I, D> asDefaultQuery(OmegaQuery<S, I, D> query) {
            return query;
        }
    }

    interface DFALassoOracle<S, I> extends
            DFAOracle<DFALasso<?, I>, I, OmegaQuery<S, I, Boolean>>,
            LassoOracle<DFALasso<?, I>, S, I, Boolean> {}

    interface MealyLassoOracle<S, I, O> extends
            MealyOracle<MealyLasso<?, I, O>, I, O, OmegaQuery<S, I, Word<O>>>,
            LassoOracle<MealyLasso<?, I, O>, S, I, Word<O>> {}

}
