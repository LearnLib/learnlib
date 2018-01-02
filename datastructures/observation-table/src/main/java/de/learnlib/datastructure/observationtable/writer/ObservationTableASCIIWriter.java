/* Copyright (C) 2013-2018 TU Dortmund
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
package de.learnlib.datastructure.observationtable.writer;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import de.learnlib.datastructure.observationtable.ObservationTable;
import de.learnlib.datastructure.observationtable.Row;
import net.automatalib.words.Word;

public class ObservationTableASCIIWriter<I, D> extends AbstractObservationTableWriter<I, D> {

    private boolean rowSeparators;

    public ObservationTableASCIIWriter(Function<? super Word<? extends I>, ? extends String> wordToString,
                                       Function<? super D, ? extends String> outputToString,
                                       boolean rowSeparators) {
        super(wordToString, outputToString);
        this.rowSeparators = rowSeparators;
    }

    public ObservationTableASCIIWriter() {
        this(true);
    }

    public ObservationTableASCIIWriter(boolean rowSeparators) {
        this.rowSeparators = rowSeparators;
    }

    public void setRowSeparators(boolean rowSeparators) {
        this.rowSeparators = rowSeparators;
    }

    @Override
    public void write(ObservationTable<? extends I, ? extends D> table, Appendable out) throws IOException {
        writeInternal(table, super.wordToString, super.outputToString, out);
    }

    /**
     * Utility method to bind wildcard generics.
     *
     * @see #write(ObservationTable, Appendable)
     */
    private <I, D> void writeInternal(ObservationTable<I, D> table,
                                      Function<? super Word<? extends I>, ? extends String> wordToString,
                                      Function<? super D, ? extends String> outputToString,
                                      Appendable out) throws IOException {
        List<Word<I>> suffixes = table.getSuffixes();
        int numSuffixes = suffixes.size();

        int[] colWidth = new int[numSuffixes + 1];

        int i = 1;
        for (Word<I> suffix : suffixes) {
            colWidth[i++] = wordToString.apply(suffix).length();
        }

        for (Row<I> row : table.getAllRows()) {
            int thisWidth = wordToString.apply(row.getLabel()).length();
            if (thisWidth > colWidth[0]) {
                colWidth[0] = thisWidth;
            }

            i = 1;
            for (D value : table.rowContents(row)) {
                thisWidth = outputToString.apply(value).length();
                if (thisWidth > colWidth[i]) {
                    colWidth[i] = thisWidth;
                }
                i++;
            }
        }

        appendSeparatorRow(out, '=', colWidth);
        String[] content = new String[numSuffixes + 1];

        // Header
        content[0] = "";
        i = 1;
        for (Word<I> suffix : suffixes) {
            content[i++] = wordToString.apply(suffix);
        }
        appendContentRow(out, content, colWidth);
        appendSeparatorRow(out, '=', colWidth);

        boolean first = true;
        for (Row<I> spRow : table.getShortPrefixRows()) {
            if (first) {
                first = false;
            } else if (rowSeparators) {
                appendSeparatorRow(out, '-', colWidth);
            }
            content[0] = wordToString.apply(spRow.getLabel());
            i = 1;
            for (D value : table.rowContents(spRow)) {
                content[i++] = outputToString.apply(value);
            }
            appendContentRow(out, content, colWidth);
        }

        appendSeparatorRow(out, '=', colWidth);

        first = true;
        for (Row<I> lpRow : table.getLongPrefixRows()) {
            if (first) {
                first = false;
            } else if (rowSeparators) {
                appendSeparatorRow(out, '-', colWidth);
            }
            content[0] = wordToString.apply(lpRow.getLabel());
            i = 1;
            for (D value : table.rowContents(lpRow)) {
                content[i++] = outputToString.apply(value);
            }
            appendContentRow(out, content, colWidth);
        }

        appendSeparatorRow(out, '=', colWidth);
    }

    private static void appendSeparatorRow(Appendable a, char sepChar, int[] colWidth) throws IOException {
        a.append('+').append(sepChar);
        appendRepeated(a, sepChar, colWidth[0]);
        for (int i = 1; i < colWidth.length; i++) {
            a.append(sepChar).append('+').append(sepChar);
            appendRepeated(a, sepChar, colWidth[i]);
        }
        a.append(sepChar).append("+\n");
    }

    private static void appendContentRow(Appendable a, String[] content, int[] colWidth) throws IOException {
        a.append("| ");
        appendRightPadded(a, content[0], colWidth[0]);
        for (int i = 1; i < content.length; i++) {
            a.append(" | ");
            appendRightPadded(a, content[i], colWidth[i]);
        }
        a.append(" |\n");
    }

    private static void appendRepeated(Appendable a, char c, int count) throws IOException {
        for (int i = 0; i < count; i++) {
            a.append(c);
        }
    }

    private static void appendRightPadded(Appendable a, String string, int width) throws IOException {
        a.append(string);
        appendRepeated(a, ' ', width - string.length());
    }

}
