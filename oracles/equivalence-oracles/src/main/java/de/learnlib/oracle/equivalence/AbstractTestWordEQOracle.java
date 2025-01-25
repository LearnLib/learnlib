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
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import de.learnlib.logging.Category;
import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.DefaultQuery;
import net.automatalib.automaton.concept.Output;
import net.automatalib.common.util.collection.IteratorUtil;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract equivalence oracle that takes care of query batching and hypothesis checking and allows extending classes
 * to solely focus on test word generation by implementing {@link #generateTestWords(Output, Collection)}.
 * <p>
 * Being {@link Stream stream}-based, this oracle encourages the lazy computation of counterexamples, so that all
 * counterexamples do not have to be computed upfront, but only until the first valid counterexample is found.
 *
 * @param <A>
 *         hypothesis type
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output (domain) type
 */
public abstract class AbstractTestWordEQOracle<A extends Output<I, D>, I, D> implements EquivalenceOracle<A, I, D> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTestWordEQOracle.class);

    private final MembershipOracle<I, D> membershipOracle;
    private final int batchSize;

    public AbstractTestWordEQOracle(MembershipOracle<I, D> membershipOracle) {
        this(membershipOracle, 1);
    }

    public AbstractTestWordEQOracle(MembershipOracle<I, D> membershipOracle, int batchSize) {
        this.membershipOracle = membershipOracle;
        this.batchSize = batchSize;
    }

    @Override
    public @Nullable DefaultQuery<I, D> findCounterExample(A hypothesis, Collection<? extends I> inputs) {
        // Fail fast on empty inputs
        if (inputs.isEmpty()) {
            LOGGER.warn(Category.COUNTEREXAMPLE,
                        "Passed empty set of inputs to equivalence oracle; no counterexample can be found!");
            return null;
        }

        final Stream<Word<I>> testWordStream = generateTestWords(hypothesis, inputs);
        final Stream<DefaultQuery<I, D>> queryStream = testWordStream.map(DefaultQuery::new);
        final Stream<DefaultQuery<I, D>> answeredQueryStream = answerQueries(queryStream);

        final Stream<DefaultQuery<I, D>> ceStream = answeredQueryStream.filter(query -> {
            D hypOutput = hypothesis.computeOutput(query.getInput());
            return !Objects.equals(hypOutput, query.getOutput());
        });

        return ceStream.findFirst().orElse(null);
    }

    /**
     * Generate the stream of test words that should be used for the current equivalence check cycle.
     *
     * @param hypothesis
     *         the current hypothesis of the learning algorithm
     * @param inputs
     *         the collection of inputs to consider
     *
     * @return the stream of test words used for equivalence testing
     *
     * @see EquivalenceOracle#findCounterExample(Object, Collection)
     */
    protected abstract Stream<Word<I>> generateTestWords(A hypothesis, Collection<? extends I> inputs);

    private Stream<DefaultQuery<I, D>> answerQueries(Stream<DefaultQuery<I, D>> stream) {
        if (isBatched()) {
            return IteratorUtil.stream(IteratorUtil.batch(stream.iterator(), this.batchSize))
                               .peek(membershipOracle::processQueries)
                               .flatMap(List::stream);
        } else {
            return stream.peek(membershipOracle::processQuery);
        }
    }

    private boolean isBatched() {
        return this.batchSize > 1;
    }

}
