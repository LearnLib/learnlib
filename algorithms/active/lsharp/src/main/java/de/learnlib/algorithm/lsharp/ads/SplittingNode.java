package de.learnlib.algorithm.lsharp.ads;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import de.learnlib.algorithm.lsharp.ads.SepSeq.Status;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.common.util.Pair;

public class SplittingNode<S, I, O> {
    public List<S> label;
    public HashMap<O, Integer> children = new HashMap<>();
    public HashMap<I, List<S>> successors = new HashMap<>();
    public SepSeq<I> sepSeq = new SepSeq<>(null, new LinkedList<>());
    public HashMap<I, PartitionInfo.Type> splitMap = new HashMap<>();

    public SplittingNode(List<S> block) {
        this.label = new LinkedList<>(new HashSet<>(block));
    }

    public List<I> inputsOfType(PartitionInfo.Type type) {
        return this.splitMap.entrySet().stream().filter(e -> e.getValue().equals(type)).map(e -> e.getKey())
                .collect(Collectors.toList());
    }

    public boolean hasState(S state) {
        return this.label.contains(state);
    }

    public boolean isSeparated() {
        return !this.children.isEmpty();
    }

    public Integer size() {
        return this.label.size();
    }

    public void analyse(MealyMachine<S, I, ?, O> mealy, Alphabet<I> inputAlphabet) {
        List<S> rBlock = this.label;
        List<Pair<I, PartitionInfo<S, I, O>>> infos = inputAlphabet.parallelStream()
                .map(i -> Pair.of(i, new PartitionInfo<>(mealy, i, rBlock)))
                .filter(p -> p.getSecond().iType() != PartitionInfo.Type.USELESS).collect(Collectors.toList());

        Optional<I> injSepInput = infos.stream().filter(p -> p.getSecond().iType().equals(PartitionInfo.Type.SEP_INJ))
                .map(p -> p.getFirst()).findAny();

        if (injSepInput.isPresent()) {
            List<I> sepSeq = new LinkedList<>();
            sepSeq.add(injSepInput.get());
            this.sepSeq = new SepSeq<I>(Status.INJ, sepSeq);
        }

        for (Pair<I, PartitionInfo<S, I, O>> pair : infos) {
            this.successors.put(pair.getFirst(), pair.getSecond().allDests());
            this.splitMap.put(pair.getFirst(), pair.getSecond().iType());
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof SplittingNode<?, ?, ?>) {
            SplittingNode<?, ?, ?> casted = (SplittingNode<?, ?, ?>) other;
            boolean labelsEq = this.label.equals(casted.label);
            if (!labelsEq) {
                return false;
            }

            boolean seqEq = this.sepSeq.equals(casted.sepSeq);
            if (!seqEq) {
                return false;
            }

            Set<O> selfOuts = this.children.keySet();
            Set<?> otherOuts = casted.children.keySet();
            boolean outsEq = selfOuts.equals(otherOuts);

            if (!outsEq) {
                return false;
            }

            return true;
        }

        return false;
    }
}
