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
package de.learnlib.lstar.dfa;

import java.util.Collections;
import java.util.List;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.FastDFA;
import net.automatalib.automata.fsa.impl.FastDFAState;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.util.Words;
import de.learnlib.api.MembershipOracle;
import de.learnlib.lstar.AbstractAutomatonLStar;
import de.learnlib.lstar.table.Row;


/**
 * An implementation of Angluin's L* algorithm for learning DFAs, as described in the paper
 * "Learning Regular Sets from Queries and Counterexamples".
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <I> input symbol class.
 */
public class ClassicLStarDFA<I>
	extends AbstractAutomatonLStar<DFA<?,I>, I, Boolean, FastDFAState, FastDFAState, Boolean, Void, FastDFA<I>> {
	
	/**
	 * Constructor.
	 * @param alphabet the learning alphabet.
	 * @param oracle the DFA oracle.
	 */
	public ClassicLStarDFA(Alphabet<I> alphabet, MembershipOracle<I,Boolean> oracle) {
		super(alphabet, oracle, new FastDFA<I>(alphabet));
	}

	
	/*
	 * (non-Javadoc)
	 * @see de.learnlib.lstar.AbstractLStar#initialSuffixes()
	 */
	@Override
	protected List<Word<I>> initialSuffixes() {
		return Collections.singletonList(Words.<I>epsilon());
	}


	/*
	 * (non-Javadoc)
	 * @see de.learnlib.lstar.AbstractAutomatonLStar#stateProperty(de.learnlib.lstar.Row)
	 */
	@Override
	protected Boolean stateProperty(Row<I> stateRow) {
		return table.cellContents(stateRow, 0);
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.lstar.AbstractAutomatonLStar#transitionProperty(de.learnlib.lstar.Row, int)
	 */
	@Override
	protected Void transitionProperty(Row<I> stateRow, int inputIdx) {
		return null;
	}


	@Override
	protected DFA<?, I> exposeInternalHypothesis() {
		return internalHyp;
	}


}