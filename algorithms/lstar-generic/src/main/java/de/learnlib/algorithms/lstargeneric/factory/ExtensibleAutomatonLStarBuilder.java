package de.learnlib.algorithms.lstargeneric.factory;

import java.util.Collections;
import java.util.List;

import net.automatalib.words.Word;
import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandler;
import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategies;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategy;
import de.learnlib.factory.LearnerBuilder;

public abstract class ExtensibleAutomatonLStarBuilder<M, I, O, B extends ExtensibleAutomatonLStarBuilder<M,I,O,B>> extends
		LearnerBuilder<M, I, O, B> {
	
	protected List<Word<I>> initialSuffixes = Collections.emptyList();
	protected ClosingStrategy<? super I, ? super O> closingStrategy = ClosingStrategies.CLOSE_FIRST;
	protected ObservationTableCEXHandler<? super I, ? super O> cexHandler = ObservationTableCEXHandlers.CLASSIC_LSTAR;


	/**
	 * @return the initialSuffixes
	 */
	public List<Word<I>> getInitialSuffixes() {
		return initialSuffixes;
	}


	/**
	 * @param initialSuffixes the initialSuffixes to set
	 */
	public void setInitialSuffixes(List<Word<I>> initialSuffixes) {
		this.initialSuffixes = initialSuffixes;
	}
	
	public void setDefaultInitialSuffixes() {
		this.initialSuffixes = Collections.emptyList();
	}
	
	public B withInitialSuffixes(List<Word<I>> initialSuffixes) {
		setInitialSuffixes(initialSuffixes);
		return _this();
	}
	
	public B withDefaultInitialSuffixes() {
		setDefaultInitialSuffixes();
		return _this();
	}


	/**
	 * @return the closingStrategy
	 */
	public ClosingStrategy<? super I, ? super O> getClosingStrategy() {
		return closingStrategy;
	}


	/**
	 * @param closingStrategy the closingStrategy to set
	 */
	public void setClosingStrategy(
			ClosingStrategy<? super I, ? super O> closingStrategy) {
		this.closingStrategy = closingStrategy;
	}
	
	public B withClosingStrategy(ClosingStrategy<? super I,? super O> closingStrategy) {
		setClosingStrategy(closingStrategy);
		return _this();
	}


	/**
	 * @return the cexHandler
	 */
	public ObservationTableCEXHandler<? super I, ? super O> getCexHandler() {
		return cexHandler;
	}


	/**
	 * @param cexHandler the cexHandler to set
	 */
	public void setCexHandler(
			ObservationTableCEXHandler<? super I, ? super O> cexHandler) {
		this.cexHandler = cexHandler;
	}
	
	public B withCexHandler(ObservationTableCEXHandler<? super I, ? super O> cexHandler) {
		setCexHandler(cexHandler);
		return _this();
	}


}
