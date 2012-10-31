package de.ls5.learnlib.os.api;

/**
 *
 * @author merten
 */
public interface LearningAlgorithm<A, W> {
	
	public A createHypothesis();
	
	public A refineHypothesis(W counterexample);
	
	
}
