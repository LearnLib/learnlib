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

import java.util.Arrays;

import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandler;
import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategies;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategy;
import de.learnlib.algorithms.lstargeneric.mealy.ExtensibleLStarMealyBuilder;
import de.learnlib.api.MembershipOracle.MealyMembershipOracle;
import de.learnlib.testsupport.it.learner.AbstractMealyLearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList.MealyLearnerVariantList;

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public class ExtensibleLStarMealyIT extends AbstractMealyLearnerIT {

	@Override
	protected <I, O> void addLearnerVariants(Alphabet<I> alphabet,
			MealyMembershipOracle<I, O> mqOracle,
			MealyLearnerVariantList<I, O> variants) {
		ExtensibleLStarMealyBuilder<I, O> builder = new ExtensibleLStarMealyBuilder<>();
		builder.setAlphabet(alphabet);
		builder.setOracle(mqOracle);
		
		builder.setInitialPrefixes(Arrays.asList(Word.<I>epsilon(), Word.fromLetter(alphabet.getSymbol(0))));
		
		for(ObservationTableCEXHandler<? super I, ? super Word<O>> handler : ObservationTableCEXHandlers.values()) {
			builder.setCexHandler(handler);
			for(ClosingStrategy<? super I, ? super Word<O>> closingStrategy : ClosingStrategies.values()) {
				builder.setClosingStrategy(closingStrategy);
				
				String variantName = "cexHandler=" + handler + ",closingStrategy=" + closingStrategy;
				variants.addLearnerVariant(variantName, builder.create());
			}
		}
	}

}
