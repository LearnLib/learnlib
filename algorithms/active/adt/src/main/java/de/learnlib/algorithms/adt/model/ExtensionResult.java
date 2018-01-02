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
package de.learnlib.algorithms.adt.model;

import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.algorithms.adt.adt.ADTNode;
import de.learnlib.algorithms.adt.api.ADTExtender;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.words.Word;

/**
 * A class that describes the possible result a {@link ADTExtender} can return. Extending a parent trace can either <ul>
 * <li> yield a counterexample, if the output of the parent trace does not match the output of the hypothesis states of
 * the leaves </li> <li> yield a valid replacement for the temporary splitter </li> <li> yield no result, if the
 * referenced hypothesis states cannot be distinguished by means of extending the parent trace </li> </ul>
 *
 * @param <S>
 *         (hypothesis) state type
 * @param <I>
 *         input alphabet type
 * @param <O>
 *         output alphabet type
 *
 * @author frohme
 */
@ParametersAreNonnullByDefault
public class ExtensionResult<S, I, O> {

    private static final ExtensionResult<?, ?, ?> EMPTY = new ExtensionResult();

    private final DefaultQuery<I, Word<O>> counterExample;

    private final ADTNode<S, I, O> replacement;

    private ExtensionResult() {
        this.replacement = null;
        this.counterExample = null;
    }

    public ExtensionResult(final ADTNode<S, I, O> ext) {
        this.replacement = ext;
        this.counterExample = null;
    }

    public ExtensionResult(final DefaultQuery<I, Word<O>> ce) {
        this.replacement = null;
        this.counterExample = ce;
    }

    /**
     * Utility method, returning the (singleton) object indicating, no result could be computed.
     *
     * @param <S>
     *         (hypothesis) state type
     * @param <I>
     *         input alphabet type
     * @param <O>
     *         output alphabet type
     *
     * @return the empty result
     */
    @SuppressWarnings("unchecked")
    public static <S, I, O> ExtensionResult<S, I, O> empty() {
        return (ExtensionResult<S, I, O>) EMPTY;
    }

    /**
     * Utility method, indicating the search for an extension has revealed a counterexample.
     *
     * @return {@code true} if a counter example was found, {@code false} otherwise
     */
    public boolean isCounterExample() {
        return this.counterExample != null;
    }

    /**
     * Return the found counterexample.
     *
     * @return the counterexample
     */
    public DefaultQuery<I, Word<O>> getCounterExample() {
        return counterExample;
    }

    /**
     * Utility method, indicating if the search for an extending replacement was a success.
     *
     * @return {@code true} if a replacement was found, {@code false} otherwise
     */
    public boolean isReplacement() {
        return this.replacement != null;
    }

    /**
     * Return the proposed replacement.
     *
     * @return the replacement
     */
    public ADTNode<S, I, O> getReplacement() {
        return replacement;
    }
}
