package de.learnlib.algorithms.angluin;

import de.ls5.words.Word;

public class InconsistencyDataHolder<S> {

	private final Word<S> firstState;
	private final Word<S> secondState;
	private final Word<S> differingSymbol;

	public InconsistencyDataHolder(Word<S> firstState, Word<S> secondState, Word<S> differingSymbol) {
		this.firstState = firstState;
		this.secondState = secondState;
		this.differingSymbol = differingSymbol;
	}

	public Word<S> getFirstState() {
		return firstState;
	}

	public Word<S> getSecondState() {
		return secondState;
	}

	public Word<S> getDifferingSymbol() {
		return differingSymbol;
	}
}
