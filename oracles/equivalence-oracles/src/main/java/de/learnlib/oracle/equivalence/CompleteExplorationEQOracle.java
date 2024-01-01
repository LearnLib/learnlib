/* Copyright (C) 2013-2024 TU Dortmund University
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
import de.learnlib.oracle.EquivalenceOracle.DFAEquivalenceOracle;
import de.learnlib.oracle.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.oracle.EquivalenceOracle.MooreEquivalenceOracle;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.oracle.MembershipOracle.MooreMembershipOracle;
import de.learnlib.tooling.annotation.refinement.GenerateRefinement;
import de.learnlib.tooling.annotation.refinement.Generic;
import de.learnlib.tooling.annotation.refinement.Interface;
import de.learnlib.tooling.annotation.refinement.Mapping;
import net.automatalib.automaton.concept.Output;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.MooreMachine;
import net.automatalib.common.util.collection.CollectionsUtil;
import net.automatalib.word.Word;

/**
 * Implements an equivalence check by complete exploration up to a given depth, i.e., by testing all possible sequences
 * of a certain length within a specified range.
 *
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 */
@GenerateRefinement(name = "DFACompleteExplorationEQOracle",
                    generics = "I",
                    parentGenerics = {@Generic(clazz = DFA.class, generics = {"?", "I"}),
                                      @Generic("I"),
                                      @Generic(clazz = Boolean.class)},
                    typeMapping = @Mapping(from = MembershipOracle.class,
                                           to = DFAMembershipOracle.class,
                                           generics = @Generic("I")),
                    interfaces = @Interface(clazz = DFAEquivalenceOracle.class, generics = @Generic("I")),
                    classDoc = "A {@link DFA}-specific refinement of {@link CompleteExplorationEQOracle}.\n" +
                               "@param <I> input symbol type\n")
@GenerateRefinement(name = "MealyCompleteExplorationEQOracle",
                    generics = {"I", "O"},
                    parentGenerics = {@Generic(clazz = MealyMachine.class, generics = {"?", "I", "?", "O"}),
                                      @Generic("I"),
                                      @Generic(clazz = Word.class, generics = "O")},
                    typeMapping = @Mapping(from = MembershipOracle.class,
                                           to = MealyMembershipOracle.class,
                                           generics = {@Generic("I"), @Generic("O")}),
                    interfaces = @Interface(clazz = MealyEquivalenceOracle.class,
                                            generics = {@Generic("I"), @Generic("O")}),
                    classDoc = "A {@link MealyMachine}-specific refinement of {@link CompleteExplorationEQOracle}.\n" +
                               "@param <I> input symbol type\n" +
                               "@param <O> output symbol type\n")
@GenerateRefinement(name = "MooreCompleteExplorationEQOracle",
                    generics = {"I", "O"},
                    parentGenerics = {@Generic(clazz = MooreMachine.class, generics = {"?", "I", "?", "O"}),
                                      @Generic("I"),
                                      @Generic(clazz = Word.class, generics = "O")},
                    typeMapping = @Mapping(from = MembershipOracle.class,
                                           to = MooreMembershipOracle.class,
                                           generics = {@Generic("I"), @Generic("O")}),
                    interfaces = @Interface(clazz = MooreEquivalenceOracle.class,
                                            generics = {@Generic("I"), @Generic("O")}),
                    classDoc = "A {@link MooreMachine}-specific refinement of {@link CompleteExplorationEQOracle}.\n" +
                               "@param <I> input symbol type\n" +
                               "@param <O> output symbol type\n")
public class CompleteExplorationEQOracle<A extends Output<I, D>, I, D> extends AbstractTestWordEQOracle<A, I, D> {

    private final int minDepth;
    private final int maxDepth;

    /**
     * Constructor.
     *
     * @param sulOracle
     *         interface to the system under learning
     * @param maxDepth
     *         maximum exploration depth
     */
    public CompleteExplorationEQOracle(MembershipOracle<I, D> sulOracle, int maxDepth) {
        this(sulOracle, 1, maxDepth);
    }

    /**
     * Constructor.
     *
     * @param sulOracle
     *         interface to the system under learning
     * @param minDepth
     *         minimum exploration depth
     * @param maxDepth
     *         maximum exploration depth
     */
    public CompleteExplorationEQOracle(MembershipOracle<I, D> sulOracle, int minDepth, int maxDepth) {
        this(sulOracle, minDepth, maxDepth, 1);
    }

    /**
     * Constructor.
     *
     * @param sulOracle
     *         interface to the system under learning
     * @param minDepth
     *         minimum exploration depth
     * @param maxDepth
     *         maximum exploration depth
     * @param batchSize
     *         size of the batches sent to the membership oracle
     */
    public CompleteExplorationEQOracle(MembershipOracle<I, D> sulOracle, int minDepth, int maxDepth, int batchSize) {
        super(sulOracle, batchSize);
        this.minDepth = Math.min(minDepth, maxDepth);
        this.maxDepth = Math.max(minDepth, maxDepth);
    }

    @Override
    protected Stream<Word<I>> generateTestWords(A hypothesis, Collection<? extends I> inputs) {
        return Streams.stream(CollectionsUtil.allTuples(inputs, minDepth, maxDepth)).map(Word::fromList);
    }
}
