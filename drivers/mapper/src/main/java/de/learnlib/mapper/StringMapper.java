/* Copyright (C) 2015 TU Dortmund
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
 * http://www.gnu.de/documents/lgpl.en.html.
 */
package de.learnlib.mapper;

import java.util.HashMap;
import java.util.Map;

import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.SimpleAlphabet;

public class StringMapper<CI> extends AbstractMapper<String, String, CI, Object> {
	
	private final Map<String,CI> inputs = new HashMap<>();
	private final Alphabet<String> mappedInputs = new SimpleAlphabet<>();

	public StringMapper(Alphabet<CI> alphabet) {
		for (CI input : alphabet) {
			String str = input.toString();
			inputs.put(str, input);
			mappedInputs.add(str);
		}
	}
	
	@Override
	public CI mapInput(String abstractInput) {
		return inputs.get(abstractInput);
	}

	@Override
	public String mapOutput(Object concreteOutput) {
		return concreteOutput.toString();
	}

	public Alphabet<String> getMappedInputs() {
		return mappedInputs;
	}
}
