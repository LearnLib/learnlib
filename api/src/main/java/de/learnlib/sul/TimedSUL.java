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
package de.learnlib.sul;

import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimeStepSequence;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.symbol.time.TimeoutSymbol;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Interface for a SUL with MMLT semantics.
 *
 * @param <I>
 *         Input type for non-delaying inputs
 * @param <O>
 *         Output symbol type
 */
public interface TimedSUL<I, O> extends SUL<InputSymbol<I>, TimedOutput<O>> {

    /**
     * Follows the provided input word, starting at the current system state. The input word must not contain timeout
     * symbols. Otherwise, an error occurs.
     *
     * @param input
     *         the input word
     */
    default void follow(Word<TimedInput<I>> input) {
        this.follow(input, -1);
    }

    /**
     * Follows the provided input word, starting at the current configuration.
     *
     * @param input
     *         the input word
     * @param maxTimeout
     *         maximum waiting time to use for {@link TimeoutSymbol}s.
     */
    default void follow(Word<TimedInput<I>> input, long maxTimeout) {
        for (TimedInput<I> s : input) {
            if (s instanceof InputSymbol<I> ndi) {
                this.step(ndi);
            } else if (s instanceof TimeStepSequence<I>) {
                this.collectTimeouts((TimeStepSequence<I>) s);
            } else if (s instanceof TimeoutSymbol<I>) {
                if (maxTimeout <= 0) {
                    throw new IllegalArgumentException("Must supply timeout when using timeout symbols.");
                }
                this.timeoutStep(maxTimeout);
            } else {
                throw new IllegalArgumentException("Unknown suffix type.");
            }
        }
    }

    /**
     * Waits until a timeout occurs or the provided time is reached. May observe no timeout if either the waiting time
     * is too small or if the active location has no timers.
     *
     * @param maxTime
     *         maximum waiting time.
     *
     * @return observed timer output with waiting time, or {@code null} if no timeout was observed.
     */
    @Nullable TimedOutput<O> timeoutStep(long maxTime);

    /**
     * Waits for one time unit and returns the observed output.
     *
     * @return {@code null} if no output occurred, or a timer output if at least one timer expired. The delay of this
     * output is set to zero.
     */

    default @Nullable TimedOutput<O> timeStep() {
        TimedOutput<O> res = this.timeoutStep(1);
        if (res != null) {
            return new TimedOutput<>(res.symbol());
        }
        return null;
    }

    /**
     * Waits for the specified time and returns all observed timeouts.
     *
     * @param input
     *         Waiting time.
     *
     * @return Observed timeouts. Empty, if none.
     */
    default Word<TimedOutput<O>> collectTimeouts(TimeStepSequence<I> input) {
        WordBuilder<TimedOutput<O>> wbOutput = new WordBuilder<>();

        long remainingTime = input.timeSteps();
        while (remainingTime > 0) {
            TimedOutput<O> nextTimeout = this.timeoutStep(remainingTime);
            if (nextTimeout == null) {
                // No timer will expire during remaining waiting time:
                break;
            } else {
                wbOutput.append(nextTimeout);
                remainingTime -= nextTimeout.delay();
            }
        }

        return wbOutput.toWord();
    }

    @Override
    default TimedSUL<I, O> fork() {
        throw new UnsupportedOperationException();
    }
}
