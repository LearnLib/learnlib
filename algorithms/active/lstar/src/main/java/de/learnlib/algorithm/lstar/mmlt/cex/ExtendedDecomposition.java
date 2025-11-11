package de.learnlib.algorithm.lstar.mmlt.cex;


import net.automatalib.symbol.time.TimedInput;
import net.automatalib.automaton.mmlt.State;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An extended decomposition represents a transition with an incorrect target or output in the expanded form of
 * a hypothesis MMLT.
 *
 * @param state         Source state in expanded form of hypothesis
 * @param input         Input of some transition with incorrect output or target source state
 * @param discriminator If not null: transition has incorrect target
 * @param <I>           Input type for non-delaying inputs
 */
record ExtendedDecomposition<I, O>(State<Integer, O> state,
                                   @NonNull TimedInput<I> input,
                                   @Nullable Word<TimedInput<I>> discriminator) {

    public ExtendedDecomposition(State<Integer, O> state, @NonNull TimedInput<I> input) {
        this(state, input, null);
    }

    public boolean isForIncorrectOutput() {
        return this.discriminator == null;
    }

    @Override
    public String toString() {
        if (this.isForIncorrectOutput()) {
            return String.format("Incorrect output (%s|%s)", state, input);
        } else {
            return String.format("Incorrect target (%s|%s|%s)", state, input, discriminator);
        }
    }
}
