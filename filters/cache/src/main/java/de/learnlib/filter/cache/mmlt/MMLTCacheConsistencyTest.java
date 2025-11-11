package de.learnlib.filter.cache.mmlt;

import de.learnlib.algorithm.MMLTModelParams;
import de.learnlib.oracle.EquivalenceOracle.MMLTEquivalenceOracle;
import de.learnlib.query.DefaultQuery;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimeStepSequence;
import net.automatalib.symbol.time.TimeoutSymbol;
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
public class MMLTCacheConsistencyTest<I, O> implements MMLTEquivalenceOracle<I, O> {
    private final static Logger logger = LoggerFactory.getLogger(MMLTCacheConsistencyTest.class);

    private final TimedSULTreeCache<I, O> sulCache;
    private final MMLTModelParams<O> modelParams;

    MMLTCacheConsistencyTest(TimedSULTreeCache<I, O> sulCache, MMLTModelParams<O> modelParams) {
        this.sulCache = sulCache;
        this.modelParams = modelParams;
    }

    private DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> queryCache(Word<TimedInput<I>> word) {
        WordBuilder<TimedInput<I>> wbInput = new WordBuilder<>();
        WordBuilder<TimedOutput<O>> wbOutput = new WordBuilder<>();

        this.sulCache.pre();
        for (var sym : word) {
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
                throw new AssertionError("Symbol type must not be used in cache.");
            }
        }
        this.sulCache.post();

        return new DefaultQuery<>(wbInput.toWord(), wbOutput.toWord());
    }

    /**
     * The cache does not use timeout symbols. Using these instead of time-step-sequences has several performance benefits.
     * This function converts a query with a time-step-sequence to one that uses timeout symbols where possible.
     *
     * @param originalQuery Original query
     * @return Converted query
     */
    private DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> convertTimeSequences(DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> originalQuery) {
        WordBuilder<TimedInput<I>> wbInput = new WordBuilder<>();
        WordBuilder<TimedOutput<O>> wbOutput = new WordBuilder<>();

        int symIdx = 0;
        var queryInput = originalQuery.getInput();
        var queryOutput = originalQuery.getOutput();

        while (symIdx < queryInput.length()) {
            var inputSym = queryInput.getSymbol(symIdx);
            var outputSym = queryOutput.getSymbol(symIdx);
            symIdx++;

            if (inputSym instanceof InputSymbol<I> ds) {
                wbInput.append(ds);
                wbOutput.append(outputSym);
            } else if (inputSym instanceof TimeStepSequence<I> ws) {
                if (!outputSym.symbol().equals(this.modelParams.silentOutput()) || ws.timeSteps() == this.modelParams.maxTimeoutWaitingTime()) {
                    // Found a timeout OR no timeout after max_delay:
                    wbInput.append(new TimeoutSymbol<>());
                    wbOutput.append(outputSym);
                    continue;
                }
                if (ws.timeSteps() >= this.modelParams.maxTimeoutWaitingTime()) {
                    throw new AssertionError("Wait time that exceeds max_delay in cache.");
                }

                // Special case: silent output before max delay
                // Cannot replace with "timeout", as this implies wait until max_delay.
                // Hence: skip subsequent waits until reaching wait with output OR max_delay OR end of word:
                long combinedWaitTime = ws.timeSteps();
                TimedOutput<O> combinedOutput = outputSym;

                while (combinedOutput.symbol().equals(this.modelParams.silentOutput()) && combinedWaitTime < this.modelParams.maxTimeoutWaitingTime()
                        && symIdx < queryInput.length() &&
                        queryInput.getSymbol(symIdx) instanceof TimeStepSequence<I> nextWs) {
                    combinedWaitTime += nextWs.timeSteps();
                    combinedOutput = queryOutput.getSymbol(symIdx);
                    symIdx++;
                }

                if (combinedWaitTime >= this.modelParams.maxTimeoutWaitingTime() || !combinedOutput.symbol().equals(this.modelParams.silentOutput())) {
                    wbInput.append(new TimeoutSymbol<>());

                    if (combinedOutput.symbol().equals(this.modelParams.silentOutput())) {
                        // Reached max delay -> waiting for any time will now produce no more timeouts:
                        wbOutput.append(new TimedOutput<>(this.modelParams.silentOutput()));
                    } else {
                        // Found non-silent output:
                        wbOutput.append(new TimedOutput<>(combinedOutput.symbol(), combinedWaitTime));
                    }
                } else {
                    // Reached end of word before max_delay OR non-wait symbol -> ignore rest of this word:
                    if (symIdx < queryInput.length() - 1) {
                        logger.debug("Ignoring at least one symbol during cache comparison.");
                    }
                    break;
                }
            }
        }
        return new DefaultQuery<>(wbInput.toWord(), wbOutput.toWord());
    }

    private DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> reduceToAllowedInputs(Set<TimedInput<I>> allowedInputs, DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> query) {
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
    public @Nullable DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> findCounterExample(MMLT<?, I, ?, O> hypothesis, Collection<? extends TimedInput<I>> inputs) {
        Set<TimedInput<I>> allowedInputs = new HashSet<>(inputs);
        boolean allInputsConsidered = allowedInputs.containsAll(hypothesis.getSemantics().getInputAlphabet());

        // Query all cached words:
        List<Word<TimedInput<I>>> cachedWords = this.sulCache.listAllWords();

        List<DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>>> counterexamples = new ArrayList<>();
        for (var word : cachedWords) {
            // First, query word as-is (may include wait-symbols in input):
            DefaultQuery<TimedInput<I>, Word<TimedOutput<O>>> rawCacheQuery = this.queryCache(word);

            // Next, convert query that includes wait-symbols to query with timeout-symbols:
            var convertedQuery = this.convertTimeSequences(rawCacheQuery);

            // The counterexample may only use a subset of the allowed inputs.
            // If so, cut the query to the prefix of the word that is allowed:
            var reducedQuery = (allInputsConsidered) ? convertedQuery : this.reduceToAllowedInputs(allowedInputs, convertedQuery);

            // Finally, query hypothesis using the converted query:
            Word<TimedOutput<O>> hypOutput = hypothesis.getSemantics().computeSuffixOutput(Word.epsilon(), reducedQuery.getInput());

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
