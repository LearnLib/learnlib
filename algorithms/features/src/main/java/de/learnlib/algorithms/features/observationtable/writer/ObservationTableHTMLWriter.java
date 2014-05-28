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

import java.io.IOException;
import java.util.List;

import net.automatalib.words.Word;

import com.google.common.base.Function;

import de.learnlib.algorithms.features.observationtable.ObservationTable;
import de.learnlib.algorithms.features.observationtable.ObservationTable.Row;

/**
 * Writes an observation table as a HTML table.
 * <p>
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type (upper bound)
 * @param <D> output domain type (upper bound)
 */
public class ObservationTableHTMLWriter<I, D> extends AbstractObservationTableWriter<I,D> {
	
	public ObservationTableHTMLWriter(
			Function<? super Word<? extends I>,? extends String> wordToString,
			Function<? super D,? extends String> outputToString) {
		super(wordToString, outputToString);
	}
	
	@Override
	public void write(ObservationTable<? extends I,? extends D> table, Appendable out) throws IOException {
		List<? extends Word<? extends I>> suffixes = table.getSuffixes();
		
		out.append("<table class=\"learnlib-observationtable\">\n");
		out.append("	<thead>\n");
		out.append("		<tr><th rowspan=\"2\" class=\"prefix\">Prefix</th><th colspan=\"").append(Integer.toString(suffixes.size())).append("\" class=\"suffixes-header\">Suffixes</th></tr>\n");
		out.append("		<tr>");
		for(Word<? extends I> suffix : suffixes) {
			out.append("<td>").append(wordToString(suffix)).append("</td>");
		}
		out.append("</tr>\n");
		out.append("	</thead>\n");
		out.append("	<tbody>\n");
		
		for(Row<? extends I,? extends D> row : table.getShortPrefixRows()) {
			out.append("		<tr class=\"short-prefix\"><td class=\"prefix\">").append(wordToString(row.getLabel())).append("</td>");
			for(D value : row) {
				out.append("<td class=\"suffix-column\">").append(outputToString(value)).append("</td>");
			}
			out.append("</tr>\n");
		}
		
		out.append("		<tr><td colspan=\"").append(Integer.toString(suffixes.size() + 1)).append("\"></td></tr>\n");
		
		for(Row<? extends I,? extends D> row : table.getLongPrefixRows()) {
			out.append("		<tr class=\"long-prefix\"><td>").append(wordToString(row.getLabel())).append("</td>");
			for(D value : row) {
				out.append("<td class=\"suffix-column\">").append(outputToString(value)).append("</td>");
			}
			out.append("</tr>\n");
		}
		
		out.append("</table>\n");
	}
	
	
}
