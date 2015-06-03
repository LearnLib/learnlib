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

import de.learnlib.api.SULException;
import de.learnlib.mapper.api.Mapper;

final class MapperComposition<AI, AO, ACI, CAO, CI, CO> implements Mapper<AI,AO,CI,CO> {
	
	private final Mapper<? super AI,? extends AO,ACI,CAO> mapper1;
	private final Mapper<? super ACI,? extends CAO,? extends CI,? super CO> mapper2;
	
	

	public MapperComposition(Mapper<? super AI,? extends AO,ACI,CAO> outerMapper,
			Mapper<? super ACI,? extends CAO,? extends CI,? super CO> innerMapper) {
		this.mapper1 = outerMapper;
		this.mapper2 = innerMapper;
	}

	@Override
	public void pre() {
		mapper1.pre();
		mapper2.pre();
	}

	@Override
	public void post() {
		mapper2.post();
		mapper1.post();
	}

	@Override
	public CI mapInput(AI abstractInput) {
		ACI aci = mapper1.mapInput(abstractInput);
		return mapper2.mapInput(aci);
	}

	@Override
	public AO mapOutput(CO concreteOutput) {
		CAO cao = mapper2.mapOutput(concreteOutput);
		return mapper1.mapOutput(cao);
	}

	@Override
	public MappedException<? extends AO> mapWrappedException(SULException exception) {
		MappedException<? extends CAO> mappedEx;
		try {
			mappedEx = mapper2.mapWrappedException(exception);
		}
		catch(SULException ex) {
			return mapper1.mapWrappedException(ex);
		}
		catch(RuntimeException ex) {
			return mapper1.mapUnwrappedException(ex);
		}
		
		return mapMappedException(mappedEx);
	}

	@Override
	public MappedException<? extends AO> mapUnwrappedException(
			RuntimeException exception) throws RuntimeException {
		MappedException<? extends CAO> mappedEx;
		try {
			mappedEx = mapper2.mapUnwrappedException(exception);
		}
		catch(SULException ex) {
			return mapper1.mapWrappedException(ex);
		}
		catch(RuntimeException ex) {
			return mapper1.mapUnwrappedException(ex);
		}
		
		return mapMappedException(mappedEx);
	}

	@Override
	public boolean canFork() {
		return mapper1.canFork() && mapper2.canFork();
	}
	
	@Override
	public Mapper<AI,AO,CI,CO> fork() {
		return new MapperComposition<>(mapper1.fork(), mapper2.fork());
	}
	
	private MappedException<? extends AO> mapMappedException(MappedException<? extends CAO> mappedEx) {
		AO thisStepOutput = mapper1.mapOutput(mappedEx.getThisStepOutput());
		Optional<? extends CAO> repeatOutput = mappedEx.getSubsequentStepsOutput();
		if(repeatOutput.isPresent()) {
			AO repeatOutputMapped = mapper1.mapOutput(repeatOutput.get());
			return MappedException.repeatOutput(thisStepOutput, repeatOutputMapped);
		}
		return MappedException.ignoreAndContinue(thisStepOutput);
	}
	
	
}
