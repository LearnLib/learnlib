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
package de.learnlib.filter.cache.mmlt;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import de.learnlib.filter.cache.LearningCache.MMLTLearningCache;
import de.learnlib.filter.cache.mmlt.CacheTreeNode.CacheTreeTransition;
import de.learnlib.oracle.EquivalenceOracle.MMLTEquivalenceOracle;
import de.learnlib.statistic.Statistics;
import de.learnlib.statistic.StatisticsCollector;
import de.learnlib.sul.TimedSUL;
import de.learnlib.time.MMLTModelParams;
import net.automatalib.common.util.collection.AbstractSimplifiedIterator;
import net.automatalib.common.util.collection.IteratorUtil;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Caches queries sent to a {@link TimedSUL}.
 *
 * @param <I>
 *         input symbol type (of non-delaying inputs)
 * @param <O>
 *         output symbol type
 */
public class TimedSULTreeCache<I, O> implements TimedSUL<I, O>, MMLTLearningCache<I, O> {

    private final TimedSUL<I, O> delegate;

    private final CacheTreeNode<I, O> cacheRoot;
    private CacheTreeNode<I, O> currentState;

    private final MMLTModelParams<O> modelParams;
    private final TimedOutput<O> silentOutput;
    private boolean cacheMiss;
    private boolean init;

    private final StatisticsCollector statisticsCollector;

    public TimedSULTreeCache(TimedSUL<I, O> delegate, MMLTModelParams<O> modelParams) {
        this.delegate = delegate;
        this.modelParams = modelParams;
        this.silentOutput = new TimedOutput<>(modelParams.silentOutput());
        this.statisticsCollector = Statistics.getCollector();

        // Init cache:
        this.cacheRoot = new CacheTreeNode<>(null, null);
        this.currentState = this.cacheRoot;
    }

    private void followCurrentPrefix(CacheTreeNode<I, O> current) {
        Word<TimedInput<I>> prefix = extractWord(current);
        this.delegate.pre();
        this.delegate.follow(prefix);
    }

    @Override
    public TimedOutput<O> step(InputSymbol<I> input) {
        if (!init) {
            throw new IllegalStateException();
        }

        if (!cacheMiss) {
            if (this.currentState.hasChild(input)) {
                TimedOutput<O> output = this.currentState.getOutput(input);
                this.currentState = this.currentState.getChild(input);
                return output;
            }
            this.followCurrentPrefix(this.currentState);
            this.cacheMiss = true;
        }

        // Cache miss -> query + insert:
        TimedOutput<O> output = this.delegate.step(input);
        this.currentState = this.currentState.addUntimedChild(input, output);
        return output;
    }

    @Override
    public @Nullable TimedOutput<O> timeoutStep(long maxTime) {
        if (!init) {
            throw new IllegalStateException();
        }

        long remaining = maxTime;
        if (!this.cacheMiss) {
            // Move to closest state in cache:
            while (remaining > 0) {
                if (!currentState.hasTimeChild()) {
                    break; // cache miss
                }

                if (currentState.getTimeout() > remaining) {
                    // Split current timeout:
                    this.currentState = this.currentState.splitTimeout(remaining, this.silentOutput);
                    return null; // no timer in this state
                }

                TimedOutput<O> currentOutput = currentState.getTimeoutOutput();
                remaining -= currentState.getTimeout();
                this.currentState = this.currentState.getTimeoutChild();

                if (!currentOutput.equals(this.silentOutput)) {
                    // Found valid timeout:
                    return new TimedOutput<>(currentOutput.symbol(), maxTime - remaining);
                }
            }

            if (remaining == 0) {
                return null; // no timer in this state
            }

            this.followCurrentPrefix(this.currentState);
            this.cacheMiss = true;
        }

        TimedOutput<O> timeoutStepResult = this.delegate.timeoutStep(remaining);
        if (timeoutStepResult == null) { // no timers here
            this.currentState = this.currentState.addTimeChild(remaining, this.silentOutput);
            return null;
        } else {
            this.currentState = this.currentState.addTimeChild(timeoutStepResult.delay(),
                                                               new TimedOutput<>(timeoutStepResult.symbol()));
            return new TimedOutput<>(timeoutStepResult.symbol(), maxTime - remaining + timeoutStepResult.delay());
        }

    }

    @Override
    public void pre() {
        this.currentState = this.cacheRoot;
        this.cacheMiss = false;
        this.init = true;
    }

    @Override
    public void post() {
        this.init = false;

        if (this.cacheMiss) {
            this.delegate.post();
            statisticsCollector.increaseCounter("Cache_Missed_Count", "Cache misses");
        } else {
            statisticsCollector.increaseCounter("Cache_Hit_Count", "Cache hits");
        }
    }

    @Override
    public MMLTEquivalenceOracle<I, O> createCacheConsistencyTest() {
        return new MMLTCacheConsistencyTest<>(this, this.modelParams);
    }

    /**
     * Returns an iterator that traverses all words (leaves of this tree) in a BFS-style fashion.
     *
     * @return an iterator over all words of this tree
     */
    public Iterator<Word<TimedInput<I>>> allWordsIterator() {
        return IteratorUtil.map(new LeavesIterator<>(this.cacheRoot), this::extractWord);
    }

    private Word<TimedInput<I>> extractWord(CacheTreeNode<I, O> leaf) {
        final WordBuilder<TimedInput<I>> wb = new WordBuilder<>();

        // Move towards the root:
        CacheTreeNode<I, O> nodeIter = leaf.getParent();
        TimedInput<I> inputIter = leaf.getParentInput();
        while (nodeIter != null && inputIter != null) {
            wb.append(inputIter);
            inputIter = nodeIter.getParentInput();
            nodeIter = nodeIter.getParent();
        }

        // Start at root -> flip buffer:
        wb.reverse();
        return wb.toWord();
    }

    private static final class LeavesIterator<I, O> extends AbstractSimplifiedIterator<CacheTreeNode<I, O>> {

        private final Deque<CacheTreeNode<I, O>> queue;

        private LeavesIterator(CacheTreeNode<I, O> root) {
            this.queue = new ArrayDeque<>();
            this.queue.add(root);
        }

        @Override
        protected boolean calculateNext() {

            while (!queue.isEmpty()) {
                @SuppressWarnings("nullness") //false positive https://github.com/typetools/checker-framework/issues/399
                final @NonNull CacheTreeNode<I, O> node = queue.poll();

                boolean hasChildren = false;

                if (node.hasTimeChild()) {
                    queue.add(node.getTimeoutChild());
                    hasChildren = true;
                }

                for (CacheTreeTransition<I, O> t : node.getUntimedChildren().values()) {
                    queue.add(t.target());
                    hasChildren = true;
                }

                if (!hasChildren) {
                    super.nextValue = node;
                    return true;
                }
            }

            return false;
        }
    }
}
