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
package de.learnlib.lstar;

import java.util.ArrayList;

import net.automatalib.automata.MutableDeterministic;
import net.automatalib.commons.util.Pair;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.words.Alphabet;
import de.learnlib.api.MembershipOracle;
import de.learnlib.lstar.table.Row;


/**
 * Abstract base class for algorithms that produce (subclasses of) {@link MutableDeterministic}
 * automata. 
 * 
 * This class provides the L*-style hypothesis construction. Implementing classes solely have
 * to specify how state and transition properties should be derived.
 *  
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <A> automaton class, must be a subclass of {@link MutableDeterministic}
 * @param <I> input symbol class
 * @param <O> output class
 * @param <SP> state property class
 * @param <TP> transition property class
 */
public abstract class AbstractAutomatonLStar<A,I,O,S,T,SP,TP,AI extends MutableDeterministic<S,I,T,SP,TP>> extends
		AbstractLStar<A, I, O> {

	protected final AI internalHyp;
	protected final ArrayList<Pair<Row<I>,S>> stateInfos
		= new ArrayList<Pair<Row<I>,S>>();
	
	/**
	 * Constructor.
	 * @param alphabet the learning alphabet
	 * @param oracle the learning oracle
	 */
	public AbstractAutomatonLStar(Alphabet<? extends I> alphabet,
			MembershipOracle<I, O> oracle,
			AI internalHyp) {
		super(alphabet, oracle);
		this.internalHyp = internalHyp;
		internalHyp.clear();
	}
	
	/**
	 * Derives a state property from the corresponding row.
	 * @param stateRow the row for which the state is created
	 * @return the state property of the corresponding state
	 */
	protected abstract SP stateProperty(Row<I> stateRow);
	
	/**
	 * Derives a transition property from the corresponding transition.
	 * N.B.: Not the transition row is passed to this method, but the
	 * row for the outgoing state. The transition row can be retrieved
	 * by <code>stateRow.getSuccessor(inputIdx)</code>.
	 * @param stateRow the row for the source state
	 * @param inputIdx the index of the input symbol to consider
	 * @return the transition property of the corresponding transition
	 */
	protected abstract TP transitionProperty(Row<I> stateRow, int inputIdx);
	
	
	protected abstract A exposeInternalHypothesis();
	
	/**
	 * Performs the L*-style hypothesis construction.
	 * For creating states and transitions, the {@link #stateProperty(Row)} and
	 * {@link #transitionProperty(Row, int)} methods are used to derive the
	 * respective properties.
	 * @param model the model to output
	 */
	protected void updateInternalHypothesis() {
		if(!table.isInitialized())
			throw new IllegalStateException("Cannot update internal hypothesis: not initialized");
		
		int oldStates = internalHyp.size();
		int numDistinct = table.numDistinctRows();
		

		int newStates = numDistinct - oldStates;
		
		if(newStates <= 0)
			return;
		
		stateInfos.addAll(CollectionsUtil.<Pair<Row<I>,S>>nullList(newStates));
		
		
		// TODO: Is there a quicker way than iterating over *all* rows?
		// FIRST PASS: Create new hypothesis states
		for(Row<I> sp : table.getShortPrefixRows()) {
			int id = sp.getRowContentId();
			if(stateInfos.get(id) != null)
				continue;
			
			
			SP prop = stateProperty(sp);
			
			S state = null;
			if(id == 0)
				state = internalHyp.addInitialState(prop);
			else
				state = internalHyp.addState(prop);
			
			stateInfos.set(id, Pair.make(sp, state));
		}
		
		// SECOND PASS: Create hypothesis transitions
		for(Pair<Row<I>,S> info : stateInfos) {
			Row<I> sp = info.getFirst();
			S state = info.getSecond();
			
			for(int i = 0; i < alphabet.size(); i++) {
				I input = alphabet.getSymbol(i);
				
				Row<I> succ = sp.getSuccessor(i);
				int succId = succ.getRowContentId();
				
				if(succId < oldStates)
					continue;
				
				S succState = stateInfos.get(succId).getSecond();
				
				TP prop = transitionProperty(sp, i);
				internalHyp.setTransition(state, input, succState, prop);
			}
		}
	}
	
	public A getHypothesisModel() {
		updateInternalHypothesis();
		return exposeInternalHypothesis();
	}

}
