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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.function.BiFunction;
import java.util.function.Function;

import de.learnlib.Resumable;
import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.cli.option.Options;
import de.learnlib.logging.Category;
import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.statistic.Statistics;
import de.learnlib.statistic.StatisticsService;
import de.learnlib.util.Experiment;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.concept.FiniteRepresentation;
import net.automatalib.serialization.InputModelSerializer;
import net.automatalib.ts.simple.SimpleTS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRunner<A extends Alphabet<I>, M extends SimpleTS<?, I> & FiniteRepresentation, I, D, OR>
        implements Runner {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRunner.class);

    public static final String LEARNER_KEY = "learner";
    public static final String EQO_KEY = "eqo";

    private final Function<Options, A> alphabetCreator;
    private final BiFunction<Options, ? super A, OR> mqoCreator;
    private final Function<Options, Constructor<A, M, I, D, OR>> learnerCreator;
    private final BiFunction<Options, OR, EquivalenceOracle<M, I, D>> eqoCreator;
    private final Function<Options, InputModelSerializer<I, M>> serializerCreator;

    public AbstractRunner(Function<Options, A> alphabetCreator,
                          BiFunction<Options, ? super A, OR> mqoCreator,
                          Function<Options, Constructor<A, M, I, D, OR>> learnerCreator,
                          BiFunction<Options, OR, EquivalenceOracle<M, I, D>> eqoCreator,
                          Function<Options, InputModelSerializer<I, M>> serializerCreator) {
        this.alphabetCreator = alphabetCreator;
        this.mqoCreator = mqoCreator;
        this.learnerCreator = learnerCreator;
        this.eqoCreator = eqoCreator;
        this.serializerCreator = serializerCreator;
    }

    @Override
    public void run(Options options) {

        final A alphabet = alphabetCreator.apply(options);
        final OR mqo = mqoCreator.apply(options, alphabet);

        final OR learnerOracle;
        if (options.statistics) {
            learnerOracle = getCounter(mqo, LEARNER_KEY);
        } else {
            learnerOracle = mqo;
        }

        final LearningAlgorithm<M, I, D> learner =
                learnerCreator.apply(options).constructLearner(alphabet, learnerOracle);

        final OR eqoOracle;
        if (options.statistics) {
            eqoOracle = getCounter(mqo, EQO_KEY);
        } else {
            eqoOracle = mqo;
        }

        final EquivalenceOracle<M, I, D> eqo = eqoCreator.apply(options, eqoOracle);
        final InputModelSerializer<I, M> serializer = serializerCreator.apply(options);
        final Experiment<M, I, D> experiment = buildExperiment(learner, eqo, alphabet, serializer, options);

        final M hyp = experiment.run();

        if (options.statistics) {
            final StatisticsService service = Statistics.getService();
            LOGGER.info(Category.STATISTIC, service.print());
        }

        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            serializer.writeModel(baos, hyp, alphabet);

            LOGGER.info(Category.MODEL, "Final hypothesis:\n{}", baos.toString(StandardCharsets.UTF_8));
            if (options.output != null) {
                Files.write(options.output, baos.toByteArray());
            }
        } catch (IOException e) {
            LOGGER.warn("Could not write hypothesis", e);
        }
    }

    private Experiment<M, I, D> buildExperiment(LearningAlgorithm<M, I, D> learner,
                                                EquivalenceOracle<M, I, D> eqo,
                                                Alphabet<I> alphabet,
                                                InputModelSerializer<I, M> serializer,
                                                Options options) {
        if (learner instanceof Resumable<?> r) {
            return new SnapshottingExperiment<>(learner, r, eqo, alphabet, serializer, options);
        } else {
            if (options.resumeFrom != null || options.snapshotDir != null) {
                throw new IllegalArgumentException(String.format(
                        "Resuming learning processes is not supported by '%s' ('%s')",
                        options.learner,
                        options.type));
            }
            return new Experiment<>(learner, eqo, alphabet, serializer);
        }
    }

    protected abstract OR getCounter(OR delegate, String id);
}
