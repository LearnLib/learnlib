package de.learnlib.algorithm.lstar.mmlt.hyp;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.automaton.mmlt.MMLTSemantics;
import net.automatalib.automaton.mmlt.MealyTimerInfo;
import net.automatalib.automaton.mmlt.State;
import net.automatalib.automaton.mmlt.SymbolCombiner;
import net.automatalib.automaton.mmlt.impl.CompactMMLTSemantics;
import net.automatalib.symbol.time.TimeStepSequence;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An MMLT hypothesis that includes a prefix mapping.
 * This mapping assigns a short prefix to each location.
 *
 * @param <S> Location type
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class LocalTimerMealyHypothesis<S, I, T, O> implements MMLT<S, I, T, O>, IInternalLocalTimerMealyHypothesis<S, I, O> {
    private final MMLT<S, I, T, O> automaton;
    private final Map<S, Word<TimedInput<I>>> prefixMap; // location -> prefix

    public LocalTimerMealyHypothesis(MMLT<S, I, T, O> automaton, Map<S, Word<TimedInput<I>>> prefixMap) {
        this.automaton = automaton;
        this.prefixMap = prefixMap;
    }

    @Override
    public Word<TimedInput<I>> getPrefix(State<S, O> configuration) {
        var locPrefix = getLocationPrefix(configuration);
        if (configuration.isEntryConfig()) {
            return locPrefix; // entry distance = 0
        } else {
            return locPrefix.append(new TimeStepSequence<>(configuration.getEntryDistance()));
        }
    }

    @Override
    public Word<TimedInput<I>> getPrefix(Word<TimedInput<I>> prefix) {
        var resultingConfig = getSemantics().getState(prefix);
        return getPrefix(resultingConfig);
    }


    @Override
    public Word<TimedInput<I>> getLocationPrefix(State<S, O> configuration) {
        var locPrefix = this.prefixMap.get(configuration.getLocation());
        if (locPrefix == null) throw new AssertionError();
        return locPrefix;
    }


    @Override
    public Word<TimedInput<I>> getPrefix(S location) {
        return prefixMap.get(location);
    }

    @Override
    public O getSilentOutput() {
        return automaton.getSilentOutput();
    }

    @Override
    public SymbolCombiner<O> getOutputCombiner() {
        return automaton.getOutputCombiner();
    }

    @Override
    public Alphabet<I> getInputAlphabet() {
        return automaton.getInputAlphabet();
    }

    @Override
    public Alphabet<I> getUntimedAlphabet() {
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
    public @Nullable T getTransition(S location, I input) {
        return automaton.getTransition(location, input);
    }

    @Override
    public boolean isLocalReset(S location, I input) {
        return automaton.isLocalReset(location, input);
    }

    @Override
    public List<MealyTimerInfo<S, O>> getSortedTimers(S location) {
        return automaton.getSortedTimers(location);
    }

    @Override
    public MMLTSemantics<S, I, ?, O> getSemantics() {
        return new CompactMMLTSemantics<>(this);
    }

    @Override
    public Void getStateProperty(S state) {
        return automaton.getStateProperty(state);
    }

    @Override
    public O getTransitionProperty(T transition) {
        return automaton.getTransitionProperty(transition);
    }

    @Override
    public S getSuccessor(T transition) {
        return automaton.getSuccessor(transition);
    }
}
