package de.learnlib.oracle.symbol_filters.mmlt;

import de.learnlib.symbol_filter.SymbolFilterResponse;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealySemanticInputSymbol;
import net.automatalib.alphabet.time.mmlt.NonDelayingInput;
import net.automatalib.automaton.time.mmlt.LocalTimerMealy;
import net.automatalib.word.Word;

class LocalTimerMealySymbolFilterUtil {

    /**
     * Returns IGNORE if the provided input triggers a transition that silently self-loops,
     * and ACCEPT otherwise.
     *
     * @param automaton Automaton
     * @param prefix    State prefix
     * @param symbol    Input symbol
     * @param <S>       Location type
     * @param <I>       Input type for non-delaying inputs
     * @param <O>       Output symbol type
     * @return IGNORE for silent self-loops, ACCEPT otherwise.
     */
    static <S, I, O> SymbolFilterResponse isIgnorable(LocalTimerMealy<S, I, O> automaton, Word<LocalTimerMealySemanticInputSymbol<I>> prefix, NonDelayingInput<I> symbol) {
        var targetConfig = automaton.getSemantics().traceInputs(prefix);
        var trans = automaton.getSemantics().getTransition(targetConfig, symbol);

        boolean ignorable = trans.output().equals(automaton.getSemantics().getSilentOutput()) && targetConfig.equals(trans.target());

        return ignorable ? SymbolFilterResponse.IGNORE : SymbolFilterResponse.ACCEPT;
    }
}
