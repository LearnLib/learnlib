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
package de.learnlib.acex;

/**
 * Interface for an analyzer of {@link AbstractCounterexample}s.
 * 
 * @author Malte Isberner
 *
 */
public interface AcexAnalyzer {
	
	/**
	 * Analyzes an abstract counterexample. This method returns the index of
	 * the corresponding distinguishing suffix.
	 * 
	 * @param acex the abstract counterexample
	 * @return the suffix index
	 */
	default public int analyzeAbstractCounterexample(AbstractCounterexample acex) {
		return analyzeAbstractCounterexample(acex, 0, acex.getLength());
	}
	
	default public int analyzeAbstractCounterexample(AbstractCounterexample acex, int low) {
		return analyzeAbstractCounterexample(acex, low, acex.getLength());
	}
	
	public int analyzeAbstractCounterexample(AbstractCounterexample acex, int low, int high);
	
}
