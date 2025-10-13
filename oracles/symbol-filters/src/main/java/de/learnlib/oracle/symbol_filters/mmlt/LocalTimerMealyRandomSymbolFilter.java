package de.learnlib.oracle.symbol_filters.mmlt;


import de.learnlib.oracle.symbol_filters.RandomSymbolFilter;
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
public class LocalTimerMealyRandomSymbolFilter<S, I, O> extends RandomSymbolFilter<LocalTimerMealySemanticInputSymbol<I>, NonDelayingInput<I>> {

    private final LocalTimerMealy<S, I, O> automaton;

    public LocalTimerMealyRandomSymbolFilter(LocalTimerMealy<S, I, O> automaton,
                                             double inaccurateProb, Random random) {
        super(inaccurateProb, random);
        this.automaton = automaton;
    }


    @Override
    protected SymbolFilterResponse isIgnorable(Word<LocalTimerMealySemanticInputSymbol<I>> prefix, NonDelayingInput<I> symbol) {
        return LocalTimerMealySymbolFilterUtil.isIgnorable(this.automaton, prefix, symbol);
    }
}