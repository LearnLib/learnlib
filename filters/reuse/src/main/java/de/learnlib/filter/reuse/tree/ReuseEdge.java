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
package de.learnlib.filter.reuse.tree;

/**
 * A {@link ReuseEdge} connects two {@link ReuseNode}'s in the {@link ReuseTree} and is labeled with input and output
 * behavior. Please note that an edge may be reflexive if domain knowledge is used (input is invariant and/or output is
 * a failure output).
 *
 * @param <S>
 *         system state class
 * @param <I>
 *         input symbol class
 * @param <O>
 *         output symbol class
 */
public class ReuseEdge<S, I, O> {

    private final ReuseNode<S, I, O> source;
    private final ReuseNode<S, I, O> target;
    private final I input;
    private final O output;

    /**
     * Default constructor.
     *
     * @param source
     *         the source node
     * @param target
     *         the target node
     * @param input
     *         the input symbol
     * @param output
     *         the output symbol
     */
    public ReuseEdge(ReuseNode<S, I, O> source, ReuseNode<S, I, O> target, I input, O output) {
        this.source = source;
        this.target = target;
        this.input = input;
        this.output = output;
    }

    /**
     * The source node from this edge.
     *
     * @return the source
     */
    public ReuseNode<S, I, O> getSource() {
        return source;
    }

    /**
     * The target node from this edge.
     *
     * @return the target
     */
    public ReuseNode<S, I, O> getTarget() {
        return target;
    }

    /**
     * The respective input on this edge.
     *
     * @return the input
     */
    public I getInput() {
        return input;
    }

    /**
     * The respective output on this edge.
     *
     * @return the output
     */
    public O getOutput() {
        return output;
    }

    @Override
    public String toString() {
        return source + " -> " + target + " i/o " + input + "/" + output;
    }
}
