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

import java.io.IOException;
import java.util.List;

import net.automatalib.words.Word;

import com.google.common.base.Function;
import com.google.common.base.Functions;

import de.learnlib.algorithms.features.observationtable.ObservationTable.Row;

public class ObservationTableHTMLWriter<I, O> {
	
	private final Appendable out;
	private final Function<? super Word<I>,? extends String> wordToString;
	private final Function<? super O,? extends String> outputToString;
	
	
	public ObservationTableHTMLWriter(Appendable out,
			Function<? super Word<I>,? extends String> wordToString,
			Function<? super O,? extends String> outputToString) {
		if(wordToString == null) {
			wordToString = Functions.toStringFunction();
		}
		if(outputToString == null) {
			outputToString = Functions.toStringFunction();
		}
		
		this.out = out;
		this.wordToString = wordToString;
		this.outputToString = outputToString;
	}
	
	public void write(ObservationTable<I,O> table) throws IOException {
		List<? extends Word<I>> suffixes = table.getSuffixes();
		
		out.append("<table class=\"learnlib-observationtable\">\n");
		out.append("	<thead>\n");
		out.append("		<tr><th rowspan=\"2\" class=\"prefix\">Prefix</th><th colspan=\"").append(Integer.toString(suffixes.size())).append("\" class=\"suffixes-header\">Suffixes</th></tr>\n");
		out.append("		<tr>");
		for(Word<I> suffix : suffixes) {
			out.append("<td>").append(wordToString.apply(suffix)).append("</td>");
		}
		out.append("</tr>\n");
		out.append("	</thead>\n");
		out.append("	<tbody>\n");
		
		for(Row<I,O> row : table.getShortPrefixRows()) {
			out.append("		<tr class=\"short-prefix\"><td class=\"prefix\">").append(wordToString.apply(row.getLabel())).append("</td>");
			for(O value : row.getValues()) {
				out.append("<td class=\"suffix-column\">").append(outputToString.apply(value)).append("</td>");
			}
			out.append("</tr>\n");
		}
		
		out.append("		<tr><td colspan=\"").append(Integer.toString(suffixes.size() + 1)).append("\"></td></tr>\n");
		
		for(Row<I,O> row : table.getLongPrefixRows()) {
			out.append("		<tr class=\"long-prefix\"><td>").append(wordToString.apply(row.getLabel())).append("</td>");
			for(O value : row.getValues()) {
				out.append("<td class=\"suffix-column\">").append(outputToString.apply(value)).append("</td>");
			}
			out.append("</tr>\n");
		}
		
		out.append("</table>\n");
	}
	
	
}
