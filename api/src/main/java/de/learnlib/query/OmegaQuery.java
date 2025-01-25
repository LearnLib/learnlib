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

import java.util.Objects;

import de.learnlib.sul.ObservableSUL;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A query that represents information about infinite words in an ultimately periodic pattern. That is, for two finite
 * strings <i>u</i>, <i>v</i>, this class represents the query of the infinite word <i>uv<sup>ω</sup></i>.
 * <p>
 * When answering OmegaQueries, one needs to specify the periodicity <i>p</i> of the looping suffix <i>v</i>, i.e. for
 * what <i>p</i> the answer contains information about the response to the query <i>uv<sup>p</sup></i> which can then
 * be generalized to the infinite case since <i>u(v<sup>p</sup>)<sup>ω</sup></i> = <i>uv<sup>ω</sup></i>.
 * <p>
 * If one cannot determine this value (e.g. because the response exhibits a non-periodic pattern), one may specify a
 * negative value for <i>p</i>. {@link #isUltimatelyPeriodic()} then consequently returns {@code false}. In this case
 * the output of the query ({@link #getOutput()}) may be undefined.
 *
 * @param <I>
 *         the input type
 * @param <D>
 *         the output type
 *
 * @see DefaultQuery
 * @see Query
 * @see ObservableSUL#getState()
 */
public class OmegaQuery<I, D> {

    private final Word<I> prefix;
    private final Word<I> loop;
    private final int repeat;

    private @Nullable D output;
    private int periodicity;

    public OmegaQuery(Word<I> prefix, Word<I> loop, int repeat) {
        this.prefix = prefix;
        this.loop = loop;
        this.repeat = repeat;
    }

    public void answer(@Nullable D output, int periodicity) {
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

    public @Nullable D getOutput() {
        return output;
    }

    public int getPeriodicity() {
        return periodicity;
    }

    public boolean isUltimatelyPeriodic() {
        return periodicity > 0;
    }

    public DefaultQuery<I, @Nullable D> asDefaultQuery() {
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
    public final boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OmegaQuery)) {
            return false;
        }

        final OmegaQuery<?, ?> that = (OmegaQuery<?, ?>) o;
        return periodicity == that.periodicity && Objects.equals(prefix, that.prefix) &&
               Objects.equals(loop, that.loop) && Objects.equals(output, that.output);
    }

    @Override
    public final int hashCode() {
        int result = 1;
        result = 31 * result + Objects.hashCode(prefix);
        result = 31 * result + Objects.hashCode(loop);
        result = 31 * result + Objects.hashCode(output);
        result = 31 * result + Integer.hashCode(periodicity);
        return result;
    }
}

