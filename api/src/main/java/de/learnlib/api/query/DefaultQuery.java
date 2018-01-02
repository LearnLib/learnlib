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

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.words.Word;

/**
 * A query is a container for tests a learning algorithms performs, containing the actual test and the corresponding
 * result.
 *
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 *
 * @author Maik Merten
 */
@ParametersAreNonnullByDefault
public class DefaultQuery<I, D> extends AbstractQuery<I, D> {

    private D output;

    public DefaultQuery(Word<I> prefix, Word<I> suffix, @Nullable D output) {
        this(prefix, suffix);
        this.output = output;
    }

    public DefaultQuery(Word<I> prefix, Word<I> suffix) {
        super(prefix, suffix);
    }

    public DefaultQuery(Word<I> input) {
        super(input);
    }

    public DefaultQuery(Word<I> input, @Nullable D output) {
        super(input);
        this.output = output;
    }

    public DefaultQuery(Query<I, ?> query) {
        super(query);
    }

    @Nullable
    public D getOutput() {
        return output;
    }

    /**
     * Checks if the query is normalized, i.e., if all the information is stored in the {@link #getSuffix() suffix} part
     * of the counterexample.
     *
     * @return {@code true} if the counterexample is normalized, {@code false} otherwise.
     */
    public boolean isNormalized() {
        return prefix.isEmpty();
    }

    @Override
    public void answer(@Nullable D output) {
        this.output = output;
    }

    /**
     * @see AbstractQuery#toStringWithAnswer(Object)
     */
    @Override
    public String toString() {
        return toStringWithAnswer(output);
    }

}
