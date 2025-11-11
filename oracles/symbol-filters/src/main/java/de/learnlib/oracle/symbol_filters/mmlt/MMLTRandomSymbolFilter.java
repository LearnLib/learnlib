package de.learnlib.oracle.symbol_filters.mmlt;


import de.learnlib.oracle.symbol_filters.RandomSymbolFilter;
import de.learnlib.symbol_filter.SymbolFilterResponse;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.word.Word;

import java.util.Random;

/**
 * A symbol filter that falsely answers a query with a specified probability.
 *
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class MMLTRandomSymbolFilter<I, O> extends RandomSymbolFilter<TimedInput<I>, InputSymbol<I>> {

    private final MMLT<?, I, ?, O> automaton;

    public MMLTRandomSymbolFilter(MMLT<?, I, ?, O> automaton,
                                  double inaccurateProb, Random random) {
        super(inaccurateProb, random);
        this.automaton = automaton;
    }


    @Override
    protected SymbolFilterResponse isIgnorable(Word<TimedInput<I>> prefix, InputSymbol<I> symbol) {
        return MMLTSymbolFilterUtil.isIgnorable(this.automaton, prefix, symbol);
    }
}