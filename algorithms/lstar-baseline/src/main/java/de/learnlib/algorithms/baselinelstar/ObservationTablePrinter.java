/* Copyright (C) 2013 TU Dortmund
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
