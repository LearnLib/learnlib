/* Copyright (C) 2013-2017 TU Dortmund
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
package de.learnlib.datastructure.observationtable.reader;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.learnlib.datastructure.observationtable.InvalidRowException;
import de.learnlib.datastructure.observationtable.NoSuchRowException;
import de.learnlib.datastructure.observationtable.OTUtils;
import de.learnlib.datastructure.observationtable.ObservationTable;
import net.automatalib.words.Word;

/**
 * This class represents the data structure of an {@link ObservationTable} wihout providing any meaningful
 * functionalily. It is used to store the result of reading string representations like with {@link
 * OTUtils#fromString(String, net.automatalib.words.Alphabet, ObservationTableReader)}.
 *
 * @param <I>
 *         The input type.
 * @param <D>
 *         The output domain type.
 */
public class SimpleObservationTable<I, D> implements ObservationTable<I, D> {

    final List<? extends Word<I>> suffixes;

    public SimpleObservationTable(List<? extends Word<I>> suffixes) {
        this.suffixes = suffixes;
    }

    @Nonnull
    @Override
    public Collection<Word<I>> getAllPrefixes() {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public Collection<Word<I>> getShortPrefixes() {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public Collection<Word<I>> getLongPrefixes() {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public Collection<Row<I, D>> getShortPrefixRows() {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public Collection<Row<I, D>> getLongPrefixRows() {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public Row<I, D> getRow(Word<I> prefix) throws NoSuchRowException {
        throw new NoSuchRowException();
    }

    @Nonnull
    @Override
    public Collection<Row<I, D>> getAllRows() {
        return Collections.emptyList();
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Nullable
    @Override
    public Row<I, D> findUnclosedRow() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public Word<I> findDistinguishingSuffix(Inconsistency<I, D> inconsistency)
            throws NoSuchRowException, InvalidRowException {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public Word<I> findDistinguishingSuffix(Row<I, D> firstRow, Row<I, D> secondRow) throws InvalidRowException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int findDistinguishingSuffixIndex(Inconsistency<I, D> inconsistency)
            throws NoSuchRowException, InvalidRowException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int findDistinguishingSuffixIndex(Row<I, D> firstRow, Row<I, D> secondRow) throws InvalidRowException {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public Word<I> getSuffix(@Nonnegative int index) throws IndexOutOfBoundsException {
        return suffixes.get(index);
    }

    @Nullable
    @Override
    public Row<I, D> getSuccessorRow(Row<I, D> spRow, @Nullable I symbol) throws InvalidRowException {
        throw new InvalidRowException();
    }

    @Nonnull
    @Override
    public List<Word<I>> getSuffixes() {
        return Collections.unmodifiableList(suffixes);
    }

    @Override
    public boolean isConsistent(Collection<? extends I> inputs) {
        return false;
    }

    @Nullable
    @Override
    public Inconsistency<I, D> findInconsistency(Collection<? extends I> inputs) {
        throw new UnsupportedOperationException();
    }
}
