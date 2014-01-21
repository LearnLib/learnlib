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

public class ObservationTableASCIIWriter<I,O> {
	
	private final Appendable out;
	private final boolean rowSeparators;
	private final Function<? super Word<I>,? extends String> wordToString;
	private final Function<? super O,? extends String> outputToString;
	
	public ObservationTableASCIIWriter(
			Appendable out,
			boolean rowSeparators,
			Function<? super Word<I>,? extends String> wordToString,
			Function<? super O,? extends String> outputToString) {
		
		if(wordToString == null) {
			wordToString = Functions.toStringFunction();
		}
		if(outputToString == null) {
			outputToString = Functions.toStringFunction();
		}
		
		this.out = out;
		this.rowSeparators = rowSeparators;
		this.wordToString = wordToString;
		this.outputToString = outputToString;
	}
	
	public ObservationTableASCIIWriter(Appendable out, boolean rowSeparators) {
		this(out, rowSeparators, Functions.toStringFunction(), Functions.toStringFunction());
	}
	
	public ObservationTableASCIIWriter(Appendable out) {
		this(out, true);
	}
	
	
	public void write(ObservationTable<I,O> table) throws IOException {
		List<? extends Word<I>> suffixes = table.getSuffixes();
		int numSuffixes = suffixes.size();
		
		int[] colWidth = new int[numSuffixes + 1];
		
		int i = 1;
		for(Word<I> suffix : suffixes) {
			colWidth[i++] = wordToString.apply(suffix).length();
		}
		
		for(Row<I,O> row : table.getAllRows()) {
			int thisWidth = wordToString.apply(row.getLabel()).length();
			if(thisWidth > colWidth[0]) {
				colWidth[0] = thisWidth;
			}
			
			i = 1;
			for(O value : row.getValues()) {
				thisWidth = outputToString.apply(value).length();
				if(thisWidth > colWidth[i]) {
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
		for(Word<I> suffix : suffixes) {
			content[i++] = wordToString.apply(suffix);
		}
		appendContentRow(out, content, colWidth);
		appendSeparatorRow(out, '=', colWidth);
		
		boolean first = true;
		for(Row<I,O> spRow : table.getShortPrefixRows()) {
			if(first) {
				first = false;
			}
			else if(rowSeparators) {
				appendSeparatorRow(out, '-', colWidth);
			}
			content[0] = wordToString.apply(spRow.getLabel());
			i = 1;
			for(O value : spRow.getValues()) {
				content[i++] = outputToString.apply(value);
			}
			appendContentRow(out, content, colWidth);
		}
		
		appendSeparatorRow(out, '=', colWidth);
		
		first = true;
		for(Row<I,O> lpRow : table.getLongPrefixRows()) {
			if(first) {
				first = false;
			}
			else if(rowSeparators) {
				appendSeparatorRow(out, '-', colWidth);
			}
			content[0] = wordToString.apply(lpRow.getLabel());
			i = 1;
			for(O value : lpRow.getValues()) {
				content[i++] = outputToString.apply(value);
			}
			appendContentRow(out, content, colWidth);
		}
		
		appendSeparatorRow(out, '=', colWidth);
	}
	
	
	private static void appendSeparatorRow(Appendable a, char sepChar, int[] colWidth) throws IOException {
		a.append('+').append(sepChar);
		appendRepeated(a, sepChar, colWidth[0]);
		for(int i = 1; i < colWidth.length; i++) {
			a.append(sepChar).append('+').append(sepChar);
			appendRepeated(a, sepChar, colWidth[i]);
		}
		a.append(sepChar).append("+\n");
	}
	
	private static void appendContentRow(Appendable a, String[] content, int[] colWidth) throws IOException {
		a.append("| ");
		appendRightPadded(a, content[0], colWidth[0]);
		for(int i = 1; i < content.length; i++) {
			a.append(" | ");
			appendRightPadded(a, content[i], colWidth[i]);
		}
		a.append(" |\n");
	}
	
	private static void appendRightPadded(Appendable a, String string, int width) throws IOException {
		a.append(string);
		appendRepeated(a, ' ', width - string.length());
	}
	
	private static void appendRepeated(Appendable a, char c, int count) throws IOException {
		for(int i = 0; i < count; i++) {
			a.append(c);
		}
	}
}
