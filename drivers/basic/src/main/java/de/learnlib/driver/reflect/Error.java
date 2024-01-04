/* Copyright (C) 2013-2024 TU Dortmund University
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
package de.learnlib.driver.reflect;

import java.util.Objects;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A wrapper for representing an exception-based method output via its {@link Class#getSimpleName()}.
 */
public final class Error extends MethodOutput {

    private final Throwable cause;
    private final String id;

    public Error(Throwable cause) {
        this.cause = cause;
        this.id = cause.getClass().getSimpleName();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Error)) {
            return false;
        }

        final Error other = (Error) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {
        return this.id;
    }

    public Throwable getCause() {
        return cause;
    }
}
