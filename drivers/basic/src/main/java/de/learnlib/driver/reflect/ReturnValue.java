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
package de.learnlib.driver.reflect;

import java.util.Objects;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A (non-empty) output of a method call.
 *
 * @param <T>
 *         return value type
 */
public final class ReturnValue<T> extends MethodOutput {

    private final T ret;

    public ReturnValue(T ret) {
        this.ret = ret;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.ret);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ReturnValue)) {
            return false;
        }

        final ReturnValue<?> other = (ReturnValue<?>) obj;
        return Objects.equals(this.ret, other.ret);
    }

    @Override
    public String toString() {
        return Objects.toString(this.ret);
    }

    public T getValue() {
        return ret;
    }

}
