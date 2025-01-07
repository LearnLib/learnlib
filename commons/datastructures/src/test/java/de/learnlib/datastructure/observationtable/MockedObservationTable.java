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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.learnlib.datastructure.observationtable.reader.SimpleObservationTable;
import net.automatalib.word.Word;

/**
 * Mock-up observation table for testing writers.
 *
 * @param <I>
 *         input symbol type
 * @param <D>
 *         observation (output) domain type
 */
public class MockedObservationTable<I, D> extends SimpleObservationTable<I, D> {

    private final Map<List<D>, Integer> contentsToIdMap;
    private final List<List<D>> rowContents;

    private final List<RowImpl<I>> rows;
    private final List<RowImpl<I>> shortPrefixes;
    private final List<RowImpl<I>> longPrefixes;

    MockedObservationTable(List<? extends Word<I>> suffixes) {
        super(suffixes);
        this.rowContents = new LinkedList<>();
        this.contentsToIdMap = new HashMap<>();

        this.rows = new LinkedList<>();
        this.shortPrefixes = new LinkedList<>();
        this.longPrefixes = new LinkedList<>();
    }

    void addShortPrefix(Word<I> prefix, List<D> contents) {
        shortPrefixes.add(addPrefix(prefix, contents));
    }

    void addLongPrefix(Word<I> prefix, List<D> contents) {
        longPrefixes.add(addPrefix(prefix, contents));
    }

    private RowImpl<I> addPrefix(Word<I> prefix, List<D> contents) {
        assert getSuffixes().size() == contents.size();

        final RowImpl<I> row = new RowImpl<>(prefix, rows.size());

        final int contentId = contentsToIdMap.computeIfAbsent(contents, k -> {
            rowContents.add(k);
            return contentsToIdMap.size();
        });

        row.setRowContentId(contentId);
        rows.add(row);

        return row;
    }

    @Override
    public Collection<Row<I>> getShortPrefixRows() {
        return Collections.unmodifiableList(shortPrefixes);
    }

    @Override
    public Collection<Row<I>> getLongPrefixRows() {
        return Collections.unmodifiableList(longPrefixes);
    }

    @Override
    public Row<I> getRow(int idx) {
        return rows.get(idx);
    }

    @Override
    public int numberOfDistinctRows() {
        return contentsToIdMap.size();
    }

    @Override
    public List<D> rowContents(Row<I> row) {
        return this.rowContents.get(this.getRow(row.getRowId()).getRowContentId());
    }

}
