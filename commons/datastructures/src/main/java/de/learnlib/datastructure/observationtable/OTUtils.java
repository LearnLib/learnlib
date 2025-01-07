/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of LearnLib <https://learnlib.de>.
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

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Objects;
import java.util.function.Function;

import de.learnlib.datastructure.observationtable.reader.ObservationTableReader;
import de.learnlib.datastructure.observationtable.writer.ObservationTableHTMLWriter;
import de.learnlib.datastructure.observationtable.writer.ObservationTableWriter;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.common.util.IOUtil;
import net.automatalib.word.Word;

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

    /**
     * Convenience method for {@link #writeHTMLToFile(ObservationTable, File)} that automatically creates (and returns)
     * a temporary file to write to. Note that the file is {@link File#deleteOnExit() deleted} upon VM termination.
     *
     * @param table
     *         the observation table to write to the file
     * @param <I>
     *         input symbol type
     * @param <D>
     *         output domain type
     *
     * @return the temporary file with the observation table's contents written to
     *
     * @throws IOException
     *         when writing to the file fails
     */
    public static <I, D> File writeHTMLToFile(ObservationTable<I, D> table) throws IOException {
        final File file = File.createTempFile("learnlib-ot", ".html");
        file.deleteOnExit();
        writeHTMLToFile(table, file);
        return file;
    }

    /**
     * Convenience method for {@link #writeHTMLToFile(ObservationTable, File, Function, Function)} that uses
     * {@link Object#toString()} to render words and outputs of the observation table.
     *
     * @param table
     *         the observation table to write to the file
     * @param file
     *         the file to write to
     * @param <I>
     *         input symbol type
     * @param <D>
     *         output domain type
     *
     * @throws IOException
     *         when writing to the file fails
     */
    public static <I, D> void writeHTMLToFile(ObservationTable<I, D> table, File file) throws IOException {
        writeHTMLToFile(table, file, Objects::toString, Objects::toString);
    }

    /**
     * Writes the contents of a given observation table into a file using HTML as a (markup) language and the given
     * functions to display inputs and outputs.
     *
     * @param table
     *         the observation table to write to the file
     * @param file
     *         the file to write to
     * @param wordToString
     *         the input word renderer
     * @param outputToString
     *         the output value renderer
     * @param <I>
     *         input symbol type
     * @param <D>
     *         output domain type
     *
     * @throws IOException
     *         when writing to the file fails
     */
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
}
