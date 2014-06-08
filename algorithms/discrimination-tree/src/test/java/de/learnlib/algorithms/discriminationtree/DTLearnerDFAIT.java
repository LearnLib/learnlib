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
package de.learnlib.algorithms.discriminationtree;

import de.learnlib.algorithms.discriminationtree.dfa.DTLearnerDFABuilder;
import de.learnlib.api.MembershipOracle.DFAMembershipOracle;
import de.learnlib.counterexamples.LocalSuffixFinder;
import de.learnlib.counterexamples.LocalSuffixFinders;
import de.learnlib.testsupport.it.learner.AbstractDFALearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList.DFALearnerVariantList;

import net.automatalib.words.Alphabet;

import org.testng.annotations.Test;

@Test
public class DTLearnerDFAIT extends AbstractDFALearnerIT {
	
	private static final boolean[] boolValues = { false, true };

	@Override
	protected <I> void addLearnerVariants(Alphabet<I> alphabet,
			int targetSize,
			DFAMembershipOracle<I> mqOracle, DFALearnerVariantList<I> variants) {
		DTLearnerDFABuilder<I> builder = new DTLearnerDFABuilder<>();
		builder.setAlphabet(alphabet);
		builder.setOracle(mqOracle);
		
		for(boolean epsilonRoot : boolValues) {
			builder.setEpsilonRoot(epsilonRoot);
			for(LocalSuffixFinder<? super I, ? super Boolean> suffixFinder : LocalSuffixFinders.values()) {
				builder.setSuffixFinder(suffixFinder);
				
				String name = "epsilonRoot=" + epsilonRoot + ",suffixFinder=" + suffixFinder.toString();
				variants.addLearnerVariant(name, builder.create());
			}
		}
	}
}
