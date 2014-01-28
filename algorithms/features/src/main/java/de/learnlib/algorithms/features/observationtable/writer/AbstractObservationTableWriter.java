/* Copyright (C) 2014 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 *
 * LearnLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 *
 * LearnLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with LearnLib; if not, see
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package de.learnlib.algorithms.features.observationtable.writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

import com.google.common.base.Function;
import com.google.common.base.Functions;

import de.learnlib.algorithms.features.observationtable.ObservationTable;

import net.automatalib.words.Word;

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

	@Override
	public void write(ObservationTable<? extends I, ? extends O> table,
			PrintStream out) {
		try {
			write(table, (Appendable)out);
		}
		catch(IOException ex) {
			throw new AssertionError("Writing to PrintStream must not throw");
		}
	}

	@Override
	public void write(ObservationTable<? extends I, ? extends O> table,
			StringBuilder out) {
		try {
			write(table, (Appendable)out);
		}
		catch(IOException ex) {
			throw new AssertionError("Writing to StringBuilder must not throw");
		}
	}
	
	@Override
	public void write(ObservationTable<? extends I, ? extends O> table,
			File file) throws IOException {
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			write(table, bw);
		}
	}
	
	
}
