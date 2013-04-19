package de.learnlib.mealy;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Word;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.oracles.DefaultQuery;

final class MealyLearnerWrapper<M extends MealyMachine<?, I, ?, O>, I, O> implements LearningAlgorithm<M, I, Word<O>> {
	
	private final LearningAlgorithm<M, I, O> learner;
	
	private M hypothesis = null;
	
	public MealyLearnerWrapper(LearningAlgorithm<M, I, O> learner) {
		this.learner = learner;
	}

	@Override
	public void startLearning() {
		learner.startLearning();
	}

	@Override
	public boolean refineHypothesis(DefaultQuery<I, Word<O>> ceQuery) {
		if(hypothesis == null)
			hypothesis = learner.getHypothesisModel();
		
		DefaultQuery<I,O> reducedQry = MealyUtil.reduceCounterExample(hypothesis, ceQuery);
		
		if(reducedQry == null)
			return false;
		
		hypothesis = null;
		return learner.refineHypothesis(reducedQry);
	}

	@Override
	public M getHypothesisModel() {
		hypothesis = learner.getHypothesisModel();
		return hypothesis;
	}

}
