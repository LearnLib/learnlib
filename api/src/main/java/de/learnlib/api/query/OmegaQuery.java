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
package de.learnlib.api.query;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import de.learnlib.api.ObservableSUL;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.Word;

/**
 * A query that contains information about infinite words.
 *
 * In addition to the behavior of {@link DefaultQuery}, an {@link OmegaQuery} contains information about which states
 * have been visited. So that an oracle can decide whether an infinite word is in a language or not.
 *
 * States that need to be recorded are given in {@link #getIndices()}. Every index therein is the <i>i</i>th state
 * obtained after the <i>i</i>th symbol. Actual states in {@link #getStates()} correspond to those indices in
 * {@link #getIndices()}.
 *
 * Invariant: {@code ({@link #getStates()} == null) == ({@link #getOutput()} == null)}.
 *
 * Every constructor in this class accepts a set of integers, that can later be retrieved with {@code
 * {@link #getIndices()}}.
 *
 * Answering this query with output is done obviously via {@link DefaultQuery#answer(Object)}, but additionally one
 * has to call {@link #setStates(List)} to satisfy the invariant.
 *
 * @see DefaultQuery
 * @see Query
 * @see ObservableSUL#getState()
 *
 * @param <S> the state type
 * @param <I> the input type
 * @param <D> the output type
 */
public class OmegaQuery<S, I, D> extends DefaultQuery<I, D> {

    private Set<Integer> indices;

    private List<S> states;

    public OmegaQuery(Word<I> prefix, Word<I> suffix, @Nullable D output, Set<Integer> indices) {
        super(prefix, suffix, output);
        this.indices = indices;
    }

    public OmegaQuery(Word<I> prefix, Word<I> suffix, Set<Integer> indices) {
        this(prefix, suffix, null, indices);
    }

    public OmegaQuery(Word<I> input, Set<Integer> indices) {
        this(Word.epsilon(), input, null, indices);
    }

    public OmegaQuery(Word<I> input, @Nullable D output, Set<Integer> indices) {
        this(Word.epsilon(), input, output, indices);
    }

    public OmegaQuery(Query<I, ?> query, Set<Integer> indices) {
        this(query.getPrefix(), query.getSuffix(), null, indices);
    }

    public Pair<D, List<S>> getOutputStates() {
        return Pair.of(getOutput(), states);
    }

    @Override
    public String toString() {
        return "OmegaQuery[" + prefix + '|' + suffix + " / " + getOutput() + ", " + indices + " - " + states + ']';
    }

    public Set<Integer> getIndices() {
        return indices;
    }

    public List<S> getStates() {
        return states;
    }

    public void setStates(List<S> states) {
        this.states = states;
    }
}

