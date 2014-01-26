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
package de.learnlib.algorithms.baselinelstar.it;

import net.automatalib.words.Alphabet;
import de.learner.testsupport.it.learner.AbstractDFALearnerIT;
import de.learner.testsupport.it.learner.LearnerVariantList.DFALearnerVariantList;
import de.learnlib.algorithms.baselinelstar.BaselineLStar;
import de.learnlib.api.MembershipOracle.DFAMembershipOracle;

public class BaselineLStarIT extends AbstractDFALearnerIT {

	@Override
	protected <I> void addLearnerVariants(Alphabet<I> alphabet,
			DFAMembershipOracle<I> mqOracle, DFALearnerVariantList<I> variants) {
		variants.addLearnerVariant("default", new BaselineLStar<>(alphabet, mqOracle));
	}

	
}
