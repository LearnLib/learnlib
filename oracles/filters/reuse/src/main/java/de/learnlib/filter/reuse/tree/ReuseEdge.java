/* Copyright (C) 2013-2018 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
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
package de.learnlib.filter.reuse.tree;

/**
 * A {@link ReuseEdge} connects two {@link ReuseNode}'s in the {@link ReuseTree} and is labeled with input and output
 * behavior. Please note that a edge may be reflexive if domain knowledge is used (input is invariant and/or output is a
 * failure output).
 *
 * @param <S>
 *         system state class
 * @param <I>
 *         input symbol class
 * @param <O>
 *         output symbol class
 *
 * @author Oliver Bauer
 */
public class ReuseEdge<S, I, O> {

    private final ReuseNode<S, I, O> source;
    private final ReuseNode<S, I, O> target;
    private final I input;
    private final O output;

    /**
     * Default constructor.
     *
     * @param source,
     *         not allowed to be {@code null}.
     * @param target,
     *         not allowed to be {@code null}.
     * @param input,
     *         not allowed to be {@code null}.
     * @param output,
     *         in case of quiescence maybe {@code null}.
     */
    public ReuseEdge(final ReuseNode<S, I, O> source, final ReuseNode<S, I, O> target, final I input, final O output) {
        if (source == null) {
            throw new IllegalArgumentException("Source not allowed to be null.");
        }
        if (target == null) {
            throw new IllegalArgumentException("Target not allowed to be null.");
        }
        if (input == null) {
            throw new IllegalArgumentException("Input not allowed to be null.");
        }
        this.source = source;
        this.target = target;
        this.input = input;
        this.output = output;
    }

    /**
     * The source node from this edge.
     *
     * @return source, never {@code null}.
     */
    public final ReuseNode<S, I, O> getSource() {
        return source;
    }

    /**
     * The target node from this edge.
     *
     * @return target, never {@code null}.
     */
    public final ReuseNode<S, I, O> getTarget() {
        return target;
    }

    /**
     * The respective input on this edge, never {@code null}.
     *
     * @return input, not {@code null}
     */
    public final I getInput() {
        return input;
    }

    /**
     * The respective output on this edge. In case of quiescence the output is {@code null}.
     *
     * @return output
     */
    public final O getOutput() {
        return output;
    }

    @Override
    public final String toString() {
        return source.toString() + " -> " + target.toString() + " i/o " + input + "/" + output;
    }
}