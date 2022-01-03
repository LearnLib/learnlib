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
import net.automatalib.util.automata.conformance.WpMethodTestsIterator;
import net.automatalib.words.Word;

/**
 * Implements an equivalence test by applying the Wp-method test on the given hypothesis automaton, as described in
 * "Test Selection Based on Finite State Models" by S. Fujiwara et al.
 *
 * @param <A>
 *         automaton type
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 *
 * @author Malte Isberner
 * @author frohme
 */
@GenerateRefinement(name = "DFAWpMethodEQOracle",
                    generics = "I",
                    parentGenerics = {@Generic(clazz = DFA.class, generics = {"?", "I"}),
                                      @Generic("I"),
                                      @Generic(clazz = Boolean.class)},
                    parameterMapping = @Map(from = MembershipOracle.class,
                                            to = DFAMembershipOracle.class,
                                            withGenerics = "I"),
                    interfaces = @Interface(clazz = DFAEquivalenceOracle.class, generics = "I"))
@GenerateRefinement(name = "MealyWpMethodEQOracle",
                    generics = {"I", "O"},
                    parentGenerics = {@Generic(clazz = MealyMachine.class, generics = {"?", "I", "?", "O"}),
                                      @Generic("I"),
                                      @Generic(clazz = Word.class, generics = "O")},
                    parameterMapping = @Map(from = MembershipOracle.class,
                                            to = MealyMembershipOracle.class,
                                            withGenerics = {"I", "O"}),
                    interfaces = @Interface(clazz = MealyEquivalenceOracle.class, generics = {"I", "O"}))
public class WpMethodEQOracle<A extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & Output<I, D>, I, D>
        extends AbstractTestWordEQOracle<A, I, D> {

    private final int lookahead;
    private final int expectedSize;

    /**
     * Constructor. Convenience method for {@link #WpMethodEQOracle(MembershipOracle, int, int)} that sets {@code
     * expectedSize} to 0.
     *
     * @param sulOracle
     *         interface to the system under learning
     * @param lookahead
     *         the maximum length of the "middle" part of the test cases
     */
    public WpMethodEQOracle(MembershipOracle<I, D> sulOracle, int lookahead) {
        this(sulOracle, lookahead, 0);
    }

    /**
     * Constructor. Convenience method for {@link #WpMethodEQOracle(MembershipOracle, int, int, int)} that sets {@code
     * batchSize} to 1.
     *
     * @param sulOracle
     *         interface to the system under learning
     * @param lookahead
     *         the (minimal) maximum length of the "middle" part of the test cases
     * @param expectedSize
     *         the expected size of the system under learning
     */
    public WpMethodEQOracle(MembershipOracle<I, D> sulOracle, int lookahead, int expectedSize) {
        this(sulOracle, lookahead, expectedSize, 1);
    }

    /**
     * Constructor. Uses {@link Math#max(int, int) Math.max}{@code (lookahead, expectedSize - }{@link
     * UniversalDeterministicAutomaton#size() hypothesis.size()}{@code )} to determine the maximum length of sequences,
     * that should be appended to the state-cover (first phase) and remaining transition-cover (second phase) part of
     * the test sequence to account for the fact that the system under learning may have more states than the current
     * hypothesis.
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
     * @see WpMethodTestsIterator
     */
    public WpMethodEQOracle(MembershipOracle<I, D> sulOracle, int lookahead, int expectedSize, int batchSize) {
        super(sulOracle, batchSize);
        this.lookahead = lookahead;
        this.expectedSize = expectedSize;
    }

    @Override
    protected Stream<Word<I>> generateTestWords(A hypothesis, Collection<? extends I> inputs) {
        return Streams.stream(new WpMethodTestsIterator<>(hypothesis,
                                                          inputs,
                                                          Math.max(lookahead, expectedSize - hypothesis.size())));
    }
}
