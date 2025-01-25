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
package de.learnlib.example.aaar;

import java.util.Objects;

import org.checkerframework.checker.nullness.qual.Nullable;

class Event {

    static final class Msg<D> extends Event {

        final int seq;
        final D data;

        Msg(int seq, D data) {
            this.seq = seq;
            this.data = data;
        }

        @Override
        public String toString() {
            return "msg(" + seq + ',' + data + ')';
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (!(o instanceof Msg)) {
                return false;
            }
            final Msg<?> that = (Msg<?>) o;

            return this.seq == that.seq && Objects.equals(this.data, that.data);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.seq, this.data);
        }
    }

    static final class Recv extends Event {

        @Override
        public String toString() {
            return "recv";
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            return obj instanceof Recv;
        }
    }
}
