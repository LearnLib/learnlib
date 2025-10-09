package de.learnlib.algorithm.lstar.mmlt;

import de.learnlib.algorithm.LocalTimerMealyModelParams;
import de.learnlib.datastructure.observationtable.Row;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealySemanticInputSymbol;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealyOutputSymbol;
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
    private final Alphabet<LocalTimerMealySemanticInputSymbol<I>> alphabet;

    private final LocalTimerMealyObservationTable<I, O> table;
    private final Map<Word<LocalTimerMealySemanticInputSymbol<I>>, LocalTimerMealyOutputSymbol<O>> transitionOutputMap;
    private final Set<Word<LocalTimerMealySemanticInputSymbol<I>>> transitionResetSet; // all transitions that trigger a reset

    private final LocalTimerMealyModelParams<O> modelParams;

    public LStarLocalTimerMealyHypDataContainer(Alphabet<LocalTimerMealySemanticInputSymbol<I>> alphabet, LocalTimerMealyModelParams<O> modelParams, LocalTimerMealyObservationTable<I, O> table) {
        this.alphabet = alphabet;
        this.modelParams = modelParams;
        this.table = table;

        this.transitionOutputMap = new HashMap<>();
        this.transitionResetSet = new HashSet<>();
    }

    @Nullable
    protected LocalTimerMealyOutputSymbol<O> getTransitionOutput(Row<LocalTimerMealySemanticInputSymbol<I>> stateRow, int inputIdx) {
        Row<LocalTimerMealySemanticInputSymbol<I>> transRow = stateRow.getSuccessor(inputIdx);
        if (transRow == null) {
            return null;
        }

        return this.transitionOutputMap.getOrDefault(transRow.getLabel(), null);
    }


    public LocalTimerMealyModelParams<O> getModelParams() {
        return modelParams;
    }

    public Alphabet<LocalTimerMealySemanticInputSymbol<I>> getAlphabet() {
        return alphabet;
    }


    public LocalTimerMealyObservationTable<I, O> getTable() {
        return table;
    }

    public Map<Word<LocalTimerMealySemanticInputSymbol<I>>, LocalTimerMealyOutputSymbol<O>> getTransitionOutputMap() {
        return transitionOutputMap;
    }

    public Set<Word<LocalTimerMealySemanticInputSymbol<I>>> getTransitionResetSet() {
        return transitionResetSet;
    }
}
