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
package de.learnlib.algorithms.features.observationtable.reader;

import com.google.common.collect.Maps;
import de.learnlib.algorithms.features.observationtable.ObservationTable;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SuffixASCIIReader<I,D> implements ObservationTableReader<I,D> {

	private static final String WORD_DELIMITER = ";";
	private static final String SYMBOL_DELIMITER = ",";

	@Override
	public ObservationTable<I, D> read(String source, Alphabet<I> alphabet) {
		Map<String, I> nameToSymbol = generateNameToSymbolMap(alphabet);
		String[] suffixWords = source.split(WORD_DELIMITER);

		List<Word<I>> suffixes = new ArrayList<>(suffixWords.length);

		for (String suffixWord : suffixWords) {
			String[] symbolNames = suffixWord.split(SYMBOL_DELIMITER);
			Word<I> word = Word.epsilon();
			if (!suffixWord.isEmpty()) {
				for (String symbolName : symbolNames) {
					word = word.append(nameToSymbol.get(symbolName));
				}
			}
			suffixes.add(word);
		}

		return new SimpleObservationTable<>(suffixes);
	}

	private Map<String, I> generateNameToSymbolMap(Alphabet<I> alphabet) {
		Map<String, I> nameToSymbol = Maps.newHashMapWithExpectedSize(alphabet.size());

		for (I symbol : alphabet) {
			String symbolName = symbol.toString();
			if (nameToSymbol.containsKey(symbolName)) {
				throw new IllegalArgumentException(
						"Symbol name '" + symbolName + "' is used more than once in alphabet");
			}
			else {
				nameToSymbol.put(symbolName, symbol);
			}
		}

		return nameToSymbol;
	}

}
