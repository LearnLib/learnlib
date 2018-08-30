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
package de.learnlib.util;

import java.util.LinkedList;
import java.util.Queue;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.api.oracle.AutomatonOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.DeterministicAutomaton;
import net.automatalib.words.Word;

/**
 * An {@link AutomatonOracle} that processes words in a breadth-first manner.
 *
 * @param <A> the automaton type
 * @param <I> the input type
 * @param <D> the output type
 *
 * @author Jeroen Meijer
 */
@ParametersAreNonnullByDefault
public abstract class AbstractBFOracle<A extends DeterministicAutomaton<?, I, ?>, I, D>
        implements AutomatonOracle<A, I, D> {

    /**
     * The queue containing the words to find a counterexample with.
     */
    private final Queue<Word<I>> queue = new LinkedList<>();

    /**
     * The {@link MembershipOracle} used to answer {@link DefaultQuery}s.
     */
    private final MembershipOracle<I, D> membershipOracle;

    private double multiplier;

    protected AbstractBFOracle(MembershipOracle<I, D> membershipOracle, double multiplier) {
        this.membershipOracle = membershipOracle;
        this.multiplier = multiplier;
    }

    @Override
    public DefaultQuery<I, D> processInput(A hypothesis, Word<I> input) {
        final DefaultQuery<I, D> query = new DefaultQuery<>(input);
        membershipOracle.processQuery(query);

        return query;
    }

    @Override
    public double getMultiplier() {
        return multiplier;
    }

    @Override
    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public MembershipOracle<I, D> getMembershipOracle() {
        return membershipOracle;
    }

    /**
     * Returns the next input word, by popping from a queue.
     *
     * @see AutomatonOracle#nextInput()
     */
    @Nullable
    @Override
    public Word<I> nextInput() {
        return queue.poll();
    }

    /**
     * Adds a new input word to the queue.
     *
     * @see AutomatonOracle#addWord(Word)
     */
    @Override
    public void addWord(Word<I> input) {
        queue.add(input);
    }

    /**
     * Clears the queue.
     */
    @Override
    public void pre() {
        queue.clear();
        addWord(Word.epsilon());
    }
}
