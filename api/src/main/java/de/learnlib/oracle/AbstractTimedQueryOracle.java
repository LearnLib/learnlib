package de.learnlib.oracle;

import de.learnlib.query.DefaultQuery;
import de.learnlib.query.Query;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealySemanticInputSymbol;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealyOutputSymbol;
import net.automatalib.automaton.time.mmlt.MealyTimerInfo;
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
public abstract class AbstractTimedQueryOracle<I, O> implements MembershipOracle.MealyMembershipOracle<LocalTimerMealySemanticInputSymbol<I>, LocalTimerMealyOutputSymbol<O>> {

    /**
     * Response for a timer query.
     *
     * @param aborted True if query was aborted due to missing timeout.
     * @param timers  Identified timers
     * @param <O>     Untimed output suffix type
     */
    public record TimerQueryResult<O>(boolean aborted, List<MealyTimerInfo<O>> timers) {

    }

    @Override
    public void processQueries(Collection<? extends Query<LocalTimerMealySemanticInputSymbol<I>, Word<LocalTimerMealyOutputSymbol<O>>>> collection) {
        for (var q : collection) {
            DefaultQuery<LocalTimerMealySemanticInputSymbol<I>, Word<LocalTimerMealyOutputSymbol<O>>> query = new DefaultQuery<>(q.getPrefix(), q.getSuffix());
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
    public abstract TimerQueryResult<O> queryTimers(Word<LocalTimerMealySemanticInputSymbol<I>> prefix, long maxTotalWaitingTime);

    /**
     * Queries the suffix output for the provided query.
     *
     * @param query Input query.
     */
    public void querySuffixOutput(DefaultQuery<LocalTimerMealySemanticInputSymbol<I>, Word<LocalTimerMealyOutputSymbol<O>>> query) {
        this.querySuffixOutputInternal(query);
    }

    /**
     * Like querySuffixOutput but does not require a query object.
     *
     * @param prefix Prefix
     * @param suffix Suffix
     */
    public final Word<LocalTimerMealyOutputSymbol<O>> querySuffixOutput(Word<LocalTimerMealySemanticInputSymbol<I>> prefix, Word<LocalTimerMealySemanticInputSymbol<I>> suffix) {
        DefaultQuery<LocalTimerMealySemanticInputSymbol<I>, Word<LocalTimerMealyOutputSymbol<O>>> query = new DefaultQuery<>(prefix, suffix);
        this.querySuffixOutputInternal(query);
        return query.getOutput();
    }

    /**
     * Queries the output for the provided input sequence.
     *
     * @param query Input query.
     */
    protected abstract void querySuffixOutputInternal(DefaultQuery<LocalTimerMealySemanticInputSymbol<I>, Word<LocalTimerMealyOutputSymbol<O>>> query);

}
