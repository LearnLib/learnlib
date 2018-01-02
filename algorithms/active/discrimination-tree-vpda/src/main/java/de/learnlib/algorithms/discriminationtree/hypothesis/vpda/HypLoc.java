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
package de.learnlib.algorithms.discriminationtree.hypothesis.vpda;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import de.learnlib.api.AccessSequenceProvider;
import net.automatalib.commons.util.array.RichArray;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.Word;

/**
 * @param <I>
 *         input symbol type
 *
 * @author Malte Isberner
 */
public class HypLoc<I> implements AccessSequenceProvider<I> {

    private final AbstractHypTrans<I> treeIncoming;
    private final Word<I> aseq;
    private final RichArray<HypIntTrans<I>> intSuccessors;
    private final RichArray<List<HypRetTrans<I>>> returnSuccessors;
    final int index;
    private boolean accepting;
    private DTNode<I> leaf;

    public HypLoc(VPDAlphabet<I> alphabet, int index, boolean accepting, AbstractHypTrans<I> treeIncoming) {
        this.index = index;
        this.accepting = accepting;
        this.intSuccessors = new RichArray<>(alphabet.getNumInternals());
        this.returnSuccessors = new RichArray<>(alphabet.getNumReturns(),
                                                (Supplier<List<HypRetTrans<I>>>) ArrayList::new);
        this.treeIncoming = treeIncoming;
        this.aseq = (treeIncoming != null) ? treeIncoming.getAccessSequence() : Word.epsilon();
    }

    public HypLoc(VPDAlphabet<I> alphabet, int index, boolean accepting, Word<I> aseq) {
        this.index = index;
        this.accepting = accepting;
        this.intSuccessors = new RichArray<>(alphabet.getNumInternals());
        this.returnSuccessors = new RichArray<>(alphabet.getNumReturns(),
                                                (Supplier<List<HypRetTrans<I>>>) ArrayList::new);
        this.treeIncoming = null;
        this.aseq = aseq;
    }

    public void updateStackAlphabetSize(int newStackAlphaSize) {
        for (int i = 0; i < returnSuccessors.length; i++) {
            List<HypRetTrans<I>> transList = returnSuccessors.get(i);
            if (transList == null) {
                transList = new ArrayList<>(Collections.nCopies(newStackAlphaSize, null));
                returnSuccessors.set(i, transList);
            } else if (transList.size() < newStackAlphaSize) {
                transList.addAll(Collections.nCopies(newStackAlphaSize - transList.size(), null));
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
        List<HypRetTrans<I>> succList = returnSuccessors.get(retSymId);
        if (succList != null && stackSym < succList.size()) {
            return succList.get(stackSym);
        }
        return null;
    }

    public void setReturnTransition(int retSymId, int stackSym, HypRetTrans<I> trans) {
        List<HypRetTrans<I>> succList = returnSuccessors.get(retSymId);
        if (succList == null) {
            succList = new ArrayList<>(stackSym + 1);
            returnSuccessors.set(retSymId, succList);
        }
        int numSuccs = succList.size();
        if (numSuccs <= stackSym) {
            succList.addAll(Collections.nCopies(stackSym + 1 - numSuccs, null));
        }
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
