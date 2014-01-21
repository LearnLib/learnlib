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
package de.learnlib.algorithms.features.observationtable;

import java.awt.Desktop;
import java.awt.HeadlessException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import net.automatalib.words.Word;

import com.google.common.base.Function;
import com.google.common.base.Functions;

public abstract class OTUtils {

	private static final String HTML_FILE_HEADER = "<html><head>\n"
			+ "<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n"
			+ "<style type=\"text/css\">\n"
			+ "table.learnlib-observationtable { border-width: 1px; border: solid; }\n"
			+ "table.learnlib-observationtable th.suffixes-header { text-align: center; }\n"
			+ "table.learnlib-observationtable th.prefix { vertical-align: top; }\n"
			+ "table.learnlib-observationtable .suffix-column { text-align: left; }\n"
			+ "table.learnlib-observationtable tr { border-width: 1px; border: solid; }\n"
			+ "table.learnlib-observationtable tr.long-prefix { background-color: #dfdfdf; }\n"
			+ "</style></head>\n"
			+ "<body>\n";
	private static final String HTML_FILE_FOOTER = "</body></html>\n";
	
	
	public static <I,O>
	void writeHTMLToFile(
			ObservationTable<I, O> table,
			File file,
			Function<? super Word<I>,? extends String> wordToString,
			Function<? super O,? extends String> outputToString) throws IOException {
		
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			bw.write(HTML_FILE_HEADER);
			ObservationTableHTMLWriter<I, O> otWriter
				= new ObservationTableHTMLWriter<>(bw, wordToString, outputToString);
			otWriter.write(table);
			bw.write(HTML_FILE_FOOTER);
		}
	}
	
	public static <I,O>
	void writeHTMLToFile(
			ObservationTable<I, O> table,
			File file) throws IOException {
		writeHTMLToFile(table, file, Functions.toStringFunction(), Functions.toStringFunction());
	}
	
	public static <I,O>
	void displayHTMLInBrowser(
			ObservationTable<I,O> table,
			Function<? super Word<I>,? extends String> wordToString,
			Function<? super O,? extends String> outputToString) throws IOException, HeadlessException, UnsupportedOperationException {
		File tempFile = File.createTempFile("learnlib-ot" , ".html");
		writeHTMLToFile(table, tempFile, wordToString, outputToString);
		Desktop.getDesktop().browse(tempFile.toURI());
	}
	
	public static <I,O>
	void displayHTMLInBrowser(ObservationTable<I,O> table) throws IOException, HeadlessException, UnsupportedOperationException {
		displayHTMLInBrowser(table, Functions.toStringFunction(), Functions.toStringFunction());
	}
	
	
	public static <I,O>
	void writeASCIIToFile(
			ObservationTable<I,O> table,
			File file,
			Function<? super Word<I>,? extends String> wordToString,
			Function<? super O,? extends String> outputToString) throws IOException {
		
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			ObservationTableASCIIWriter<I, O> otWriter
				= new ObservationTableASCIIWriter<>(bw, true, wordToString, outputToString);
			otWriter.write(table);
		}
	}
	
	public static <I,O>
	void writeASCIIToFile(
			ObservationTable<I,O> table,
			File file) throws IOException {
		writeASCIIToFile(table, file, Functions.toStringFunction(), Functions.toStringFunction());
	}
	
	public static <I,O>
	void writeASCII(
			ObservationTable<I, O> table,
			Appendable out,
			Function<? super Word<I>,? extends String> wordToString,
			Function<? super O,? extends String> outputToString) throws IOException {
		
		ObservationTableASCIIWriter<I, O> otWriter
			= new ObservationTableASCIIWriter<>(out, true, wordToString, outputToString);
		otWriter.write(table);
	}
	
	public static <I,O>
	void writeASCII(
			ObservationTable<I, O> table,
			Appendable out) throws IOException {
		
		ObservationTableASCIIWriter<I, O> otWriter
			= new ObservationTableASCIIWriter<>(out, true);
		otWriter.write(table);
	}
	
	public static <I,O>
	void writeASCIIToSysout(
			ObservationTable<I, O> table,
			Function<? super Word<I>,? extends String> wordToString,
			Function<? super O,? extends String> outputToString) {
		
		ObservationTableASCIIWriter<I, O> otWriter
			= new ObservationTableASCIIWriter<>(System.out, true, wordToString, outputToString);
		try {
			otWriter.write(table);
		}
		catch(IOException ex) {
			throw new IllegalStateException("Writing to System.out must not throw");
		}
	}
	
	public static <I,O>
	void writeASCIIToSysout(
			ObservationTable<I, O> table) {
		writeASCIIToSysout(table, Functions.toStringFunction(), Functions.toStringFunction());
	}
	
	private OTUtils() {
		throw new IllegalStateException("Constructor should never be invoked");
	}
}
