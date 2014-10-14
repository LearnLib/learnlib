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
package de.learnlib.algorithms.ttt.mealy.it;

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import org.testng.annotations.Test;

import de.learnlib.algorithms.ttt.mealy.TTTLearnerMealyBuilder;
import de.learnlib.api.MembershipOracle.MealyMembershipOracle;
import de.learnlib.counterexamples.LocalSuffixFinder;
import de.learnlib.counterexamples.LocalSuffixFinders;
import de.learnlib.testsupport.it.learner.AbstractMealyLearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList.MealyLearnerVariantList;

@Test
public class TTTLearnerMealyIT extends AbstractMealyLearnerIT {

	@Override
	protected <I, O> void addLearnerVariants(Alphabet<I> alphabet,
			MealyMembershipOracle<I, O> mqOracle,
			MealyLearnerVariantList<I, O> variants) {
		
		TTTLearnerMealyBuilder<I,O> builder = new TTTLearnerMealyBuilder<>();
		builder.setAlphabet(alphabet);
		builder.setOracle(mqOracle);
		
		for (LocalSuffixFinder<? super I, ? super Word<O>> suffixFinder : LocalSuffixFinders.values()) {
			builder.setSuffixFinder(suffixFinder);
			variants.addLearnerVariant("suffixFinder=" + suffixFinder, builder.create());
		}
	}
	
}
