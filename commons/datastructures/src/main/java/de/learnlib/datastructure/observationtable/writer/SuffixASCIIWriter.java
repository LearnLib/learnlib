/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of LearnLib <https://learnlib.de>.
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
package de.learnlib.datastructure.observationtable.writer;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Function;

import de.learnlib.datastructure.observationtable.ObservationTable;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A de.learnlib.datastructures.writer which stores the suffixes of an {@link ObservationTable} in plaintext. The
 * delimiter in this format will be a single semicolon for words and single commas for symbols, so these characters must
 * not be used in the string representation of the symbols.
 *
 * @param <I>
 *         The input type.
 * @param <D>
 *         The output domain type
 */
public class SuffixASCIIWriter<I, D> extends AbstractObservationTableWriter<I, D> {

    private static final String WORD_DELIMITER = ";";
    private static final String SYMBOL_DELIMITER = ",";

    private static final Function<? super Word<?>, String> WORD_TO_STRING = is -> {
        if (is == null || is.isEmpty()) {
            return "";
        }

        final StringJoiner joiner = new StringJoiner(SYMBOL_DELIMITER);

        for (@Nullable Object symbol : is) {
            String stringRepresentation = Objects.toString(symbol);
            if (stringRepresentation.contains(SYMBOL_DELIMITER) || stringRepresentation.contains(WORD_DELIMITER)) {
                throw new IllegalArgumentException(
                        "Symbol '" + stringRepresentation + "' must not contain " + "delimiters '" + SYMBOL_DELIMITER +
                        "' or '" + WORD_DELIMITER + '\'');
            }

            joiner.add(stringRepresentation);
        }

        return joiner.toString();
    };

    public SuffixASCIIWriter() {
        super(WORD_TO_STRING, Objects::toString);
    }

    @Override
    public void write(ObservationTable<? extends I, ? extends D> table, Appendable out) throws IOException {
        List<? extends Word<? extends I>> suffixes = table.getSuffixes();

        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (Word<? extends I> word : suffixes) {
            if (first) {
                first = false;
            } else {
                sb.append(WORD_DELIMITER);
            }

            String stringRepresentation = wordToString(word);
            if (stringRepresentation.contains(WORD_DELIMITER)) {
                throw new IllegalArgumentException(
                        "Delimiter '" + WORD_DELIMITER + "' must not be used in symbol names. " +
                        "Symbol containing the delimiter was '" + stringRepresentation + '\'');
            } else {
                sb.append(stringRepresentation);
            }
        }

        out.append(sb.toString());
    }

}
