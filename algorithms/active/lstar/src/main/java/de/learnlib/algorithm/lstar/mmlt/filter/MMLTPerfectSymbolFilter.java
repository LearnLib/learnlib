package de.learnlib.algorithm.lstar.mmlt.filter;


import de.learnlib.filter.symbol.AbstractPerfectSymbolFilter;
import de.learnlib.filter.FilterResponse;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.word.Word;

/**
 * A symbol filter for MMLTs that correctly accepts and ignores all transitions
 * that silently self-loop.
 *
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class MMLTPerfectSymbolFilter<I, O> extends AbstractPerfectSymbolFilter<TimedInput<I>, InputSymbol<I>> {

    private final MMLT<?, I, ?, O> automaton;

    public MMLTPerfectSymbolFilter(MMLT<?, I, ?, O> automaton) {
        this.automaton = automaton;
    }

    @Override
    protected FilterResponse isIgnorable(Word<TimedInput<I>> prefix, InputSymbol<I> symbol) {
        return MMLTSymbolFilterUtil.isIgnorable(this.automaton, prefix, symbol);
    }

}
