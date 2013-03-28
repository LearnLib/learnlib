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
package de.learnlib.algorithms.lstargeneric;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.words.Alphabet;

import org.testng.Assert;

import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.oracles.DefaultQuery;

public class LearningTest {
	
	public static <I,O,M extends UniversalDeterministicAutomaton<?, I, ?, ?, ?>> void testLearnModel(UniversalDeterministicAutomaton<?, I, ?, ?, ?> target,
			Alphabet<I> alphabet,
			LearningAlgorithm<M, I, O> learner,
			EquivalenceOracle<? super M, I, O> eqOracle) {
		int maxRounds = target.size();
		
		learner.startLearning();
		
		while(maxRounds-- > 0) {
			M hyp = learner.getHypothesisModel();
			
			DefaultQuery<I, O> ce = eqOracle.findCounterExample(hyp, alphabet);
			
			if(ce == null)
				break;
			
			Assert.assertNotEquals(maxRounds, 0);
			
			learner.refineHypothesis(ce);
		}
		
		M hyp = learner.getHypothesisModel();
		
		Assert.assertEquals(hyp.size(), target.size());
	}

}
