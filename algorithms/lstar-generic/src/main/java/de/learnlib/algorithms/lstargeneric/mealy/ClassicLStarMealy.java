/* Copyright (C) 2013 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.learnlib.algorithms.lstargeneric.mealy;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.misberner.buildergen.annotations.GenerateBuilder;

import de.learnlib.algorithms.lstargeneric.ExtensibleAutomatonLStar;
import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandler;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategy;
import de.learnlib.algorithms.lstargeneric.table.Row;
import de.learnlib.api.MembershipOracle;
import de.learnlib.mealy.MealyUtil;

import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.MutableMealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.automata.transout.impl.compact.CompactMealyTransition;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * An implementation of the L*Mealy algorithm for inferring Mealy machines, as described
 * by Oliver Niese in his Ph.D. thesis.
 * 
 * @author Malte Isberner 
 *
 * @param <I> input symbol class
 * @param <O> output symbol class
 */
public class ClassicLStarMealy<I, O> extends
		ExtensibleAutomatonLStar<MealyMachine<?, I, ?, O>, I, O, Integer, CompactMealyTransition<O>, Void, O, CompactMealy<I,O>> {

	
	public static <A extends MutableMealyMachine<?,I,?,O>,I,O>
	ClassicLStarMealy<I,O> createForSymbolOracle(Alphabet<I> alphabet,
			MembershipOracle<I,O> oracle,
			List<Word<I>> initialSuffixes,
			ObservationTableCEXHandler<I, O> cexHandler,
			ClosingStrategy<? super I,? super O> closingStrategy) {
		return new ClassicLStarMealy<>(alphabet, oracle,
				initialSuffixes,
				cexHandler,
				closingStrategy);
	}
	
	public static <A extends MutableMealyMachine<?,I,?,O>,I,O>
	ClassicLStarMealy<I,O> createForWordOracle(Alphabet<I> alphabet,
			MembershipOracle<I,Word<O>> oracle,
			List<Word<I>> initialSuffixes,
			ObservationTableCEXHandler<? super I, ? super O> cexHandler,
			ClosingStrategy<? super I,? super O> closingStrategy) {
		return new ClassicLStarMealy<>(alphabet, MealyUtil.wrapWordOracle(oracle),
				initialSuffixes,
				cexHandler,
				closingStrategy);
	}
	
	
	/**
	 * Constructor.
	 * @param alphabet the learning alphabet
	 * @param oracle the (Mealy) oracle
	 */
	public ClassicLStarMealy(Alphabet<I> alphabet,
			MembershipOracle<I, O> oracle,
			List<Word<I>> initialSuffixes,
			ObservationTableCEXHandler<? super I, ? super O> cexHandler,
			ClosingStrategy<? super I, ? super O> closingStrategy) {
		this(alphabet, oracle, Collections.singletonList(Word.<I>epsilon()), initialSuffixes,
				cexHandler, closingStrategy);
	}
	
	@GenerateBuilder(defaults = ExtensibleAutomatonLStar.BuilderDefaults.class)
	public ClassicLStarMealy(Alphabet<I> alphabet,
			MembershipOracle<I, O> oracle,
			List<Word<I>> initialPrefixes,
			List<Word<I>> initialSuffixes,
			ObservationTableCEXHandler<? super I, ? super O> cexHandler,
			ClosingStrategy<? super I, ? super O> closingStrategy) {
		super(alphabet, oracle, new CompactMealy<I,O>(alphabet),
				initialPrefixes,
				LStarMealyUtil.ensureSuffixCompliancy(initialSuffixes, alphabet, true),
				cexHandler,
				closingStrategy);
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
			suffixes.add(Word.fromLetter(sym));
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

	@Override
	protected SuffixOutput<I, O> hypothesisOutput() {
		return new SuffixOutput<I,O>() {
			@Override
			public O computeOutput(Iterable<? extends I> input) {
				return computeSuffixOutput(Collections.<I>emptyList(), input);
			}
			@Override
			public O computeSuffixOutput(Iterable<? extends I> prefix, Iterable<? extends I> suffix) {
				Word<O> wordOut = internalHyp.computeSuffixOutput(prefix, suffix);
				if(wordOut.isEmpty())
					return null;
				return wordOut.lastSymbol();
			}
			
		};
	}

}
