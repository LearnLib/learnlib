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
package de.learnlib.oracle.equivalence.spa;

import java.util.Collection;
import java.util.stream.Stream;

import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.equivalence.AbstractTestWordEQOracle;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.procedural.SPA;
import net.automatalib.common.util.collection.IteratorUtil;
import net.automatalib.util.automaton.conformance.SPATestsIterator;
import net.automatalib.util.automaton.conformance.WMethodTestsIterator;
import net.automatalib.util.automaton.conformance.WpMethodTestsIterator;
import net.automatalib.word.Word;

/**
 * An {@link SPA} version of {@link de.learnlib.oracle.equivalence.WpMethodEQOracle} which generates test sequences
 * based on the partial W-method for each procedure.
 *
 * @param <I>
 *         input symbol type
 */
public class WpMethodEQOracle<I> extends AbstractTestWordEQOracle<SPA<?, I>, I, Boolean> {

    private final int lookahead;
    private final int expectedSize;

    /**
     * Constructor. Convenience method for {@link #WpMethodEQOracle(MembershipOracle, int)} that sets {@code lookahead}
     * to 1.
     *
     * @param sulOracle
     *         interface to the system under learning
     */
    public WpMethodEQOracle(MembershipOracle<I, Boolean> sulOracle) {
        this(sulOracle, 1);
    }

    /**
     * Constructor. Convenience method for {@link #WpMethodEQOracle(MembershipOracle, int, int)} that sets
     * {@code expectedSize} to 0.
     *
     * @param sulOracle
     *         interface to the system under learning
     * @param lookahead
     *         the maximum length of the "middle" part of the test cases
     */
    public WpMethodEQOracle(MembershipOracle<I, Boolean> sulOracle, int lookahead) {
        this(sulOracle, lookahead, 0);
    }

    /**
     * Constructor. Convenience method for {@link #WpMethodEQOracle(MembershipOracle, int, int, int)} that sets
     * {@code batchSize} to 1.
     *
     * @param sulOracle
     *         interface to the system under learning
     * @param lookahead
     *         the (minimal) maximum length of the "middle" part of the test cases
     * @param expectedSize
     *         the expected size of the system under learning
     */
    public WpMethodEQOracle(MembershipOracle<I, Boolean> sulOracle, int lookahead, int expectedSize) {
        this(sulOracle, lookahead, expectedSize, 1);
    }

    /**
     * Constructor. Uses
     * {@link Math#max(int, int) Math.max}{@code (lookahead, expectedSize - }{@link DFA#size() hypothesis.size()}{@code
     * )} (for each procedural {@code hypothesis}) to determine the maximum length of sequences, that should be appended
     * to the transition-cover part of the test sequence to account for the fact that the system under learning may have
     * more states than the current hypothesis.
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
    public WpMethodEQOracle(MembershipOracle<I, Boolean> sulOracle, int lookahead, int expectedSize, int batchSize) {
        super(sulOracle, batchSize);
        this.lookahead = lookahead;
        this.expectedSize = expectedSize;
    }

    @Override
    protected Stream<Word<I>> generateTestWords(SPA<?, I> hypothesis, Collection<? extends I> inputs) {
        if (!(inputs instanceof ProceduralInputAlphabet)) {
            throw new IllegalArgumentException("Inputs are not a procedural alphabet");
        }

        @SuppressWarnings("unchecked")
        final ProceduralInputAlphabet<I> alphabet = (ProceduralInputAlphabet<I>) inputs;

        return IteratorUtil.stream(new SPATestsIterator<>(hypothesis,
                                                          alphabet,
                                                          (dfa, alph) -> new WpMethodTestsIterator<>(dfa,
                                                                                                     alph,
                                                                                                     Math.max(lookahead,
                                                                                                              expectedSize -
                                                                                                              dfa.size()))));
    }
}
