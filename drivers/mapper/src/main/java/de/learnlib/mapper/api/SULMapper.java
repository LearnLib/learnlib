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
package de.learnlib.mapper.api;

import java.util.Optional;

import javax.annotation.Nonnull;

import de.learnlib.api.Mapper;
import de.learnlib.api.Mapper.SynchronousMapper;
import de.learnlib.api.SUL;
import de.learnlib.api.exception.SULException;
import de.learnlib.mapper.SULMappers;

/**
 * An extension of the {@link Mapper} interface specifically for {@link SUL}s.
 * <p>
 * The class {@link SULMappers} provides static utility functions for manipulating mappers.
 * <p>
 * SULMappers, like {@link SUL}s, may be {@link SUL#fork() forkable}. The requirements and semantics of {@link #fork()}
 * are basically the same as set forth for {@link SUL#fork()}. Stateless mappers (e.g., with empty {@link #pre()} and
 * {@link #post()} implementations), should always be forkable, and {@link #fork()} may just return {@code this}.
 * Stateful mappers may require more sophisticated fork logic, but in general it should be possible to fork them as
 * well.
 * <p>
 * Note: despite the above recommendation that mappers should almost always be forkable, the default implementations of
 * {@link #canFork()} and {@link #fork()} indicate non-forkability for backwards compatibility reasons.
 *
 * @param <AI>
 *         abstract input symbol type.
 * @param <AO>
 *         abstract output symbol type.
 * @param <CI>
 *         concrete input symbol type.
 * @param <CO>
 *         concrete output symbol type.
 *
 * @author Malte Isberner
 */
public interface SULMapper<AI, AO, CI, CO> extends SynchronousMapper<AI, AO, CI, CO> {

    /**
     * Checks whether it is possible to {@link #fork() fork} this mapper.
     *
     * @return {@code true} if this mapper can be forked, {@code false} otherwise.
     */
    default boolean canFork() {
        return false;
    }

    /**
     * Forks this mapper, i.e., returns a mapper that behaves in exactly the same way as this mapper, but allows for
     * being used independently of this mapper.
     * <p>
     * If {@link #canFork()} returned {@code false}, this method must throw an {@link UnsupportedOperationException}.
     * Otherwise, it must return a non-{@code null} object representing the fork of this mapper.
     *
     * @return a fork of this mapper (for stateless mappers, generally {@code this} should be returned)
     *
     * @throws UnsupportedOperationException
     *         if this mapper is not forkable
     */
    @Nonnull
    default SULMapper<AI, AO, CI, CO> fork() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Maps a wrapped {@link SULException} to an abstract output symbol, or rethrows it if it is unmappable.
     *
     * @param exception
     *         the wrapped exception that was thrown
     *
     * @return the concrete output symbol the exception was mapped to, if applicable
     *
     * @throws SULException
     *         if the exception cannot be mapped, or if a new exception occurs while trying to map the given exception
     */
    default MappedException<? extends AO> mapWrappedException(SULException exception) throws SULException {
        return mapUnwrappedException(exception);
    }

    /**
     * Maps an unwrapped {@link RuntimeException} to an abstract output symbol, or rethrows it if it is unmappable.
     *
     * @param exception
     *         the runtime exception that was thrown
     *
     * @return the concrete output symbol the exception was mapped to, if applicable
     *
     * @throws SULException
     *         if a new exception occurs while trying to map the given exception
     * @throws RuntimeException
     *         if the given exception cannot be mapped, or if a new exception occurs while trying to map the given
     *         exception
     */
    default MappedException<? extends AO> mapUnwrappedException(RuntimeException exception) {
        throw exception;
    }

    final class MappedException<AO> {

        private final AO thisStepOutput;
        private final Optional<AO> subsequentStepsOutput;

        private MappedException(AO thisStepOutput, AO subsequentStepsOutput) {
            this.thisStepOutput = thisStepOutput;
            this.subsequentStepsOutput = Optional.of(subsequentStepsOutput);
        }

        private MappedException(AO output) {
            this.thisStepOutput = output;
            this.subsequentStepsOutput = Optional.empty();
        }

        public static <AO> MappedException<AO> ignoreAndContinue(AO output) {
            return new MappedException<>(output);
        }

        public static <AO> MappedException<AO> repeatOutput(AO output) {
            return repeatOutput(output, output);
        }

        public static <AO> MappedException<AO> repeatOutput(AO thisStepOutput, AO subsequentOutput) {
            return new MappedException<>(thisStepOutput, subsequentOutput);
        }

        public static <AO> MappedException<AO> pass(SULException exception) throws SULException {
            throw exception;
        }

        public AO getThisStepOutput() {
            return thisStepOutput;
        }

        public Optional<AO> getSubsequentStepsOutput() {
            return subsequentStepsOutput;
        }
    }
}
