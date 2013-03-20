package de.learnlib.algorithms.angluin;

import net.automatalib.words.Word;

public class CombinedWord<S> {

	private Word<S> prefix;
	private Word<S> suffix;

	public CombinedWord(Word<S> prefix, Word<S> suffix) {
		this.prefix = prefix;
		this.suffix = suffix;
	}

	public CombinedWord(Word<S> prefix, S symbol) {
		this.prefix = prefix;
		this.suffix = Word.fromSymbols(symbol);
	}


	public Word<S> getPrefix() {
		return prefix;
	}

	public Word<S> getSuffix() {
		return suffix;
	}

	public Word<S> getWord() {
		return prefix.concat(suffix);
	}
}
