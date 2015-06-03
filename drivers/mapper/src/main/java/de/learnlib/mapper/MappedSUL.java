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
		this.inError = false;
		this.repeatedErrorOutput = null;
		mapper.pre();
		sul.pre();
	}

	@Override
	public void post() {
		sul.post();
		mapper.post();
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
	
	@Override
	public boolean canFork() {
		return mapper.canFork() && sul.canFork();
	}
	
	@Override
	public MappedSUL<AI,AO,CI,CO> fork() {
		return new MappedSUL<>(mapper.fork(), sul.fork());
	}

}
