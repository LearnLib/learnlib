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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimeStepSequence;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A node in the {@link TimedSULTreeCache}. A node has a parent and children for an arbitrary number of transitions with
 * a non-delaying input. There is at most one timed transition. This transition has a sequence of time steps as input.
 * The output is the output at the last time step in the sequence.
 *
 * @param <I>
 *         input symbol type (of non-delaying inputs)
 * @param <O>
 *         output symbol type
 */
class CacheTreeNode<I, O> {

    private @Nullable CacheTreeNode<I, O> parent;
    private @Nullable TimedInput<I> parentInput;
    private long timeout;
    private @Nullable CacheTreeTransition<I, O> timeTransition;
    private final Map<InputSymbol<I>, CacheTreeTransition<I, O>> untimedChildren;

    CacheTreeNode(@Nullable CacheTreeNode<I, O> parent, @Nullable TimedInput<I> parentInput) {
        this.parent = parent;
        this.parentInput = parentInput;

        this.timeTransition = null;
        this.timeout = -1;

        this.untimedChildren = new HashMap<>();
    }

    public CacheTreeNode<I, O> addTimeChild(long timeout, TimedOutput<O> output) {
        if (this.hasTimeChild()) {
            throw new IllegalStateException("State already has time child.");
        }

        CacheTreeNode<I, O> newChild = new CacheTreeNode<>(this, new TimeStepSequence<>(timeout));
        this.timeout = timeout;
        this.timeTransition = new CacheTreeTransition<>(output, newChild);
        return newChild;
    }

    // -------------------------------------------------------

    @EnsuresNonNullIf(result = true, expression = "this.timeTransition")
    public boolean hasTimeChild() {
        return this.timeTransition != null;
    }

    public long getTimeout() {
        if (!this.hasTimeChild()) {
            throw new IllegalStateException();
        }
        return timeout;
    }

    public TimedOutput<O> getTimeoutOutput() {
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
     * Breaks the time sequence: introduces a new child cx after the given number of time steps and adds the former
     * child as child to cx.
     *
     * @param newTimeout
     *         Time at which the timeout sequence is split
     * @param output
     *         Output at the end of the new time sequence
     *
     * @return New child node
     */
    public CacheTreeNode<I, O> splitTimeout(long newTimeout, TimedOutput<O> output) {
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

    public TimedInput<I> getParentInput() {
        return parentInput;
    }

    public void setParent(CacheTreeNode<I, O> parent, TimedInput<I> parentInput) {
        this.parent = parent;
        this.parentInput = parentInput;
    }

    // -------------------------------------------------------
    public boolean hasChild(InputSymbol<I> input) {
        return this.untimedChildren.containsKey(input);
    }

    public TimedOutput<O> getOutput(InputSymbol<I> input) {
        return this.untimedChildren.get(input).output();
    }

    public CacheTreeNode<I, O> getChild(InputSymbol<I> input) {
        return this.untimedChildren.get(input).target();
    }

    public CacheTreeNode<I, O> addUntimedChild(InputSymbol<I> input, TimedOutput<O> output) {
        if (untimedChildren.containsKey(input)) {
            throw new IllegalArgumentException("State already has an child for this input.");
        }

        CacheTreeNode<I, O> child = new CacheTreeNode<>(this, input);
        this.untimedChildren.put(input, new CacheTreeTransition<>(output, child));
        return child;
    }

    public Map<InputSymbol<I>, CacheTreeTransition<I, O>> getUntimedChildren() {
        return Collections.unmodifiableMap(this.untimedChildren);
    }

    public record CacheTreeTransition<I, O>(TimedOutput<O> output, CacheTreeNode<I, O> target) {}
}
