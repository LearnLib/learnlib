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
public final class OmegaQuery<I, D> {

    private final Word<I> prefix;
    private final Word<I> loop;
    private final int repeat;

    private D output;
    private int periodicity;

    public OmegaQuery(Word<I> prefix, Word<I> loop, int repeat) {
        this.prefix = prefix;
        this.loop = loop;
        this.repeat = repeat;
    }

    public void answer(D output, int periodicity) {
        this.output = output;
        this.periodicity = periodicity;
    }

    public Word<I> getPrefix() {
        return prefix;
    }

    public Word<I> getLoop() {
        return loop;
    }

    public int getRepeat() {
        return repeat;
    }

    @Nullable
    public D getOutput() {
        return output;
    }

    public int getPeriodicity() {
        return periodicity;
    }

    public boolean isUltimatelyPeriodic() {
        return periodicity > 0;
    }

    public DefaultQuery<I, D> asDefaultQuery() {
        final WordBuilder<I> wb = new WordBuilder<>(prefix.length() + loop.length() * periodicity);
        wb.append(prefix);
        wb.repeatAppend(periodicity, loop);
        return new DefaultQuery<>(wb.toWord(), output);
    }

    @Override
    public String toString() {
        return "OmegaQuery{" + "prefix=" + prefix + ", loop=" + loop + ", repeat=" + repeat + ", output=" + output +
               ", periodicity=" + periodicity + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OmegaQuery)) {
            return false;
        }
        OmegaQuery<?, ?> that = (OmegaQuery<?, ?>) o;
        return periodicity == that.periodicity && Objects.equals(prefix, that.prefix) &&
               Objects.equals(loop, that.loop) && Objects.equals(output, that.output);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prefix, loop, output, periodicity);
    }
}

