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
package de.learnlib.oracles.mealy;

import net.automatalib.automata.concepts.SODetOutputAutomaton;
import net.automatalib.commons.util.comparison.CmpUtil;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.Query;

public class SymbolEQOracleWrapper<A extends SODetOutputAutomaton<?,I,?,Word<O>>, I, O> implements
		EquivalenceOracle<A, I, O> {
	
	private final EquivalenceOracle<? super A, I, Word<O>> wordEqOracle;

	public SymbolEQOracleWrapper(EquivalenceOracle<? super A,I,Word<O>> wordEqOracle) {
		this.wordEqOracle = wordEqOracle;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.EquivalenceOracle#findCounterExample(java.lang.Object, net.automatalib.words.Alphabet)
	 */
	@Override
	public Query<I, O> findCounterExample(A hypothesis, Alphabet<I> alphabet) {
		Query<I,Word<O>> wordCeQry = wordEqOracle.findCounterExample(hypothesis, alphabet);
		if(wordCeQry == null)
			return null;
		
		Word<O> hypOut = hypothesis.computeSuffixOutput(wordCeQry.getPrefix(), wordCeQry.getSuffix());
		Word<O> ceOut = wordCeQry.getOutput();
		
		
		int len = hypOut.length();
		if(len != ceOut.length())
			throw new IllegalStateException("Output word length does not align with suffix length, truncating CE will not work");
		
		

		for(int i = 0; i < len; i++) {
			O hypSym = hypOut.getSymbol(i), ceSym = ceOut.getSymbol(i);
			
			if(!CmpUtil.equals(hypSym, ceSym)) {
				Query<I,O> result = new Query<I,O>(wordCeQry.getPrefix(),
						wordCeQry.getSuffix().prefix(i+1));
				result.setOutput(ceSym);
				return result;
			}
		}
		
		throw new IllegalStateException("Counterexample returned by underlying EQ oracle was none");
	}

}
