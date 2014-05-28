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

import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import com.google.common.base.Function;
import com.google.common.base.Functions;

import de.learnlib.algorithms.features.observationtable.reader.ObservationTableReader;
import de.learnlib.algorithms.features.observationtable.writer.ObservationTableHTMLWriter;
import de.learnlib.algorithms.features.observationtable.writer.ObservationTableWriter;

@ParametersAreNonnullByDefault
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
	
	
	public static <I,D>
	String toString(
			ObservationTable<? extends I,? extends D> table,
			ObservationTableWriter<I,D> writer) {
		StringBuilder sb = new StringBuilder();
		writer.write(table, sb);
		
		return sb.toString();
	}

	public static <I,D> ObservationTable<I,D> fromString(String source, Alphabet<I> alphabet,
			ObservationTableReader<I,D> reader) {
		return reader.read(source, alphabet);
	}
	
	
	
	public static <I,D>
	void writeHTMLToFile(
			ObservationTable<I, D> table,
			File file,
			Function<? super Word<? extends I>,? extends String> wordToString,
			Function<? super D,? extends String> outputToString) throws IOException {
		
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			bw.write(HTML_FILE_HEADER);
			ObservationTableHTMLWriter<I, D> otWriter
				= new ObservationTableHTMLWriter<>(wordToString, outputToString);
			otWriter.write(table, bw);
			bw.write(HTML_FILE_FOOTER);
		}
	}
	
	public static <I,D>
	void writeHTMLToFile(
			ObservationTable<I, D> table,
			File file) throws IOException {
		writeHTMLToFile(table, file, Functions.toStringFunction(), Functions.toStringFunction());
	}
	
	/**
	 * Displays the observation table as a HTML document in the default browser.
	 * <p>
	 * This method internally relies on {@link Desktop#browse(java.net.URI)}, hence it will not
	 * work if {@link Desktop} is not supported, or if the application is running in headless mode.
	 * <p>
	 * <b>IMPORTANT NOTE:</b> Calling this method may delay the termination of the JVM by up to 5 seconds.
	 * This is due to the fact that the temporary file created in this method is marked for deletion
	 * upon JVM termination. If the JVM terminates too early, it might be deleted before it was loaded
	 * by the browser.
	 *   
	 * @param table the observation table to display
	 * @param wordToString the transformation from words to strings. This transformation is <b>not</b> required
	 * nor expected to escape HTML entities
	 * @param outputToString the transformation from outputs to strings. This transformation is <b>not</b> required
	 * nor expected to escape HTML entities
	 * @throws IOException if creating or writing to the temporary file fails 
	 * @throws HeadlessException if the JVM is running in headless mode
	 * @throws UnsupportedOperationException if {@link Desktop#getDesktop()} is not supported by the system
	 */
	public static <I,D>
	void displayHTMLInBrowser(
			ObservationTable<I,D> table,
			Function<? super Word<? extends I>,? extends String> wordToString,
			Function<? super D,? extends String> outputToString) throws IOException, HeadlessException, UnsupportedOperationException {
		File tempFile = File.createTempFile("learnlib-ot" , ".html");
		
		// Doing this might cause problems if the startup delay of the browser
		// causes it to start only after the JVM has exited.
		// Temp directory should be wiped regularly anyway.
		// tempFile.deleteOnExit();
		writeHTMLToFile(table, tempFile, wordToString, outputToString);
		
		Desktop desktop = Desktop.getDesktop();
		// We use browse() instead of open() because, e.g., web developers may have
		// an HTML editor set up as their default application to open HTML files
		desktop.browse(tempFile.toURI());
	}
	
	public static <I,D>
	void displayHTMLInBrowser(ObservationTable<I,D> table) throws IOException, HeadlessException, UnsupportedOperationException {
		displayHTMLInBrowser(table, Functions.toStringFunction(), Functions.toStringFunction());
	}
	
	
	
	private OTUtils() {
		throw new AssertionError("Constructor should never be invoked");
	}
}
