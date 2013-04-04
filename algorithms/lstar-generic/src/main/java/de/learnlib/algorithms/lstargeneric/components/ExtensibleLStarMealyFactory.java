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
package de.learnlib.algorithms.lstargeneric.components;

import java.util.Collections;
import java.util.List;

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandler;
import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategies;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategy;
import de.learnlib.algorithms.lstargeneric.mealy.ExtensibleLStarMealy;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.components.LLComponent;
import de.learnlib.components.LLComponentFactory;
import de.learnlib.components.LLComponentParameter;

@LLComponent(name = "ExtensibleLStarMealy", type = LearningAlgorithm.class)
public class ExtensibleLStarMealyFactory<I, O> implements
		LLComponentFactory<ExtensibleLStarMealy<I, O>> {
	
	private Alphabet<I> alphabet;
	private MembershipOracle<I, Word<O>> oracle;
	private List<Word<I>> initialSuffixes = Collections.emptyList();
	private ObservationTableCEXHandler<? super I, ? super Word<O>> cexHandler = ObservationTableCEXHandlers.CLASSIC_LSTAR;
	private ClosingStrategy<? super I, ? super Word<O>> closingStrategy = ClosingStrategies.CLOSE_FIRST;

	
	@LLComponentParameter(name = "alphabet", required = true)
	public void setAlphabet(Alphabet<I> alphabet) {
		this.alphabet = alphabet;
	}
	
	@LLComponentParameter(name = "oracle", required = true)
	public void setOracle(MembershipOracle<I,Word<O>> oracle) {
		this.oracle = oracle;
	}
	
	@LLComponentParameter(name = "initialSuffixes")
	public void setInitialSuffix(List<Word<I>> initialSuffixes) {
		this.initialSuffixes = initialSuffixes;
	}
	
	@LLComponentParameter(name = "cexHandler")
	public void setCEXHandler(ObservationTableCEXHandler<? super I, ? super Word<O>> cexHandler) {
		this.cexHandler = cexHandler;
	}
	
	@LLComponentParameter(name = "closingStrategy")
	public void setClosingStrategy(ClosingStrategy<? super I,? super Word<O>> closingStrategy) {
		this.closingStrategy = closingStrategy;
	}
	@Override
	public ExtensibleLStarMealy<I, O> instantiate() {
		return new ExtensibleLStarMealy<>(alphabet, oracle, initialSuffixes, cexHandler, closingStrategy);
	}
	
}
