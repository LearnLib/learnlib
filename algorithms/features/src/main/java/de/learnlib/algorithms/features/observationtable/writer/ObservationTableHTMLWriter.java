/* Copyright (C) 2013-2017 TU Dortmund
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

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import de.learnlib.algorithms.features.observationtable.ObservationTable;
import de.learnlib.algorithms.features.observationtable.ObservationTable.Row;
import net.automatalib.words.Word;

/**
 * Writes an observation table as a HTML table.
 * <p>
 *
 * @param <I>
 *         input symbol type (upper bound)
 * @param <D>
 *         output domain type (upper bound)
 *
 * @author Malte Isberner
 */
public class ObservationTableHTMLWriter<I, D> extends AbstractObservationTableWriter<I, D> {

    public ObservationTableHTMLWriter(Function<? super Word<? extends I>, ? extends String> wordToString,
                                      Function<? super D, ? extends String> outputToString) {
        super(wordToString, outputToString);
    }

    @Override
    public void write(ObservationTable<? extends I, ? extends D> table, Appendable out) throws IOException {
        List<? extends Word<? extends I>> suffixes = table.getSuffixes();

        out.append("<table class=\"learnlib-observationtable\">\n");
        out.append("\t<thead>\n");
        out.append("\t\t<tr><th rowspan=\"2\" class=\"prefix\">Prefix</th><th colspan=\"")
           .append(Integer.toString(suffixes.size()))
           .append("\" class=\"suffixes-header\">Suffixes</th></tr>\n");
        out.append("\t\t<tr>");
        for (Word<? extends I> suffix : suffixes) {
            out.append("<td>").append(wordToString(suffix)).append("</td>");
        }
        out.append("</tr>\n");
        out.append("\t</thead>\n");
        out.append("\t<tbody>\n");

        for (Row<? extends I, ? extends D> row : table.getShortPrefixRows()) {
            out.append("\t\t<tr class=\"short-prefix\"><td class=\"prefix\">")
               .append(wordToString(row.getLabel()))
               .append("</td>");
            for (D value : row) {
                out.append("<td class=\"suffix-column\">").append(outputToString(value)).append("</td>");
            }
            out.append("</tr>\n");
        }

        out.append("\t\t<tr><td colspan=\"").append(Integer.toString(suffixes.size() + 1)).append("\"></td></tr>\n");

        for (Row<? extends I, ? extends D> row : table.getLongPrefixRows()) {
            out.append("\t\t<tr class=\"long-prefix\"><td>").append(wordToString(row.getLabel())).append("</td>");
            for (D value : row) {
                out.append("<td class=\"suffix-column\">").append(outputToString(value)).append("</td>");
            }
            out.append("</tr>\n");
        }

        out.append("</table>\n");
    }

}
