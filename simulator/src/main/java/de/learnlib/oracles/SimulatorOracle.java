/* Copyright (C) 2013 TU Dortmund
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
package de.learnlib.oracles;

import java.util.Collection;

import net.automatalib.automata.concepts.SuffixOutput;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;

/**
 * A membership oracle backed by an automaton. The automaton must implement
 * the {@link SuffixOutput} concept, allowing to identify a suffix part in the output
 * (relative to a prefix/suffix subdivision in the input).
 *   
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <I> input symbol class
 * @param <O> (suffix) output class
 */
public class SimulatorOracle<I, O> implements MembershipOracle<I, O> {
	
	private final SuffixOutput<I, O> automaton;
	
	/**
	 * Constructor.
	 * @param automaton the suffix-observable automaton
	 */
	public SimulatorOracle(SuffixOutput<I,O> automaton) {
		this.automaton = automaton;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.MembershipOracle#processQueries(java.util.Collection)
	 */
	@Override
	public void processQueries(Collection<? extends Query<I, O>> queries) {
		for(Query<I,O> q : queries) {
			O output = automaton.computeSuffixOutput(q.getPrefix(), q.getSuffix());
			q.answer(output);
		}
	}
	
}
