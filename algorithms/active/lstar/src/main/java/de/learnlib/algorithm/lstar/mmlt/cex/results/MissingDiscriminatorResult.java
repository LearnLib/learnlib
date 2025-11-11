package de.learnlib.algorithm.lstar.mmlt.cex.results;

import net.automatalib.symbol.time.TimedInput;
import net.automatalib.word.Word;

/**
 * The target at the identified transition is incorrect due to a missing discriminator.
 *
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class MissingDiscriminatorResult<I, O> extends CexAnalysisResult<I, O> {
    private final Integer location;
    private final TimedInput<I> input;
    private final Word<TimedInput<I>> discriminator;

    public MissingDiscriminatorResult(Integer location, TimedInput<I> input, Word<TimedInput<I>> discriminator) {
        this.location = location;
        this.input = input;
        this.discriminator = discriminator;
    }

    public Integer getLocation() {
        return location;
    }

    public TimedInput<I> getInput() {
        return input;
    }

    public Word<TimedInput<I>> getDiscriminator() {
        return discriminator;
    }

}
