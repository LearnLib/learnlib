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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.api.ObservableSUL;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

/**
 * A query that contains information about infinite words.
 * <p>
 * In addition to the behavior of {@link DefaultQuery}, an {@link OmegaQuery} contains information about which states
 * have been visited. So that an oracle can decide whether an infinite word is in a language or not.
 * <p>
 * States that are recorded are those after every applied {@link #getLoop()} and the state after {@link #getPrefix()}.
 * States that are recorded can be obtained with {@link #getStates()}
 * <p>
 * Invariant: {@code ({@link #getStates()}.isEmpty()) == ({@link #getOutput()} == null)}.
 * <p>
 * Every constructor in this class accepts an integer, that indicates how often {@link #getLoop()} should be applied.
 * The integer should be greater than zero.
 * <p>
 * Answering this query with output is done obviously via {@link #answer(Object)}, but additionally one has to call
 * {@link #setStates(List)} to satisfy the invariant.
 *
 * @param <S>
 *         the state type
 * @param <I>
 *         the input type
 * @param <D>
 *         the output type
 *
 * @see DefaultQuery
 * @see Query
 * @see ObservableSUL#getState()
 */
@ParametersAreNonnullByDefault
public final class OmegaQuery<S, I, D> {

    private final List<S> states;

    private final Word<I> prefix;

    private final Word<I> loop;

    private final int repeat;

    private D output;

    public OmegaQuery(Word<I> prefix, Word<I> loop, int repeat, @Nullable D output, @Nullable List<S> states) {
        assert repeat > 0;
        this.prefix = prefix;
        this.loop = loop;
        this.repeat = repeat;
        this.output = output;
        this.states = states;
    }

    public OmegaQuery(Word<I> prefix, Word<I> loop, int repeat) {
        this(prefix, loop, repeat, null, new ArrayList<>());
    }

    public void addState(S state) {
        states.add(state);
    }

    public void setStates(List<S> states) {
        this.states.addAll(states);
    }

    public void answer(D output) {
        this.output = output;
    }

    public Word<I> getPrefix() {
        return prefix;
    }

    public Word<I> getLoop() {
        return loop;
    }

    @Nullable
    public D getOutput() {
        return output;
    }

    public List<S> getStates() {
        return states;
    }

    public int getRepeat() {
        return repeat;
    }

    private static <S, I, D> String toString(Word<I> prefix, Word<I> loop, int repeat, D output, List<S> states) {
        return "OmegaQuery[" + prefix + ".(" + loop + ")^" + repeat + " / " + output + ", " + states + ']';
    }

    @Override
    public String toString() {
        return toString(prefix, loop, repeat, output, states);
    }

    public DefaultQuery<I, D> asDefaultQuery() {
        final WordBuilder<I> wb = new WordBuilder<>(prefix.length() + loop.length() * repeat);
        wb.append(prefix);
        wb.repeatAppend(repeat, loop);
        return new DefaultQuery<I, D>(wb.toWord(), output) {

            @Override
            public String toString() {
                return OmegaQuery.toString(OmegaQuery.this.prefix,
                                           OmegaQuery.this.loop,
                                           OmegaQuery.this.repeat,
                                           output,
                                           states);
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OmegaQuery<?, ?, ?> that = (OmegaQuery<?, ?, ?>) o;
        return repeat == that.repeat && Objects.equals(states, that.states) && Objects.equals(prefix, that.prefix) &&
               Objects.equals(loop, that.loop) && Objects.equals(output, that.output);
    }

    @Override
    public int hashCode() {
        return Objects.hash(states, prefix, loop, repeat, output);
    }
}

