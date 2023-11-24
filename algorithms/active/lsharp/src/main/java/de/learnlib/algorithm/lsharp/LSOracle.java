package de.learnlib.algorithm.lsharp;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.Nullable;

import de.learnlib.algorithm.lsharp.ads.ADSStatus;
import de.learnlib.algorithm.lsharp.ads.ADSTree;
import de.learnlib.oracle.MembershipOracle;
import net.automatalib.common.util.Pair;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;

public class LSOracle<I, O> {

    private MembershipOracle<I, Word<O>> sul;
    private NormalObservationTree<I, O> obsTree;
    private Rule2 rule2;
    private Rule3 rule3;
    private Word<I> sinkState;
    private O sinkOutput;
    private Random random;

    protected LSOracle(MembershipOracle<I, Word<O>> sul, NormalObservationTree<I, O> obsTree, Rule2 rule2, Rule3 rule3,
            Word<I> sinkState, O sinkOutput, Random random) {
        this.sul = sul;
        this.obsTree = obsTree;
        this.rule2 = rule2;
        this.rule3 = rule3;
        this.sinkState = sinkState;
        this.sinkOutput = sinkOutput;
        this.random = random;
    }

    public NormalObservationTree<I, O> getTree() {
        return this.obsTree;
    }

    public void makeSink(LSState s) {
        for (I i : obsTree.getInputAlphabet()) {
            obsTree.insertObservation(s, Word.fromLetter(i), Word.fromLetter(sinkOutput));
        }
    }

    public LSState addObservation(Word<I> i, Word<O> o) {
        return obsTree.insertObservation(null, i, o);
    }

    private <T> List<T> randomN(List<T> list, Integer n) {
        List<T> shuffled = new LinkedList<>(list);
        Collections.shuffle(shuffled, random);
        return shuffled.subList(0, n);
    }

    private Pair<Word<I>, Word<O>> rule3IO(List<Word<I>> candidates, Word<I> prefix) {
        switch (this.rule3) {
        case ADS:
            if (candidates.size() == 2) {
                Word<I> q1Acc = candidates.get(0);
                Word<I> q2Acc = candidates.get(1);
                LSState q1 = obsTree.getSucc(obsTree.defaultState(), q1Acc);
                LSState q2 = obsTree.getSucc(obsTree.defaultState(), q2Acc);
                Objects.requireNonNull(q1);
                Objects.requireNonNull(q2);

                Word<I> wit = Apartness.computeWitness(obsTree, q1, q2);
                Objects.requireNonNull(wit);

                WordBuilder<I> inputSeq = new WordBuilder<>(prefix);
                assert !(Apartness.accStatesAreApart(obsTree, prefix, q1Acc)
                        || Apartness.accStatesAreApart(obsTree, prefix, q2Acc));
                inputSeq.append(wit);
                Word<O> outputSeq = this.outputQuery(inputSeq.toWord());

                return Pair.of(inputSeq.toWord(), outputSeq);

            } else {
                List<LSState> candss = candidates.stream().map(acc -> obsTree.getSucc(obsTree.defaultState(), acc))
                        .collect(Collectors.toList());
                ADSTree<LSState, I, O> suffix = new ADSTree<>(obsTree, candss, sinkOutput);
                return this.adaptiveOutputQuery(prefix, null, suffix);
            }
        case SEPSEQ:
            List<LSState> withS = randomN(candidates, 2).stream()
                    .map(acc -> obsTree.getSucc(obsTree.defaultState(), acc)).collect(Collectors.toList());
            Word<I> wit = Apartness.computeWitness(obsTree, withS.get(0), withS.get(1));

            Word<I> inputSeq = Word.fromWords(wit);
            return Pair.of(inputSeq, this.outputQuery(inputSeq));
        }
        throw new RuntimeException("Shouldnt get here!");
    }

