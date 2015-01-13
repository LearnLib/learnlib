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
package de.learnlib.counterexamples;

import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.words.Word;
import de.learnlib.acex.AcexAnalyzer;
import de.learnlib.acex.analyzers.NamedAcexAnalyzer;
import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.counterexamples.prefixtransform.PrefixTransformAcex;

/**
 * Wraps a {@link NamedAcexAnalyzer}. This class is both responsible for adapting
 * it to the standard LearnLib {@link LocalSuffixFinder} interface, and for
 * maintaining statistics. Hence, a new object of this class should be instantiated
 * for every learning process.
 *  
 * @author Malte Isberner
 *
 */
public class AcexLocalSuffixFinder implements LocalSuffixFinder<Object, Object> {
	
	public static <RI, RO> int findSuffixIndex(
			AcexAnalyzer analyzer,
			boolean reduce,
			Query<RI, RO> ceQuery,
			AccessSequenceTransformer<RI> asTransformer,
			SuffixOutput<RI, RO> hypOutput, MembershipOracle<RI, RO> oracle) {
		
		Word<RI> counterexample = ceQuery.getInput();
		
		// Create the view of an abstract counterexample
		PrefixTransformAcex<RI, RO> acex
			= new PrefixTransformAcex<>(counterexample, oracle, asTransformer, hypOutput);
		
		int start = 0;
		
		if (reduce) {
			start = acex.getReductionPotential();
		}
		
		int idx = analyzer.analyzeAbstractCounterexample(acex, start);
		
		// Note: There is an off-by-one mismatch between the old and the new interface
		return idx + 1;
	}

	private final AcexAnalyzer analyzer;
	private final boolean reduce;
	private final String name;
	
	/**
	 * Constructor.
	 * @param analyzer the analyzer to be wrapped
	 * @param reduce whether or not to reduce counterexamples
	 */
	public AcexLocalSuffixFinder(AcexAnalyzer analyzer, boolean reduce, String name) {
		this.analyzer = analyzer;
		this.reduce = reduce;
		this.name = name;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.learnlib.counterexamples.LocalSuffixFinder#findSuffixIndex(de.learnlib.api.Query, de.learnlib.api.AccessSequenceTransformer, net.automatalib.automata.concepts.SuffixOutput, de.learnlib.api.MembershipOracle)
	 */
	@Override
	public <RI, RO> int findSuffixIndex(Query<RI, RO> ceQuery,
			AccessSequenceTransformer<RI> asTransformer,
			SuffixOutput<RI, RO> hypOutput, MembershipOracle<RI, RO> oracle) {
		
		return findSuffixIndex(analyzer, reduce, ceQuery, asTransformer, hypOutput, oracle);
	}
	
	@Override
	public String toString() {
		return name;
	}
}
