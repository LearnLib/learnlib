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
package de.learnlib.filter.cache.mmlt;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import de.learnlib.oracle.EquivalenceOracle.MMLTEquivalenceOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.time.MMLTModelParams;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimeStepSequence;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.symbol.time.TimeoutSymbol;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Searches for counterexamples by comparing the behavior of the hypothesis and the query cache. If there are multiple
 * counterexamples, the shortest one is returned.
 *
 * @param <I>
 *         input symbol type (of non-delaying inputs)
 * @param <O>
 *         output symbol type
 */
@SuppressWarnings("PMD.TestClassWithoutTestCases") // not a traditional test class
public class MMLTCacheConsistencyTest<I, O> implements MMLTEquivalenceOracle<I, O> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MMLTCacheConsistencyTest.class);

    private final TimedSULTreeCache<I, O> sulCache;
    private final MMLTModelParams<O> modelParams;

    MMLTCacheConsistencyTest(TimedSULTreeCache<I, O> sulCache, MMLTModelParams<O> modelParams) {
        this.sulCache = sulCache;
        this.modelParams = modelParams;
    }

    private DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> queryCache(Word<TimedInput<I>> word) {
        WordBuilder<TimedInput<I>> wbInput = new WordBuilder<>(word.length());
        WordBuilder<TimedOutput<O>> wbOutput = new WordBuilder<>(word.length());

        this.sulCache.pre();
        for (TimedInput<I> sym : word) {
            if (sym instanceof InputSymbol<I> ndi) {
                TimedOutput<O> res = this.sulCache.step(ndi);
                wbInput.append(ndi);
                wbOutput.append(res);
            } else if (sym instanceof TimeStepSequence<I> ws) {
                TimedOutput<O> res = this.sulCache.timeoutStep(ws.timeSteps());
                wbInput.append(ws);

                if (res == null) {
                    wbOutput.append(new TimedOutput<>(this.modelParams.silentOutput()));
                } else {
                    wbOutput.append(res);
                }
            } else {
                throw new IllegalArgumentException("Symbol type " + sym.getClass() + " must not be used in cache.");
            }
        }
        this.sulCache.post();

        return new DefaultQuery<>(wbInput.toWord(), wbOutput.toWord());
    }

    /**
     * The cache does not use timeout symbols. Using these instead of time-step-sequences has several performance
     * benefits. This function converts a query with a time-step-sequence to one that uses timeout symbols where
     * possible.
     *
     * @param originalQuery
     *         the original query
     *
     * @return the converted query
     */
    private DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> convertTimeSequences(DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> originalQuery) {
        WordBuilder<TimedInput<I>> wbInput = new WordBuilder<>();
        WordBuilder<TimedOutput<O>> wbOutput = new WordBuilder<>();

        int symIdx = 0;
        Word<TimedInput<I>> queryInput = originalQuery.getInput();
        Word<TimedOutput<O>> queryOutput = originalQuery.getOutput();

        while (symIdx < queryInput.length()) {
            TimedInput<I> inputSym = queryInput.getSymbol(symIdx);
            TimedOutput<O> outputSym = queryOutput.getSymbol(symIdx);
            symIdx++;

            if (inputSym instanceof InputSymbol<I> ds) {
                wbInput.append(ds);
                wbOutput.append(outputSym);
            } else if (inputSym instanceof TimeStepSequence<I> ws) {
                if (!Objects.equals(outputSym.symbol(), this.modelParams.silentOutput()) ||
                    ws.timeSteps() == this.modelParams.maxTimeoutWaitingTime()) {
                    // Found a timeout OR no timeout after max_delay:
                    wbInput.append(new TimeoutSymbol<>());
                    wbOutput.append(outputSym);
                    continue;
                }

                assert ws.timeSteps() < this.modelParams.maxTimeoutWaitingTime() :
                        "Wait time that exceeds max_delay in cache.";

                // Special case: silent output before max delay
                // Cannot replace with "timeout", as this implies wait until max_delay.
                // Hence: skip subsequent waits until reaching wait with output OR max_delay OR end of word:
                long combinedWaitTime = ws.timeSteps();
                TimedOutput<O> combinedOutput = outputSym;

                while (Objects.equals(combinedOutput.symbol(), this.modelParams.silentOutput()) &&
                       combinedWaitTime < this.modelParams.maxTimeoutWaitingTime() && symIdx < queryInput.length() &&
                       queryInput.getSymbol(symIdx) instanceof TimeStepSequence<I> nextWs) {
                    combinedWaitTime += nextWs.timeSteps();
                    combinedOutput = queryOutput.getSymbol(symIdx);
                    symIdx++;
                }

                if (combinedWaitTime >= this.modelParams.maxTimeoutWaitingTime() ||
                    !Objects.equals(combinedOutput.symbol(), this.modelParams.silentOutput())) {
                    wbInput.append(new TimeoutSymbol<>());

                    if (Objects.equals(combinedOutput.symbol(), this.modelParams.silentOutput())) {
                        // Reached max delay -> waiting for any time will now produce no more timeouts:
                        wbOutput.append(new TimedOutput<>(this.modelParams.silentOutput()));
                    } else {
                        // Found non-silent output:
                        wbOutput.append(new TimedOutput<>(combinedOutput.symbol(), combinedWaitTime));
                    }
                } else {
                    // Reached end of word before max_delay OR non-wait symbol -> ignore rest of this word:
                    if (symIdx < queryInput.length() - 1) {
                        LOGGER.debug("Ignoring at least one symbol during cache comparison.");
                    }
                    break;
                }
            }
        }
        return new DefaultQuery<>(wbInput.toWord(), wbOutput.toWord());
    }

    private DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> reduceToAllowedInputs(Set<TimedInput<I>> allowedInputs,
                                                                                    DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> query) {
        // Find the longest prefix with allowed inputs:
        int prefixLength = 0;
        while (prefixLength < query.length() && allowedInputs.contains(query.getInput().getSymbol(prefixLength))) {
            prefixLength++;
        }

        if (prefixLength == query.length()) {
            return query; // maximum length -> no need to reduce
        } else {
            return new DefaultQuery<>(query.getInput().prefix(prefixLength), query.getOutput().prefix(prefixLength));
        }
    }

    @Override
    public @Nullable DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> findCounterExample(MMLT<?, I, ?, O> hypothesis,
                                                                                          Collection<? extends TimedInput<I>> inputs) {
        Set<TimedInput<I>> allowedInputs = new HashSet<>(inputs);
        boolean allInputsConsidered = allowedInputs.containsAll(hypothesis.getSemantics().getInputAlphabet());

        // Iterator over all cached words:
        Iterator<Word<TimedInput<I>>> iter = this.sulCache.allWordsIterator();

        while (iter.hasNext()) {
            Word<TimedInput<I>> word = iter.next();

            // First, query word as-is (may include wait-symbols in input):
            DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> rawCacheQuery = this.queryCache(word);

            // Next, convert query that includes wait-symbols to query with timeout-symbols:
            DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> convertedQuery = this.convertTimeSequences(rawCacheQuery);

            // The counterexample may only use a subset of the allowed inputs.
            // If so, cut the query to the prefix of the word that is allowed:
            DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> reducedQuery =
                    allInputsConsidered ? convertedQuery : this.reduceToAllowedInputs(allowedInputs, convertedQuery);

            // Finally, query hypothesis using the converted query:
            Word<TimedOutput<O>> hypOutput = hypothesis.getSemantics().computeOutput(reducedQuery.getInput());

            if (!hypOutput.equals(reducedQuery.getOutput())) {
                // Hyp gives different output than cache (= SUL):
                return reducedQuery;
            }
        }

        return null;
    }
}
