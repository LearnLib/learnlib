package de.learnlib.oracle.symbol_filters.mmlt;


import de.learnlib.symbol_filter.SymbolFilter;
import de.learnlib.symbol_filter.SymbolFilterResponse;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealySemanticInputSymbol;
import net.automatalib.alphabet.time.mmlt.NonDelayingInput;
import net.automatalib.automaton.time.mmlt.LocalTimerMealy;
import net.automatalib.word.Word;

/**
 * A symbol filter that correctly accepts and ignores all transitions.
 *
 * @param <S> Location type
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class PerfectSymbolFilter<S, I, O> implements SymbolFilter<I, O> {

    private final LocalTimerMealy<S, I, O> sulModel;

    public PerfectSymbolFilter(LocalTimerMealy<S, I, O> sulModel) {
        this.sulModel = sulModel;
    }

    @Override
    public SymbolFilterResponse query(Word<LocalTimerMealySemanticInputSymbol<I>> prefix, NonDelayingInput<I> symbol) {
        // Check if silent self-loop:

        var targetConfig = this.sulModel.getSemantics().traceInputs(prefix);
        var trans = this.sulModel.getSemantics().getTransition(targetConfig, symbol);

        if (trans.output().equals(sulModel.getSemantics().getSilentOutput()) && targetConfig.equals(trans.target())) {
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
