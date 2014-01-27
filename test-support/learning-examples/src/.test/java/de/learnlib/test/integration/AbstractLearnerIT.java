package de.learnlib.test.integration;

import java.util.Collection;

import net.automatalib.automata.DeterministicAutomaton;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.oracles.DefaultQuery;

public abstract class AbstractLearnerIT {

	protected int roundsUpperBound(DeterministicAutomaton<?, ?, ?> automaton) {
		return automaton.size();
	}
	
	
	public static <M,I,O> void testLearner(LearningAlgorithm<M,I,O> learner,
			Collection<? extends I> inputs,
			EquivalenceOracle<? super M, I, O> eqOracle,
			int maxRounds) {
		learner.startLearning();
		
		for(int i = 0; i < maxRounds+1; i++) {
			M hyp = learner.getHypothesisModel();
			
			DefaultQuery<I,O> ce = eqOracle.findCounterExample(hyp, inputs);
			
			if(ce == null) {
				return;
			}
			
			boolean refineResult = learner.refineHypothesis(ce);
			Assert.assertTrue(refineResult, "Learner did not recognize counterexample");
		}
		
		Assert.fail("Learning took too long, requiring more than " + maxRounds + " counterexamples");
	}
}
