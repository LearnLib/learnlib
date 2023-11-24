package de.learnlib.algorithm.lsharp.ads;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.learnlib.algorithm.lsharp.ads.PartitionInfo.Type;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.common.util.Pair;
import net.automatalib.common.util.Triple;
import net.automatalib.word.Word;

public class SplittingTree<S extends Comparable<S>, I, O> {
    public ArenaTree<SplittingNode<S, I, O>, Void> tree = new ArenaTree<>();
    public SeparatingNodes<S, Integer> sepLCA = new SeparatingNodes<>();
    public HashSet<Integer> analysed = new HashSet<>();

    public SplittingTree(MealyMachine<S, I, ?, O> fsm, Alphabet<I> inputAlphabet, List<S> rootLabel) {
        Helpers<I> helpers = new Helpers<>();
        this.construct(fsm, inputAlphabet, rootLabel, helpers);
        this.analysed.addAll(helpers.analysedIndices);
    }

    public void construct(MealyMachine<S, I, ?, O> fsm, Alphabet<I> inputAlphabet, List<S> initialLabel,
            Helpers<I> helpers) {
        helpers.analysedIndices.addAll(this.analysed);
        SplittingNode<S, I, O> rootNode = new SplittingNode<>(initialLabel);
        Integer rootIndex = this.tree.node(rootNode);
        helpers.nodesInTree.add(rootIndex);
        Integer originalBlockSize = initialLabel.size();
        helpers.partition.add(Pair.of(rootIndex, originalBlockSize));

        while (!helpers.partition.isEmpty()) {
            Pair<Integer, Integer> el = helpers.partition.poll();
            Integer rIndex = el.getFirst();
            if (this.get(rIndex).size() == 1) {
                continue;
            }

            if (!helpers.analysedIndices.contains(rIndex)) {
                this.analyse(rIndex, fsm, inputAlphabet, helpers.analysedIndices);
            }

            if (this.get(rIndex).sepSeq.isSet()) {
                this.separate(rIndex, fsm, helpers);
            } else {
                helpers.dependent.add(rIndex);
            }

            Integer maximal = 0;
            if (!helpers.partition.isEmpty()) {
                maximal = helpers.partition.peek().getSecond();
            }

            Integer currBlockSize = this.get(rIndex).size();
            if (!helpers.dependent.isEmpty() && maximal <= currBlockSize) {
                HashSet<Integer> nodesSeen = new HashSet<>();
                boolean stable = false;
                while (!stable) {
                    stable = true;
                    for (Integer r : helpers.dependent) {
                        if (!nodesSeen.add(r)) {
                            continue;
                        }
                        stable &= this.initTransOnInjInputs(fsm, inputAlphabet, r, helpers);
                    }
                    if (!stable) {
                        nodesSeen.clear();
                    }
                }

                this.processDependent(fsm, helpers);
                if (!helpers.dependent.isEmpty()) {
                    HashSet<Integer> nodesSeen2 = new HashSet<>();
                    boolean stable2 = false;
                    while (!stable2) {
                        stable2 = true;
                        for (Integer r : helpers.dependent) {
                            if (!nodesSeen2.add(r)) {
                                continue;
                            }
                            stable2 &= this.initTransOnInjInputs(fsm, inputAlphabet, r, helpers);
                            if (!get(r).sepSeq.isInjective()) {
                                stable2 &= this.initTransOnNonInjInputs(fsm, inputAlphabet, r, helpers);
                            }
                        }
                        if (!stable2) {
                            nodesSeen2.clear();
                        }
                    }
                    this.processDependent(fsm, helpers);
                }
            }

            if (get(rIndex).isSeparated()) {
                helpers.partition.add(Pair.of(rIndex, get(rIndex).size()));
            }
        }

        LinkedList<S> block = new LinkedList<>(initialLabel);
        LinkedList<Triple<S, S, Integer>> separatingNodes = new LinkedList<>();

        for (S x : new LinkedList<>(block)) {
            for (S y : new LinkedList<>(block)) {
                if (x.compareTo(y) < 0) {
                    Integer lca = this.LCAOfTwo(x, y);
                    Objects.requireNonNull(lca);
                    separatingNodes.add(Triple.of(x, y, lca));
                }
            }
        }

        for (Triple<S, S, Integer> triple : separatingNodes) {
            if (this.sepLCA.checkPair(triple.getFirst(), triple.getSecond()) == null) {
                this.sepLCA.insert(triple.getFirst(), triple.getSecond(), triple.getThird());
            }
        }
    }

