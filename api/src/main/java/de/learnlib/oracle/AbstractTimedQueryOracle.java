package de.learnlib.oracle;

import de.learnlib.query.DefaultQuery;
import de.learnlib.query.Query;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.automaton.mmlt.MealyTimerInfo;
import net.automatalib.word.Word;

import java.util.Collection;
import java.util.List;

/**
 * Type of oracle used by an MMLT learner.
 * <p>
 * Like a traditional query oracle, this answers output queries.
 * In addition, it infers timers by observing timeouts.
 *
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public abstract class AbstractTimedQueryOracle<I, O> implements MembershipOracle.MealyMembershipOracle<TimedInput<I>, TimedOutput<O>> {

    /**
     * Response for a timer query.
     *
     * @param aborted True if query was aborted due to missing timeout.
     * @param timers  Identified timers
     * @param <O>     Untimed output suffix type
     */
    public record TimerQueryResult<O>(boolean aborted, List<MealyTimerInfo<?, O>> timers) {

    }

    @Override
    public void processQueries(Collection<? extends Query<TimedInput<I>, Word<TimedOutput<O>>>> collection) {
        for (var q : collection) {
            DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> query = new DefaultQuery<>(q.getPrefix(), q.getSuffix());
            this.querySuffixOutput(query);
            q.answer(query.getOutput());
        }
    }

    /**
     * Observes and aggregates any timeouts that occur after providing the given input to the SUL.
     * Stops when observing inconsistent behavior.
     *
     * @param prefix              Input to give to the SUL.
     * @param maxTotalWaitingTime Maximum total time that is waited for timeouts.
     * @return Observed timeouts. Empty, if none.
     */
    public abstract TimerQueryResult<O> queryTimers(Word<TimedInput<I>> prefix, long maxTotalWaitingTime);

    /**
     * Queries the suffix output for the provided query.
     *
     * @param query Input query.
     */
    public void querySuffixOutput(DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> query) {
        this.querySuffixOutputInternal(query);
    }

    /**
     * Like querySuffixOutput but does not require a query object.
     *
     * @param prefix Prefix
     * @param suffix Suffix
     */
    public final Word<TimedOutput<O>> querySuffixOutput(Word<TimedInput<I>> prefix, Word<TimedInput<I>> suffix) {
        DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> query = new DefaultQuery<>(prefix, suffix);
        this.querySuffixOutputInternal(query);
        return query.getOutput();
    }

    /**
     * Queries the output for the provided input sequence.
     *
     * @param query Input query.
     */
    protected abstract void querySuffixOutputInternal(DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> query);

}
