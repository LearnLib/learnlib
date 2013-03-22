package de.learnlib.algorithms.angluin;

import net.automatalib.words.Word;

import java.util.Collection;

public class ObservationTablePrinter {

	public static <S> String getPrintableStringRepresentation(ObservationTable<S> observationTable) {
		StringBuilder sb = new StringBuilder();

		int firstColumnWidth = getFirstColumnWidth(observationTable);
		int maxSuffixLength = getMaxWordLength(observationTable.getSuffixes()) + 3;

		Word<S> emptyWord = Word.epsilon();
		sb.append(paddedString(emptyWord.toString(), firstColumnWidth));
		sb.append(" | ");
		for (Word<S> suffix : observationTable.getSuffixes()) {
			sb.append(paddedString(suffix.toString(), maxSuffixLength));
		}

		sb.append("\n\n");

		for (Word<S> state : observationTable.getStates()) {
			sb.append(paddedString(state.toString(), firstColumnWidth)).append(" | ");
			sb.append(stringPresentationOfRow(observationTable.getRowForPrefix(state), maxSuffixLength));
			sb.append("\n");
		}
		sb.append("\n");

		for (Word<S> candidate : observationTable.getCandidates()) {
			sb.append(paddedString(candidate.toString(), firstColumnWidth)).append(" | ");
			sb.append(stringPresentationOfRow(observationTable.getRowForPrefix(candidate), maxSuffixLength));
			sb.append("\n");
		}

		return sb.toString();
	}

	private static <S> int getFirstColumnWidth(ObservationTable<S> observationTable) {
		int maxStateLength = getMaxWordLength(observationTable.getStates());
		int maxCandidateLength = getMaxWordLength(observationTable.getCandidates());
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

	private static String stringPresentationOfRow(ObservationTableRow row, int length) {
		StringBuilder sb = new StringBuilder();
		for (Boolean value : row.getValues()) {
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
			sb.append(" ");
		}
		return sb.toString();
	}

}