    public Word<I> separatingSequence(List<S> block) {
        Integer lca = getLCA(block);
        return lca != null ? Word.fromList(tree.get(lca).sepSeq.seq) : null;
    }

    public Integer getLCA(List<S> blockIn) {
        LinkedList<S> block = new LinkedList<>(blockIn);
        S pivot = block.getFirst();
        List<S> rem = block.subList(1, block.size());

        return rem.stream().map(x -> this.sepLCA.checkPair(x, pivot)).filter(x -> x != null).max((x, y) -> {
            Integer xSize = tree.apply(x, SplittingNode::size);
            Integer ySize = tree.apply(y, SplittingNode::size);
            return xSize.compareTo(ySize);
        }).orElse(null);
    }

    public void scoreAndUpdate(Integer r, I input, Integer rx, MealyMachine<S, I, ?, O> fsm, BestNode<I> bestR) {
        Integer score = Scoring.scoreXfer(get(r), input, get(rx), fsm);
        if (score < bestR.score) {
            bestR.update(input, rx, score);
        }
    }

    public boolean initTransOnInjInputs(MealyMachine<S, I, ?, O> fsm, Alphabet<I> inputAlphabet, Integer r,
            Helpers<I> helpers) {
        boolean stable = true;
        BestNode<I> bestR = helpers.bestNode.computeIfAbsent(r, k -> new BestNode<>());
        List<I> injectiveXferInputs = get(r).inputsOfType(Type.XFER_INJ);

        for (I input : injectiveXferInputs) {
            List<S> destBlock = get(r).successors.get(input);
            Objects.requireNonNull(destBlock);
            Integer rx = maybeLCA(destBlock, helpers.nodesInTree);
            if (rx != null) {
                if (helpers.dependent.contains(rx)) {
                    helpers.addTransitionFromToVia(r, rx, input);
                } else {
                    scoreAndUpdate(r, input, rx, fsm, bestR);
                }
            } else {
                SplittingNode<S, I, O> newNode = new SplittingNode<>(destBlock);
                Integer rx2 = this.findNodeExact(destBlock);
                if (rx2 == null) {
                    rx2 = this.tree.node(newNode);
                }

                if (helpers.analysedIndices.contains(rx2) && get(rx2).sepSeq.isInjective()) {
                    scoreAndUpdate(r, input, rx2, fsm, bestR);
                }

                if (!helpers.analysedIndices.contains(rx2) || !get(rx2).sepSeq.isSet()) {
                    if (!helpers.analysedIndices.contains(rx2)) {
                        this.analyse(rx, fsm, inputAlphabet, helpers.analysedIndices);
                    }
                    if (get(rx2).sepSeq.isSet()) {
                        scoreAndUpdate(r, input, rx2, fsm, bestR);
                    } else {
                        stable = !helpers.dependent.add(rx2);
                    }
                    helpers.addTransitionFromToVia(r, rx2, input);
                }
            }
        }
        helpers.bestNode.put(r, bestR);

        bestR = helpers.bestNode.get(r);
        Objects.requireNonNull(bestR);

        boolean nextHasInjSeq = false;
        if (bestR.next != null) {
            nextHasInjSeq = get(bestR.next).sepSeq.isInjective();
        }

        if (nextHasInjSeq) {
            helpers.dependentPrioQueue.add(Pair.of(r, bestR.score));
        }

        return stable;
    }

