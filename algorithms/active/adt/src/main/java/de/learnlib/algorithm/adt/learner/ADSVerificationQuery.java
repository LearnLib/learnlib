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
package de.learnlib.algorithm.adt.learner;

import java.util.Objects;

import de.learnlib.algorithm.adt.automaton.ADTState;
import de.learnlib.query.AdaptiveQuery;
import de.learnlib.query.DefaultQuery;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Utility class to verify ADSs. This query tracks the current ADT node for the given inputs and compares it with an
 * expected output, potentially constructing a counterexample from the observed data.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
class ADSVerificationQuery<I, O> implements AdaptiveQuery<I, O> {

    private final Word<I> prefix;
    private final Word<I> suffix;
    private final Word<O> expectedOutput;
    private final WordBuilder<O> outputBuilder;
    private final ADTState<I, O> state;

    private final int prefixLength;
    private final int suffixLength;
    private int idx;
    private @Nullable DefaultQuery<I, Word<O>> counterexample;

    ADSVerificationQuery(Word<I> prefix, Word<I> suffix, Word<O> expectedSuffixOutput, ADTState<I, O> state) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.expectedOutput = expectedSuffixOutput;
        this.outputBuilder = new WordBuilder<>(suffix.size());
        this.state = state;

        this.prefixLength = prefix.length();
        this.suffixLength = suffix.length();
        this.idx = 0;
    }

    @Override
    public I getInput() {
        if (idx < prefixLength) {
            return prefix.getSymbol(idx);
        } else {
            return suffix.getSymbol(idx - prefixLength);
        }
    }

    @Override
    public Response processOutput(O out) {
        if (idx < prefixLength) {
            idx++;
            return Response.SYMBOL;
        } else {
            outputBuilder.append(out);

            if (!Objects.equals(out, expectedOutput.getSymbol(idx - prefixLength))) {
                counterexample =
                        new DefaultQuery<>(prefix, suffix.prefix(outputBuilder.size()), outputBuilder.toWord());
                return Response.FINISHED;
            } else if (outputBuilder.size() < suffixLength) {
                idx++;
                return Response.SYMBOL;
            } else {
                return Response.FINISHED;
            }
        }
    }

    @Nullable
    DefaultQuery<I, Word<O>> getCounterexample() {
        return counterexample;
    }

    ADTState<I, O> getState() {
        return state;
    }

    Word<I> getSuffix() {
        return suffix;
    }

    Word<O> getExpectedOutput() {
        return expectedOutput;
    }
}
