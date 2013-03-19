package de.learnlib.algorithms.angluin;

import net.automatalib.words.Word;
import net.automatalib.words.impl.ArrayWord;

public class CombinedWord<S> {

	private Word<S> prefix;
	private Word<S> suffix;

	public CombinedWord(Word<S> prefix, Word<S> suffix) {
		this.prefix = prefix;
		this.suffix = suffix;
	}


	public Word<S> getPrefix() {
		return prefix;
	}

	public Word<S> getSuffix() {
		return suffix;
	}

	public Word<S> getWord() {
		ArrayWord<S> word = new ArrayWord<S>();
		word.addAll(prefix);
		word.addAll(suffix);
		return word;
	}
}
