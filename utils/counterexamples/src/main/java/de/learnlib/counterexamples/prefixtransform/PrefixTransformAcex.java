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
package de.learnlib.counterexamples.prefixtransform;

import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.words.Word;

import com.google.common.base.Objects;

import de.learnlib.acex.impl.BaseAbstractCounterexample;
import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.MQUtil;


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
		O oracleOut = MQUtil.output(oracle, transformedPrefix, suffix);
		
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
