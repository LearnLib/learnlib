/* Copyright (C) 2015 TU Dortmund
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
package de.learnlib.counterexamples.acex;

import java.util.Objects;
import java.util.function.Function;

import net.automatalib.words.Word;
import de.learnlib.acex.impl.BaseAbstractCounterexample;
import de.learnlib.api.MembershipOracle;

/**
 * An abstract counterexample for output inconsistencies, based on prefix transformations.
 * <p>
 * Note: this class cannot be used for Mealy machines, use {@link MealyOutInconsPrefixTransformAcex}
 * instead.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 * @param <D> output domain type
 */
public class OutInconsPrefixTransformAcex<I,D> extends BaseAbstractCounterexample<D> {
	
	private final Word<I> suffix;
	private final MembershipOracle<I, D> oracle;
	
	private final Function<Word<I>,Word<I>> asTransform;
	
	public OutInconsPrefixTransformAcex(
			Word<I> suffix,
			MembershipOracle<I,D> oracle,
			Function<Word<I>, Word<I>> asTransform) {
		this(suffix, suffix.length() + 1, oracle, asTransform);
	}
	
	protected OutInconsPrefixTransformAcex(
			Word<I> suffix,
			int length,
			MembershipOracle<I,D> oracle,
			Function<Word<I>, Word<I>> asTransform) {
		super(length);
		this.suffix = suffix;
		this.oracle = oracle;
		this.asTransform = asTransform;
	}


	/*
	 * (non-Javadoc)
	 * @see de.learnlib.abstractcounterexamples.AbstractCounterexample#doComputeEffect(int)
	 */
	@Override
	protected D computeEffect(int index) {
		Word<I> prefix = this.suffix.prefix(index);
		Word<I> suffix = this.suffix.subWord(index);
		
		Word<I> asPrefix = asTransform.apply(prefix);
		return oracle.answerQuery(asPrefix, suffix);
	}


	@Override
	public boolean checkEffects(D eff1, D eff2) {
		return Objects.equals(eff1, eff2);
	}

}
