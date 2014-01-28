/* Copyright (C) 2013 TU Dortmund
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
package de.learnlib.algorithms.baselinelstar;

import net.automatalib.words.Word;

import java.util.Collection;

/**
 * With this class the contents of an {@link ObservationTable} can be
 * printed in a human-readable format.
 */
public class ObservationTablePrinter {

	public static <I> String getPrintableStringRepresentation(ObservationTable<I> observationTable) {
		StringBuilder sb = new StringBuilder();

		int firstColumnWidth = getFirstColumnWidth(observationTable);
		int maxSuffixLength = getMaxWordLength(observationTable.getSuffixes()) + 3;

		Word<I> emptyWord = Word.epsilon();
		sb.append(paddedString(emptyWord.toString(), firstColumnWidth));
		sb.append(" | ");
		for (Word<I> suffix : observationTable.getSuffixes()) {
			sb.append(paddedString(suffix.toString(), maxSuffixLength));
		}

		sb.append("\n\n");

		for (Word<I> state : observationTable.getShortPrefixLabels()) {
			sb.append(paddedString(state.toString(), firstColumnWidth)).append(" | ");
			sb.append(stringPresentationOfRow(observationTable.getRowForPrefix(state), maxSuffixLength));
			sb.append('\n');
		}
		sb.append('\n');

		for (Word<I> candidate : observationTable.getLongPrefixLabels()) {
			sb.append(paddedString(candidate.toString(), firstColumnWidth)).append(" | ");
			sb.append(stringPresentationOfRow(observationTable.getRowForPrefix(candidate), maxSuffixLength));
			sb.append('\n');
		}

		return sb.toString();
	}

	private static <I> int getFirstColumnWidth(ObservationTable<I> observationTable) {
		int maxStateLength = getMaxWordLength(observationTable.getShortPrefixLabels());
		int maxCandidateLength = getMaxWordLength(observationTable.getLongPrefixLabels());
		return Math.max(maxStateLength, maxCandidateLength);
	}

	private static <S> int getMaxWordLength(Collection<Word<S>> words) {
		int length = 0;

		for (Word<S> word : words) {
			int wordLength = word.toString().length();
			if (wordLength > length) {
				length = wordLength;
			}
		}

		return length;
	}

	private static <I> String stringPresentationOfRow(ObservationTableRow<I> row, int length) {
		StringBuilder sb = new StringBuilder();
		for (Boolean value : row.getContents()) {
			if (value) {
				sb.append(paddedString("1", length));
			}
			else {
				sb.append(paddedString("0", length));
			}
		}
		return sb.toString();
	}

	private static String paddedString(String string, int length) {
		StringBuilder sb = new StringBuilder(length);
		sb.append(string);
		for (int i = string.length(); i < length; i++) {
			sb.append(' ');
		}
		return sb.toString();
	}

}
