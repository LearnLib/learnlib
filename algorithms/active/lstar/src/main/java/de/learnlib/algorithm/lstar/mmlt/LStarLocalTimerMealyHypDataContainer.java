package de.learnlib.algorithm.lstar.mmlt;

import de.learnlib.algorithm.LocalTimerMealyModelParams;
import de.learnlib.datastructure.observationtable.Row;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Stores various data used for describing the MMLT hypothesis.
 * This includes the OT, a list of local resets, and a list of outputs.
 *
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
class LStarLocalTimerMealyHypDataContainer<I, O> {
    private final Alphabet<TimedInput<I>> alphabet;

    private final LocalTimerMealyObservationTable<I, O> table;
    private final Map<Word<TimedInput<I>>, TimedOutput<O>> transitionOutputMap;
    private final Set<Word<TimedInput<I>>> transitionResetSet; // all transitions that trigger a reset

    private final LocalTimerMealyModelParams<O> modelParams;

    public LStarLocalTimerMealyHypDataContainer(Alphabet<TimedInput<I>> alphabet, LocalTimerMealyModelParams<O> modelParams, LocalTimerMealyObservationTable<I, O> table) {
        this.alphabet = alphabet;
        this.modelParams = modelParams;
        this.table = table;

        this.transitionOutputMap = new HashMap<>();
        this.transitionResetSet = new HashSet<>();
    }

    @Nullable
    protected TimedOutput<O> getTransitionOutput(Row<TimedInput<I>> stateRow, int inputIdx) {
        Row<TimedInput<I>> transRow = stateRow.getSuccessor(inputIdx);
        if (transRow == null) {
            return null;
        }

        return this.transitionOutputMap.getOrDefault(transRow.getLabel(), null);
    }


    public LocalTimerMealyModelParams<O> getModelParams() {
        return modelParams;
    }

    public Alphabet<TimedInput<I>> getAlphabet() {
        return alphabet;
    }


    public LocalTimerMealyObservationTable<I, O> getTable() {
        return table;
    }

    public Map<Word<TimedInput<I>>, TimedOutput<O>> getTransitionOutputMap() {
        return transitionOutputMap;
    }

    public Set<Word<TimedInput<I>>> getTransitionResetSet() {
        return transitionResetSet;
    }
}
