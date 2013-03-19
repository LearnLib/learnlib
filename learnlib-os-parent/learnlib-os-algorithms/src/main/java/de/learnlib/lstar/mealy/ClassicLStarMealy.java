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
package de.learnlib.lstar.mealy;

import java.util.ArrayList;
import java.util.List;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.MutableMealyMachine;
import net.automatalib.automata.transout.impl.FastMealy;
import net.automatalib.automata.transout.impl.FastMealyState;
import net.automatalib.automata.transout.impl.MealyTransition;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.util.Words;
import oracles.mealy.SymbolOracleWrapper;
import de.learnlib.api.MembershipOracle;
import de.learnlib.lstar.AbstractAutomatonLStar;
import de.learnlib.lstar.table.Row;

/**
 * An implementation of the L*Mealy algorithm for inferring Mealy machines, as described
 * by Oliver Niese in his Ph.D. thesis.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <I> input symbol class
 * @param <O> output symbol class
 */
public class ClassicLStarMealy<I, O> extends
		AbstractAutomatonLStar<MealyMachine<?, I, ?, O>, I, O, FastMealyState<O>, MealyTransition<FastMealyState<O>, O>, Void, O, FastMealy<I,O>> {

	
	public static <A extends MutableMealyMachine<?,I,?,O>,I,O>
	ClassicLStarMealy<I,O> createForSymbolOracle(Alphabet<I> alphabet,
			MembershipOracle<I,O> oracle) {
		return new ClassicLStarMealy<>(alphabet, oracle);
	}
	
	public static <A extends MutableMealyMachine<?,I,?,O>,I,O>
	ClassicLStarMealy<I,O> createForWordOracle(Alphabet<I> alphabet,
			MembershipOracle<I,Word<O>> oracle) {
		return new ClassicLStarMealy<>(alphabet, new SymbolOracleWrapper<>(oracle));
	}
	
	
	/**
	 * Constructor.
	 * @param alphabet the learning alphabet
	 * @param oracle the (Mealy) oracle
	 */
	public ClassicLStarMealy(Alphabet<I> alphabet,
			MembershipOracle<I, O> oracle) {
		super(alphabet, oracle, new FastMealy<I,O>(alphabet));
	}
	
	

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.lstar.AbstractAutomatonLStar#stateProperty(de.learnlib.lstar.Row)
	 */
	@Override
	protected Void stateProperty(Row<I> stateRow) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.lstar.AbstractAutomatonLStar#transitionProperty(de.learnlib.lstar.Row, int)
	 */
	@Override
	protected O transitionProperty(Row<I> stateRow, int inputIdx) {
		return table.cellContents(stateRow, inputIdx);
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.lstar.AbstractLStar#initialSuffixes()
	 */
	@Override
	protected List<Word<I>> initialSuffixes() {
		List<Word<I>> suffixes = new ArrayList<Word<I>>(alphabet.size());
		for(int i = 0; i < alphabet.size(); i++) {
			I sym = alphabet.getSymbol(i);
			suffixes.add(Words.asWord(sym));
		}
		return suffixes;
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.lstar.AbstractAutomatonLStar#exposeInternalHypothesis()
	 */
	@Override
	protected MealyMachine<?, I, ?, O> exposeInternalHypothesis() {
		return internalHyp;
	}

}
