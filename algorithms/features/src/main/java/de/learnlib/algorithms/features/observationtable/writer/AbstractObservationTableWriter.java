/* Copyright (C) 2014 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

public abstract class AbstractObservationTableWriter<I,D> implements ObservationTableWriter<I,D> {
	
	private Function<? super Word<? extends I>,? extends String> wordToString;
	private Function<? super D,? extends String> outputToString;
	
	
	
	public AbstractObservationTableWriter() {
		this(Functions.toStringFunction(), Functions.toStringFunction());
	}
	
	public AbstractObservationTableWriter(
			Function<? super Word<? extends I>,? extends String> wordToString,
			Function<? super D,? extends String> outputToString) {
		this.wordToString = safeToStringFunction(wordToString);
		this.outputToString = safeToStringFunction(outputToString);
	}
	
	
	
	public void setWordToString(Function<? super Word<? extends I>,? extends String> wordToString) {
		this.wordToString = safeToStringFunction(wordToString);
	}
	
	public void setOutputToString(Function<? super D,? extends String> outputToString) {
		this.outputToString = safeToStringFunction(outputToString);
	}
	
	
	protected String wordToString(Word<? extends I> word) {
		return wordToString.apply(word);
	}
	
	protected String outputToString(D output) {
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
	public void write(ObservationTable<? extends I, ? extends D> table,
			PrintStream out) {
		try {
			write(table, (Appendable)out);
		}
		catch(IOException ex) {
			throw new AssertionError("Writing to PrintStream must not throw");
		}
	}

	@Override
	public void write(ObservationTable<? extends I, ? extends D> table,
			StringBuilder out) {
		try {
			write(table, (Appendable)out);
		}
		catch(IOException ex) {
			throw new AssertionError("Writing to StringBuilder must not throw");
		}
	}
	
	@Override
	public void write(ObservationTable<? extends I, ? extends D> table,
			File file) throws IOException {
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			write(table, bw);
		}
	}
	
	
}
