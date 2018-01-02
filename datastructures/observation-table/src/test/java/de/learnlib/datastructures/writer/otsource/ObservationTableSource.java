/* Copyright (C) 2013-2018 TU Dortmund
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
package de.learnlib.datastructures.writer.otsource;

import java.util.ArrayList;
import java.util.List;

import de.learnlib.datastructure.observationtable.ObservationTable;
import de.learnlib.datastructure.observationtable.reader.SimpleObservationTable;
import net.automatalib.words.Word;

/**
 * This class creates observation tables which may be used for testing purposes.
 */
public final class ObservationTableSource {

    private ObservationTableSource() {
    }

    public static ObservationTable<String, String> otWithFourSuffixes() {
        List<Word<String>> suffixes = new ArrayList<>();
        suffixes.add(Word.epsilon());
        suffixes.add(Word.fromLetter("A"));
        suffixes.add(Word.fromLetter("B"));
        suffixes.add(Word.fromLetter("A").concat(Word.fromLetter("B")));
        return new SimpleObservationTable<>(suffixes);
    }

    public static ObservationTable<String, String> otWithFourSuffixesUsingDelimiterInNames() {
        List<Word<String>> suffixes = new ArrayList<>();
        suffixes.add(Word.epsilon());
        suffixes.add(Word.fromLetter("A,"));
        suffixes.add(Word.fromLetter("B"));
        suffixes.add(Word.fromLetter("A,").concat(Word.fromLetter("B")));
        return new SimpleObservationTable<>(suffixes);
    }

}
