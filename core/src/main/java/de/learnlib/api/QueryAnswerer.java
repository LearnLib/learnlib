package de.learnlib.api;

import net.automatalib.words.Word;

public interface QueryAnswerer<I, O> {
	public O answerQuery(Word<I> prefix, Word<I> suffix);
}
