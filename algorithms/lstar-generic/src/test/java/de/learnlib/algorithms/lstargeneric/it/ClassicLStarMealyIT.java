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
package de.learnlib.algorithms.lstargeneric.it;

import net.automatalib.words.Alphabet;

import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandler;
import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategies;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategy;
import de.learnlib.algorithms.lstargeneric.mealy.ClassicLStarMealyBuilder;
import de.learnlib.api.MembershipOracle;
import de.learnlib.testsupport.it.learner.AbstractMealySymLearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList.MealySymLearnerVariantList;

public class ClassicLStarMealyIT extends AbstractMealySymLearnerIT {

	@Override
	protected <I, O> void addLearnerVariants(Alphabet<I> alphabet,
			MembershipOracle<I, O> mqOracle,
			MealySymLearnerVariantList<I, O> variants) {
		
		ClassicLStarMealyBuilder<I, O> builder = new ClassicLStarMealyBuilder<>();
		builder.setAlphabet(alphabet);
		builder.setOracle(mqOracle);
		
		for(ObservationTableCEXHandler<? super I, ? super O> handler : ObservationTableCEXHandlers.values()) {
			builder.setCexHandler(handler);
			for(ClosingStrategy<? super I, ? super O> closingStrategy : ClosingStrategies.values()) {
				builder.setClosingStrategy(closingStrategy);
				
				String variantName = "cexHandler=" + handler + ",closingStrategy=" + closingStrategy;
				variants.addLearnerVariant(variantName, builder.create());
			}
		}
	}

}
