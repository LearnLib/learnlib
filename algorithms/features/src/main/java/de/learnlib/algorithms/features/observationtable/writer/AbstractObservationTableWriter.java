package de.learnlib.algorithms.features.observationtable.writer;

import net.automatalib.words.Word;

import com.google.common.base.Function;
import com.google.common.base.Functions;

public abstract class AbstractObservationTableWriter<I,O> implements ObservationTableWriter<I,O> {
	
	private Function<? super Word<? extends I>,? extends String> wordToString;
	private Function<? super O,? extends String> outputToString;
	
	
	
	public AbstractObservationTableWriter() {
		this(Functions.toStringFunction(), Functions.toStringFunction());
	}
	
	public AbstractObservationTableWriter(
			Function<? super Word<? extends I>,? extends String> wordToString,
			Function<? super O,? extends String> outputToString) {
		this.wordToString = safeToStringFunction(wordToString);
		this.outputToString = safeToStringFunction(outputToString);
	}
	
	
	
	public void setWordToString(Function<? super Word<? extends I>,? extends String> wordToString) {
		this.wordToString = safeToStringFunction(wordToString);
	}
	
	public void setOutputToString(Function<? super O,? extends String> outputToString) {
		this.outputToString = safeToStringFunction(outputToString);
	}
	
	
	protected String wordToString(Word<? extends I> word) {
		return wordToString.apply(word);
	}
	
	protected String outputToString(O output) {
		return outputToString.apply(output);
	}
	
	
	protected static <T>
	Function<? super T,? extends String> safeToStringFunction(Function<? super T,? extends String> toStringFunction) {
		if(toStringFunction != null) {
			return toStringFunction;
		}
		return Functions.toStringFunction();
	}
}
