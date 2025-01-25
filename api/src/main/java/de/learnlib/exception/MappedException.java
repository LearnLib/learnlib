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
package de.learnlib.exception;

import java.util.Optional;

import de.learnlib.sul.SUL;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A mapped exception allows one to gracefully handle exceptions thrown, e.g., during a
 * {@link SUL#step(Object) SUL's step method} by providing information about the output symbol that should be emitted
 * instead and subsequent outputs.
 *
 * @param <O>
 *         output symbol type
 */
public final class MappedException<O> {

    private final O thisStepOutput;
    private final @Nullable O subsequentStepsOutput;

    private MappedException(O output) {
        this(output, null);
    }

    private MappedException(O thisStepOutput, @Nullable O subsequentStepsOutput) {
        this.thisStepOutput = thisStepOutput;
        this.subsequentStepsOutput = subsequentStepsOutput;
    }

    public static <O> MappedException<O> ignoreAndContinue(O output) {
        return new MappedException<>(output);
    }

    public static <O> MappedException<O> repeatOutput(O output) {
        return repeatOutput(output, output);
    }

    public static <O> MappedException<O> repeatOutput(O thisStepOutput, O subsequentOutput) {
        return new MappedException<>(thisStepOutput, subsequentOutput);
    }

    public static <O> MappedException<O> pass(SULException exception) {
        throw exception;
    }

    public O getThisStepOutput() {
        return thisStepOutput;
    }

    public Optional<O> getSubsequentStepsOutput() {
        return Optional.ofNullable(subsequentStepsOutput);
    }
}
