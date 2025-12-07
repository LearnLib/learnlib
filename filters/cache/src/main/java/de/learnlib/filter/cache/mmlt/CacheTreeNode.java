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
import org.checkerframework.checker.nullness.qual.EnsuresKeyFor;
import org.checkerframework.checker.nullness.qual.EnsuresKeyForIf;
import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf;
import org.checkerframework.checker.nullness.qual.KeyFor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.PolyNull;

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

    CacheTreeNode(@PolyNull CacheTreeNode<I, O> parent, @PolyNull TimedInput<I> parentInput) {
        this.parent = parent;
        this.parentInput = parentInput;

        this.timeTransition = null;
        this.timeout = -1;

        this.untimedChildren = new HashMap<>();
    }

    CacheTreeNode<I, O> addTimeChild(long timeout, TimedOutput<O> output) {
        assert !this.hasTimeChild() : "State already has time child.";
        CacheTreeNode<I, O> newChild = new CacheTreeNode<>(this, new TimeStepSequence<>(timeout));
        this.timeout = timeout;
        this.timeTransition = new CacheTreeTransition<>(output, newChild);
        return newChild;
    }

    // -------------------------------------------------------

    @EnsuresNonNullIf(result = true, expression = "this.timeTransition")
    boolean hasTimeChild() {
        return this.timeTransition != null;
    }

    long getTimeout() {
        assert this.hasTimeChild();
        return timeout;
    }

    TimedOutput<O> getTimeoutOutput() {
        assert this.hasTimeChild();
        return this.timeTransition.output();
    }

    CacheTreeNode<I, O> getTimeoutChild() {
        assert this.hasTimeChild();
        return this.timeTransition.target();
    }

    /**
     * Breaks the time sequence: introduces a new child cx after the given number of time steps and adds the former
     * child as child to cx.
     *
     * @param newTimeout
     *         the time at which the timeout sequence is split
     * @param output
     *         the output at the end of the new time sequence
     *
     * @return the new child node
     */
    CacheTreeNode<I, O> splitTimeout(long newTimeout, TimedOutput<O> output) {
        CacheTreeTransition<I, O> trans = this.timeTransition;
        assert trans != null && newTimeout < this.getTimeout() : "Must split at lower timeout.";

        CacheTreeNode<I, O> newChild = new CacheTreeNode<>(this, new TimeStepSequence<>(newTimeout));
        newChild.timeout = this.timeout - newTimeout;
        newChild.timeTransition = trans; // keep output + target
        trans.target().setParent(newChild, new TimeStepSequence<>(this.timeout - newTimeout));

        this.timeout = newTimeout;
        this.timeTransition = new CacheTreeTransition<>(output, newChild);

        return newChild;
    }

    // -------------------------------------------------------
    @Nullable CacheTreeNode<I, O> getParent() {
        return parent;
    }

    @Nullable TimedInput<I> getParentInput() {
        return parentInput;
    }

    void setParent(CacheTreeNode<I, O> parent, TimedInput<I> parentInput) {
        this.parent = parent;
        this.parentInput = parentInput;
    }

    // -------------------------------------------------------
    @EnsuresKeyForIf(result = true, expression = "#1", map = "this.untimedChildren")
    boolean hasChild(InputSymbol<I> input) {
        return this.untimedChildren.containsKey(input);
    }

    TimedOutput<O> getOutput(@KeyFor("this.untimedChildren") InputSymbol<I> input) {
        return this.untimedChildren.get(input).output();
    }

    CacheTreeNode<I, O> getChild(@KeyFor("this.untimedChildren") InputSymbol<I> input) {
        return this.untimedChildren.get(input).target();
    }

    @EnsuresKeyFor(value = "#1", map = "this.untimedChildren")
    CacheTreeNode<I, O> addUntimedChild(InputSymbol<I> input, TimedOutput<O> output) {
        assert !untimedChildren.containsKey(input) : "State already has an child for this input.";
        CacheTreeNode<I, O> child = new CacheTreeNode<>(this, input);
        this.untimedChildren.put(input, new CacheTreeTransition<>(output, child));
        return child;
    }

    Map<InputSymbol<I>, CacheTreeTransition<I, O>> getUntimedChildren() {
        return Collections.unmodifiableMap(this.untimedChildren);
    }

    record CacheTreeTransition<I, O>(TimedOutput<O> output, CacheTreeNode<I, O> target) {}
}
