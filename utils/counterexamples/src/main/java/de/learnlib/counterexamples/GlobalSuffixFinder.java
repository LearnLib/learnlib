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

import java.util.List;

import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.words.Word;
import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;

/**
 * Interface for a global suffix finder. A global suffix finder takes a counterexample
 * (plus other supplemental information), and returns a list of words that, when used
 * as distinguishing suffixes, will expose at least one additional state in the hypothesis.
 * <p>
 * Please note that the type parameters of these class only constitute <i>upper</i> bounds
 * for the respective input symbol and output classes, denoting the requirements of the
 * process in general. A suffix finder which does not
 * exploit any properties of the used classes will implement this interface with
 * <tt>&lt;Object,Object&gt;</tt> generic arguments only. The genericity is still maintained
 * due to the <tt>RI</tt> and <tt>RO</tt> generic parameters in the
 * {@link #findSuffixes(Query, AccessSequenceTransformer, SuffixOutput, MembershipOracle)}
 * method.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type upper bound
 * @param <D> output domain type upper bound
 */
public interface GlobalSuffixFinder<I,D> {
	
	/**
	 * Finds a set of distinguishing suffixes which will allow to expose at least one additional
	 * state in the hypothesis.
	 * @param <RI> real input symbol type used for *this* counterexample analysis
	 * @param <RD> real output domain type used for *this* counterexample analysis
	 * @param ceQuery the counterexample query that triggered the refinement. Note that the same
	 * restrictions as in {@link LearningAlgorithm#refineHypothesis(de.learnlib.oracles.DefaultQuery)}
	 * apply.
	 * @param asTransformer an {@link AccessSequenceTransformer} used for access sequence transformation,
	 * if applicable.
	 * @param hypOutput interface to the output generation of the hypothesis, with the aim of
	 * comparing outputs of the hypothesis and the SUL.
	 * @param oracle interface to the System Under Learning (SUL).
	 * @return a set of distinguishing suffixes, or the empty set if the counterexample
	 * could not be analyzed.
	 */
	public <RI extends I,RD extends D>
	List<? extends Word<RI>> findSuffixes(Query<RI,RD> ceQuery,
			AccessSequenceTransformer<RI> asTransformer,
			SuffixOutput<RI,RD> hypOutput,
			MembershipOracle<RI,RD> oracle);
	
}
