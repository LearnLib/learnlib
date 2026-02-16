/* Copyright (C) 2013-2026 TU Dortmund University
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
package de.learnlib.algorithm.lambda.lstar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.learnlib.AccessSequenceTransformer;
import de.learnlib.datastructure.observationtable.ObservationTable;
import de.learnlib.datastructure.observationtable.Row;
import de.learnlib.datastructure.observationtable.RowImpl;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.common.util.HashUtil;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.NonNull;

class OTView<I, D> implements ObservationTable<I, D> {

    private final Alphabet<I> alphabet;
    private final List<List<D>> rowContents;
    private final List<Row<I>> shortPrefixes;
    private final List<Row<I>> longPrefixes;
    private final List<Word<I>> suffixes;
    private final AccessSequenceTransformer<I> asTransformer;

    OTView(Alphabet<I> alphabet,
           Set<Word<I>> shortPrefixes,
           Map<Word<I>, List<D>> rows,
           List<Word<I>> suffixes,
           AccessSequenceTransformer<I> asTransformer) {
        final int numInputs = alphabet.size();
        final int numRows = rows.size();
        final int numSP = shortPrefixes.size();
        final int numLP = numRows - numSP;

        List<List<D>> contents = new ArrayList<>(numRows);
        List<RowImpl<I>> shortPrefs = new ArrayList<>(numSP);
        List<RowImpl<I>> longPrefs = new ArrayList<>(numLP);

        final Map<Word<I>, RowImpl<I>> wordRowMap = new HashMap<>(HashUtil.capacity(numRows));
        int idx = 0;
        for (Entry<Word<I>, List<D>> e : rows.entrySet()) {
            final Word<I> label = e.getKey();
            final RowImpl<I> r = new RowImpl<>(label, idx++);

            contents.add(Collections.unmodifiableList(e.getValue()));
            wordRowMap.put(label, r);

            if (shortPrefixes.contains(label)) {
                shortPrefs.add(r);
                r.makeShort(numInputs);
            } else {
                longPrefs.add(r);
            }
        }

        for (RowImpl<I> sp : shortPrefs) {
            final Word<I> label = sp.getLabel();
            for (int i = 0; i < numInputs; i++) {
                @SuppressWarnings("nullness") // short prefixes + 1-letter extensions = long prefixes
                final @NonNull RowImpl<I> row = wordRowMap.get(label.append(alphabet.getSymbol(i)));
                sp.setSuccessor(i, row);
            }
        }

        this.alphabet = alphabet;
        this.rowContents = Collections.unmodifiableList(contents);
        this.shortPrefixes = Collections.unmodifiableList(shortPrefs);
        this.longPrefixes = Collections.unmodifiableList(longPrefs);
        this.suffixes = Collections.unmodifiableList(suffixes);
        this.asTransformer = asTransformer;
    }

    @Override
    public Alphabet<I> getInputAlphabet() {
        return alphabet;
    }

    @Override
    public Collection<Row<I>> getShortPrefixRows() {
        return shortPrefixes;
    }

    @Override
    public Collection<Row<I>> getLongPrefixRows() {
        return longPrefixes;
    }

    @Override
    public Row<I> getRow(int idx) {
        final int numSP = shortPrefixes.size();
        if (idx < numSP) {
            return shortPrefixes.get(idx);
        } else {
            return longPrefixes.get(idx - numSP);
        }
    }

    @Override
    public int numberOfDistinctRows() {
        return shortPrefixes.size() + longPrefixes.size();
    }

    @Override
    public List<Word<I>> getSuffixes() {
        return this.suffixes;
    }

    @Override
    public List<D> rowContents(Row<I> row) {
        return rowContents.get(row.getRowId());
    }

    @Override
    public Word<I> transformAccessSequence(Word<I> word) {
        return this.asTransformer.transformAccessSequence(word);
    }
}