    public List<Word<I>> identifyFrontier(Word<I> fsAcc, List<Word<I>> candidates) {
        LSState fs = obsTree.getSucc(obsTree.defaultState(), fsAcc);
        Objects.requireNonNull(fs);
        candidates.removeIf(b -> {
            LSState bs = obsTree.getSucc(obsTree.defaultState(), b);
            Objects.requireNonNull(bs);
            return Apartness.statesAreApart(obsTree, fs, bs);
        });

        Integer orgCandLen = candidates.size();
        if (orgCandLen < 2) {
            return candidates;
        }

        Pair<Word<I>, Word<O>> pair = rule3IO(candidates, fsAcc);
        obsTree.insertObservation(null, pair.getFirst(), pair.getSecond());
        candidates.removeIf(b -> Apartness.accStatesAreApart(obsTree, fsAcc, b));
        assert candidates.size() != orgCandLen;
        return candidates;
    }

    public List<Pair<Word<I>, List<Word<I>>>> exploreFrontier(List<Word<I>> basis) {
        List<Pair<Word<I>, I>> toExplore = new LinkedList<>();
        for (Word<I> b : basis) {
            for (I i : obsTree.getInputAlphabet()) {
                LSState bs = obsTree.getSucc(obsTree.defaultState(), b);
                Objects.requireNonNull(bs);
                if (obsTree.getSucc(bs, Word.fromLetter(i)) == null) {
                    toExplore.add(Pair.of(b, i));
                }
            }
        }
        return toExplore.stream().map(p -> this.exploreFrontier(p.getFirst(), p.getSecond(), basis))
                .collect(Collectors.toList());
    }

    private Pair<Word<I>, Word<O>> rule2IO(Word<I> accessQ, I i, List<LSState> bss, List<Word<I>> basis) {
        switch (this.rule2) {
        case ADS:
            ADSTree<LSState, I, O> suffix = new ADSTree<>(obsTree, bss, sinkOutput);
            return this.adaptiveOutputQuery(accessQ, i, suffix);
        case NOTHING:
            Word<I> prefix = accessQ.append(i);
            Word<O> oSeq = this.outputQuery(prefix);
            return Pair.of(prefix, oSeq);
        case SEPSEQ:
            Word<I> wit = Word.epsilon();
            if (basis.size() >= 2) {
                List<LSState> ran = randomN(basis, 2).stream().map(b -> obsTree.getSucc(obsTree.defaultState(), b))
                        .collect(Collectors.toList());
                Objects.requireNonNull(ran.get(0));
                Objects.requireNonNull(ran.get(1));
                wit = Apartness.computeWitness(obsTree, ran.get(0), ran.get(1));
            }
            Word<I> inputSeq = accessQ.append(i).concat(wit);
            Word<O> outputSeq = this.outputQuery(inputSeq);
            return Pair.of(inputSeq, outputSeq);
        }
        throw new RuntimeException("Shouldnt get here!");
    }

    public Pair<Word<I>, List<Word<I>>> exploreFrontier(Word<I> accQ, I i, List<Word<I>> basis) {
        Word<I> accessQ = Word.fromWords(accQ);
        LSState q = obsTree.getSucc(obsTree.defaultState(), accQ);
        Objects.requireNonNull(q);
        List<LSState> bss = basis.stream().map(bAcc -> obsTree.getSucc(obsTree.defaultState(), bAcc))
                .collect(Collectors.toList());
        Pair<Word<I>, Word<O>> query = rule2IO(accessQ, i, bss, basis);
        Word<I> inputSeq = query.getFirst();
        Word<O> outputSeq = query.getSecond();

        obsTree.insertObservation(null, inputSeq, outputSeq);
        LSState fs = obsTree.getSucc(q, Word.fromLetter(i));
        Objects.requireNonNull(fs);
        List<Word<I>> bsNotSep = basis.parallelStream().filter(b -> {
            LSState bs = obsTree.getSucc(obsTree.defaultState(), b);
            Objects.requireNonNull(bs);
            return !Apartness.statesAreApart(obsTree, fs, bs);
        }).collect(Collectors.toList());
        return Pair.of(accQ.append(i), bsNotSep);
    }

