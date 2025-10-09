package de.learnlib.algorithm.lstar.mmlt.cex;


import net.automatalib.alphabet.time.mmlt.LocalTimerMealySemanticInputSymbol;
import net.automatalib.automaton.time.mmlt.semantics.LocalTimerMealyConfiguration;
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
record ExtendedDecomposition<S, I, O>(LocalTimerMealyConfiguration<S, I, O> state,
                                      @NonNull LocalTimerMealySemanticInputSymbol<I> input,
                                      @Nullable Word<LocalTimerMealySemanticInputSymbol<I>> discriminator) {

    public ExtendedDecomposition(LocalTimerMealyConfiguration<S, I, O> state, @NonNull LocalTimerMealySemanticInputSymbol<I> input) {
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
