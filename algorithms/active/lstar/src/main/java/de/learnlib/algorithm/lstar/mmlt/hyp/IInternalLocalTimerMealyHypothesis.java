package de.learnlib.algorithm.lstar.mmlt.hyp;

import net.automatalib.symbol.time.TimedInput;
import net.automatalib.automaton.mmlt.MealyTimerInfo;
import net.automatalib.automaton.mmlt.State;
import net.automatalib.word.Word;

import java.util.List;

/**
 * Defines several methods that the learner can use to interact with its hypothesis.
 * These methods should not be used by the teacher, to maintain separation between both.
 *
 * @param <S> Location type
 * @param <I> Input type for non-delaying inputs
 */
public interface IInternalLocalTimerMealyHypothesis<S, I, O> {

    /**
     * Returns the prefix assigned to the provided configuration.
     * The assigned prefix is the concatenation of the prefix assigned to the active location
     * and the minimal number of time steps needed to reach the configuration after entering its location
     * (= entry distance).
     *
     * @param configuration Considered configuration
     * @return Assigned prefix
     */
    Word<TimedInput<I>> getPrefix(State<S, O> configuration);

    Word<TimedInput<I>> getPrefix(Word<TimedInput<I>> prefix);

    /**
     * Returns the prefix assigned to the location that is active in the provided configuration.
     *
     * @param configuration Considered configuration
     * @return Assigned prefix
     */
    Word<TimedInput<I>> getLocationPrefix(State<S, O> configuration);

    /**
     * Returns a prefix for the given location.
     * This prefix is deterministic in the RS learner.
     *
     * @param location Location
     * @return Location prefix
     */
    Word<TimedInput<I>> getPrefix(S location);

    /**
     * Convenience method that sorts timers of the provided location by initial value.
     *
     * @return Sorted timers. Empty list if no timers.
     */
//    List<MealyTimerInfo<O>> getSortedTimers(S location);
}
