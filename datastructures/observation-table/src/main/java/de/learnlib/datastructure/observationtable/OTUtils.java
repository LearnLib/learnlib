/* Copyright (C) 2013-2021 TU Dortmund
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
package de.learnlib.datastructure.observationtable;

import java.awt.Desktop;
import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Objects;
import java.util.function.Function;

import de.learnlib.datastructure.observationtable.reader.ObservationTableReader;
import de.learnlib.datastructure.observationtable.writer.ObservationTableHTMLWriter;
import de.learnlib.datastructure.observationtable.writer.ObservationTableWriter;
import net.automatalib.commons.util.IOUtil;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public final class OTUtils {

    private static final String HTML_FILE_HEADER =
            "<!doctype html>" + System.lineSeparator()
                    + "<html><head>" + System.lineSeparator()
                    + "<meta charset=\"UTF-8\">" + System.lineSeparator()
                    + "<style type=\"text/css\">" + System.lineSeparator()
                    + "table.learnlib-observationtable { border-width: 1px; border: solid; }" + System.lineSeparator()
                    + "table.learnlib-observationtable th.suffixes-header { text-align: center; }" + System.lineSeparator()
                    + "table.learnlib-observationtable th.prefix { vertical-align: top; }" + System.lineSeparator()
                    + "table.learnlib-observationtable .suffix-column { text-align: left; }" + System.lineSeparator()
                    + "table.learnlib-observationtable tr { border-width: 1px; border: solid; }" + System.lineSeparator()
                    + "table.learnlib-observationtable tr.long-prefix { background-color: #dfdfdf; }" + System.lineSeparator()
                    + "</style></head>" + System.lineSeparator()
                    + "<body>" + System.lineSeparator();

    private static final String HTML_FILE_FOOTER = "</body></html>" + System.lineSeparator();

    private OTUtils() {
        // prevent instantiation
    }

    public static <I, D> String toString(ObservationTable<I, D> table, ObservationTableWriter<I, D> writer) {
        StringBuilder sb = new StringBuilder();
        writer.write(table, sb);

        return sb.toString();
    }

    public static <I, D> ObservationTable<I, D> fromString(String source,
                                                           Alphabet<I> alphabet,
                                                           ObservationTableReader<I, D> reader) {
        return reader.read(source, alphabet);
    }

    public static <I, D> void writeHTMLToFile(ObservationTable<I, D> table, File file) throws IOException {
        writeHTMLToFile(table, file, Objects::toString, Objects::toString);
    }

    public static <I, D> void writeHTMLToFile(ObservationTable<I, D> table,
                                              File file,
                                              Function<? super Word<? extends I>, ? extends String> wordToString,
                                              Function<? super D, ? extends String> outputToString) throws IOException {

        try (Writer w = IOUtil.asBufferedUTF8Writer(file)) {
            w.write(HTML_FILE_HEADER);
            ObservationTableHTMLWriter<I, D> otWriter = new ObservationTableHTMLWriter<>(wordToString, outputToString);
            otWriter.write(table, w);
            w.write(HTML_FILE_FOOTER);
        }
    }

    /**
     * Convenience method for {@link #displayHTMLInBrowser(ObservationTable, Function, Function)} that uses {@link
     * Object#toString()} to render words and outputs of the observation table.
     */
    public static <I, D> void displayHTMLInBrowser(ObservationTable<I, D> table) throws IOException {
        displayHTMLInBrowser(table, Objects::toString, Objects::toString);
    }

    /**
     * Displays the observation table as a HTML document in the default browser.
     * <p>
     * This method internally relies on {@link Desktop#browse(java.net.URI)}, hence it will not work if {@link Desktop}
     * is not supported, or if the application is running in headless mode.
     * <p>
     * <b>IMPORTANT NOTE:</b> Calling this method may delay the termination of the JVM by up to 5 seconds. This is due
     * to the fact that the temporary file created in this method is marked for deletion upon JVM termination. If the
     * JVM terminates too early, it might be deleted before it was loaded by the browser.
     *
     * @param table
     *         the observation table to display
     * @param wordToString
     *         the transformation from words to strings. This transformation is <b>not</b> required nor expected to
     *         escape HTML entities
     * @param outputToString
     *         the transformation from outputs to strings. This transformation is <b>not</b> required nor expected to
     *         escape HTML entities
     *
     * @throws IOException
     *         if creating or writing to the temporary file fails
     * @throws HeadlessException
     *         if the JVM is running in headless mode
     * @throws UnsupportedOperationException
     *         if {@link Desktop#getDesktop()} is not supported by the system
     */
    public static <I, D> void displayHTMLInBrowser(ObservationTable<I, D> table,
                                                   Function<? super Word<? extends I>, ? extends String> wordToString,
                                                   Function<? super D, ? extends String> outputToString)
            throws IOException {
        File tempFile = File.createTempFile("learnlib-ot", ".html");

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
}
