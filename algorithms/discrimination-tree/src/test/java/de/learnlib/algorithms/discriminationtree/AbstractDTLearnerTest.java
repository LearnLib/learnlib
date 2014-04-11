package de.learnlib.algorithms.discriminationtree;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.words.Alphabet;

import org.testng.Assert;

import de.learnlib.api.LearningAlgorithm;
import de.learnlib.counterexamples.LocalSuffixFinder;
import de.learnlib.counterexamples.LocalSuffixFinders;
import de.learnlib.eqtests.basic.SimulatorEQOracle;
import de.learnlib.oracles.DefaultQuery;

public abstract class AbstractDTLearnerTest {
	@SuppressWarnings("unchecked")
	protected static LocalSuffixFinder<Object,Object>[] SUFFIX_FINDERS = new LocalSuffixFinder[]{
		LocalSuffixFinders.FIND_LINEAR,
		LocalSuffixFinders.FIND_LINEAR_REVERSE,
		LocalSuffixFinders.RIVEST_SCHAPIRE
	};
	
	protected static <M extends UniversalDeterministicAutomaton<?, I, ?, ?, ?>,I,O,R extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & SuffixOutput<I, O>>
	void testLearn(
			LearningAlgorithm<M, I, O> learner,
			Alphabet<I> alphabet,
			R referenceAutomaton) {
		SimulatorEQOracle<I, O> eqOracle = new SimulatorEQOracle<>(referenceAutomaton);
		
		int maxRounds = referenceAutomaton.size();
		
		int i = 0;
		
		learner.startLearning();
		
		while(i < maxRounds) {
			i++;
			
			M hypothesis = learner.getHypothesisModel();
			DefaultQuery<I,O> ce = eqOracle.findCounterExample(hypothesis, alphabet);
			
			if(ce == null)
				return;
			
			Assert.assertTrue(learner.refineHypothesis(ce), "Counterexample not recognized");
		}
		
		Assert.fail("Learning took too long.");
	}
}
