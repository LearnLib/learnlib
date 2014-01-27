package de.learnlib.test.integration;

import java.util.ArrayList;
import java.util.List;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.examples.LearningExample.DFALearningExample;

@Test
public abstract class AbstractDFALearnerIT {

	protected abstract <I>
	void createLearnerVariants(Alphabet<I> alphabet,
			MembershipOracle<I,Boolean> oracle,
			List<? super LearningAlgorithm<DFA<?,I>,I,Boolean>> learnerList);

	
	
	@Test
	public void testStandardExamples() {
		for(DFALearningExample<?> example : LearnerExamples.getDFAExamples()) {
			doTestExample(example);
		}
	}
	
	private <I> void doTestExample(DFALearningExample<I> example) {
		List<LearningAlgorithm<DFA<?,I>,I,Boolean>> learnerList
			= new ArrayList<>();
		createLearnerVariants(example.getAlphabet(), example.createMembershipOracle(), learnerList);
		
		
		for(LearningAlgorithm<DFA<?,I>,I,Boolean> algo : learnerList) {
			doTestLearner()
		}
	}

}