    public Word<O> outputQuery(Word<I> inputSeq) {
        Word<O> out = obsTree.getObservation(null, inputSeq);
        if (out != null) {
            return out;
        }

        out = sul.answerQuery(inputSeq);

        if (sinkState == null && out.lastSymbol().equals(sinkOutput)) {
            sinkState = inputSeq;
        }

        this.addObservation(inputSeq, out);
        return out;
    }

    public Pair<Word<I>, Word<O>> adaptiveOutputQuery(Word<I> prefix, I infix, ADSTree<LSState, I, O> suffix) {
        if (infix != null) {
            prefix = prefix.append(infix);
        }

        return this.adaptiveOutputQuery(prefix, suffix);
    }

    public Pair<Word<I>, Word<O>> adaptiveOutputQuery(Word<I> prefix, ADSTree<LSState, I, O> suffix) {
        @Nullable
        Pair<Word<I>, Word<O>> treeReply = null;
        LSState treeSucc = obsTree.getSucc(obsTree.defaultState(), prefix);
        if (treeSucc != null) {
            treeReply = this.answerADSFromTree(suffix, treeSucc);
        }

        suffix.resetToRoot();
        if (treeReply != null) {
            throw new RuntimeException("ADS is not increasing the norm, we already knew this information.");
        }

        Word<O> prefixOut = sul.answerQuery(prefix);
        if (prefixOut.lastSymbol().equals(sinkOutput)) {
            LSState sink = this.addObservation(prefix, prefixOut);
            if (sinkState == null) {
                sinkState = prefix;
            }
            this.makeSink(sink);
            return Pair.of(prefix, prefixOut);
        }
        Pair<Word<I>, Word<O>> pair = this.sulAdaptiveQuery(prefix, suffix);
        Word<I> inputSeq = prefix.concat(pair.getFirst());
        Word<O> outputSeq = prefixOut.concat(pair.getSecond());
        this.addObservation(inputSeq, outputSeq);
        return Pair.of(inputSeq, outputSeq);
    }

    public Pair<Word<I>, Word<O>> sulAdaptiveQuery(Word<I> prefix, ADSTree<LSState, I, O> ads) {
        WordBuilder<I> inputsSent = new WordBuilder<>();
        WordBuilder<O> outputsReceived = new WordBuilder<>();
        O lastOutput = null;

        try {
            I nextInput = ads.nextInput(lastOutput);
            while (nextInput != null) {
                inputsSent.add(nextInput);
                O out = sul.answerQuery(prefix.concat(inputsSent.toWord())).lastSymbol();
                lastOutput = out;
                outputsReceived.add(out);

                nextInput = ads.nextInput(lastOutput);
            }
        } catch (ADSStatus e) {
        }

        return Pair.of(inputsSent.toWord(), outputsReceived.toWord());
    }

    public @Nullable Pair<Word<I>, Word<O>> answerADSFromTree(ADSTree<LSState, I, O> ads, LSState fromState) {
        O prevOutput = null;
        WordBuilder<I> inputsSent = new WordBuilder<>();
        WordBuilder<O> outputsReceived = new WordBuilder<>();
        LSState currState = fromState;

        try {
            I nextInput = ads.nextInput(prevOutput);
            while (nextInput != null) {
                inputsSent.add(nextInput);
                @Nullable
                Pair<O, LSState> pair = obsTree.getOutSucc(currState, nextInput);
                if (pair == null) {
                    return null;
                }
                prevOutput = pair.getFirst();
                outputsReceived.add(pair.getFirst());
                currState = pair.getSecond();

                nextInput = ads.nextInput(prevOutput);
            }
        } catch (ADSStatus e) {
        }

        ads.resetToRoot();
        return Pair.of(inputsSent.toWord(), outputsReceived.toWord());
    }

}
