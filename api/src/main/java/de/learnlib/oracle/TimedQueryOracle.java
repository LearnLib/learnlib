package de.learnlib.oracle;

import java.util.List;

import net.automatalib.automaton.mmlt.MealyTimerInfo;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.word.Word;

/**
 * Type of oracle used by an MMLT learner.
 * <p>
 * Like a traditional query oracle, this answers output queries.
 * In addition, it infers timers by observing timeouts.
 *
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public interface TimedQueryOracle<I, O> extends MembershipOracle.MealyMembershipOracle<TimedInput<I>, TimedOutput<O>> {

    /**
     * Observes and aggregates any timeouts that occur after providing the given input to the SUL.
     * Stops when observing inconsistent behavior.
     *
     * @param prefix              Input to give to the SUL.
     * @param maxTotalWaitingTime Maximum total time that is waited for timeouts.
     * @return Observed timeouts. Empty, if none.
     */
    TimerQueryResult<O> queryTimers(Word<TimedInput<I>> prefix, long maxTotalWaitingTime);

    /**
     * Response for a timer query.
     *
     * @param aborted True if query was aborted due to missing timeout.
     * @param timers  Identified timers
     * @param <O>     Untimed output suffix type
     */
    record TimerQueryResult<O>(boolean aborted, List<MealyTimerInfo<?, O>> timers) {}

}