    public void processDependent(MealyMachine<S, I, ?, O> fsm, Helpers<I> helpers) {
        while (!helpers.dependentPrioQueue.isEmpty()) {
            Integer r = helpers.dependentPrioQueue.poll().getFirst();
            if (get(r).isSeparated()) {
                continue;
            }

            BestNode<I> bestR = helpers.bestNode.get(r);
            Objects.requireNonNull(bestR);

            I xferInput = bestR.input;
            Objects.requireNonNull(xferInput);

            List<I> seq = null;
            if (bestR.next != null) {
                seq = get(bestR.next).sepSeq.seq;
            }

            LinkedList<I> sepSeqBase = new LinkedList<>();
            sepSeqBase.add(xferInput);
            sepSeqBase.addAll(seq);

            boolean IWIsSepInj = PartitionInfo.inputWordIsSepInj(fsm, Word.fromList(sepSeqBase), get(r).label);
            SepSeq<I> sepSeq = new SepSeq<I>(IWIsSepInj ? SepSeq.Status.INJ : SepSeq.Status.NONINJ, sepSeqBase);
            this.tree.arena.get(r).value.sepSeq = sepSeq;
            this.separate(r, fsm, helpers);

            HashSet<Pair<Integer, I>> transToR = helpers.transitionsTo.computeIfAbsent(r, k -> null);
            if (transToR != null) {
                for (Pair<Integer, I> pair : transToR) {
                    if (get(pair.getFirst()).sepSeq.isSet()) {
                        continue;
                    }

                    Integer scoreP = Scoring.scoreXfer(get(pair.getFirst()), pair.getSecond(), get(r), fsm);
                    Integer bestPScore = helpers.scoreOf(pair.getFirst());
                    if (scoreP < bestPScore) {
                        BestNode<I> bestP = new BestNode<>(pair.getSecond(), r, scoreP);
                        helpers.bestNode.put(pair.getFirst(), bestP);
                        helpers.dependentPrioQueue.add(Pair.of(pair.getFirst(), scoreP));
                    }
                }
            }
            helpers.dependent.remove(r);
        }
    }

    private boolean inputNonInjSeparating(MealyMachine<S, I, ?, O> fsm, I i, List<S> label) {
        return (new PartitionInfo<>(fsm, i, label)).nonInjSepInput();
    }

    public boolean initTransOnNonInjInputs(MealyMachine<S, I, ?, O> fsm, Alphabet<I> inpuAlphabet, Integer r,
            Helpers<I> helpers) {
        boolean stable = true;
        BestNode<I> bestR = helpers.bestNode.computeIfAbsent(r, k -> new BestNode<>());
        Pair<I, Integer> bestNonInjSepInput = inpuAlphabet.stream()
                .filter(x -> inputNonInjSeparating(fsm, x, get(r).label))
                .map(x -> Pair.of(x, Scoring.scoreSep(get(r), x, fsm))).filter(p -> p.getSecond() < bestR.score)
                .min((p1, p2) -> p1.getSecond().compareTo(p2.getSecond())).orElse(null);

        if (bestNonInjSepInput != null) {
            bestR.update(bestNonInjSepInput.getFirst(), null, bestNonInjSepInput.getSecond());
        }

        List<I> invalidXferInputs = get(r).inputsOfType(Type.XFER_NON_INJ);
        for (I input : invalidXferInputs) {
            List<S> succ = get(r).successors.get(input);
            Objects.requireNonNull(succ);

            Integer maybeRX = maybeLCA(succ, helpers.nodesInTree);
            Integer nextR;
            if (maybeRX != null) {
                Integer rx = maybeRX;
                nextR = rx;
                if (get(rx).sepSeq.isSet()) {
                    Integer newScore = Scoring.scoreXfer(get(r), input, get(rx), fsm);
                    if (newScore < bestR.score) {
                        bestR.update(input, rx, newScore);
                    }
                }
            } else {
                SplittingNode<S, I, O> newNode = new SplittingNode<>(succ);
                Integer rx = findNodeExact(succ);
                if (rx == null) {
                    rx = tree.node(newNode);
                }
                nextR = rx;
                if (!helpers.analysedIndices.contains(rx) || !get(rx).sepSeq.isSet()) {
                    if (!helpers.analysedIndices.contains(rx)) {
                        analyse(rx, fsm, inpuAlphabet, helpers.analysedIndices);
                    }
                    if (get(rx).sepSeq.isSet()) {
                        Integer score = Scoring.scoreXfer(get(r), input, get(rx), fsm);
                        if (score < bestR.score) {
                            bestR.update(input, rx, score);
                        }
                    }
                    helpers.transitionsTo.computeIfAbsent(rx, k -> new HashSet<>()).add(Pair.of(r, input));
                }
                if (!get(rx).sepSeq.isSet()) {
                    stable = !helpers.dependent.add(rx);
                }
            }
            if (get(nextR).sepSeq.isSet()) {
                Integer score = Scoring.scoreXfer(get(r), input, get(nextR), fsm);
                if (score < bestR.score) {
                    bestR.update(input, nextR, score);
                }
            }
        }
        Integer rPriority = helpers.scoreOf(r);
        if (rPriority != Integer.MAX_VALUE) {
            helpers.dependentPrioQueue.add(Pair.of(r, rPriority));
        }
        return stable;
    }

