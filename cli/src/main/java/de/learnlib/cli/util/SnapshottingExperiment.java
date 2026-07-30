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
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import de.learnlib.Resumable;
import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.cli.option.Options;
import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.util.Experiment;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.concept.FiniteRepresentation;
import net.automatalib.serialization.InputModelSerializer;
import org.apache.fory.Fory;
import org.apache.fory.annotation.Nullable;
import org.apache.fory.exception.ForyException;

final class SnapshottingExperiment<A extends FiniteRepresentation, I, D> extends Experiment<A, I, D> {

    private static final Fory FORY;
    private static final DateTimeFormatter DTF;

    static {
        FORY = Fory.builder()
                   .requireClassRegistration(false)
                   .withCodegen(false)
                   .withRefTracking(true)
                   .withXlang(false)
                   .build();
        DTF = DateTimeFormatter.ofPattern("-yyyyMMdd-HHmmss-");
    }

    private final Resumable<?> resumable;
    private final String fingerPrint;

    private final @Nullable Path resumeFrom;
    private final @Nullable Path snapshotDir;

    SnapshottingExperiment(LearningAlgorithm<? extends A, I, D> learningAlgorithm,
                           Resumable<?> resumable,
                           EquivalenceOracle<? super A, I, D> equivalenceAlgorithm,
                           Alphabet<I> inputs,
                           InputModelSerializer<I, ? super A> serializer,
                           Options options) {
        super(learningAlgorithm, equivalenceAlgorithm, inputs, serializer);

        this.resumable = resumable;
        this.fingerPrint = DTF.format(LocalDateTime.now());
        this.resumeFrom = options.resumeFrom;
        this.snapshotDir = options.snapshotDir;

        if (this.resumeFrom != null && !this.resumeFrom.toFile().isFile()) {
            throw new IllegalArgumentException(String.format("Provided resume path '%s' is not a file",
                                                             options.resumeFrom));
        }

        if (this.snapshotDir != null && !this.snapshotDir.toFile().isDirectory()) {
            throw new IllegalArgumentException(String.format("Provided snapshot path '%s' is not a directory",
                                                             options.snapshotDir));
        }
    }

    @Override
    protected void initializeLearning() {
        if (this.resumeFrom == null) {
            super.initializeLearning();
        } else {
            LOGGER.info("Resuming learning process from file '{}'", resumeFrom);
            try {
                resumLearner(this.resumable);
            } catch (IOException | ForyException e) {
                LOGGER.warn("Could not resume learning process. Starting from scratch...", e);
                super.initializeLearning();
            }
        }
    }

    private <T> void resumLearner(Resumable<T> resumable) throws IOException {
        @SuppressWarnings("unchecked")
        final T state = (T) FORY.deserialize(Files.readAllBytes(resumeFrom));
        resumable.resume(state);
    }

    @Override
    protected void postRefinementHook() {
        if (this.snapshotDir == null) {
            super.postRefinementHook();
        } else {
            snapshotState(this.resumable);
        }
    }

    private <T> void snapshotState(Resumable<T> resumable) {
        final T suspend = resumable.suspend();
        final String round = Integer.toString(getRound() - 1); // do not count startLearning round
        final String fileName = "learnlib" + fingerPrint + round + ".bin";
        LOGGER.info("Writing snapshot to file '{}'", fileName);
        try {
            Files.write(this.snapshotDir.resolve(fileName), FORY.serialize(suspend));
        } catch (IOException | ForyException e) {
            LOGGER.warn("Could not write learner state. Continuing without...", e);
        }
    }
}
