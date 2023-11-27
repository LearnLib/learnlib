/* Copyright (C) 2013-2023 TU Dortmund
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

package de.learnlib.algorithm.lsharp.ads;

import java.util.List;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.Nullable;

public class SepSeq<T> {
    enum Status {
        INJ, NONINJ
    }

    public final List<T> seq;
    public final @Nullable Status status;

    public SepSeq(Status status, List<T> seq) {
        this.seq = seq;
        this.status = status;
    }

    public Boolean isInjective() {
        if (this.status == null) {
            return false;
        }

        return this.status.compareTo(Status.INJ) == 0;
    }

    public Boolean isSet() {
        return this.status != null;
    }

    public List<T> get() {
        return this.seq;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof SepSeq<?>) {
            SepSeq<?> casted = (SepSeq<?>) other;
            return this.seq.equals(casted.seq) && this.status.equals(casted.status);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(seq, status);
    }
}
