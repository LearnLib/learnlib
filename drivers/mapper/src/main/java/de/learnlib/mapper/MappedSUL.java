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
package de.learnlib.mapper;

import com.google.common.base.Optional;

import de.learnlib.api.SUL;
import de.learnlib.api.SULException;
import de.learnlib.mapper.api.Mapper;
import de.learnlib.mapper.api.Mapper.MappedException;

public class MappedSUL<AI, AO, CI, CO> implements SUL<AI, AO> {
	
	private final Mapper<? super AI,? extends AO,? extends CI,? super CO> mapper;
	private final SUL<? super CI,? extends CO> sul;
	
	private boolean inError = false;
	private AO repeatedErrorOutput = null;

	public MappedSUL(Mapper<? super AI,? extends AO,? extends CI,? super CO> mapper, SUL<? super CI,? extends CO> sul) {
		this.mapper = mapper;
		this.sul = sul;
	}

	@Override
	public void pre() {
		mapper.pre();
		sul.pre();
	}

	@Override
	public void post() {
		sul.post();
		mapper.post();
		this.inError = false;
		this.repeatedErrorOutput = null;
	}

	@Override
	public AO step(AI in) throws SULException {
		if(inError) {
			return repeatedErrorOutput;
		}
		
		CI concreteInput = mapper.mapInput(in);
		MappedException<? extends AO> mappedEx;
		try {
			CO concreteOutput = sul.step(concreteInput);
			return mapper.mapOutput(concreteOutput);
		}
		catch(SULException ex) {
			mappedEx = mapper.mapWrappedException(ex);
		}
		catch(RuntimeException ex) {
			mappedEx = mapper.mapUnwrappedException(ex);
		}
		Optional<? extends AO> repeatOutput = mappedEx
				.getSubsequentStepsOutput();

		if (repeatOutput.isPresent()) {
			this.inError = true;
			this.repeatedErrorOutput = repeatOutput.get();
		}

		return mappedEx.getThisStepOutput();
	}

}
