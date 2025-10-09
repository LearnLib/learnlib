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
package de.learnlib.algorithm.sparse;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.learnlib.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.counterexample.LocalSuffixFinders;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.util.mealy.MealyUtil;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.MutableMealyMachine;
import net.automatalib.common.util.Pair;
import net.automatalib.word.Word;

class GenericSparseLearner<S, I, O> implements MealyLearner<I, O> {

    private final Alphabet<I> alphabet;
    private final MealyMembershipOracle<I, O> oracle;

    /**
     * Suffixes.
     */
    private final Deque<Word<I>> sufs;

    /**
     * Core rows.
     */
    private final List<CoreRow<S, I, O>> cRows;

    /**
     * Fringe rows.
     */
    private final Deque<FringeRow<S, I, O>> fRows;

    /**
     * Maps fringe prefixes to rows.
     */
    private final Map<Word<I>, FringeRow<S, I, O>> prefToFringe;

    /**
     * List of unique suffix-output cells.
     */
    private final List<Pair<Word<I>, Word<O>>> cells;

    /**
     * Maps each suffix-output cell to its list index.
     */
    private final Map<Pair<Word<I>, Word<O>>, Integer> cellToIdx;

    /**
     * Hypothesis.
     */
    private final MutableMealyMachine<S, I, ?, O> hyp;

    /**
     * Maps each state to its core row prefix.
     */
    private final Map<S, Word<I>> stateToPrefix;

    /**
     * Computes access sequences.
     */
    private final Function<Word<I>, Word<I>> accSeq;

    /**
     * For fast suffix ranking, this map stores the core row partitions created by each suffix.
     */
    private final Map<Word<I>, List<BitSet>> sufToVecs;

    /**
     * Helper map for efficiently constructing the suffix partition map (see {@link #sufToVecs}).
     */
    private final Map<Word<I>, Map<Word<O>, Integer>> sufToOutToIdx;

    protected GenericSparseLearner(Alphabet<I> alphabet,
                                   MealyMembershipOracle<I, O> oracle,
                                   List<Word<I>> initialSuffixes,
                                   MutableMealyMachine<S, I, ?, O> emptyMachine) {
        this.alphabet = alphabet;
        this.oracle = oracle;
        sufs = new ArrayDeque<>(initialSuffixes);
        cRows = new ArrayList<>();
        fRows = new ArrayDeque<>();
        prefToFringe = new HashMap<>();
        cells = new ArrayList<>();
        cellToIdx = new HashMap<>();
        hyp = emptyMachine;
        stateToPrefix = new HashMap<>();
        accSeq = p -> stateToPrefix.get(hyp.getState(p));
        sufToVecs = new HashMap<>();
        sufToOutToIdx = new HashMap<>();
    }

    @Override
    public MealyMachine<S, I, ?, O> getHypothesisModel() {
        return hyp;
    }

    @Override
    public void startLearning() {
        final S init = hyp.addInitialState();
        final CoreRow<S, I, O> c = new CoreRow<>(Word.epsilon(), init, 0);
        cRows.add(c);
        sufs.forEach(s -> addSuffixToCoreRow(c, s));
        stateToPrefix.put(init, c.prefix);
        extendFringe(c, init, new Leaf<>(c, 1, sufs.size(), Collections.emptyList()));
        fRows.forEach(f -> query(f, Word.epsilon())); // query transition outputs
        // initially, transition outputs must be queried manually,
        // for later transitions, they derive from suffix queries
        updateHypothesis();
    }

    @Override
    public boolean refineHypothesis(DefaultQuery<I, Word<O>> q) {
        final DefaultQuery<I, Word<O>> qs = MealyUtil.shortenCounterExample(hyp, q);
        if (qs == null) {
            return false;
        }

        final int oldSize = hyp.size();
        identifyNewState(qs);
        assert hyp.size() > oldSize;
        updateHypothesis();
        assert hyp.size() == cRows.size();
        refineHypothesis(q); // recursively exhaust counterexample
        return true;
    }

    private void updateHypothesis() {
        for (FringeRow<S, I, O> f : fRows) {
            classifyFringePrefix(f);
            if (f.leaf == null) {
                updateHypothesis();
                return;
            } else {
                assert f.transOut != null;
                hyp.setTransition(f.srcState, f.transIn, f.leaf.cRow.state, f.transOut);
            }
        }
    }

