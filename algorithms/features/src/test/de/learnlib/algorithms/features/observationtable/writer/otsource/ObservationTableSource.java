package de.learnlib.algorithms.features.observationtable.writer.otsource;

import de.learnlib.algorithms.features.observationtable.ObservationTable;
import net.automatalib.words.Word;

import java.util.ArrayList;
import java.util.List;

/**
 * This class creates observation tables which may be used for testing purposes.
 */
public class ObservationTableSource {

	private ObservationTableSource() {
	}

	public static ObservationTable<String,String> otWithFourSuffixes() {
		List<Word<String>> suffixes = new ArrayList<>();
		suffixes.add(Word.<String>epsilon());
		suffixes.add(Word.fromLetter("A"));
		suffixes.add(Word.fromLetter("B"));
		suffixes.add(Word.fromLetter("A").concat(Word.fromLetter("B")));
		return new SimpleObservationTable(suffixes);
	}

	public static ObservationTable<String,String> otWithFourSuffixesUsingDelimiterInNames() {
		List<Word<String>> suffixes = new ArrayList<>();
		suffixes.add(Word.<String>epsilon());
		suffixes.add(Word.fromLetter("A,"));
		suffixes.add(Word.fromLetter("B"));
		suffixes.add(Word.fromLetter("A,").concat(Word.fromLetter("B")));
		return new SimpleObservationTable(suffixes);
	}


}
