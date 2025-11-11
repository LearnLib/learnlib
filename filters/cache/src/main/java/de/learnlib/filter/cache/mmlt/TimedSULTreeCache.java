package de.learnlib.filter.cache.mmlt;


import de.learnlib.algorithm.MMLTModelParams;
import de.learnlib.filter.cache.LearningCache.MMLTLearningCache;
import de.learnlib.oracle.EquivalenceOracle.MMLTEquivalenceOracle;
import de.learnlib.statistic.container.DummyStatsContainer;
import de.learnlib.statistic.container.LearnerStatsProvider;
import de.learnlib.statistic.container.StatsContainer;
import de.learnlib.sul.TimedSUL;
import net.automatalib.alphabet.impl.GrowingMapAlphabet;

import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimeStepSequence;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.graph.Graph;
import net.automatalib.graph.concept.GraphViewable;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;

/**
 * Caches queries sent to a LocalTimerMealySUL.
 *
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class TimedSULTreeCache<I, O> implements TimedSUL<I, O>,
                                                MMLTLearningCache<I, O>, GraphViewable, LearnerStatsProvider {
    private final TimedSUL<I, O> delegate;

    private final CacheTreeNode<I, O> cacheRoot;
    private CacheTreeNode<I, O> currentState;

    private final MMLTModelParams<O> modelParams;
    private final TimedOutput<O> silentOutput;
    private boolean cacheMiss;

    private StatsContainer stats = new DummyStatsContainer();

    @Override
    public void setStatsContainer(StatsContainer container) {
        this.stats = container;
    }

    public TimedSULTreeCache(TimedSUL<I, O> delegate, MMLTModelParams<O> modelParams) {
        this.delegate = delegate;
        this.modelParams = modelParams;
        this.silentOutput = new TimedOutput<>(modelParams.silentOutput());

        // Init cache:
        this.cacheRoot = new CacheTreeNode<>(null, null);
        this.currentState = null;
    }


    private void followCurrentPrefix() {
        this.delegate.pre();

        WordBuilder<TimedInput<I>> wbPrefix = new WordBuilder<>();

        var current = this.currentState;
        while (current.getParent() != null) {
            wbPrefix.append(current.getParentInput());
            current = current.getParent();
        }

        Word<TimedInput<I>> prefix = wbPrefix.reverse().toWord();
        this.delegate.follow(prefix);
    }

    @Override
    public TimedOutput<O> step(InputSymbol<I> input) {
        if (this.currentState == null) {
            throw new IllegalStateException();
        }

        if (!cacheMiss) {
            if (this.currentState.hasChild(input)) {
                TimedOutput<O> output = this.currentState.getOutput(input);
                this.currentState = this.currentState.getChild(input);
                return output;
            }
            this.followCurrentPrefix();
            this.cacheMiss = true;
        }

        // Cache miss -> query + insert:
        TimedOutput<O> output = this.delegate.step(input);
        this.currentState = this.currentState.addUntimedChild(input, output);
        return output;
    }


    @Override
    public @Nullable TimedOutput<O> timeoutStep(long maxTime) {
        if (currentState == null) {
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

            this.followCurrentPrefix();
            this.cacheMiss = true;
        }


        TimedOutput<O> timeoutStepResult = this.delegate.timeoutStep(remaining);
        if (timeoutStepResult == null) { // no timers here
            this.currentState = this.currentState.addTimeChild(remaining, this.silentOutput);
            return null;
        } else {
            this.currentState = this.currentState.addTimeChild(timeoutStepResult.delay(), new TimedOutput<>(timeoutStepResult.symbol()));
            return new TimedOutput<>(timeoutStepResult.symbol(), maxTime - remaining + timeoutStepResult.delay());
        }

    }


    @Override
    public void pre() {
        this.currentState = this.cacheRoot;
        this.cacheMiss = false;
    }

    @Override
    public void post() {
        this.currentState = null;

        if (this.cacheMiss) {
            this.delegate.post();
            stats.increaseCounter("Cache_Missed_Count", "Cache misses");
        } else {
            stats.increaseCounter("Cache_Hit_Count", "Cache hits");
        }
    }

    // -------------------------------------------------------

    /**
     * Returns the leaves of the cache tree.
     *
     * @return List of leaf nodes.
     */
    private List<CacheTreeNode<I, O>> getLeaves() {
        List<CacheTreeNode<I, O>> leaves = new ArrayList<>();

        Deque<CacheTreeNode<I, O>> unvisited = new ArrayDeque<>();
        unvisited.add(this.cacheRoot);

        while (!unvisited.isEmpty()) {
            CacheTreeNode<I, O> currentNode = unvisited.remove();

            int successors = 0;
            if (currentNode.hasTimeChild()) {
                unvisited.add(currentNode.getTimeoutChild());
                successors++;
            }

            for (var sym : currentNode.getUntimedChildren().keySet()) {
                unvisited.add(currentNode.getChild(sym));
                successors++;
            }
            if (successors == 0) { // leaf
                leaves.add(currentNode);
            }
        }

        return leaves;
    }

    @Override
    public List<Word<TimedInput<I>>> listAllWords() {
        List<CacheTreeNode<I, O>> leaves = this.getLeaves();

        List<Word<TimedInput<I>>> finalWords = new ArrayList<>(leaves.size());

        for (var leaf : leaves) {
            // Word builder capacity = number of predecessors:
            int symCount = leaf.getNumPredecessors();
            WordBuilder<TimedInput<I>> wbInput = new WordBuilder<>(symCount);

            // Move towards the root:
            var current = leaf;
            while (current.getParent() != null) {
                wbInput.append(current.getParentInput());
                current = current.getParent();
            }

            // Start at root -> flip buffer:
            wbInput.reverse();
            finalWords.add(wbInput.toWord());
        }

        return finalWords;
    }


    @Override
    public Graph<?, ?> graphView() {
        // Convert tree to a mealy automaton:
        CompactMealy<TimedInput<I>, TimedOutput<O>> mealy = new CompactMealy<>(new GrowingMapAlphabet<>());

        Map<CacheTreeNode<I, O>, Integer> stateMap = new HashMap<>();
        stateMap.put(this.cacheRoot, mealy.addInitialState());

        Deque<CacheTreeNode<I, O>> pending = new ArrayDeque<>();
        pending.add(this.cacheRoot);


        while (!pending.isEmpty()) {
            CacheTreeNode<I, O> current = pending.remove();

            if (current.hasTimeChild()) {
                var child = current.getTimeoutChild();
                if (!stateMap.containsKey(child)) {
                    stateMap.put(child, mealy.addState());
                    pending.add(child);
                }
                mealy.addAlphabetSymbol(new TimeStepSequence<>(current.getTimeout()));
                mealy.addTransition(stateMap.get(current), new TimeStepSequence<>(current.getTimeout()), stateMap.get(child), current.getTimeoutOutput());
            }

            for (var sym : current.getUntimedChildren().keySet()) {
                var child = current.getChild(sym);
                if (!stateMap.containsKey(child)) {
                    stateMap.put(child, mealy.addState());
                    pending.add(child);
                }
                mealy.addAlphabetSymbol(sym);
                mealy.addTransition(stateMap.get(current), sym, stateMap.get(child), current.getOutput(sym));
            }
        }

        return mealy.graphView();
    }

    @Override
    public MMLTEquivalenceOracle<I, O> createCacheConsistencyTest() {
        return new MMLTCacheConsistencyTest<>(this, this.modelParams);
    }

}
