/* Copyright (C) 2014 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 *
 * LearnLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 *
 * LearnLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with LearnLib; if not, see
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package de.learnlib.mapper.api;

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
	
}
