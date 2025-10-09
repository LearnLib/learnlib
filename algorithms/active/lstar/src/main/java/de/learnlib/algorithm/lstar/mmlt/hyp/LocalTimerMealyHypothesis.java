package de.learnlib.algorithm.lstar.mmlt.hyp;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealyInputSymbol;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealySemanticInputSymbol;
import net.automatalib.alphabet.time.mmlt.NonDelayingInput;
import net.automatalib.alphabet.time.mmlt.TimeStepSequence;
import net.automatalib.automaton.time.mmlt.AbstractSymbolCombiner;
import net.automatalib.automaton.time.mmlt.LocalTimerMealy;
import net.automatalib.automaton.time.mmlt.MealyTimerInfo;
import net.automatalib.automaton.time.mmlt.semantics.LocalTimerMealyConfiguration;
import net.automatalib.automaton.time.mmlt.semantics.LocalTimerMealySemantics;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * An MMLT hypothesis that includes a prefix mapping.
 * This mapping assigns a short prefix to each location.
 *
 * @param <S> Location type
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class LocalTimerMealyHypothesis<S, I, O> implements LocalTimerMealy<S, I, O>, IInternalLocalTimerMealyHypothesis<S, I, O> {
    private final LocalTimerMealy<S, I, O> automaton;
    private final Map<S, Word<LocalTimerMealySemanticInputSymbol<I>>> prefixMap; // location -> prefix

    public LocalTimerMealyHypothesis(LocalTimerMealy<S, I, O> automaton, Map<S, Word<LocalTimerMealySemanticInputSymbol<I>>> prefixMap) {
        this.automaton = automaton;
        this.prefixMap = prefixMap;
    }

    @Override
    public Word<LocalTimerMealySemanticInputSymbol<I>> getPrefix(LocalTimerMealyConfiguration<S, I, O> configuration) {
        var locPrefix = getLocationPrefix(configuration);
        if (configuration.isEntryConfig()) {
            return locPrefix; // entry distance = 0
        } else {
            return locPrefix.append(new TimeStepSequence<>(configuration.getEntryDistance()));
        }
    }

    @Override
    public Word<LocalTimerMealySemanticInputSymbol<I>> getPrefix(Word<LocalTimerMealySemanticInputSymbol<I>> prefix) {
        var resultingConfig = getSemantics().traceInputs(prefix);
        return getPrefix(resultingConfig);
    }


    @Override
    public Word<LocalTimerMealySemanticInputSymbol<I>> getLocationPrefix(LocalTimerMealyConfiguration<S, I, O> configuration) {
        var locPrefix = this.prefixMap.get(configuration.getLocation());
        if (locPrefix == null) throw new AssertionError();
        return locPrefix;
    }


    @Override
    public Word<LocalTimerMealySemanticInputSymbol<I>> getPrefix(S location) {
        return prefixMap.get(location);
    }

    @Override
    public O getSilentOutput() {
        return automaton.getSilentOutput();
    }

    @Override
    public AbstractSymbolCombiner<O> getOutputCombiner() {
        return automaton.getOutputCombiner();
    }

    @Override
    public Alphabet<LocalTimerMealyInputSymbol<I>> getInputAlphabet() {
        return automaton.getInputAlphabet();
    }

    @Override
    public Alphabet<NonDelayingInput<I>> getUntimedAlphabet() {
        return automaton.getUntimedAlphabet();
    }

    @Override
    public S getInitialState() {
        return automaton.getInitialState();
    }

    @Override
    public Collection<S> getStates() {
        return automaton.getStates();
    }

    @Override
    public @Nullable LocalTimerMealyTransition<S, O> getTransition(S location, LocalTimerMealyInputSymbol<I> input) {
        return automaton.getTransition(location, input);
    }

    @Override
    public boolean isLocalReset(S location, NonDelayingInput<I> input) {
        return automaton.isLocalReset(location, input);
    }

    @Override
    public List<MealyTimerInfo<O>> getSortedTimers(S location) {
        return automaton.getSortedTimers(location);
    }

    @Override
    public LocalTimerMealySemantics<S, I, O> getSemantics() {
        return new net.automatalib.automaton.time.impl.mmlt.LocalTimerMealySemantics<>(this);
    }


}
