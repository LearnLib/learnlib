package de.learnlib.filter.cache.mmlt;

import net.automatalib.alphabet.time.mmlt.LocalTimerMealySemanticInputSymbol;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealyOutputSymbol;
import net.automatalib.alphabet.time.mmlt.NonDelayingInput;
import net.automatalib.alphabet.time.mmlt.TimeStepSequence;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A node in the tree cache used by the MMLT learner.
 * <p>
 * A node has a parent and children for an arbitrary number of transitions
 * with a non-delaying input.
 * There is at most one timed transition.
 * This transition has a sequence of time steps as input.
 * The output is the output at the last time step in the sequence.
 *
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class CacheTreeNode<I, O> {
    private record CacheTreeTransition<I, O>(LocalTimerMealyOutputSymbol<O> output, CacheTreeNode<I, O> target) {
    }


    @Nullable
    private CacheTreeNode<I, O> parent;
    private LocalTimerMealySemanticInputSymbol<I> parentInput;

    private long timeout;
    @Nullable
    private CacheTreeTransition<I, O> timeTransition;

    private Map<NonDelayingInput<I>, CacheTreeTransition<I, O>> untimedChildren;

    public CacheTreeNode(CacheTreeNode<I, O> parent, LocalTimerMealySemanticInputSymbol<I> parentInput) {
        this.parent = parent;
        this.parentInput = parentInput;

        this.timeTransition = null;
        this.timeout = -1;

        this.untimedChildren = new HashMap<>();
    }

    public CacheTreeNode<I, O> addTimeChild(long timeout, LocalTimerMealyOutputSymbol<O> output) {
        if (this.hasTimeChild()) {
            throw new IllegalStateException("State already has time child.");
        }

        CacheTreeNode<I, O> newChild = new CacheTreeNode<>(this, new TimeStepSequence<>(timeout));
        this.timeout = timeout;
        this.timeTransition = new CacheTreeTransition<>(output, newChild);
        return newChild;
    }


    // -------------------------------------------------------

    /**
     * Infers the number of predecessor nodes, i.e., the level
     * of this node.
     *
     * @return Number of predecessors. Zero if this is the cache root.
     */
    public int getNumPredecessors() {
        int parentCount = 0;
        var current = this;

        while (current.getParent() != null) {
            parentCount++;
            current = current.getParent();
        }
        return parentCount;
    }

    public boolean hasTimeChild() {
        return this.timeTransition != null;
    }

    public long getTimeout() {
        if (!this.hasTimeChild()) {
            throw new IllegalStateException();
        }
        return timeout;
    }

    public LocalTimerMealyOutputSymbol<O> getTimeoutOutput() {
        if (!this.hasTimeChild()) {
            throw new IllegalStateException();
        }
        return this.timeTransition.output();
    }

    public CacheTreeNode<I, O> getTimeoutChild() {
        if (!this.hasTimeChild()) {
            throw new IllegalStateException();
        }
        return this.timeTransition.target();
    }

    /**
     * Breaks the time sequence: introduces a new child cx after the given number of time steps
     * and adds the former child as child to cx.
     *
     * @param newTimeout Time at which the timeout sequence is split
     * @param output     Output at the end of the new time sequence
     * @return New child node
     */
    public CacheTreeNode<I, O> splitTimeout(long newTimeout, LocalTimerMealyOutputSymbol<O> output) {
        if (this.timeTransition == null || newTimeout >= this.getTimeout()) {
            throw new IllegalArgumentException("Must split at lower timeout.");
        }

        CacheTreeNode<I, O> newChild = new CacheTreeNode<>(this, new TimeStepSequence<>(newTimeout));
        newChild.timeout = this.timeout - newTimeout;
        newChild.timeTransition = this.timeTransition; // keep output + target
        this.timeTransition.target().setParent(newChild, new TimeStepSequence<>(this.timeout - newTimeout));

        this.timeout = newTimeout;
        this.timeTransition = new CacheTreeTransition<>(output, newChild);

        return newChild;
    }

    // -------------------------------------------------------
    public CacheTreeNode<I, O> getParent() {
        return parent;
    }

    public LocalTimerMealySemanticInputSymbol<I> getParentInput() {
        return parentInput;
    }

    public void setParent(CacheTreeNode<I, O> parent, LocalTimerMealySemanticInputSymbol<I> parentInput) {
        this.parent = parent;
        this.parentInput = parentInput;
    }

    // -------------------------------------------------------
    public boolean hasChild(NonDelayingInput<I> input) {
        return this.untimedChildren.containsKey(input);
    }

    public LocalTimerMealyOutputSymbol<O> getOutput(NonDelayingInput<I> input) {
        return this.untimedChildren.get(input).output();
    }

    public CacheTreeNode<I, O> getChild(NonDelayingInput<I> input) {
        return this.untimedChildren.get(input).target();
    }

    public CacheTreeNode<I, O> addUntimedChild(NonDelayingInput<I> input, LocalTimerMealyOutputSymbol<O> output) {
        if (untimedChildren.containsKey(input)) {
            throw new IllegalArgumentException("State already has an child for this input.");
        }

        CacheTreeNode<I, O> child = new CacheTreeNode<>(this, input);
        this.untimedChildren.put(input, new CacheTreeTransition<>(output, child));
        return child;
    }

    public Map<NonDelayingInput<I>, CacheTreeTransition<I, O>> getUntimedChildren() {
        return Collections.unmodifiableMap(this.untimedChildren);
    }
}