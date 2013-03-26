package de.learnlib.algorithms.lstargeneric;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.words.Alphabet;

import org.testng.Assert;

import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.Query;

public class LearningTest {
	
	public static <I,O,M extends UniversalDeterministicAutomaton<?, I, ?, ?, ?>> void testLearnModel(UniversalDeterministicAutomaton<?, I, ?, ?, ?> target,
			Alphabet<I> alphabet,
			LearningAlgorithm<M, I, O> learner,
			EquivalenceOracle<? super M, I, O> eqOracle) {
		int maxRounds = target.size();
		
		learner.startLearning();
		
		while(maxRounds-- > 0) {
			M hyp = learner.getHypothesisModel();
			
			Query<I, O> ce = eqOracle.findCounterExample(hyp, alphabet);
			
			if(ce == null)
				break;
			
			Assert.assertNotEquals(maxRounds, 0);
			
			learner.refineHypothesis(ce);
		}
		
		M hyp = learner.getHypothesisModel();
		
		Assert.assertEquals(hyp.size(), target.size());
	}

}
