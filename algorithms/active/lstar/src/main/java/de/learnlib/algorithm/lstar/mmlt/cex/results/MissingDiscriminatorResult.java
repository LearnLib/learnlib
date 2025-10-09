package de.learnlib.algorithm.lstar.mmlt.cex.results;

import net.automatalib.alphabet.time.mmlt.LocalTimerMealySemanticInputSymbol;
import net.automatalib.word.Word;

/**
 * The target at the identified transition is incorrect due to a missing discriminator.
 *
 * @param <S> Location type
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class MissingDiscriminatorResult<S, I, O> extends CexAnalysisResult<S, I, O> {
    private final S location;
    private final LocalTimerMealySemanticInputSymbol<I> input;
    private final Word<LocalTimerMealySemanticInputSymbol<I>> discriminator;

    public MissingDiscriminatorResult(S location, LocalTimerMealySemanticInputSymbol<I> input, Word<LocalTimerMealySemanticInputSymbol<I>> discriminator) {
        this.location = location;
        this.input = input;
        this.discriminator = discriminator;
    }

    public S getLocation() {
        return location;
    }

    public LocalTimerMealySemanticInputSymbol<I> getInput() {
        return input;
    }

    public Word<LocalTimerMealySemanticInputSymbol<I>> getDiscriminator() {
        return discriminator;
    }

}
