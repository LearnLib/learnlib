/* Copyright (C) 2013-2026 TU Dortmund University
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
package de.learnlib.cli.util;

import java.io.IOException;
import java.util.function.BiFunction;
import java.util.function.Function;

import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.cli.option.Options;
import de.learnlib.filter.statistic.oracle.CounterOracle;
import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.statistic.Statistics;
import de.learnlib.statistic.StatisticsService;
import de.learnlib.util.Experiment;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.concept.FiniteRepresentation;
import net.automatalib.serialization.InputModelSerializer;
import net.automatalib.ts.simple.SimpleTS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Runner<A extends Alphabet<I>, M extends SimpleTS<?, I> & FiniteRepresentation, I, D> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Runner.class);

    private final Function<Options, A> alphabetCreator;
    private final Function<Options, MembershipOracle<I, D>> mqoCreator;
    private final Function<Options, Constructor<A, M, I, D>> learnerCreator;
    private final BiFunction<Options, MembershipOracle<I, D>, EquivalenceOracle<M, I, D>> eqoCreator;
    private final Function<Options, InputModelSerializer<I, M>> serializerCreator;

    public Runner(Function<Options, A> alphabetCreator,
                  Function<Options, MembershipOracle<I, D>> mqoCreator,
                  Function<Options, Constructor<A, M, I, D>> learnerCreator,
                  BiFunction<Options, MembershipOracle<I, D>, EquivalenceOracle<M, I, D>> eqoCreator,
                  Function<Options, InputModelSerializer<I, M>> serializerCreator) {
        this.alphabetCreator = alphabetCreator;
        this.mqoCreator = mqoCreator;
        this.learnerCreator = learnerCreator;
        this.eqoCreator = eqoCreator;
        this.serializerCreator = serializerCreator;
    }

    @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
    public void run(Options options) {

        final A alphabet = alphabetCreator.apply(options);
        final MembershipOracle<I, D> mqo = mqoCreator.apply(options);

        final MembershipOracle<I, D> learnerOracle;
        if (options.statistics) {
            learnerOracle = new CounterOracle<>(mqo, "learner");
        } else {
            learnerOracle = mqo;
        }

        final LearningAlgorithm<M, I, D> learner =
                learnerCreator.apply(options).constructLearner(alphabet, learnerOracle);

        final MembershipOracle<I, D> eqoOracle;
        if (options.statistics) {
            eqoOracle = new CounterOracle<>(mqo, "eqo");
        } else {
            eqoOracle = mqo;
        }

        final EquivalenceOracle<M, I, D> eqo = eqoCreator.apply(options, eqoOracle);
        final InputModelSerializer<I, M> serializer = serializerCreator.apply(options);

        final Experiment<M> experiment = new Experiment<>(learner, eqo, alphabet);
        experiment.run();

        final M hyp = experiment.getFinalHypothesis();

        if (options.statistics) {
            StatisticsService service = Statistics.getService();
            LOGGER.info(service.print());
            service.clear();
        }

        try {
            if (options.output != null) {
                serializer.writeModel(options.output, hyp, alphabet);
            } else {
                serializer.writeModel(System.out, hyp, alphabet);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
