/* Copyright (C) 2013-2017 TU Dortmund
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
package de.learnlib.eqtests.basic;

import java.util.Collection;
import java.util.stream.Stream;

import com.google.common.collect.Streams;
import de.learnlib.api.MembershipOracle;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.Output;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.util.automata.conformance.IncrementalWMethodTestsIterator;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public class IncrementalWMethodEQOracle<A extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & Output<I, D>, I, D>
        extends AbstractTestWordEQOracle<A, I, D> {

    private final IncrementalWMethodTestsIterator<I> incrementalWMethodIt;
    private int maxDepth;

    public IncrementalWMethodEQOracle(Alphabet<I> alphabet, MembershipOracle<I, D> oracle) {
        this(alphabet, oracle, 1);
    }

    public IncrementalWMethodEQOracle(Alphabet<I> alphabet, MembershipOracle<I, D> oracle, int maxDepth) {
        this(alphabet, oracle, maxDepth, 1);
    }

    public IncrementalWMethodEQOracle(Alphabet<I> alphabet,
                                      MembershipOracle<I, D> oracle,
                                      int maxDepth,
                                      int batchSize) {
        super(oracle, batchSize);

        this.incrementalWMethodIt = new IncrementalWMethodTestsIterator<>(alphabet);
        this.incrementalWMethodIt.setMaxDepth(maxDepth);

        this.maxDepth = maxDepth;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.incrementalWMethodIt.setMaxDepth(maxDepth);
        this.maxDepth = maxDepth;
    }

    @Override
    protected Stream<Word<I>> generateTestWords(A hypothesis, Collection<? extends I> inputs) {
        // FIXME: warn about inputs being ignored?
        incrementalWMethodIt.update(hypothesis);

        return Streams.stream(incrementalWMethodIt);
    }

    public static class DFAIncrementalWMethodEQOracle<I> extends IncrementalWMethodEQOracle<DFA<?, I>, I, Boolean>
            implements DFAEquivalenceOracle<I> {

        public DFAIncrementalWMethodEQOracle(Alphabet<I> alphabet, MembershipOracle<I, Boolean> oracle) {
            super(alphabet, oracle);
        }

        public DFAIncrementalWMethodEQOracle(Alphabet<I> alphabet, MembershipOracle<I, Boolean> oracle, int maxDepth) {
            super(alphabet, oracle, maxDepth);
        }

        public DFAIncrementalWMethodEQOracle(Alphabet<I> alphabet,
                                             MembershipOracle<I, Boolean> oracle,
                                             int maxDepth,
                                             int batchSize) {
            super(alphabet, oracle, maxDepth, batchSize);
        }
    }

    public static class MealyIncrementalWMethodEQOracle<I, O>
            extends IncrementalWMethodEQOracle<MealyMachine<?, I, ?, O>, I, Word<O>>
            implements MealyEquivalenceOracle<I, O> {

        public MealyIncrementalWMethodEQOracle(Alphabet<I> alphabet, MembershipOracle<I, Word<O>> oracle) {
            super(alphabet, oracle);
        }

        public MealyIncrementalWMethodEQOracle(Alphabet<I> alphabet,
                                               MembershipOracle<I, Word<O>> oracle,
                                               int maxDepth) {
            super(alphabet, oracle, maxDepth);
        }

        public MealyIncrementalWMethodEQOracle(Alphabet<I> alphabet,
                                               MembershipOracle<I, Word<O>> oracle,
                                               int maxDepth,
                                               int batchSize) {
            super(alphabet, oracle, maxDepth, batchSize);
        }
    }

}
