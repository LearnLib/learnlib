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
package de.learnlib.acex.analyzers;

import de.learnlib.acex.AcexAnalyzer;


/**
 * An abstract counterexample analyzer that carries a name.
 * 
 * @author Malte Isberner
 *
 */
public abstract class NamedAcexAnalyzer implements AcexAnalyzer {
	
	private final String name;

	/**
	 * Constructor.
	 * @param name the name of the counterexample analyzer
	 */
	public NamedAcexAnalyzer(String name) {
		this.name = name;
	}
	

	/**
	 * Retrieves the name of this analyzer.
	 * @return the name of this analyzer
	 */
	public String getName() {
		return name;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getName();
	}
	
}
