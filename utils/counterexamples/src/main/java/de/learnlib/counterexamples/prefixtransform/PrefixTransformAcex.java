/* Copyright (C) 2014 TU Dortmund
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
package de.learnlib.counterexamples.prefixtransform;

import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.words.Word;

import com.google.common.base.Objects;

import de.learnlib.acex.impl.BaseAbstractCounterexample;
import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.api.MembershipOracle;


/**
 * An abstract counterexample whose data is derived from prefix transformation.
 * While this is the only way of deriving abstract counterexamples discussed in the
 * paper, in principle other ways are possible, too.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 * @param <O> output type
 */
public class PrefixTransformAcex<I, O> extends BaseAbstractCounterexample {
	
	private final Word<I> counterexample;
	private final MembershipOracle<I, O> oracle;
	private final AccessSequenceTransformer<I> asTransformer;
	private final SuffixOutput<I,O> hypothesis;

	
	public PrefixTransformAcex(
			Word<I> counterexample,
			MembershipOracle<I, O> oracle,
			AccessSequenceTransformer<I> asTransformer,
			SuffixOutput<I, O> hypothesis) {
		super(counterexample.length());
		this.counterexample = counterexample;
		this.oracle = oracle;
		this.asTransformer = asTransformer;
		this.hypothesis = hypothesis;
	}


	/*
	 * (non-Javadoc)
	 * @see de.learnlib.abstractcounterexamples.AbstractCounterexample#doComputeEffect(int)
	 */
	@Override
	protected int computeEffect(int index) {
		Word<I> prefix = counterexample.prefix(index);
		Word<I> suffix = counterexample.subWord(index);
		Word<I> transformedPrefix = asTransformer.transformAccessSequence(prefix);
		
		O hypOut = hypothesis.computeSuffixOutput(transformedPrefix, suffix);
		O oracleOut = oracle.answerQuery(transformedPrefix, suffix);
		
		return Objects.equal(hypOut, oracleOut) ? 1 : 0;
	}
	
	public int getReductionPotential() {
		int pot = 0;
		
		Word<I> prefix;
		do {
			prefix = counterexample.prefix(++pot);
		} while(pot < counterexample.length() && asTransformer.isAccessSequence(prefix));
		
		return pot - 1;
	}

}
