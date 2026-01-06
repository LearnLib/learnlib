/* Copyright (C) 2013-2026 TU Dortmund University
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
package de.learnlib.algorithm.lstar.mmlt.cex.results;

import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimedInput;

/**
 * There should be a local reset at the specified transition.
 *
 * @param <I>
 *         input symbol type (of non-delaying inputs)
 * @param <O>
 *         output symbol type
 */
public class MissingResetResult<I, O> implements CexAnalysisResult<I, O> {

    private final Integer location;
    private final InputSymbol<I> input;

    public MissingResetResult(Integer location, InputSymbol<I> input) {
        this.location = location;
        this.input = input;
    }

    public Integer getLocation() {
        return location;
    }

    public TimedInput<I> getInput() {
        return input;
    }
}
