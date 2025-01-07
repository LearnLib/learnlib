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
package de.learnlib.query;

/**
 * An adaptive query is a variation of the (regular) {@link Query} that allows one to dynamically select the symbols to
 * query based on responses to previous symbols.
 * <p>
 * After {@link #getInput() fetching} the current input symbol that should be evaluated on the system under learning,
 * its respective output must be {@link #processOutput(Object) processed} by the query object in order to determine the
 * next action to make. This essentially establishes a symbol-wise dialogue between the query object and the system
 * under learning and makes adaptive queries inherently stateful.
 * <p>
 * For the semantics of this conversation, see the different {@link Response} values and their respective
 * documentation.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
public interface AdaptiveQuery<I, O> {

    /**
     * Returns the current input symbol that should be evaluated on the system under learning.
     *
     * @return the current input symbol
     */
    I getInput();

    /**
     * Processes the output of the system under learning to the latest returned input symbol.
     *
     * @param out
     *         the output of the system under learning
     *
     * @return the next action to make
     */
    Response processOutput(O out);

    /**
     * The different types of responses when processing outputs from the system under learning.
     */
    enum Response {
        /**
         * Indicates that the query is finished and no more symbols should be processed. After returning this value, the
         * behavior of {@link #getInput()} is undefined and may as well throw an exception.
         */
        FINISHED,
        /**
         * Indicates that the system under learning should be reset to its initial state. After returning this value,
         * {@link #getInput()} must return the next input symbol that should be queried after the reset.
         */
        RESET,
        /**
         * Indicates that further symbols follow. After returning this value, {@link #getInput()} must return the next
         * input symbol that should be queried.
         */
        SYMBOL
    }
}
