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
package de.learnlib.datastructure.pta;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.Nullable;

public class PTATransition<S extends AbstractBasePTAState<S, ?, ?>> {

    private final S source;
    private final int index;

    PTATransition(S source, @NonNegative int index) {
        this.source = source;
        this.index = index;
    }

    public S getSource() {
        return source;
    }


    public @NonNegative int getIndex() {
        return index;
    }

    public @Nullable S getTarget() {
        return source.getSuccessor(index);
    }

}
