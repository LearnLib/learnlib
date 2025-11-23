/* Copyright (C) 2013-2025 TU Dortmund University
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
package de.learnlib.oracle.equivalence.mmlt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;

import de.learnlib.oracle.EquivalenceOracle.MMLTEquivalenceOracle;
import de.learnlib.oracle.TimedQueryOracle;
import de.learnlib.query.DefaultQuery;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.automaton.mmlt.State;
import net.automatalib.automaton.mmlt.TimerInfo;
import net.automatalib.common.util.random.RandomUtil;
import net.automatalib.common.util.string.AbstractPrintable;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimeStepSequence;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.symbol.time.TimeoutSymbol;
import net.automatalib.util.automaton.cover.MMLTCover;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Searches for counterexamples that reveal local resets by
 * <ul>
 *     <li>taking any prefix from a known location</li>
 *     <li>appending a single time step</li>
 *     <li>appending inputs of all non-delaying inputs that self-loop in that location</li>
 *     <li>appending timeout</li>
 * </ul>.
 *
 * @param <I>
 *         input symbol type (of non-delaying inputs)
 * @param <O>
 *         output symbol type
 */
public class ResetSearchEQOracle<I, O> implements MMLTEquivalenceOracle<I, O> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResetSearchEQOracle.class);

    private final TimedQueryOracle<I, O> timeOracle;
    private final Random locPrefixRandom;

    private final double loopInsertPercentage;
    private final double testedLocPercentage;

    private final long loopingInputSelectionSeed;

    public ResetSearchEQOracle(TimedQueryOracle<I, O> timeOracle,
                               long seed,
                               double loopInsertPercentage,
                               double testedLocPercentage) {
        this.timeOracle = timeOracle;
        this.locPrefixRandom = new Random(seed);
        this.loopInsertPercentage = loopInsertPercentage;
        this.testedLocPercentage = testedLocPercentage;

        this.loopingInputSelectionSeed = seed;
    }

    private <S, T> List<TimedInput<I>> getLoopingSymbols(S sourceLoc,
                                                         List<TimedInput<I>> alphabet,
                                                         MMLT<S, I, T, O> hypothesis) {
        final List<TimedInput<I>> loopingInputs = new ArrayList<>();

        for (TimedInput<I> sym : alphabet) {
            // only consider non-delaying inputs, as only these can perform local resets
            if (sym instanceof InputSymbol<I> ndi) {
                final T trans = hypothesis.getTransition(sourceLoc, ndi.symbol());

                // Collect self-loops:
                if (trans == null || Objects.equals(hypothesis.getSuccessor(trans), sourceLoc)) {
                    loopingInputs.add(sym);
                }
            }
        }

        return loopingInputs;
    }

    @Override
    public @Nullable DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> findCounterExample(MMLT<?, I, ?, O> hypothesis,
                                                                                          Collection<? extends TimedInput<I>> inputs) {
        if (loopInsertPercentage == 0) {
            return null; // oracle is disabled
        }
        List<TimedInput<I>> listInputs = new ArrayList<>(inputs);

        if (listInputs.stream().noneMatch(s -> s instanceof TimeStepSequence<I> || s instanceof TimeoutSymbol<I>)) {
            LOGGER.warn(
                    "ResetSearchOracle requires inputs to contain TimeoutSymbol and TimeStepSymbol. Will not find counterexample.");
            return null;
        }
        return this.findCexInternal(hypothesis, listInputs);
    }

    private <S, T> @Nullable DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> findCexInternal(MMLT<S, I, T, O> hypothesis,
                                                                                               List<TimedInput<I>> inputs) {

        // Retrieve prefixes from state cover, to establish some separation between learner and teacher:
        Map<S, Word<TimedInput<I>>> stateCover = MMLTCover.getMMLTLocationCover(hypothesis, inputs);

        // Only keep locations that have at least two stable configs (only these can have local resets):
        List<Word<TimedInput<I>>> prefixes = new ArrayList<>();
        for (Entry<S, Word<TimedInput<I>>> e : stateCover.entrySet()) {
            List<TimerInfo<S, O>> timers = hypothesis.getSortedTimers(e.getKey());
            if (!timers.isEmpty() && timers.get(0).initial() > 1) {
                prefixes.add(e.getValue());
            }
        }

        // Sort alphabetically, so that experiments are easily reproducible:
        prefixes.sort(Comparator.comparing(AbstractPrintable::toString));

        // Determine number of tested locations:
        int randPrefixes = (int) Math.round(testedLocPercentage * prefixes.size());
        if (randPrefixes == 0) {
            LOGGER.warn("No prefixes tested. Need higher percentage?");
            return null;
        }

        List<Word<TimedInput<I>>> chosenPrefixes = RandomUtil.sampleUnique(locPrefixRandom, prefixes, randPrefixes);

        for (Word<TimedInput<I>> prefix : chosenPrefixes) {
            // Retrieve looping symbols:
            State<S, O> state = hypothesis.getSemantics().getState(prefix);
            assert state != null;
            S sourceLoc = state.getLocation();
            List<TimedInput<I>> loopingInputs = getLoopingSymbols(sourceLoc, inputs, hypothesis);
            if (loopingInputs.isEmpty()) {
                continue; // no loops
            }

            // Determine number of looping symbols we want to append:
            int randElements = (int) Math.round(loopInsertPercentage * loopingInputs.size());
            randElements = Math.min(loopingInputs.size(), randElements);

            List<TimedInput<I>> chosenLoopingInputs =
                    RandomUtil.sampleUnique(new Random(loopingInputSelectionSeed), loopingInputs, randElements);

            // Create test word:
            WordBuilder<TimedInput<I>> wbTestWord = new WordBuilder<>();
            wbTestWord.append(prefix);
            wbTestWord.append(TimedInput.step());
            wbTestWord.append(Word.fromList(chosenLoopingInputs));
            wbTestWord.append(TimedInput.timeout());

            // Check if counterexample:
            Word<TimedInput<I>> testWord = wbTestWord.toWord();

            Word<TimedOutput<O>> hypOutput = hypothesis.getSemantics().computeSuffixOutput(Word.epsilon(), testWord);
            Word<TimedOutput<O>> sulOutput = timeOracle.answerQuery(testWord);
            if (!hypOutput.equals(sulOutput)) {
                return new DefaultQuery<>(testWord, sulOutput);
            }
        }
        return null;
    }

}
