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
package de.learnlib.eqtests.basic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.automatalib.automata.concepts.OutputAutomaton;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.DefaultQuery;

/**
 *
 * @author Maik Merten <maikmerten@googlemail.com>
 */
public class RandomWordsEQOracle<I, O, A extends OutputAutomaton<?, I, ?, O>> implements EquivalenceOracle<A, I, O> {
	
	public static class DFARandomWordsEQOracle<I> extends RandomWordsEQOracle<I,Boolean,DFA<?,I>>
			implements DFAEquivalenceOracle<I> {
		public DFARandomWordsEQOracle(MembershipOracle<I, Boolean> mqOracle,
				int minLength, int maxLength, int maxTests, Random random) {
			super(mqOracle, minLength, maxLength, maxTests, random);
		}
	}
	
	public static class MealyRandomWordsEQOracle<I,O> extends RandomWordsEQOracle<I,Word<O>,MealyMachine<?,I,?,O>>
			implements MealyEquivalenceOracle<I,O> {
		public MealyRandomWordsEQOracle(MembershipOracle<I, Word<O>> mqOracle,
				int minLength, int maxLength, int maxTests, Random random) {
			super(mqOracle, minLength, maxLength, maxTests, random);
		}
	}

	private MembershipOracle<I, O> oracle;
	private int maxTests, minLength, maxLength;
	private final Random random;

	public RandomWordsEQOracle(MembershipOracle<I, O> mqOracle, int minLength, int maxLength, int maxTests, Random random) {
		this.oracle = mqOracle;
		this.maxTests = maxTests;
		this.minLength = minLength;
		this.maxLength = maxLength;
		this.random = random;
	}

	@Override
	public DefaultQuery<I, O> findCounterExample(A hypothesis, Collection<? extends I> alpha) {

		List<? extends I> symbolList;
		if (alpha instanceof List) {
			symbolList = (List<? extends I>) alpha;
		} else {
			symbolList = new ArrayList<>(alpha);
		}
		
		int numSyms = symbolList.size();

		for (int i = 0; i < maxTests; ++i) {
			int length = minLength + random.nextInt((maxLength - minLength) + 1);

			WordBuilder<I> testtrace = new WordBuilder<>(length);
			for (int j = 0; j < length; ++j) {
				int symidx = random.nextInt(numSyms);
				I sym = symbolList.get(symidx);
				testtrace.append(sym);
			}

			DefaultQuery<I, O> query = new DefaultQuery<>(testtrace.toWord());

			// query oracle
			oracle.processQueries(Collections.singletonList(query));
			O oracleoutput = query.getOutput();

			// trace hypothesis
			O hypOutput = hypothesis.computeOutput(testtrace.toWord());

			// compare output of hypothesis and oracle
			if (!oracleoutput.equals(hypOutput)) {
				return query;
			}
		}

		// no counterexample found
		return null;
	}
}
