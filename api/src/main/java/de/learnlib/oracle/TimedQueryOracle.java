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
package de.learnlib.oracle;

import java.util.List;

import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import net.automatalib.automaton.mmlt.TimerInfo;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.word.Word;

/**
 * An oracle for querying {@link TimedInput timed inputs} and timers by observing timeouts.
 *
 * @param <I>
 *         input symbol type (of non-delaying inputs)
 * @param <O>
 *         output symbol type
 */
public interface TimedQueryOracle<I, O> extends MealyMembershipOracle<TimedInput<I>, TimedOutput<O>> {

    /**
     * Observes and aggregates any timeouts that occur after providing the given input to the SUL. Stops when observing
     * inconsistent behavior.
     *
     * @param prefix
     *         the input to give to the SUL
     * @param maxTotalWaitingTime
     *         the maximum total time that is waited for timeouts
     *
     * @return observed timeouts (may be empty)
     */
    TimerQueryResult<O> queryTimers(Word<TimedInput<I>> prefix, long maxTotalWaitingTime);

    /**
     * Response for a timer query.
     *
     * @param aborted
     *         {@code true} if query was aborted due to missing timeout, {@code false} otherwise.
     * @param timers
     *         the identified timers
     * @param <O>
     *         output symbol type
     */
    record TimerQueryResult<O>(boolean aborted, List<TimerInfo<?, O>> timers) {}

}
