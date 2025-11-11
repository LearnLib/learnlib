package de.learnlib.algorithm.lstar.mmlt;

import java.util.Map;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.mmlt.State;
import net.automatalib.automaton.mmlt.SymbolCombiner;
import net.automatalib.automaton.mmlt.impl.CompactMMLT;
import net.automatalib.symbol.time.TimeStepSequence;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.word.Word;

/**
 * An MMLT hypothesis that includes a prefix mapping. This mapping assigns a short prefix to each location.
 *
 * @param <I>
 *         Input type for non-delaying inputs
 * @param <O>
 *         Output symbol type
 */
public class MMLTHypothesis<I, O> extends CompactMMLT<I, O> {

    private final Map<Integer, Word<TimedInput<I>>> prefixMap; // location -> prefix

    MMLTHypothesis(Alphabet<I> alphabet,
                   int sizeHint,
                   O silentOuput,
                   SymbolCombiner<O> outputCombiner,
                   Map<Integer, Word<TimedInput<I>>> prefixMap) {
        super(alphabet, sizeHint, silentOuput, outputCombiner);
        this.prefixMap = prefixMap;
    }

    /**
     * Returns the prefix assigned to the provided configuration. The assigned prefix is the concatenation of the prefix
     * assigned to the active location and the minimal number of time steps needed to reach the configuration after
     * entering its location (= entry distance).
     *
     * @param configuration
     *         Considered configuration
     *
     * @return Assigned prefix
     */
    public Word<TimedInput<I>> getPrefix(State<Integer, O> configuration) {
        var locPrefix = getLocationPrefix(configuration);
        if (configuration.isEntryConfig()) {
            return locPrefix; // entry distance = 0
        } else {
            return locPrefix.append(new TimeStepSequence<>(configuration.getEntryDistance()));
        }
    }

    public Word<TimedInput<I>> getPrefix(Word<TimedInput<I>> prefix) {
        var resultingConfig = getSemantics().getState(prefix);
        return getPrefix(resultingConfig);
    }

    /**
     * Returns the prefix assigned to the location that is active in the provided configuration.
     *
     * @param configuration
     *         Considered configuration
     *
     * @return Assigned prefix
     */
    public Word<TimedInput<I>> getLocationPrefix(State<Integer, O> configuration) {
        var locPrefix = this.prefixMap.get(configuration.getLocation());
        if (locPrefix == null) {throw new AssertionError();}
        return locPrefix;
    }

    /**
     * Returns a prefix for the given location. This prefix is deterministic in the RS learner.
     *
     * @param location
     *         Location
     *
     * @return Location prefix
     */
    public Word<TimedInput<I>> getPrefix(Integer location) {
        return prefixMap.get(location);
    }
}
