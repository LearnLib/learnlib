package de.learnlib.testsupport.example.mmlt;

import de.learnlib.algorithm.LocalTimerMealyModelParams;
import net.automatalib.automaton.time.mmlt.LocalTimerMealy;

/**
 * Convenience class for storing a name, an automaton and model parameters.
 *
 * @param name      Automaton name
 * @param automaton MMLT
 * @param params    Model parameters
 * @param <S>       Location type
 * @param <I>       Input type for non-delaying inputs
 * @param <O>       Output symbol type
 */
public record LocalTimerMealyModel<S, I, O>(String name,
                                            LocalTimerMealy<S, I, O> automaton,
                                            LocalTimerMealyModelParams<O> params) {

}
