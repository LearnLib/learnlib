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
package de.learnlib.algorithms.features.observationtable.writer.otsource;

import de.learnlib.algorithms.features.observationtable.ObservationTable;
import de.learnlib.algorithms.features.observationtable.reader.SimpleObservationTable;
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
		return new SimpleObservationTable<>(suffixes);
	}

	public static ObservationTable<String,String> otWithFourSuffixesUsingDelimiterInNames() {
		List<Word<String>> suffixes = new ArrayList<>();
		suffixes.add(Word.<String>epsilon());
		suffixes.add(Word.fromLetter("A,"));
		suffixes.add(Word.fromLetter("B"));
		suffixes.add(Word.fromLetter("A,").concat(Word.fromLetter("B")));
		return new SimpleObservationTable<>(suffixes);
	}


}
