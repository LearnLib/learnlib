package de.learnlib.algorithm.lsharp.ads;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.common.util.Pair;
import net.automatalib.common.util.Triple;
import net.automatalib.word.Word;

public class PartitionInfo<S, I, O> {
    public enum Type {
        SEP_INJ, SEP_NON_INJ, XFER_INJ, XFER_NON_INJ, USELESS;
    }

    public HashMap<O, HashMap<S, HashSet<S>>> splitMap = new HashMap<>();

    public PartitionInfo(MealyMachine<S, I, ?, O> fsm, I i, List<S> block) {
        block.stream().map(s -> Triple.of(s, fsm.getOutput(s, i), fsm.getSuccessor(s, i))).forEach(triple -> {
            this.splitMap.computeIfAbsent(triple.getSecond(), k -> new HashMap<>())
                    .computeIfAbsent(triple.getThird(), k -> new HashSet<>()).add(triple.getFirst());
        });
    }

    public boolean isInjective() {
        return this.splitMap.values().stream().flatMap(x -> x.values().stream()).allMatch(srcs -> srcs.size() < 2);
    }

    public boolean isSeparating() {
        return this.splitMap.keySet().size() > 1;
    }

    public Type iType() {
        boolean separating = isSeparating();
        boolean injective = isInjective();
        if (separating) {
            if (injective) {
                return Type.SEP_INJ;
            }
            return Type.SEP_NON_INJ;
        }

        if (!separating) {
            if (injective) {
                return Type.XFER_INJ;
            }
            return Type.XFER_NON_INJ;
        }

        return Type.USELESS;
    }

    public boolean mergesAllStates() {
        Set<S> states = new HashSet<>();
        for (HashMap<S, HashSet<S>> map : this.splitMap.values()) {
            for (S state : map.keySet()) {
                states.add(state);
            }
        }

        return states.size() == 1;
    }

    public boolean nonInjSepInput() {
        return this.iType().equals(Type.SEP_NON_INJ);
    }

    public List<S> allDests() {
        HashSet<S> allDests = new HashSet<>();
        for (HashMap<S, HashSet<S>> split : this.splitMap.values()) {
            for (S dest : split.keySet()) {
                allDests.add(dest);
            }
        }
        return new LinkedList<>(allDests);
    }

    public static <S, I, O> boolean inputWordIsSepInj(MealyMachine<S, I, ?, O> fsm, Word<I> input, List<S> rBlock) {
        HashSet<Pair<Word<O>, S>> nonInj = new HashSet<>();
        Boolean inj = true;
        HashSet<Word<O>> numOuts = new HashSet<>();

        for (S s : rBlock) {
            S d = fsm.getSuccessor(s, input);
            Word<O> o = fsm.computeStateOutput(s, input);
            inj = nonInj.add(Pair.of(o, d));
            numOuts.add(o);
        }

        return ((!numOuts.isEmpty()) && inj);

    }
}