    private void classifyFringePrefix(FringeRow<S, I, O> f) {
        f.leaf.update(cRows, sufs.size());
        if (f.leaf.isUnsplit()) {
            return;
        }

        final Separator<S, I, O> sep = f.leaf.sep;
        if (sep != null) {
            followNode(f, sep);
        } else {
            f.leaf.sep = new Separator<>(pickSuffix(f.leaf.remRows), f.leaf.remRows, f.leaf.cellsIds);
            followNode(f, f.leaf.sep);
        }
    }

    private Word<I> pickSuffix(BitSet remRows) {
        assert remRows.length() <= cRows.size();
        Word<I> bestSuf = sufs.getFirst();
        int bestRank = Integer.MAX_VALUE;
        final BitSet vec = new BitSet();
        for (Word<I> s : sufs) {
            int maxOccur = 0;
            int sumOccur = 0; // checksum
            for (BitSet rows : sufToVecs.get(s)) {
                vec.or(remRows);
                vec.and(rows);
                final int occur = vec.cardinality();
                maxOccur = Math.max(maxOccur, occur);
                sumOccur += occur;
            }

            assert sumOccur == remRows.cardinality();
            if (maxOccur < bestRank) {
                // among equally ranked suffixes, pick youngest
                // (mind that suffixes are stored/iterated LIFO)
                bestSuf = s;
                bestRank = maxOccur;
                if (bestRank == 1) { // optimization: no better suffix is possible
                    return bestSuf;
                }
            }
        }

        assert bestRank < remRows.cardinality();
        return bestSuf;
    }

    private void followNode(FringeRow<S, I, O> f, Node<S, I, O> n) {
        if (n instanceof Leaf) {
            final Leaf<S, I, O> l = (Leaf<S, I, O>) n;
            assert l.isUnsplit();
            f.leaf = l;
            return;
        }

        final Separator<S, I, O> sep = (Separator<S, I, O>) n;
        final Word<O> out = query(f, sep.suffix);
        final int cellIdx = getUniqueCellIdx(sep.suffix, out);
        final Node<S, I, O> next = sep.branchMap.get(out);
        if (next != null) {
            followNode(f, next);
            return;
        }

        final BitSet remRows = new BitSet();
        sep.remRows.stream().filter(i -> cRows.get(i).cellIds.contains(cellIdx)).forEach(remRows::set);
        final List<Integer> cellIds = new ArrayList<>(sep.cellsIds); // important: copy elements!
        cellIds.add(cellIdx);
        if (remRows.isEmpty()) {
            // no compatible core prefix
            f.leaf = null;
            moveToCore(f, cellIds);
        } else if (remRows.cardinality() == 1) {
            final Leaf<S, I, O> l = new Leaf<>(cRows.get(remRows.nextSetBit(0)), cRows.size(), sufs.size(), cellIds);
            sep.branchMap.put(out, l);
            followNode(f, l);
        } else {
            final Separator<S, I, O> s = new Separator<>(pickSuffix(remRows), remRows, cellIds);
            sep.branchMap.put(out, s);
            followNode(f, s);
        }
    }

    private Word<O> query(Row<S, I, O> r, Word<I> suf) {
        final Word<O> out = oracle.answerQuery(r.prefix.concat(suf));
        if (r instanceof FringeRow) {
            final FringeRow<S, I, O> f = (FringeRow<S, I, O>) r;
            f.transOut = out.prefix(f.prefix.length()).lastSymbol();
        }

        return out.suffix(suf.length());
    }

    /**
     * Adds suffix-output pair to index if not yet contained,
     * and returns a unique identifier representing the pair.
     */
    private int getUniqueCellIdx(Word<I> suf, Word<O> out) {
        assert suf.length() == out.length();
        final Pair<Word<I>, Word<O>> cell = Pair.of(suf, out);
        final int idx = cellToIdx.computeIfAbsent(cell, c -> cellToIdx.size());
        if (idx == cells.size()) {
            cells.add(cell);
        }

        assert cellToIdx.size() == cells.size();
        return idx;
    }

