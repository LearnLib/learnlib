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
import de.learner.testsupport.it.learner.AbstractDFALearnerIT;
import de.learner.testsupport.it.learner.LearnerVariantList.DFALearnerVariantList;
import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandler;
import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategies;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategy;
import de.learnlib.algorithms.lstargeneric.dfa.ExtensibleLStarDFABuilder;
import de.learnlib.api.MembershipOracle.DFAMembershipOracle;

public class ExtensibleLStarDFAIT extends AbstractDFALearnerIT {

	@Override
	protected <I> void addLearnerVariants(Alphabet<I> alphabet,
			DFAMembershipOracle<I> mqOracle,
			DFALearnerVariantList<I> variants) {
		ExtensibleLStarDFABuilder<I> builder = new ExtensibleLStarDFABuilder<>();
		builder.setAlphabet(alphabet);
		builder.setOracle(mqOracle);
		
		for(ObservationTableCEXHandler<? super I, ? super Boolean> handler : ObservationTableCEXHandlers.values()) {
			builder.setCexHandler(handler);
			for(ClosingStrategy<? super I, ? super Boolean> closingStrategy : ClosingStrategies.values()) {
				builder.setClosingStrategy(closingStrategy);
				
				String variantName = "cexHandler=" + handler + ",closingStrategy=" + closingStrategy;
				variants.addLearnerVariant(variantName, builder.create());
			}
		}
	}

}
