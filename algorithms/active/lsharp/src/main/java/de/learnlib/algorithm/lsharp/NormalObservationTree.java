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
package de.learnlib.algorithm.lsharp;

import de.learnlib.algorithm.lsharp.ads.ArenaTree;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.common.util.Pair;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

public class NormalObservationTree<I, O> implements ObservationTree<Integer, I, O> {

    private final ArenaTree<MapTransitions<I, O>, I> tree;
    private final Alphabet<I> inputAlphabet;

    public NormalObservationTree(Alphabet<I> inputAlphabet) {
        MapTransitions<I, O> node = new MapTransitions<>(inputAlphabet.size());
        this.tree = new ArenaTree<>();
        this.tree.node(node);
        this.inputAlphabet = inputAlphabet;
    }

    @Override
    public Alphabet<I> getInputAlphabet() {
        return this.inputAlphabet;
    }

    @Override
    public Integer defaultState() {
        return 0;
    }

    private int addTransitionGetDestination(int src, I i, O o) {
        Pair<O, Integer> pair = this.tree.get(src).getOutSucc(i);

        if (pair != null) {
            return pair.getSecond();
        } else {
            int destState = this.tree.nodeWithParent(new MapTransitions<>(this.inputAlphabet.size()), src, i);
            this.tree.arena.get(src).value.addTrans(i, o, destState);
            return destState;
        }
    }

    @Override
    public Integer insertObservation(@Nullable Integer s, Word<I> input, Word<O> output) {
        int curr = s == null ? defaultState() : s;

        int max = Math.min(input.length(), output.length());
        for (int i = 0; i < max; i++) {
            curr = this.addTransitionGetDestination(curr, input.getSymbol(i), output.getSymbol(i));
        }

        return curr;
    }

    @Override
    public Word<I> getAccessSeq(Integer state) {
        return this.getTransferSeq(state, defaultState());
    }

    @Override
    public Word<I> getTransferSeq(Integer toState, Integer fromState) {
        if (toState.compareTo(fromState) == 0) {
            return Word.epsilon();
        }

        WordBuilder<I> accessSeq = new WordBuilder<>();
        int currState = toState;

        while (true) {
            Pair<I, Integer> pair = this.tree.arena.get(currState).parent;
            assert pair != null;
            I i = pair.getFirst();
            Integer parentIndex = pair.getSecond();
            accessSeq.add(i);
            if (parentIndex.equals(fromState)) {
                break;
            }
            currState = parentIndex;
        }

        accessSeq.reverse();
        return accessSeq.toWord();
    }

    @Override
    public @Nullable Word<O> getObservation(@Nullable Integer start, Word<I> input) {
        Integer s = start == null ? defaultState() : start;
        WordBuilder<O> outWord = new WordBuilder<>();
        for (I i : input) {
            Pair<O, Integer> pair = this.getOutSucc(s, i);
            if (pair == null) {
                return null;
            }
            outWord.add(pair.getFirst());
            s = pair.getSecond();
        }

        return outWord.toWord();
    }

    @Override
    public @Nullable Pair<O, Integer> getOutSucc(Integer src, I input) {
        return this.tree.get(src).getOutSucc(input);
    }

    private @Nullable Integer getSucc(Integer state, I input) {
        Pair<O, Integer> pair = getOutSucc(state, input);
        return pair == null ? null : pair.getSecond();
    }

    @Override
    public @Nullable Integer getSucc(Integer s, Word<I> input) {
        Integer src = s;
        for (I i : input) {
            src = getSucc(src, i);
            if (src == null) {
                return null;
            }
        }
        return src;
    }
}
