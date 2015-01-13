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
package de.learnlib.algorithms.kv.mealy;

import net.automatalib.words.Alphabet;

import org.testng.annotations.Test;

import de.learnlib.acex.analyzers.AcexAnalyzers;
import de.learnlib.acex.analyzers.NamedAcexAnalyzer;
import de.learnlib.api.MembershipOracle.MealyMembershipOracle;
import de.learnlib.testsupport.it.learner.AbstractMealyLearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList.MealyLearnerVariantList;

/**
 * Function test for the Kearns/Vazirani algorithm for Mealy machine learning.
 * 
 * @author Malte Isberner
 *
 */
@Test
public class KearnsVaziraniMealyIT extends AbstractMealyLearnerIT {

	private static boolean[] BOOLEAN_VALUES = { false, true };
	
	@Override
	protected <I, O> void addLearnerVariants(Alphabet<I> alphabet,
			MealyMembershipOracle<I, O> mqOracle,
			MealyLearnerVariantList<I, O> variants) {
		KearnsVaziraniMealyBuilder<I, O> builder = new KearnsVaziraniMealyBuilder<>();
		builder.setAlphabet(alphabet);
		builder.setOracle(mqOracle);
		
		for(boolean repeatedEval : BOOLEAN_VALUES) {
			builder.setRepeatedCounterexampleEvaluation(repeatedEval);
			for (NamedAcexAnalyzer analyzer : AcexAnalyzers.getAllAnalyzers()) {
				builder.setCounterexampleAnalyzer(analyzer);
				String name = String.format("repeatedEval=%s,ceAnalyzer=%s", repeatedEval, analyzer.getName());
				variants.addLearnerVariant(name, builder.create());
			}
		}		
	}


}
