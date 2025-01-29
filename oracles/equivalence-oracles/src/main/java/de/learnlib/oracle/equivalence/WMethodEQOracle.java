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
package de.learnlib.oracle.equivalence;

import java.util.Collection;
import java.util.stream.Stream;

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
import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.automaton.concept.Output;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.MooreMachine;
import net.automatalib.common.util.collection.IteratorUtil;
import net.automatalib.util.automaton.conformance.WMethodTestsIterator;
import net.automatalib.word.Word;

/**
 * Implements an equivalence test by applying the W-method test on the given hypothesis automaton, as described in
 * <a href="https://doi.org/10.1109/TSE.1978.231496">Testing Software Design Modeled by Finite-State Machines</a> by
 * T.&nbsp;S.&nbsp;Chow.
 *
 * @param <A>
 *         automaton type
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 */
@GenerateRefinement(name = "DFAWMethodEQOracle",
                    generics = @Generic(value = "I", desc = "input symbol type"),
                    parentGenerics = {@Generic(clazz = DFA.class, generics = {"?", "I"}),
                                      @Generic("I"),
                                      @Generic(clazz = Boolean.class)},
                    typeMappings = @Mapping(from = MembershipOracle.class,
                                            to = DFAMembershipOracle.class,
                                            generics = @Generic("I")),
                    interfaces = @Interface(clazz = DFAEquivalenceOracle.class, generics = @Generic("I")))
@GenerateRefinement(name = "MealyWMethodEQOracle",
                    generics = {@Generic(value = "I", desc = "input symbol type"),
                                @Generic(value = "O", desc = "output symbol type")},
                    parentGenerics = {@Generic(clazz = MealyMachine.class, generics = {"?", "I", "?", "O"}),
                                      @Generic("I"),
                                      @Generic(clazz = Word.class, generics = "O")},
                    typeMappings = @Mapping(from = MembershipOracle.class,
                                            to = MealyMembershipOracle.class,
                                            generics = {@Generic("I"), @Generic("O")}),
                    interfaces = @Interface(clazz = MealyEquivalenceOracle.class,
                                            generics = {@Generic("I"), @Generic("O")}))
@GenerateRefinement(name = "MooreWMethodEQOracle",
                    generics = {@Generic(value = "I", desc = "input symbol type"),
                                @Generic(value = "O", desc = "output symbol type")},
                    parentGenerics = {@Generic(clazz = MooreMachine.class, generics = {"?", "I", "?", "O"}),
                                      @Generic("I"),
                                      @Generic(clazz = Word.class, generics = "O")},
                    typeMappings = @Mapping(from = MembershipOracle.class,
                                            to = MooreMembershipOracle.class,
                                            generics = {@Generic("I"), @Generic("O")}),
                    interfaces = @Interface(clazz = MooreEquivalenceOracle.class,
                                            generics = {@Generic("I"), @Generic("O")}))
public class WMethodEQOracle<A extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & Output<I, D>, I, D>
        extends AbstractTestWordEQOracle<A, I, D> {

    private final int lookahead;
    private final int expectedSize;

    /**
     * Constructor. Convenience method for {@link #WMethodEQOracle(MembershipOracle, int)} that sets {@code lookahead}
     * to 1.
     *
     * @param sulOracle
     *         interface to the system under learning
     */
    public WMethodEQOracle(MembershipOracle<I, D> sulOracle) {
        this(sulOracle, 1);
    }

    /**
     * Constructor. Convenience method for {@link #WMethodEQOracle(MembershipOracle, int, int)} that sets
     * {@code expectedSize} to 0.
     *
     * @param sulOracle
     *         interface to the system under learning
     * @param lookahead
     *         the maximum length of the "middle" part of the test cases
     */
    public WMethodEQOracle(MembershipOracle<I, D> sulOracle, int lookahead) {
        this(sulOracle, lookahead, 0);
    }

    /**
     * Constructor. Convenience method for {@link #WMethodEQOracle(MembershipOracle, int, int, int)} that sets
     * {@code batchSize} to 1.
     *
     * @param sulOracle
     *         interface to the system under learning
     * @param lookahead
     *         the (minimal) maximum length of the "middle" part of the test cases
     * @param expectedSize
     *         the expected size of the system under learning
     */
    public WMethodEQOracle(MembershipOracle<I, D> sulOracle, int lookahead, int expectedSize) {
        this(sulOracle, lookahead, expectedSize, 1);
    }

    /**
     * Constructor. Uses
     * {@link Math#max(int, int) Math.max}{@code (lookahead, expectedSize - }{@link
     * UniversalDeterministicAutomaton#size() hypothesis.size()}{@code )} to determine the maximum length of sequences,
     * that should be appended to the transition-cover part of the test sequence to account for the fact that the system
     * under learning may have more states than the current hypothesis.
     *
     * @param sulOracle
     *         interface to the system under learning
     * @param lookahead
     *         the (minimal) maximum length of the "middle" part of the test cases
     * @param expectedSize
     *         the expected size of the system under learning
     * @param batchSize
     *         size of the batches sent to the membership oracle
     *
     * @see WMethodTestsIterator
     */
    public WMethodEQOracle(MembershipOracle<I, D> sulOracle, int lookahead, int expectedSize, int batchSize) {
        super(sulOracle, batchSize);
        this.lookahead = lookahead;
        this.expectedSize = expectedSize;
    }

    @Override
    protected Stream<Word<I>> generateTestWords(A hypothesis, Collection<? extends I> inputs) {
        return IteratorUtil.stream(new WMethodTestsIterator<>(hypothesis,
                                                              inputs,
                                                              Math.max(lookahead, expectedSize - hypothesis.size())));
    }
}
