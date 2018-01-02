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
package de.learnlib.oracle.equivalence;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Streams;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.concepts.Output;
import net.automatalib.commons.util.collections.BatchingIterator;
import net.automatalib.words.Word;
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
 *
 * @author frohme
 */
public abstract class AbstractTestWordEQOracle<A extends Output<I, D>, I, D> implements EquivalenceOracle<A, I, D> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTestWordEQOracle.class);

    private final MembershipOracle<I, D> membershipOracle;
    private final int batchSize;

    public AbstractTestWordEQOracle(MembershipOracle<I, D> membershipOracle) {
        this(membershipOracle, 1);
    }

    public AbstractTestWordEQOracle(MembershipOracle<I, D> membershipOracle, int batchSize) {
        Preconditions.checkArgument(batchSize > 0);

        this.membershipOracle = membershipOracle;
        this.batchSize = batchSize;
    }

    @Nullable
    @Override
    public DefaultQuery<I, D> findCounterExample(A hypothesis, Collection<? extends I> inputs) {
        // Fail fast on empty inputs
        if (inputs.isEmpty()) {
            LOGGER.warn("Passed empty set of inputs to equivalence oracle; no counterexample can be found!");
            return null;
        }

        final Stream<Word<I>> testWordStream = generateTestWords(hypothesis, inputs);
        final Stream<DefaultQuery<I, D>> queryStream = testWordStream.map(DefaultQuery<I, D>::new);
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

    private Stream<DefaultQuery<I, D>> answerQueries(final Stream<DefaultQuery<I, D>> stream) {
        if (isBatched()) {
            /*
             * FIXME: currently necessary because of a bug in the JDK
             * see https://bugs.openjdk.java.net/browse/JDK-8075939
             */
            return Streams.stream(Streams.stream(new BatchingIterator<>(stream.iterator(), this.batchSize))
                                         .peek(membershipOracle::processQueries)
                                         .flatMap(List::stream)
                                         .iterator());
        } else {
            return stream.peek(membershipOracle::processQuery);
        }
    }

    private boolean isBatched() {
        return this.batchSize > 1;
    }

}