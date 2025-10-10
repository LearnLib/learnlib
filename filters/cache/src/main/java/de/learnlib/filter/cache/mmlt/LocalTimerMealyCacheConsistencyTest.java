package de.learnlib.filter.cache.mmlt;

import de.learnlib.algorithm.LocalTimerMealyModelParams;
import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.query.DefaultQuery;
import net.automatalib.alphabet.time.mmlt.*;
import net.automatalib.automaton.time.mmlt.LocalTimerMealy;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Searches for counterexamples by comparing the behavior of the hypothesis and the query cache.
 * If there are multiple counterexamples, the shortest one is returned.
 *
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class LocalTimerMealyCacheConsistencyTest<I, O> implements EquivalenceOracle.LocalTimerMealyEquivalenceOracle<I, O> {
    private final static Logger logger = LoggerFactory.getLogger(LocalTimerMealyCacheConsistencyTest.class);

    private final LocalTimerMealyTreeSULCache<I, O> sulCache;
    private final LocalTimerMealyModelParams<O> modelParams;

    LocalTimerMealyCacheConsistencyTest(LocalTimerMealyTreeSULCache<I, O> sulCache, LocalTimerMealyModelParams<O> modelParams) {
        this.sulCache = sulCache;
        this.modelParams = modelParams;
    }

    private DefaultQuery<LocalTimerMealySemanticInputSymbol<I>, Word<LocalTimerMealyOutputSymbol<O>>> queryCache(Word<LocalTimerMealySemanticInputSymbol<I>> word) {
        WordBuilder<LocalTimerMealySemanticInputSymbol<I>> wbInput = new WordBuilder<>();
        WordBuilder<LocalTimerMealyOutputSymbol<O>> wbOutput = new WordBuilder<>();

        this.sulCache.pre();
        for (var sym : word) {
            if (sym instanceof NonDelayingInput<I> ndi) {
                LocalTimerMealyOutputSymbol<O> res = this.sulCache.step(ndi);
                wbInput.append(ndi);
                wbOutput.append(res);
            } else if (sym instanceof TimeStepSequence<I> ws) {
                LocalTimerMealyOutputSymbol<O> res = this.sulCache.timeoutStep(ws.getTimeSteps());
                wbInput.append(ws);

                if (res == null) {
                    wbOutput.append(new LocalTimerMealyOutputSymbol<>(this.modelParams.silentOutput()));
                } else {
                    wbOutput.append(res);
                }
            } else {
                throw new AssertionError("Symbol type must not be used in cache.");
            }
        }
        this.sulCache.post();

        return new DefaultQuery<>(wbInput.toWord(), wbOutput.toWord());
    }

    /**
     * The cache does not use timeout symbols. Using these instead of tau-sequences has several performance benefits.
     * This function converts a query with a tau-sequence to one that uses timeout symbols where possible.
     *
     * @param originalQuery Original query
     * @return Converted query
     */
    private DefaultQuery<LocalTimerMealySemanticInputSymbol<I>, Word<LocalTimerMealyOutputSymbol<O>>> convertTimeSequences(DefaultQuery<LocalTimerMealySemanticInputSymbol<I>, Word<LocalTimerMealyOutputSymbol<O>>> originalQuery) {
        WordBuilder<LocalTimerMealySemanticInputSymbol<I>> wbInput = new WordBuilder<>();
        WordBuilder<LocalTimerMealyOutputSymbol<O>> wbOutput = new WordBuilder<>();

        int symIdx = 0;
        var queryInput = originalQuery.getInput();
        var queryOutput = originalQuery.getOutput();

        while (symIdx < queryInput.length()) {
            var inputSym = queryInput.getSymbol(symIdx);
            var outputSym = queryOutput.getSymbol(symIdx);
            symIdx++;

            if (inputSym instanceof NonDelayingInput<I> ds) {
                wbInput.append(ds);
                wbOutput.append(outputSym);
            } else if (inputSym instanceof TimeStepSequence<I> ws) {
                if (!outputSym.getSymbol().equals(this.modelParams.silentOutput()) || ws.getTimeSteps() == this.modelParams.maxTimeoutWaitingTime()) {
                    // Found a timeout OR no timeout after max_delay:
                    wbInput.append(new TimeoutSymbol<>());
                    wbOutput.append(outputSym);
                    continue;
                }
                if (ws.getTimeSteps() >= this.modelParams.maxTimeoutWaitingTime()) {
                    throw new AssertionError("Wait time that exceeds max_delay in cache.");
                }

                // Special case: silent output before max delay
                // Cannot replace with "timeout", as this implies wait until max_delay.
                // Hence: skip subsequent waits until reaching wait with output OR max_delay OR end of word:
                long combinedWaitTime = ws.getTimeSteps();
                LocalTimerMealyOutputSymbol<O> combinedOutput = outputSym;

                while (combinedOutput.getSymbol().equals(this.modelParams.silentOutput()) && combinedWaitTime < this.modelParams.maxTimeoutWaitingTime()
                        && symIdx < queryInput.length() &&
                        queryInput.getSymbol(symIdx) instanceof TimeStepSequence<I> nextWs) {
                    combinedWaitTime += nextWs.getTimeSteps();
                    combinedOutput = queryOutput.getSymbol(symIdx);
                    symIdx++;
                }

                if (combinedWaitTime >= this.modelParams.maxTimeoutWaitingTime() || !combinedOutput.getSymbol().equals(this.modelParams.silentOutput())) {
                    wbInput.append(new TimeoutSymbol<>());

                    if (combinedOutput.getSymbol().equals(this.modelParams.silentOutput())) {
                        // Reached max delay -> waiting for any time will now produce no more timeouts:
                        wbOutput.append(new LocalTimerMealyOutputSymbol<>(this.modelParams.silentOutput()));
                    } else {
                        // Found non-silent output:
                        wbOutput.append(new LocalTimerMealyOutputSymbol<>(combinedWaitTime, combinedOutput.getSymbol()));
                    }
                } else {
                    // Reached end of word before max_delay OR non-wait symbol -> ignore rest of this word:
                    if (symIdx < queryInput.length() - 1) {
                        logger.warn("Ignoring at least one symbol during cache comparison.");
                    }
                    break;
                }
            }
        }
        return new DefaultQuery<>(wbInput.toWord(), wbOutput.toWord());
    }

    private DefaultQuery<LocalTimerMealySemanticInputSymbol<I>, Word<LocalTimerMealyOutputSymbol<O>>> reduceToAllowedInputs(Set<LocalTimerMealySemanticInputSymbol<I>> allowedInputs, DefaultQuery<LocalTimerMealySemanticInputSymbol<I>, Word<LocalTimerMealyOutputSymbol<O>>> query) {
        // Find the longest prefix with allowed inputs:
        int prefixLength = 0;
        while (prefixLength < query.getInput().length() && allowedInputs.contains(query.getInput().getSymbol(prefixLength))) {
            prefixLength++;
        }

        if (prefixLength == query.getInput().length()) {
            return query; // maximum length -> no need to reduce
        } else {
            return new DefaultQuery<>(query.getInput().subWord(0, prefixLength), query.getOutput().subWord(0, prefixLength));
        }
    }


    @Override
    public @Nullable DefaultQuery<LocalTimerMealySemanticInputSymbol<I>, Word<LocalTimerMealyOutputSymbol<O>>> findCounterExample(LocalTimerMealy<?, I, O> hypothesis, Collection<? extends LocalTimerMealySemanticInputSymbol<I>> inputs) {
        Set<LocalTimerMealySemanticInputSymbol<I>> allowedInputs = new HashSet<>(inputs);
        boolean allInputsConsidered = allowedInputs.containsAll(hypothesis.getSemantics().getInputAlphabet());

        // Query all cached words:
        List<Word<LocalTimerMealySemanticInputSymbol<I>>> cachedWords = this.sulCache.listAllWords();

        List<DefaultQuery<LocalTimerMealySemanticInputSymbol<I>, Word<LocalTimerMealyOutputSymbol<O>>>> counterexamples = new ArrayList<>();
        for (var word : cachedWords) {
            // First, query word as-is (may include wait-symbols in input):
            DefaultQuery<LocalTimerMealySemanticInputSymbol<I>, Word<LocalTimerMealyOutputSymbol<O>>> rawCacheQuery = this.queryCache(word);

            // Next, convert query that includes wait-symbols to query with timeout-symbols:
            var convertedQuery = this.convertTimeSequences(rawCacheQuery);

            // The counterexample may only use a subset of the allowed inputs.
            // If so, cut the query to the prefix of the word that is allowed:
            var reducedQuery = (allInputsConsidered) ? convertedQuery : this.reduceToAllowedInputs(allowedInputs, convertedQuery);

            // Finally, query hypothesis using the converted query:
            Word<LocalTimerMealyOutputSymbol<O>> hypOutput = hypothesis.getSemantics().computeSuffixOutput(Word.epsilon(), reducedQuery.getInput());

            if (!hypOutput.equals(reducedQuery.getOutput())) {
                // Hyp gives different output than cache (= SUL):
                counterexamples.add(reducedQuery);
            }
        }

        if (counterexamples.isEmpty()) {
            return null;
        }

        // Take the shortest word:
        return counterexamples.stream().min(Comparator.comparingInt(w -> w.getInput().length())).get();
    }

}