    public Integer maybeLCA(List<S> block, HashSet<Integer> nodesInTree) {
        if (nodesInTree.size() == 1) {
            return 0;
        }

        LinkedList<Pair<S, S>> prod = new LinkedList<>();
        for (S x : new LinkedList<>(block)) {
            for (S y : new LinkedList<>(block)) {
                if (x.compareTo(y) != 0) {
                    prod.add(Pair.of(x, y));
                }
            }
        }

        return prod.stream().map(p -> LCAOfTwo(p.getFirst(), p.getSecond())).filter(i -> i != null)
                .max((a, b) -> Integer.compare(get(a).size(), get(b).size())).orElse(null);
    }

    private Integer findChildWithState(SplittingNode<S, I, O> r, S s) {
        return r.children.values().stream().filter(i -> get(i).hasState(s)).findFirst().orElse(null);
    }

    private Pair<Integer, Integer> findChildrenAtNode(SplittingNode<S, I, O> r, S s1, S s2) {
        Integer s1Child = findChildWithState(r, s1);
        Integer s2Child = findChildWithState(r, s2);

        if (s1Child != null && s2Child != null) {
            return Pair.of(s1Child, s2Child);
        }

        return null;
    }

    private boolean nodeSplits(SplittingNode<S, I, O> node, S s1, S s2) {
        Pair<Integer, Integer> pair = findChildrenAtNode(node, s1, s2);
        if (pair != null) {
            return pair.getFirst().compareTo(pair.getSecond()) != 0;
        }

        return false;
    }

    private Integer nextNode(SplittingNode<S, I, O> node, S s1, S s2) {
        Pair<Integer, Integer> pair = findChildrenAtNode(node, s1, s2);
        if (pair != null) {
            if (pair.getFirst().compareTo(pair.getSecond()) == 0) {
                return pair.getFirst();
            }
        }

        return null;
    }

    public Integer LCAOfTwo(S s1, S s2) {
        Integer cand = 0;

        while (cand != null) {
            if (nodeSplits(get(cand), s1, s2)) {
                return cand;
            }
            cand = nextNode(get(cand), s1, s2);
        }

        return null;
    }

    public Integer findNodeExact(List<S> block) {
        Integer out = IntStream.range(0, this.tree.size() + 1).filter(i -> block.size() == get(i).size())
                .filter(i -> block.stream().allMatch(s -> get(i).hasState(s))).findFirst().orElse(-1);
        return out == -1 ? null : out;
    }

    public void analyse(Integer rIndex, MealyMachine<S, I, ?, O> fsm, Alphabet<I> inputAlphabet,
            HashSet<Integer> analysed) {
        this.tree.arena.get(rIndex).value.analyse(fsm, inputAlphabet);
        analysed.add(rIndex);
    }

    public void separate(Integer rIndex, MealyMachine<S, I, ?, O> fsm, Helpers<I> helpers) {
        if (!helpers.nodesInTree.contains(rIndex)) {
            return;
        }

        SplittingNode<S, I, O> rNode = get(rIndex);
        List<I> seq = rNode.sepSeq.seq;
        HashMap<Word<O>, List<S>> outputSrcMap = new HashMap<>();
        for (S s : rNode.label) {
            Word<O> outs = fsm.computeStateOutput(s, seq);
            outputSrcMap.computeIfAbsent(outs, k -> new LinkedList<>()).add(s);
        }

        List<Integer> childIndeces = outputSrcMap.entrySet().stream().map(e -> {
            O os = e.getKey().lastSymbol();
            SplittingNode<S, I, O> childNode = new SplittingNode<>(e.getValue());
            return Pair.of(os, childNode);
        }).map(p -> {
            Integer childNodeIndex = this.tree.node(p.getSecond());
            SplittingNode<S, I, O> rNode2 = this.tree.arena.get(rIndex).value;
            rNode2.children.put(p.getFirst(), childNodeIndex);
            return childNodeIndex;
        }).collect(Collectors.toList());

        childIndeces.stream().filter(i -> get(i).size() > 1).forEach(i -> {
            Integer prio = get(i).size();
            helpers.nodesInTree.add(prio);
            helpers.partition.add(Pair.of(i, prio));
        });
    }

    public SplittingNode<S, I, O> get(Integer index) {
        return this.tree.get(index);
    }
}
