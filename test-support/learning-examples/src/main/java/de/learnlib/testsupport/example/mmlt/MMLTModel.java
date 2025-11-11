package de.learnlib.testsupport.example.mmlt;

import de.learnlib.algorithm.MMLTModelParams;
import net.automatalib.automaton.mmlt.MMLT;

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
public record MMLTModel<S, I, T, O>(String name,
                                    MMLT<S, I, T, O> automaton,
                                    MMLTModelParams<O> params) {

}
