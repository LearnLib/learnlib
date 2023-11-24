package de.learnlib.algorithm.lsharp.ads;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;

public class Scoring {

    public static <S, I, O> Integer scoreSep(SplittingNode<S, I, O> r, I x, MealyMachine<S, I, ?, O> fsm) {
        return score(r, x, null, fsm);
    }

    public static <S, I, O> Integer scoreXfer(SplittingNode<S, I, O> r, I x, SplittingNode<S, I, O> rx,
            MealyMachine<S, I, ?, O> fsm) {
        return score(r, x, rx, fsm);
    }

    private static <S, I, O> Integer score(SplittingNode<S, I, O> r, I x, SplittingNode<S, I, O> rx,
            MealyMachine<S, I, ?, O> fsm) {
        Word<I> w = Word.fromLetter(x);
        if (rx != null) {
            List<I> seq = rx.sepSeq.seq;
            assert !seq.isEmpty();
            w.concat(Word.fromList(seq));
        }

        HashMap<Word<O>, HashMap<S, HashSet<S>>> outputdestMap = new HashMap<Word<O>, HashMap<S, HashSet<S>>>();
        for (S s : r.label) {
            Word<O> o = fsm.computeStateOutput(s, w);
            S d = fsm.getSuccessor(s, w);
            outputdestMap.computeIfAbsent(o, k -> new HashMap<>()).computeIfAbsent(d, k -> new HashSet<>()).add(s);
        }

        if (outputdestMap.size() == 1) {
            return Integer.MAX_VALUE;
        }

        LinkedList<HashSet<S>> sets = new LinkedList<>();
        for (HashMap<S, HashSet<S>> value : outputdestMap.values()) {
            for (HashSet<S> set : value.values()) {
                sets.add(set);
            }
        }

        boolean destOverlap = !sets.stream().allMatch(s -> s.size() == 1);
        boolean isValidSepSeq = !destOverlap && outputdestMap.size() > 1;

        if (isValidSepSeq) {
            return w.length();
        }

        Integer count = 0;
        Integer statesInNonInjSuccs = 0;
        Integer numValidSuccs = 0;
        Integer numSuccs = 0;
        Integer numUndistStates = 0;
        Integer nr = r.label.size();
        Integer e = w.length();

        for (HashMap<S, HashSet<S>> destSrcMap : outputdestMap.values()) {
            Set<S> srcStates = destSrcMap.values().stream().flatMap(a -> a.stream()).collect(Collectors.toSet());
            Set<S> dstStates = destSrcMap.keySet();

            if (srcStates.size() == dstStates.size()) {
                numValidSuccs += 1;
            } else {
                statesInNonInjSuccs += srcStates.size();
                numUndistStates += srcStates.size() - dstStates.size();
            }
            numSuccs += 1;
        }

        count += statesInNonInjSuccs * nr;
        count -= numValidSuccs;
        count *= nr;
        count -= numSuccs;
        count *= nr;
        count += numUndistStates;
        count *= nr;
        return count + e;

    }
}
