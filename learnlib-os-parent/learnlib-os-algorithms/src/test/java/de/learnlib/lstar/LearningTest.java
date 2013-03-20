package de.learnlib.lstar;

import org.junit.Assert;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.words.Alphabet;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.Query;
import de.learnlib.oracles.eq.SimulatorEQOracle;

public class LearningTest {
	
	public static <M extends UniversalDeterministicAutomaton<?,I,?,?,?>,I,O,S,T,A extends UniversalDeterministicAutomaton<S, I, T, ?, ?>> void testLearnModel(A target, Alphabet<I> alphabet, LearningAlgorithm<M, I, O> learner) {
		EquivalenceOracle<M, I, O> eqOracle = new SimulatorEQOracle<M, I, O>(target);
		
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
