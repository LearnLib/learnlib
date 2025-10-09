package de.learnlib.sul;

import net.automatalib.alphabet.time.mmlt.*;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A SUL with MMLT semantics. We use this type to interface with real systems and to
 * simulate MMLT models.
 *
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public abstract class LocalTimerMealySUL<I, O> {

    /**
     * Follows the provided input word, starting at the current system state.
     * The input word must not contain timeout symbols. Otherwise, an error occurs.
     *
     * @param input Input suffix.
     */
    public void follow(Word<LocalTimerMealySemanticInputSymbol<I>> input) {
        this.follow(input, -1);
    }

    /**
     * Follows the provided input word, starting at the current configuration.
     *
     * @param input      Input suffix.
     * @param maxTimeout Max. timeout to use for timeoutSymbols.
     */
    public void follow(Word<LocalTimerMealySemanticInputSymbol<I>> input, long maxTimeout) {
        for (var s : input) {
            if (s instanceof NonDelayingInput<I> ndi) {
                this.step(ndi);
            } else if (s instanceof TimeStepSequence<I>) {
                this.collectTimeouts((TimeStepSequence<I>) s);
            } else if (s instanceof TimeoutSymbol<I>) {
                if (maxTimeout <= 0) {
                    throw new IllegalArgumentException("Must supply timeout when using timeout symbols.");
                }
                this.timeoutStep(maxTimeout);
            } else {
                throw new IllegalArgumentException("Unknown suffix type.");
            }
        }
    }

    /**
     * Provides an input to the SUL and returns the observed output.
     *
     * @param input Input
     * @return SUL output.
     */
    public abstract LocalTimerMealyOutputSymbol<O> step(NonDelayingInput<I> input);

    /**
     * Waits until a timeout occurs or the provided time is reached.
     * <p>
     * We may observe no timeout if either the waiting time is too small or there are no timers defined
     * in the current location.
     *
     * @param maxTime Maximum waiting time.
     * @return Observed timer output with waiting time, or null, if no timeout observed.
     */
    @Nullable
    public abstract LocalTimerMealyOutputSymbol<O> timeoutStep(long maxTime);

    /**
     * Waits for one time unit and returns the observed output.
     *
     * @return Null if no output occurred, timer output if at least one timer expired.
     * The delay of this output is set to zero.
     */
    @Nullable
    public LocalTimerMealyOutputSymbol<O> timeStep() {
        var res = this.timeoutStep(1);
        if (res != null) {
            return new LocalTimerMealyOutputSymbol<>(res.getSymbol());
        }
        return null;
    }

    /**
     * Waits for the specified time and returns all observed timeouts.
     *
     * @param input Waiting time.
     * @return Observed timeouts. Empty, if none.
     */
    public Word<LocalTimerMealyOutputSymbol<O>> collectTimeouts(TimeStepSequence<I> input) {
        WordBuilder<LocalTimerMealyOutputSymbol<O>> wbOutput = new WordBuilder<>();

        long remainingTime = input.getTimeSteps();
        while (remainingTime > 0) {
            LocalTimerMealyOutputSymbol<O> nextTimeout = this.timeoutStep(remainingTime);
            if (nextTimeout == null) {
                // No timer will expire during remaining waiting time:
                break;
            } else {
                wbOutput.append(nextTimeout);
                remainingTime -= nextTimeout.getDelay();
            }
        }

        return wbOutput.toWord();
    }


    /**
     * Prepares the SUL for a new query.
     */
    public abstract void pre();

    /**
     * Deinitializes the SUL.
     */
    public abstract void post();
}
