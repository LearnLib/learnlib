/* Copyright (C) 2013-2026 TU Dortmund University
 * This file is part of LearnLib <https://learnlib.de>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.learnlib.algorithm.lstar.mmlt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.learnlib.datastructure.observationtable.Row;
import de.learnlib.time.MMLTModelParams;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Stores various data used for describing the {@link MMLTHypothesis}. This includes the observation table, a list of
 * local resets, and a list of outputs.
 *
 * @param <I>
 *         input symbol type (of non-delaying inputs)
 * @param <O>
 *         output symbol type
 */
class MMLTHypDataContainer<I, O> {

    private final Alphabet<TimedInput<I>> alphabet;

    private final MMLTObservationTable<I, O> table;
    private final Map<Word<TimedInput<I>>, TimedOutput<O>> transitionOutputMap;
    private final Set<Word<TimedInput<I>>> transitionResetSet; // all transitions that trigger a reset

    private final MMLTModelParams<O> modelParams;

    MMLTHypDataContainer(Alphabet<TimedInput<I>> alphabet,
                         MMLTModelParams<O> modelParams,
                         MMLTObservationTable<I, O> table) {
        this.alphabet = alphabet;
        this.modelParams = modelParams;
        this.table = table;

        this.transitionOutputMap = new HashMap<>();
        this.transitionResetSet = new HashSet<>();
    }

    @Nullable TimedOutput<O> getTransitionOutput(Row<TimedInput<I>> stateRow, int inputIdx) {
        Row<TimedInput<I>> transRow = stateRow.getSuccessor(inputIdx);
        if (transRow == null) {
            return null;
        }

        return this.transitionOutputMap.get(transRow.getLabel());
    }

    MMLTModelParams<O> getModelParams() {
        return modelParams;
    }

    Alphabet<TimedInput<I>> getAlphabet() {
        return alphabet;
    }

    MMLTObservationTable<I, O> getTable() {
        return table;
    }

    Map<Word<TimedInput<I>>, TimedOutput<O>> getTransitionOutputMap() {
        return transitionOutputMap;
    }

    Set<Word<TimedInput<I>>> getTransitionResetSet() {
        return transitionResetSet;
    }
}
