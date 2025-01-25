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
package de.learnlib.algorithm.lsharp;

public class LSState implements Comparable<LSState> {
    private final int base;

    public LSState(int base) {
        this.base = base;
    }

    public int raw() {
        return this.base;
    }

    @Override
    public int compareTo(LSState to) {
        return Integer.compare(this.base, to.raw());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof LSState)) {
            return false;
        }

        LSState state = (LSState) o;

        return this.base == state.base;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(base);
    }
}
