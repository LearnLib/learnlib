/* Copyright (C) 2014 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 * 
 * AutomataLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 * 
 * AutomataLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with AutomataLib; if not, see
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package de.learnlib.algorithms.nlstar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.fsa.impl.compact.CompactNFA;
import net.automatalib.util.automata.fsa.NFAs;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import de.learnlib.api.MembershipOracle;
import de.learnlib.nfa.NFALearner;
import de.learnlib.nfa.NFALearnerWrapper;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.oracles.MQUtil;

/**
 * The NL* algorithm, as described in the paper
 * <a href="http://ijcai.org/papers09/Papers/IJCAI09-170.pdf">"Angluin-Style Learning of NFA"</a>
 * by B. Bollig et al. (IJCAI'09).
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 */
public class NLStarLearner<I> implements NFALearner<I> {
	
	private final Alphabet<I> alphabet;
	private final ObservationTable<I> table;
	
	private CompactNFA<I> hypothesis;

	public NLStarLearner(Alphabet<I> alphabet, MembershipOracle<I, Boolean> oracle) {
		this.alphabet = alphabet;
		this.table = new ObservationTable<>(alphabet, oracle);
	}
	
	@Override
	public void startLearning() {
		if(hypothesis != null) {
			throw new IllegalStateException();
		}
		
		List<List<Row<I>>> unclosed = table.initialize();
		completeConsistentTable(unclosed);
		
		constructHypothesis();
	}
	
	public DFALearner<I> asDFALearner() {
		return new NFALearnerWrapper<>(alphabet, this);
	}
	
	@Override
	public boolean refineHypothesis(DefaultQuery<I, Boolean> ceQuery) {
		if(hypothesis == null) {
			throw new IllegalStateException();
		}
		
		boolean refined = false;
		while(MQUtil.isCounterexample(ceQuery, hypothesis)) {
			Word<I> ceWord = ceQuery.getInput();
			
			List<List<Row<I>>> unclosed = table.addSuffixes(ceWord.suffixes(false));
			completeConsistentTable(unclosed);
			constructHypothesis();
			
			refined = true;
		}
		return refined;
	}

	@Override
	public CompactNFA<I> getHypothesisModel() {
		if(hypothesis == null) {
			throw new IllegalStateException();
		}
		return hypothesis;
	}
	
	public CompactDFA<I> getDeterminizedHypothesis() {
		if(hypothesis == null) {
			throw new IllegalStateException();
		}
		return NFAs.determinize(hypothesis);
	}
	
	private void completeConsistentTable(List<List<Row<I>>> initialUnclosed) {
		List<List<Row<I>>> unclosed = initialUnclosed;
		
		
		Inconsistency<I> incons = null;
		
		do {
			while(!unclosed.isEmpty()) {
				unclosed = fixUnclosed(unclosed);
			}
			
			incons = table.findInconsistency();
			if(incons != null) {
				unclosed = fixInconsistency(incons);
			}
		} while(!unclosed.isEmpty() || incons != null);
	}
	
	
	private List<List<Row<I>>> fixUnclosed(List<List<Row<I>>> unclosed) {
		List<Row<I>> newShort = new ArrayList<>(unclosed.size());
		
		for(List<Row<I>> unclosedClass : unclosed) {
			newShort.add(unclosedClass.get(0));
		}
		
		return table.makeUpper(newShort);
	}
	
	private List<List<Row<I>>> fixInconsistency(Inconsistency<I> incons) {
		I sym = alphabet.getSymbol(incons.getSymbolIdx());
		Word<I> oldSuffix = table.getSuffix(incons.getSuffixIdx());
		
		Word<I> newSuffix = oldSuffix.prepend(sym);
		
		return table.addSuffix(newSuffix);
	}
	
	private void constructHypothesis() {
		hypothesis = new CompactNFA<>(alphabet);
		
		int[] stateMap = new int[table.getNumUpperRows()];
		Arrays.fill(stateMap, -1);
		
		List<Row<I>> upperPrimes = table.getUpperPrimes();
		
		for(Row<I> row : upperPrimes) {
			int state = hypothesis.addIntState(row.getContent(0));
			stateMap[row.getUpperId()] = state;
		}
		
		
		Row<I> firstRow = table.getUpperRow(0);
		
		
		if(firstRow.isPrime()) {
			int state = stateMap[firstRow.getUpperId()];
			hypothesis.setInitial(state, true);
		}
		else {
			for(Row<I> row : table.getCoveredRows(firstRow)) {
				if(row.isPrime()) {
					int state = stateMap[row.getUpperId()];
					hypothesis.setInitial(state, true);
				}
			}
		}
	
		// Transition relation
		for(Row<I> row : upperPrimes) {
			int state = stateMap[row.getUpperId()];
			
			for(int i = 0; i < alphabet.size(); i++) {
				Row<I> succRow = row.getSuccessorRow(i);
				
				if(succRow.isPrime()) {
					int succState = stateMap[succRow.getUpperId()];
					hypothesis.addTransition(state, i, succState);
				}
				else {
					for(Row<I> r : succRow.getCoveredRows()) {
						if(r.isPrime()) {
							int succState = stateMap[r.getUpperId()];
							hypothesis.addTransition(state, i, succState);
						}
					}
				}
			}
		}
	}

}
