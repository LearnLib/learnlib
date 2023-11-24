package de.learnlib.algorithm.lsharp;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

import de.learnlib.algorithm.lsharp.ads.ArenaTree;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.common.util.Pair;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;

public class NormalObservationTree<I, O> implements ObservationTree<LSState, I, O> {
    private ArenaTree<MapTransitions<I, O>, I> tree;
    private Alphabet<I> inputAlphabet;

    public NormalObservationTree(Alphabet<I> inputAlphabet) {
        MapTransitions<I, O> node = new MapTransitions<>(inputAlphabet.size());
        this.tree = new ArenaTree<>();
        this.tree.node(node);
        this.inputAlphabet = inputAlphabet;
    }

    public Alphabet<I> getInputAlphabet() {
        return this.inputAlphabet;
    }

    public LSState defaultState() {
        return new LSState(0);
    }

    private LSState addTransitionGetDestination(LSState src, I i, O o) {
        Integer srcRaw = src.raw();
        @Nullable
        Pair<O, LSState> pair = this.tree.get(srcRaw).getOutSucc(i);

        if (pair != null) {
            return pair.getSecond();
        } else {
            Integer destNodeIndex = this.tree.nodeWithParent(new MapTransitions<>(this.inputAlphabet.size()), srcRaw,
                    i);
            LSState destState = new LSState(destNodeIndex);
            this.tree.arena.get(srcRaw).value.addTrans(i, o, destState);
            return destState;
        }
    }

    @Override
    public LSState insertObservation(@Nullable LSState s, Word<I> input, Word<O> output) {
        LSState start = s == null ? defaultState() : s;
        LSState curr = start;

        int max = Math.min(input.length(), output.length());
        for (int i = 0; i < max; i++) {
            curr = this.addTransitionGetDestination(curr, input.getSymbol(i), output.getSymbol(i));
        }

        return curr;
    }

    @Override
    public Word<I> getAccessSeq(LSState state) {
        return this.getTransferSeq(state, defaultState());
    }

    @Override
    public Word<I> getTransferSeq(LSState toState, LSState fromState) {
        if (toState.compareTo(fromState) == 0) {
            return Word.epsilon();
        }

        WordBuilder<I> accessSeq = new WordBuilder<>();
        Integer destParentIndex = fromState.raw();
        Integer currState = toState.raw();

        while (true) {
            @Nullable
            Pair<I, Integer> pair = this.tree.arena.get(currState).parent;
            I i = pair.getFirst();
            Integer parentIndex = pair.getSecond();
            accessSeq.add(i);
            if (parentIndex == destParentIndex) {
                break;
            }
            currState = parentIndex;
        }

        accessSeq.reverse();
        return accessSeq.toWord();
    }

    @Override
    public @Nullable Word<O> getObservation(@Nullable LSState start, Word<I> input) {
        LSState s = start == null ? defaultState() : start;
        WordBuilder<O> outWord = new WordBuilder<>();
        for (I i : input) {
            @Nullable
            Pair<O, LSState> pair = this.getOutSucc(s, i);
            if (pair == null) {
                return null;
            }
            outWord.add(pair.getFirst());
            s = pair.getSecond();
        }

        return outWord.toWord();
    }

    @Override
    public @Nullable Pair<O, LSState> getOutSucc(LSState src, I input) {
        return this.tree.get(src.raw()).getOutSucc(input);
    }

    LSState _getSucc(LSState state, I input) {
        @Nullable
        Pair<O, LSState> pair = getOutSucc(state, input);
        return pair == null ? null : pair.getSecond();
    }

    @Override
    public @Nullable LSState getSucc(LSState s, Word<I> input) {
        LSState src = s;
        for (I i : input) {
            src = _getSucc(src, i);
            if (src == null) {
                return null;
            }
        }
        return src;
    }

    private class SuccComparator implements Comparator<Pair<LSState, I>> {
        @Override
        public int compare(Pair<LSState, I> p1, Pair<LSState, I> p2) {
            int len1 = getAccessSeq(p1.getFirst()).length();
            int len2 = getAccessSeq(p2.getFirst()).length();
            return Integer.compare(len1, len2);
        }

    }

    @Override
    public List<Pair<LSState, I>> noSuccDefined(List<LSState> basis, boolean sort) {
        LinkedList<Pair<LSState, I>> ret = new LinkedList<>();
        for (I input : inputAlphabet) {
            for (LSState state : basis) {
                if (this.getSucc(state, Word.fromLetter(input)) == null) {
                    ret.add(Pair.of(state, input));
                }
            }
        }

        if (sort) {
            Collections.sort(ret, new SuccComparator());
        }

        return ret;
    }

    @Override
    public Integer size() {
        return this.tree.size();
    }

    @Override
    public boolean treeAndHypStatesApartSink(LSState st, LSState sh, MealyMachine<LSState, I, ?, O> fsm, O sinkOutput,
            Integer depth) {
        return Apartness.treeAndHypStatesApartSunkBounded(this, st, sh, fsm, sinkOutput, depth);
    }

}
