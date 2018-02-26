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
package de.learnlib.oracle;

import java.util.LinkedList;
import java.util.Queue;

import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.api.oracle.AutomatonOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.api.query.Query;
import net.automatalib.ts.simple.SimpleDTS;
import net.automatalib.words.Word;

/**
 * Finds counterexamples to a given hypothesis by generating words in that hypothesis in a breadth-first manner.
 *
 * @author Jeroen Meijer
 *
 * @param <A> the automaton type
 * @param <I> the input type
 * @param <D> the output type
 * @param <Q> the Query type
 */
@ParametersAreNonnullByDefault
public abstract class AbstractBreadthFirstOracle<A extends SimpleDTS<?, I>, I, D, Q extends Query<I, D>>
        implements AutomatonOracle<A, I, D, Q> {

    /**
     * The queue containing the words to find a counterexample with.
     */
    private final Queue<Word<I>> queue = new LinkedList<>();

    /**
     * The maximum number of words to generate.
     */
    private final int maxWords;

    /**
     * Constructs a new AbstractBreadthFirstOracle.
     *
     * @param maxWords the maximum number of words to generate.
     */
    protected AbstractBreadthFirstOracle(int maxWords) {
        this.maxWords = maxWords;
    }

    /**
     * Returns the maximum number of words to generate.
     *
     * @return the maximum number of words to generate.
     */
    @Override
    public int getMaxWords() {
        return maxWords;
    }

    /**
     * Returns the next input word, by popping from a queue.
     *
     * @see AutomatonOracle#nextInput()
     */
    @Override
    public Word<I> nextInput() {
        return queue.remove();
    }

    /**
     * Adds a new input word to the queue.
     *
     * @see AutomatonOracle#addWord(Word)
     */
    @Override
    public void addWord(Word<I> input) {
        queue.add(input);
    }

    /**
     * Clears the queue.
     */
    @Override
    public void pre() {
        queue.clear();
    }

    /**
     * An implementation of a {@link AbstractBreadthFirstOracle}, that uses {@link DefaultQuery}s, and a
     * {@link MembershipOracle}.
     *
     * @param <A> the automaton type
     * @param <I> the input type
     * @param <D> the output type
     */
    public abstract static class AbstractDefaultBFOracle<A extends SimpleDTS<?, I>, I, D>
            extends AbstractBreadthFirstOracle<A, I, D, DefaultQuery<I, D>>
            implements DefaultOracle<A, I, D> {

        /**
         * The {@link MembershipOracle} used to answer {@link DefaultQuery}s.
         */
        private final MembershipOracle<I, D> membershipOracle;

        /**
         * Constructs a new AbstractDefaultBFOracle.
         *
         * @param maxWords the maximum number of words to generate.
         * @param membershipOracle the membership oracle to answer {@link DefaultQuery}s.
         */
        protected AbstractDefaultBFOracle(int maxWords, MembershipOracle<I, D> membershipOracle) {
            super(maxWords);
            this.membershipOracle = membershipOracle;
        }

        /**
         * Returns the membership oracle used to answer {@link DefaultQuery}s.
         *
         * @return the MembershipOracle.
         */
        @Override
        public MembershipOracle<I, D> getMembershipOracle() {
            return membershipOracle;
        }
    }
}