    /**
     * Returns index of new core row.
     */
    private int moveToCore(FringeRow<S, I, O> f, List<Integer> cellIds) {
        final boolean removed = fRows.remove(f);
        assert removed;
        final S state = hyp.addState();
        final CoreRow<S, I, O> c = new CoreRow<>(f.prefix, state, cRows.size());
        stateToPrefix.put(state, c.prefix);
        assert f.transOut != null;
        hyp.setTransition(f.srcState, f.transIn, state, f.transOut);
        for (Integer cellIdx : completeRowObservations(f, cellIds)) {
            final Pair<Word<I>, Word<O>> cell = this.cells.get(cellIdx);
            addCellToCoreRow(c, cell.getFirst(), cell.getSecond(), cellIdx);
        }

        cRows.add(c);
        extendFringe(c, state, new Leaf<>());
        assert c.cellIds.size() == sufs.size();
        assert c == cRows.get(c.idx);
        return c.idx;
    }

    /**
     * Takes fringe row and its observations, queries the missing entries,
     * and returns a list containing the observations for all suffixes.
     */
    private List<Integer> completeRowObservations(FringeRow<S, I, O> f, List<Integer> cellIds) {
        final List<Word<I>> sufsPresent = cellIds.stream().map(c -> this.cells.get(c).getFirst()).collect(Collectors.toList());
        final List<Word<I>> sufsMissing = sufs.stream().filter(s -> !sufsPresent.contains(s)).collect(Collectors.toList());
        final List<Integer> cellIdsFull = new ArrayList<>(cellIds); // important: copy elements!
        sufsMissing.forEach(s -> cellIdsFull.add(getUniqueCellIdx(s, query(f, s))));
        return cellIdsFull;
    }

    private void extendFringe(CoreRow<S, I, O> c, S state, Leaf<S, I, O> leaf) {
        for (I i : alphabet) {
            // add missing fringe rows for new transitions
            final Word<I> prefix = c.prefix.append(i);
            final FringeRow<S, I, O> fRow = new FringeRow<>(prefix, state, leaf);
            prefToFringe.put(prefix, fRow);
            fRows.push(fRow); // prioritize new rows during classification
        }
    }

    private void identifyNewState(DefaultQuery<I, Word<O>> q) {
        final Word<I> cex = q.getInput();
        final int idxSuf = LocalSuffixFinders.findRivestSchapire(q, accSeq::apply, hyp, oracle);
        final int idxSym = idxSuf - 1;
        final Word<I> u = accSeq.apply(cex.prefix(idxSym));
        final Word<I> ui = u.append(cex.getSymbol(idxSym));
        final FringeRow<S, I, O> f = prefToFringe.get(ui);
        assert f.leaf.isUnsplit();
        final int cRowIdx = moveToCore(f, f.leaf.cellsIds);
        if (f.leaf.cRow.cellIds.containsAll(cRows.get(cRowIdx).cellIds)) {
            // only add new suffix if the row is not yet distinguished
            addSuffixToTable(cex.subWord(idxSuf));
        }
    }

    private void addSuffixToTable(Word<I> suf) {
        assert !sufs.contains(suf);
        sufs.push(suf);

        // since core rows are prefix-closed,
        // cache hit rate is maximized by LIFO iteration
        for (int i = cRows.size() - 1; i >= 0; i--) {
            addSuffixToCoreRow(cRows.get(i), suf);
        }
    }

    private void addSuffixToCoreRow(CoreRow<S, I, O> c, Word<I> suf) {
        final Word<O> out = query(c, suf);
        final int cellIdx = getUniqueCellIdx(suf, out);
        addCellToCoreRow(c, suf, out, cellIdx);
    }

    private void addCellToCoreRow(CoreRow<S, I, O> c, Word<I> suf, Word<O> out, Integer cellIdx) {
        c.addSuffix(suf, out, cellIdx);
        updatePartitionMap(c, suf, out);
    }

    private void updatePartitionMap(CoreRow<S, I, O> c, Word<I> suf, Word<O> out) {
        final Map<Word<O>, Integer> outToIdx = sufToOutToIdx.computeIfAbsent(suf, s -> new HashMap<>());
        final int idx = outToIdx.computeIfAbsent(out, o -> outToIdx.size());
        final List<BitSet> vecs = sufToVecs.computeIfAbsent(suf, s -> new ArrayList<>());
        assert idx <= vecs.size();
        if (idx == vecs.size()) {
            vecs.add(new BitSet());
        }

        vecs.get(idx).set(c.idx);
    }
}
