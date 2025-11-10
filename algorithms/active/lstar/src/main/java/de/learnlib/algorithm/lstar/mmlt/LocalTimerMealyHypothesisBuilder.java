package de.learnlib.algorithm.lstar.mmlt;

import de.learnlib.datastructure.observationtable.Row;
import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.automaton.mmlt.impl.CompactMMLT;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimeStepSequence;
import net.automatalib.word.Word;

import java.util.HashMap;
import java.util.Map;

class LocalTimerMealyHypothesisBuilder {

    record LocalTimerMealyHypothesisBuildResult<S, I, T, O>(MMLT<S, I, T, O> automaton,
                                                         Map<Integer, Word<TimedInput<I>>> prefixMap) {

    }

    /**
     * Constructs a hypothesis MMLT from an observation table, inferred local resets, and inferred local timers.
     */
    static <I, O> LocalTimerMealyHypothesisBuildResult<Integer, I, ?, O> constructHypothesis(LStarLocalTimerMealyHypDataContainer<I, O> hypData) {

        // 1. Create map that stores link between contentID and short-prefix row:
        final Map<Integer, Row<TimedInput<I>>> locationContentIdMap = new HashMap<>(); // contentId -> sp location
        for (var spRow : hypData.getTable().getShortPrefixRows()) {
            if (locationContentIdMap.containsKey(spRow.getRowContentId())) {
                // Multiple sp rows may have same contentID. Thus, assign each id one location:
                continue;
            }
            locationContentIdMap.put(spRow.getRowContentId(), spRow);
        }

        // 2. Create untimed alphabet:
        GrowingMapAlphabet<I> alphabet = new GrowingMapAlphabet<>();
        for (var symbol : hypData.getAlphabet()) {
            if (symbol instanceof InputSymbol<I> ndi) {
                alphabet.add(ndi.symbol());
            }
        }

        // 3. Prepare objects for automaton, timers and resets:
        int numLocations = hypData.getTable().numberOfShortPrefixRows();
        var hypothesis = new CompactMMLT<>(alphabet, hypData.getModelParams().silentOutput(), hypData.getModelParams().outputCombiner());

        final Map<Integer, Integer> stateMap = new HashMap<>(numLocations); // row content id -> state id

        final Map<Integer, Word<TimedInput<I>>> prefixMap = new HashMap<>(numLocations); // state id -> location prefix

        // 4. Create one state per location:
        for (var row : hypData.getTable().getShortPrefixRows()) {
            int newStateId = hypothesis.addState();
            stateMap.putIfAbsent(row.getRowContentId(), newStateId);
            prefixMap.put(newStateId, row.getLabel());

            if (row.getLabel().equals(Word.epsilon())) {
                hypothesis.setInitialState(newStateId);
            }
        }
        // Ensure initial location:
        if (hypothesis.getInitialState() == null) {
            throw new IllegalArgumentException("Automaton must have an initial location.");
        }

        // 5. Create outgoing transitions for non-delaying inputs:
        for (var rowContentId : stateMap.keySet()) {
            Row<TimedInput<I>> spLocation = locationContentIdMap.get(rowContentId);

            for (var symbol : alphabet) {
                int symIdx = hypData.getAlphabet().getSymbolIndex(TimedInput.input(symbol));

                var transOutput = hypData.getTransitionOutput(spLocation, symIdx);
                O output = hypData.getModelParams().silentOutput(); // silent by default
                if (transOutput != null) {
                    output = transOutput.symbol();
                }

                int successorId;
                if (spLocation.getSuccessor(symIdx) == null) {
                    successorId = spLocation.getRowContentId(); // not in local alphabet -> self-loop
                } else {
                    successorId = spLocation.getSuccessor(symIdx).getRowContentId();
                }

                // Add transition to automaton:
                int sourceLocId = stateMap.get(rowContentId);
                int successorLocId = stateMap.get(successorId);
                hypothesis.addTransition(sourceLocId, symbol, successorLocId, output);

                // Check for local reset:
                var targetTransition = spLocation.getLabel().append(TimedInput.input(symbol));
                if (hypData.getTransitionResetSet().contains(targetTransition) && sourceLocId == successorLocId) {
                    hypothesis.addLocalReset(sourceLocId, symbol);
                }
            }

        }

        // 6. Add timeout transitions:
        for (var rowContentId : stateMap.keySet()) {
            Row<TimedInput<I>> spLocation = locationContentIdMap.get(rowContentId);

            var timerInfo = hypData.getTable().getLocationTimerInfo(spLocation);
            if (timerInfo == null) {
                continue; // no timers
            }

            for (var timer : timerInfo.getLocalTimers().values()) {
                if (timer.periodic()) {
                    hypothesis.addPeriodicTimer(stateMap.get(rowContentId), timer.name(), timer.initial(), timer.output());
                } else {
                    // One-shot: use successor from table
                    TimedInput<I> symbol = new TimeStepSequence<>(timer.initial());

                    int symIdx = hypData.getAlphabet().getSymbolIndex(symbol);
                    int successorId = spLocation.getSuccessor(symIdx).getRowContentId();

                    hypothesis.addOneShotTimer(stateMap.get(rowContentId), timer.name(), timer.initial(), timer.output(), stateMap.get(successorId));
                }
            }
        }

        return new LocalTimerMealyHypothesisBuildResult<>(hypothesis, prefixMap);
    }

}
