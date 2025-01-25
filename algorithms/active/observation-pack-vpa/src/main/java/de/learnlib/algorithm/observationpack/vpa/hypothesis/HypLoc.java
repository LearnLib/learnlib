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
package de.learnlib.algorithm.observationpack.vpa.hypothesis;

import de.learnlib.AccessSequenceProvider;
import net.automatalib.alphabet.VPAlphabet;
import net.automatalib.common.util.array.ArrayStorage;
import net.automatalib.word.Word;

/**
 * Hypothesis location.
 *
 * @param <I>
 *         input symbol type
 */
public class HypLoc<I> implements AccessSequenceProvider<I> {

    private final AbstractHypTrans<I> treeIncoming;
    private final Word<I> aseq;
    private final ArrayStorage<HypIntTrans<I>> intSuccessors;
    private final ArrayStorage<ArrayStorage<HypRetTrans<I>>> returnSuccessors;
    private final int index;
    private boolean accepting;
    private DTNode<I> leaf;

    public HypLoc(VPAlphabet<I> alphabet, int index, boolean accepting, AbstractHypTrans<I> treeIncoming) {
        this.index = index;
        this.accepting = accepting;
        this.intSuccessors = new ArrayStorage<>(alphabet.getNumInternals());
        this.returnSuccessors = new ArrayStorage<>(alphabet.getNumReturns());
        this.treeIncoming = treeIncoming;
        this.aseq = (treeIncoming != null) ? treeIncoming.getAccessSequence() : Word.epsilon();
    }

    public void updateStackAlphabetSize(int newStackAlphaSize) {
        for (int i = 0; i < returnSuccessors.size(); i++) {
            ArrayStorage<HypRetTrans<I>> transList = returnSuccessors.get(i);
            if (transList == null) {
                transList = new ArrayStorage<>(newStackAlphaSize);
                returnSuccessors.set(i, transList);
            } else if (transList.size() < newStackAlphaSize) {
                transList.ensureCapacity(newStackAlphaSize);
            }
        }
    }

    public DTNode<I> getLeaf() {
        return leaf;
    }

    public void setLeaf(DTNode<I> leaf) {
        this.leaf = leaf;
    }

    public boolean isRoot() {
        return treeIncoming == null;
    }

    @Override
    public Word<I> getAccessSequence() {
        return aseq;
    }

    public int getIndex() {
        return index;
    }

    public boolean isAccepting() {
        return accepting;
    }

    public void setAccepting(boolean accepting) {
        this.accepting = accepting;
    }

    public HypRetTrans<I> getReturnTransition(int retSymId, int stackSym) {
        final ArrayStorage<HypRetTrans<I>> succList = returnSuccessors.get(retSymId);
        return succList.get(stackSym);
    }

    public void setReturnTransition(int retSymId, int stackSym, HypRetTrans<I> trans) {
        ArrayStorage<HypRetTrans<I>> succList = returnSuccessors.get(retSymId);
        if (succList == null) {
            succList = new ArrayStorage<>(stackSym + 1);
            returnSuccessors.set(retSymId, succList);
        }
        succList.ensureCapacity(stackSym);
        succList.set(stackSym, trans);
    }

    public HypIntTrans<I> getInternalTransition(int intSymId) {
        return intSuccessors.get(intSymId);
    }

    public void setInternalTransition(int intSymId, HypIntTrans<I> succ) {
        intSuccessors.set(intSymId, succ);
    }

    @Override
    public String toString() {
        return Integer.toString(index);
    }
}
