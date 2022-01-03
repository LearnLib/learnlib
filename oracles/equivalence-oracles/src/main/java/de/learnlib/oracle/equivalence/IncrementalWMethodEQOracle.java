/* Copyright (C) 2013-2022 TU Dortmund
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
package de.learnlib.oracle.equivalence;

import java.util.Collection;
import java.util.stream.Stream;

import com.google.common.collect.Streams;
import de.learnlib.api.oracle.EquivalenceOracle.DFAEquivalenceOracle;
import de.learnlib.api.oracle.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.buildtool.refinement.annotation.GenerateRefinement;
import de.learnlib.buildtool.refinement.annotation.Generic;
import de.learnlib.buildtool.refinement.annotation.Interface;
import de.learnlib.buildtool.refinement.annotation.Map;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.Output;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.util.automata.conformance.IncrementalWMethodTestsIterator;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

@GenerateRefinement(name = "DFAIncrementalWMethodEQOracle",
                    generics = "I",
                    parentGenerics = {@Generic(clazz = DFA.class, generics = {"?", "I"}),
                                      @Generic("I"),
                                      @Generic(clazz = Boolean.class)},
                    parameterMapping = @Map(from = MembershipOracle.class,
                                            to = DFAMembershipOracle.class,
                                            withGenerics = "I"),
                    interfaces = @Interface(clazz = DFAEquivalenceOracle.class, generics = "I"))
@GenerateRefinement(name = "MealyIncrementalWMethodEQOracle",
                    generics = {"I", "O"},
                    parentGenerics = {@Generic(clazz = MealyMachine.class, generics = {"?", "I", "?", "O"}),
                                      @Generic("I"),
                                      @Generic(clazz = Word.class, generics = "O")},
                    parameterMapping = @Map(from = MembershipOracle.class,
                                            to = MealyMembershipOracle.class,
                                            withGenerics = {"I", "O"}),
                    interfaces = @Interface(clazz = MealyEquivalenceOracle.class, generics = {"I", "O"}))
public class IncrementalWMethodEQOracle<A extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & Output<I, D>, I, D>
        extends AbstractTestWordEQOracle<A, I, D> {

    private final IncrementalWMethodTestsIterator<I> incrementalWMethodIt;

    public IncrementalWMethodEQOracle(MembershipOracle<I, D> oracle, Alphabet<I> alphabet) {
        this(oracle, alphabet, 1);
    }

    public IncrementalWMethodEQOracle(MembershipOracle<I, D> oracle, Alphabet<I> alphabet, int maxDepth) {
        this(oracle, alphabet, maxDepth, 1);
    }

    public IncrementalWMethodEQOracle(MembershipOracle<I, D> oracle,
                                      Alphabet<I> alphabet,
                                      int maxDepth,
                                      int batchSize) {
        super(oracle, batchSize);

        this.incrementalWMethodIt = new IncrementalWMethodTestsIterator<>(alphabet);
        this.incrementalWMethodIt.setMaxDepth(maxDepth);
    }

    public int getMaxDepth() {
        return this.incrementalWMethodIt.getMaxDepth();
    }

    public void setMaxDepth(int maxDepth) {
        this.incrementalWMethodIt.setMaxDepth(maxDepth);
    }

    @Override
    protected Stream<Word<I>> generateTestWords(A hypothesis, Collection<? extends I> inputs) {
        // FIXME: warn about inputs being ignored?
        incrementalWMethodIt.update(hypothesis);

        return Streams.stream(incrementalWMethodIt);
    }
}
