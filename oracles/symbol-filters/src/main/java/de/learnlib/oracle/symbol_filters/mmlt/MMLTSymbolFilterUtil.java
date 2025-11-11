package de.learnlib.oracle.symbol_filters.mmlt;

import java.util.Objects;

import de.learnlib.symbol_filter.SymbolFilterResponse;
import net.automatalib.automaton.mmlt.MMLTSemantics;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.word.Word;

class MMLTSymbolFilterUtil {

    /**
     * Returns IGNORE if the provided input triggers a transition that silently self-loops,
     * and ACCEPT otherwise.
     *
     * @param automaton Automaton
     * @param prefix    State prefix
     * @param symbol    Input symbol
     * @param <I>       Input type for non-delaying inputs
     * @param <O>       Output symbol type
     * @return IGNORE for silent self-loops, ACCEPT otherwise.
     */
    static <I, O> SymbolFilterResponse isIgnorable(MMLT<?, I, ?, O> automaton, Word<TimedInput<I>> prefix, InputSymbol<I> symbol) {
        return isIgnorable(automaton.getSemantics(), prefix, symbol);
    }

    static <S, I, T, O> SymbolFilterResponse isIgnorable(MMLTSemantics<S, I, T, O> semantics, Word<TimedInput<I>> prefix, InputSymbol<I> symbol) {
        var targetConfig = semantics.getState(prefix);
        var trans = semantics.getTransition(targetConfig, symbol);
        var target = semantics.getSuccessor(trans);
        var output = semantics.getTransitionOutput(trans);

        boolean ignorable = Objects.equals(output, semantics.getSilentOutput()) && Objects.equals(targetConfig, target);

        return ignorable ? SymbolFilterResponse.IGNORE : SymbolFilterResponse.ACCEPT;
    }
}
