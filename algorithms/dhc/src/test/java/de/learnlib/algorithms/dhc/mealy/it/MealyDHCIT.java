/* Copyright (C) 2014 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 * 
 * AutomataLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 * 
 * AutomataLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with AutomataLib; if not, see
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package de.learnlib.algorithms.dhc.mealy.it;

import de.learnlib.algorithms.dhc.mealy.MealyDHCBuilder;
import de.learnlib.api.MembershipOracle.MealyMembershipOracle;
import de.learnlib.counterexamples.GlobalSuffixFinder;
import de.learnlib.counterexamples.GlobalSuffixFinders;
import de.learnlib.testsupport.it.learner.AbstractMealyLearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList.MealyLearnerVariantList;

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public class MealyDHCIT extends AbstractMealyLearnerIT {
	

	@Override
	protected <I, O> void addLearnerVariants(Alphabet<I> alphabet,
			MealyMembershipOracle<I, O> mqOracle,
			MealyLearnerVariantList<I, O> variants) {
		MealyDHCBuilder<I,O> builder = new MealyDHCBuilder<>();
		builder.setAlphabet(alphabet);
		builder.setOracle(mqOracle);
		
		for(GlobalSuffixFinder<? super I, ? super Word<O>> suffixFinder : GlobalSuffixFinders.values()) {
			builder.setSuffixFinder(suffixFinder);
			String name = "suffixFinder=" + suffixFinder.toString();
			variants.addLearnerVariant(name, builder.create());
		}
	}

}
