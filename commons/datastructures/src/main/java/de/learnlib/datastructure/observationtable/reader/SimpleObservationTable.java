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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.learnlib.datastructure.observationtable.OTUtils;
import de.learnlib.datastructure.observationtable.ObservationTable;
import de.learnlib.datastructure.observationtable.Row;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This class represents the data structure of an {@link ObservationTable} without providing any meaningful
 * functionality. It is used to store the result of reading string representations like with {@link
 * OTUtils#fromString(String, Alphabet, ObservationTableReader)}.
 *
 * @param <I>
 *         The input type.
 * @param <D>
 *         The output domain type.
 */
public class SimpleObservationTable<I, @Nullable D> implements ObservationTable<I, D> {

    final List<? extends Word<I>> suffixes;

    public SimpleObservationTable(List<? extends Word<I>> suffixes) {
        this.suffixes = suffixes;
    }

    @Override
    public Collection<Row<I>> getShortPrefixRows() {
        return Collections.emptyList();
    }

    @Override
    public Collection<Row<I>> getLongPrefixRows() {
        return Collections.emptyList();
    }


    @Override
    public Row<I> getRow(int idx) {
        throw new IndexOutOfBoundsException("This OT contains no data");
    }

    @Override
    public Row<I> getRow(Word<I> prefix) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int numberOfDistinctRows() {
        return 0;
    }

    @Override
    public List<Word<I>> getSuffixes() {
        return Collections.unmodifiableList(suffixes);
    }

    @Override
    public Alphabet<I> getInputAlphabet() {
        return Alphabets.fromArray();
    }

    @Override
    public @Nullable D cellContents(Row<I> row, int columnId) {
        return null;
    }

    @Override
    public List<D> rowContents(Row<I> row) {
        return Collections.emptyList();
    }

    @Override
    public Word<I> transformAccessSequence(Word<I> word) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAccessSequence(Word<I> word) {
        return false;
    }
}
