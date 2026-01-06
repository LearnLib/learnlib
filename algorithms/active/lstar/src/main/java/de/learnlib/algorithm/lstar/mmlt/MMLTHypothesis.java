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

import java.util.Map;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.automaton.mmlt.State;
import net.automatalib.automaton.mmlt.SymbolCombiner;
import net.automatalib.automaton.mmlt.impl.CompactMMLT;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.word.Word;

/**
 * The hypothesis model is a regular {@link MMLT} that includes an additional prefix mapping. This mapping assigns a
 * short prefix to each location.
 *
 * @param <I>
 *         input symbol type (of non-delaying inputs)
 * @param <O>
 *         output symbol type
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
     *         the considered configuration
     *
     * @return the assigned prefix
     */
    public Word<TimedInput<I>> getPrefix(State<Integer, O> configuration) {
        Word<TimedInput<I>> locPrefix = getLocationPrefix(configuration);
        if (configuration.isEntryConfig()) {
            return locPrefix; // entry distance = 0
        } else {
            return locPrefix.append(TimedInput.step(configuration.getEntryDistance()));
        }
    }

    /**
     * Returns a prefix for the given location. This prefix is deterministic in the learner.
     *
     * @param location
     *         the location
     *
     * @return the location prefix
     */
    public Word<TimedInput<I>> getPrefix(Integer location) {
        Word<TimedInput<I>> prefix = prefixMap.get(location);
        assert prefix != null;
        return prefix;
    }

    /**
     * Returns a prefix for the location reached by the given prefix. This prefix is deterministic in the learner.
     *
     * @param prefix
     *         the access sequence to the location
     *
     * @return the (canonical) location prefix
     */
    public Word<TimedInput<I>> getPrefix(Word<TimedInput<I>> prefix) {
        State<Integer, O> resultingConfig = getSemantics().getState(prefix);
        assert resultingConfig != null;
        return getPrefix(resultingConfig);
    }

    /**
     * Returns the prefix assigned to the location that is active in the provided configuration.
     *
     * @param configuration
     *         the considered configuration
     *
     * @return the assigned prefix
     */
    public Word<TimedInput<I>> getLocationPrefix(State<Integer, O> configuration) {
        return getPrefix(configuration.getLocation());
    }
}
