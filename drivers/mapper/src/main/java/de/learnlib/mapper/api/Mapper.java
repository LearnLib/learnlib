/* Copyright (C) 2014 TU Dortmund
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

import javax.annotation.Nonnull;

import com.google.common.base.Optional;

import de.learnlib.api.SUL;
import de.learnlib.api.SULException;
import de.learnlib.mapper.Mappers;

/**
 * A mapper that lifts a {@link SUL} from an "abstract" to a "concrete" level.
 * <p>
 * The notion of "abstract" and "concrete" is not universally defined, and mostly depends
 * on the chosen perspective. Generally speaking, the point of a {@code Mapper<AI,AO,CI,CO>}
 * is to translate a {@code SUL<CI,CO>} into a {@code SUL<AI,AO>}, and additionally provide
 * facilities to map exceptions occurring at the concrete level to symbols at the abstract level.
 * <p>
 * The class {@link Mappers} provides static utility functions for manipulating mappers.
 * <p>
 * Mappers, like {@link SUL}s, may be {@link SUL#fork() forkable}. The requirements and semantics
 * of {@link #fork()} are basically the same as set forth for {@link SUL#fork()}. Stateless
 * mappers (e.g., with empty {@link #pre()} and {@link #post()} implementations), should always
 * be forkable, and {@link #fork()} may just return {@code this}. Stateful mappers may require
 * more sophisticated fork logic, but in general it should be possible to fork them as well.
 * <p>
 * Note: despite the above recommendation that mappers should almost always be forkable,
 * the default implementations of {@link #canFork()} and {@link #fork()} indicate
 * non-forkability for backwards compatibility reasons.
 * 
 * @author Malte Isberner
 *
 * @param <AI> abstract input symbol type.
 * @param <AO> abstract output symbol type.
 * @param <CI> concrete input symbol type.
 * @param <CO> concrete output symbol type.
 */
public interface Mapper<AI,AO,CI,CO> {
	
	public static final class MappedException<AO> {
		
		public static <AO>
		MappedException<AO> ignoreAndContinue(AO output) {
			return new MappedException<>(output);
		}
		
		public static <AO>
		MappedException<AO> repeatOutput(AO thisStepOutput, AO subsequentOutput) {
			return new MappedException<>(thisStepOutput, subsequentOutput);
		}
		
		public static <AO>
		MappedException<AO> repeatOutput(AO output) {
			return repeatOutput(output, output);
		}
		
		public static <AO>
		MappedException<AO> pass(SULException exception) throws SULException {
			throw exception;
		}
		
		private final AO thisStepOutput;
		private final Optional<AO> subsequentStepsOutput;
		
		private MappedException(AO thisStepOutput, AO subsequentStepsOutput) {
			this.thisStepOutput = thisStepOutput;
			this.subsequentStepsOutput = Optional.of(subsequentStepsOutput);
		}
		
		private MappedException(AO output) {
			this.thisStepOutput = output;
			this.subsequentStepsOutput = Optional.absent();
		}
		
		public AO getThisStepOutput() {
			return thisStepOutput;
		}
		
		public Optional<AO> getSubsequentStepsOutput() {
			return subsequentStepsOutput;
		}
	}

	/**
	 * Method that is invoked before any translation steps on a word are performed.
	 */
	public void pre();
	
	/**
	 * Method that is invoked after all translation steps on a word are performed.
	 */
	public void post();
	
	/**
	 * Method that maps an abstract input to a corresponding concrete input.
	 * 
	 * @param abstractInput the abstract input
	 * @return the concrete input
	 */
	public CI mapInput(AI abstractInput);
	
	/**
	 * Method that maps a concrete output to a corresponding abstract output.
	 * 
	 * @param concreteOutput the concrete output
	 * @return the abstract output
	 */
	public AO mapOutput(CO concreteOutput);
	
	/**
	 * Maps a wrapped {@link SULException} to an abstract output symbol, or rethrows it
	 * if it is unmappable.
	 * 
	 * @param exception the wrapped exception that was thrown
	 * @return the concrete output symbol the exception was mapped to, if applicable
	 * @throws SULException if the exception cannot be mapped, or if a new exception
	 * occurs while trying to map the given exception
	 */
	public MappedException<? extends AO> mapWrappedException(SULException exception)
			throws SULException;
	
	/**
	 * Maps an unwrapped {@link RuntimeException} to an abstract output symbol, or rethrows
	 * it if it is unmappable.
	 * 
	 * @param exception the runtime exception that was thrown
	 * @return the concrete output symbol the exception was mapped to, if applicable
	 * @throws SULException if a new exception occurs while trying to map the given exception
	 * @throws RuntimeException if the given exception cannot be mapped, or if a new
	 * exception occurs while trying to map the given exception
	 */
	public MappedException<? extends AO> mapUnwrappedException(RuntimeException exception)
			throws SULException, RuntimeException;
	
	
	/**
	 * Checks whether it is possible to {@link #fork() fork} this mapper.
	 * @return {@code true} if this mapper can be forked, {@code false} otherwise.
	 */
	default public boolean canFork() {
		return false;
	}
	
	/**
	 * Forks this mapper, i.e., returns a mapper that behaves in exactly the same way
	 * as this mapper, but allows for being used independently of this mapper.
	 * <p>
	 * If {@link #canFork()} returned {@code false}, this method must throw an
	 * {@link UnsupportedOperationException}. Otherwise, it must return a
	 * non-{@code null} object representing the fork of this mapper.
	 * 
	 * @return a fork of this mapper (for stateless mappers, generally {@code this} should
	 * be returned)
	 * @throws UnsupportedOperationException if this mapper is not forkable
	 */
	@Nonnull
	default public Mapper<AI, AO, CI, CO> fork()
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
}
