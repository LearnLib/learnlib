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
package de.learnlib.datastructure.observationtable.reader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.learnlib.datastructure.observationtable.ObservationTable;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.common.util.HashUtil;
import net.automatalib.word.Word;

public class SuffixASCIIReader<I, D> implements ObservationTableReader<I, D> {

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
        Map<String, I> nameToSymbol = new HashMap<>(HashUtil.capacity(alphabet.size()));

        for (I symbol : alphabet) {
            String symbolName = Objects.toString(symbol);
            if (nameToSymbol.containsKey(symbolName)) {
                throw new IllegalArgumentException(
                        "Symbol name '" + symbolName + "' is used more than once in alphabet");
            } else {
                nameToSymbol.put(symbolName, symbol);
            }
        }

        return nameToSymbol;
    }

}
