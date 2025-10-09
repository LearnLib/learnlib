package de.learnlib.oracle.symbol_filters.mmlt;


import de.learnlib.symbol_filter.SymbolFilter;
import de.learnlib.symbol_filter.SymbolFilterResponse;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealySemanticInputSymbol;
import net.automatalib.alphabet.time.mmlt.NonDelayingInput;
import net.automatalib.automaton.time.mmlt.LocalTimerMealy;
import net.automatalib.word.Word;

import java.util.Random;

/**
 * A symbol filter that falsely answers a query with a specified probability.
 *
 * @param <S> Location type
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class RandomSymbolFilter<S, I, O> implements SymbolFilter<I, O> {

    private final double inaccurateProb;
    private final Random random;
    private final LocalTimerMealy<S, I, O> sulModel;

    public RandomSymbolFilter(LocalTimerMealy<S, I, O> sulModel,
                              double inaccurateProb, Random random) {
        if (inaccurateProb > 1 || inaccurateProb < 0) {
            throw new IllegalArgumentException("Ratios must be between zero and 1 (inclusive).");
        }

        this.inaccurateProb = inaccurateProb;
        this.random = random;

        this.sulModel = sulModel;
    }

    private boolean isSilentSelfLoop(Word<LocalTimerMealySemanticInputSymbol<I>> prefix, NonDelayingInput<I> symbol) {
        var targetConfig = this.sulModel.getSemantics().traceInputs(prefix);
        var trans = this.sulModel.getSemantics().getTransition(targetConfig, symbol);
        return trans.output().equals(sulModel.getSemantics().getSilentOutput()) && targetConfig.equals(trans.target());
    }

    @Override
    public SymbolFilterResponse query(Word<LocalTimerMealySemanticInputSymbol<I>> prefix, NonDelayingInput<I> symbol) {
        // Check if silent self-loop:
        boolean ignorable = isSilentSelfLoop(prefix, symbol);

        // Randomly misclassify:
        if (this.random.nextDouble() <= this.inaccurateProb) {
            ignorable = !ignorable;
        }

        if (ignorable) {
            return SymbolFilterResponse.IGNORE;
        } else {
            return SymbolFilterResponse.ACCEPT;
        }
    }

    @Override
    public void update(Word<LocalTimerMealySemanticInputSymbol<I>> prefix, NonDelayingInput<I> symbol, SymbolFilterResponse response) {
        throw new IllegalStateException("Not supported.");
    }
}
