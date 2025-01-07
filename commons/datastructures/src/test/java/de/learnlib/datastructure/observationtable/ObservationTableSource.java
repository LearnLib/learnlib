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
package de.learnlib.datastructure.observationtable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.automatalib.word.Word;

/**
 * This class creates observation tables which may be used for testing purposes.
 */
public final class ObservationTableSource {

    private ObservationTableSource() {}

    public static ObservationTable<String, String> otWithFourSuffixes() {
        List<Word<String>> suffixes = new ArrayList<>();
        suffixes.add(Word.epsilon());
        suffixes.add(Word.fromLetter("A"));
        suffixes.add(Word.fromLetter("B"));
        suffixes.add(Word.fromSymbols("A", "B"));

        final MockedObservationTable<String, String> result = new MockedObservationTable<>(suffixes);
        addPrefixes(result);

        return result;
    }

    public static ObservationTable<String, String> otWithFourSuffixesUsingDelimiterInNames() {
        List<Word<String>> suffixes = new ArrayList<>();
        suffixes.add(Word.epsilon());
        suffixes.add(Word.fromLetter("A,"));
        suffixes.add(Word.fromLetter("B"));
        suffixes.add(Word.fromSymbols("A,", "B"));

        final MockedObservationTable<String, String> result = new MockedObservationTable<>(suffixes);
        addPrefixes(result);

        return result;
    }

    private static void addPrefixes(MockedObservationTable<String, String> ot) {
        ot.addShortPrefix(Word.fromLetter("A"), Arrays.asList("0", "1", "2", "3"));
        ot.addShortPrefix(Word.fromSymbols("A", "B"), Arrays.asList("3", "2", "1", "0"));
        ot.addLongPrefix(Word.fromSymbols("A", "B", "C"), Arrays.asList("0123", "0123", "0123", "0123"));
    }

}
