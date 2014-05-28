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

import com.google.common.base.Function;
import de.learnlib.algorithms.features.observationtable.ObservationTable;
import net.automatalib.words.Word;

import javax.annotation.Nullable;
import javax.annotation.WillNotClose;
import java.io.IOException;
import java.util.List;

/**
 * A writer which stores the suffixes of an {@link ObservationTable} in plaintext. The delimiter in this format
 * will be a single semicolon for words and single commas for symbols, so these characters must not be used in the
 * string representation of the symbols.
 *
 * @param <I>
 *     The input type.
 * @param <D>
 *     The output domain type
 */
public class SuffixASCIIWriter<I,D> extends AbstractObservationTableWriter<I,D> {

	private static final String WORD_DELIMITER = ";";
	private static final String SYMBOL_DELIMITER = ",";

	public SuffixASCIIWriter() {
		super();

		Function<? super Word<? extends I>, String> wordToString = new Function<Word<? extends I>, String>() {
			@Nullable
			@Override
			public String apply(@Nullable Word<? extends I> is) {
				if (is == null) {
					return "";
				}

				boolean first = true;

				StringBuilder sb = new StringBuilder();

				for (I symbol : is) {
					if (first) {
						first = false;
					}
					else {
						sb.append(SYMBOL_DELIMITER);
					}

					String stringRepresentation = symbol.toString();

					if (stringRepresentation.contains(SYMBOL_DELIMITER) ||
							stringRepresentation.contains(WORD_DELIMITER)) {
						throw new IllegalArgumentException("Symbol '" + stringRepresentation + "' must not contain " +
						"delimiters '" + SYMBOL_DELIMITER + "' or '" + WORD_DELIMITER + '\'');
					}

					sb.append(symbol.toString());
				}

				return sb.toString();
			}
		};

		super.setWordToString(wordToString);
	}

	@Override
	public void write(ObservationTable<? extends I, ? extends D> table, @WillNotClose Appendable out)
			throws IOException {
		List<? extends Word<? extends I>> suffixes = table.getSuffixes();

		StringBuilder sb = new StringBuilder();
		boolean first = true;

		for (Word<? extends I> word : suffixes) {
			if (first) {
				first = false;
			}
			else {
				sb.append(WORD_DELIMITER);
			}

			String stringRepresentation = wordToString(word);
			if (stringRepresentation.contains(WORD_DELIMITER)) {
				throw new IllegalArgumentException("Delimiter '" + WORD_DELIMITER + "' must not be used in symbol names. " +
				"Symbol containing the delimiter was '" + stringRepresentation + '\'');
			}
			else {
				sb.append(stringRepresentation);
			}
		}

		out.append(sb.toString());
	}

}
