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
package de.learnlib.oracle.equivalence.mmlt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.learnlib.oracle.EquivalenceOracle.MMLTEquivalenceOracle;
import de.learnlib.oracle.TimedQueryOracle;
import de.learnlib.query.DefaultQuery;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.automaton.mmlt.State;
import net.automatalib.automaton.mmlt.impl.ReducedMMLTSemantics;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.util.automaton.Automata;
import net.automatalib.util.automaton.cover.MMLTCover;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Implements the partial W-method for {@link MMLT} learning. The key modification compared to
 * {@link de.learnlib.oracle.equivalence.RandomWpMethodEQOracle} is that prefixes are sampled from entry prefixes only
 * instead of all state prefixes.
 *
 * @param <I>
 *         input symbol type (of non-delaying inputs)
 * @param <O>
 *         output symbol type
 */
public class RandomWpMethodEQOracle<I, O> implements MMLTEquivalenceOracle<I, O> {

    private final TimedQueryOracle<I, O> timeOracle;

    private final Random random;
    private final int minSize;
    private final int rndLen;
    private final int bound;

    public RandomWpMethodEQOracle(TimedQueryOracle<I, O> timeOracle,
                                  long randomSeed,
                                  int minSize,
                                  int rndAddLength,
                                  int bound) {

        this.timeOracle = timeOracle;
        this.random = new Random(randomSeed);

        this.minSize = minSize;
        this.rndLen = rndAddLength;
        this.bound = bound;
    }

    @Override
    public @Nullable DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> findCounterExample(MMLT<?, I, ?, O> hypothesis,
                                                                                          Collection<? extends TimedInput<I>> inputs) {
        return findCounterExampleInternal(hypothesis, inputs);
    }

    private <S> @Nullable DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> findCounterExampleInternal(MMLT<S, I, ?, O> hypothesis,
                                                                                                       Collection<? extends TimedInput<I>> inputs) {
        // Make expanded form of hypothesis:
        ReducedMMLTSemantics<S, I, O> hypSemModel = ReducedMMLTSemantics.forMMLT(hypothesis);

        // Create a list of symbols (for faster access):
        List<TimedInput<I>> listAlphabet = new ArrayList<>(inputs);

        // Identify global suffixes:
        List<Word<TimedInput<I>>> globalSuffixes = Automata.characterizingSet(hypSemModel, inputs);

        // Get list of prefixes in deterministic order (so we can reproduce experiments easily):
        Map<S, Word<TimedInput<I>>> locationCover = MMLTCover.getMMLTLocationCover(hypothesis, listAlphabet);
        List<Word<TimedInput<I>>> prefixList = new ArrayList<>(locationCover.values());

        // Generate test words:
        for (int i = 0; i < this.bound; i++) {
            Word<TimedInput<I>> testword =
                    this.generateTestword(prefixList, globalSuffixes, hypothesis, hypSemModel, listAlphabet);

            Word<TimedOutput<O>> sulAnswer = timeOracle.answerQuery(testword);
            Word<TimedOutput<O>> hypAnswer = hypothesis.getSemantics().computeOutput(testword);

            // Found inconsistency if outputs do no match:
            if (!sulAnswer.equals(hypAnswer)) {
                return new DefaultQuery<>(testword, sulAnswer);
            }
        }

        return null;
    }

    private <S> Word<TimedInput<I>> generateTestword(List<Word<TimedInput<I>>> prefixes,
                                                     List<Word<TimedInput<I>>> globalSuffixes,
                                                     MMLT<S, I, ?, O> hypothesis,
                                                     ReducedMMLTSemantics<S, I, O> hypSemModel,
                                                     List<TimedInput<I>> alphabet) {

        WordBuilder<TimedInput<I>> wb = new WordBuilder<>();

        // 1. Pick a random entry config prefix:
        Word<TimedInput<I>> prefix = prefixes.get(this.random.nextInt(prefixes.size()));
        wb.append(prefix);

        // 2. Add random middle part:
        int size = minSize;
        while (size > 0 || this.random.nextDouble() > 1 / (this.rndLen + 1.0)) {
            TimedInput<I> nextSymbol = alphabet.get(this.random.nextInt(alphabet.size()));
            wb.append(nextSymbol);

            if (size > 0) {
                size--;
            }
        }

        // 3. Pick a random suffix for this state:
        // 50% chance for state testing, 50% chance for transition testing
        Word<TimedInput<I>> suffix = Word.epsilon();
        if (this.random.nextBoolean()) {
            if (!globalSuffixes.isEmpty()) {
                suffix = globalSuffixes.get(random.nextInt(globalSuffixes.size()));
            }
        } else {
            // Identify configuration reached by prefix:
            State<S, O> currentConfig = hypothesis.getSemantics().getState(wb);
            assert currentConfig != null;
            Integer state = hypSemModel.getStateForConfiguration(currentConfig, true);
            List<Word<TimedInput<I>>> localSuffixes = Automata.stateCharacterizingSet(hypSemModel, alphabet, state);

            if (!localSuffixes.isEmpty()) {
                suffix = localSuffixes.get(random.nextInt(localSuffixes.size()));
            }
        }

        wb.append(suffix);

        return wb.toWord();
    }
}
