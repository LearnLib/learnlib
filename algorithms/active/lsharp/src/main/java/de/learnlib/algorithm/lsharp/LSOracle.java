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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import de.learnlib.algorithm.lsharp.ads.ADSTree;
import de.learnlib.oracle.AdaptiveMembershipOracle;
import de.learnlib.query.AdaptiveQuery;
import de.learnlib.util.mealy.WordAdaptiveQuery;
import net.automatalib.common.util.Pair;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class LSOracle<I, O> {

    private final AdaptiveMembershipOracle<I, O> sul;
    private final NormalObservationTree<I, O> obsTree;
    private final Rule2 rule2;
    private final Rule3 rule3;
    private @MonotonicNonNull Word<I> sinkState;
    private final O sinkOutput;
    private final Random random;

    public LSOracle(AdaptiveMembershipOracle<I, O> sul,
                    NormalObservationTree<I, O> obsTree,
                    Rule2 rule2,
                    Rule3 rule3,
                    @Nullable Word<I> sinkState,
                    O sinkOutput,
                    Random random) {
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

    public void makeSink(Integer s) {
        for (I i : obsTree.getInputAlphabet()) {
            obsTree.insertObservation(s, Word.fromLetter(i), Word.fromLetter(sinkOutput));
        }
    }

    public Integer addObservation(Word<I> i, Word<O> o) {
        return obsTree.insertObservation(null, i, o);
    }

    private <T> List<T> sample2(Collection<T> collection) {
        List<T> shuffled = new ArrayList<>(collection);
        Collections.shuffle(shuffled, random);
        return shuffled.subList(0, 2);
    }

    private Pair<Word<I>, Word<O>> rule3IO(List<Word<I>> candidates, Word<I> prefix) {
        switch (this.rule3) {
            case ADS:
                if (candidates.size() == 2) {
                    Word<I> q1Acc = candidates.get(0);
                    Word<I> q2Acc = candidates.get(1);
                    Integer q1 = obsTree.getSucc(obsTree.defaultState(), q1Acc);
                    Integer q2 = obsTree.getSucc(obsTree.defaultState(), q2Acc);
                    assert q1 != null;
                    assert q2 != null;

                    Word<I> wit = ApartnessUtil.computeWitness(obsTree, q1, q2);
                    assert wit != null;

                    WordBuilder<I> inputSeq = new WordBuilder<>(prefix);
                    assert !(ApartnessUtil.accStatesAreApart(obsTree, prefix, q1Acc) ||
                             ApartnessUtil.accStatesAreApart(obsTree, prefix, q2Acc));
                    inputSeq.append(wit);
                    Word<O> outputSeq = this.outputQuery(inputSeq.toWord());

                    return Pair.of(inputSeq.toWord(), outputSeq);

                } else {
                    List<Integer> candss = getSuccs(candidates);
                    ADSTree<Integer, I, O> suffix = new ADSTree<>(obsTree, candss, sinkOutput);
                    return this.adaptiveOutputQuery(prefix, null, suffix);
                }
            case SEPSEQ:
                List<Integer> withS = getSuccs(sample2(candidates));
                Word<I> wit = ApartnessUtil.computeWitness(obsTree, withS.get(0), withS.get(1));
                assert wit != null;
                Word<I> inputSeq = prefix.concat(wit);
                return Pair.of(inputSeq, this.outputQuery(inputSeq));
            default:
                throw new IllegalStateException("Shouldn't get here!");
        }
    }

    private List<Integer> getSuccs(Collection<Word<I>> candidates) {
        List<Integer> result = new ArrayList<>(candidates.size());
        for (Word<I> c : candidates) {
            Integer succ = obsTree.getSucc(obsTree.defaultState(), c);
            assert succ != null;
            result.add(succ);
        }
        return result;
    }

    public List<Word<I>> identifyFrontier(Word<I> fsAcc, List<Word<I>> candidates) {
        Integer fs = obsTree.getSucc(obsTree.defaultState(), fsAcc);
        assert fs != null;
        candidates.removeIf(b -> {
            Integer bs = obsTree.getSucc(obsTree.defaultState(), b);
            assert bs != null;
            return ApartnessUtil.statesAreApart(obsTree, fs, bs);
        });

        int orgCandLen = candidates.size();
        if (orgCandLen < 2) {
            return candidates;
        }

        Pair<Word<I>, Word<O>> pair = rule3IO(candidates, fsAcc);
        obsTree.insertObservation(null, pair.getFirst(), pair.getSecond());
        candidates.removeIf(b -> ApartnessUtil.accStatesAreApart(obsTree, fsAcc, b));
        assert candidates.size() != orgCandLen;
        return candidates;
    }

    private Pair<Word<I>, Word<O>> rule2IO(Word<I> accessQ, I i, List<Integer> bss, Collection<Word<I>> basis) {
        switch (this.rule2) {
            case ADS:
                ADSTree<Integer, I, O> suffix = new ADSTree<>(obsTree, bss, sinkOutput);
                return this.adaptiveOutputQuery(accessQ, i, suffix);
            case NOTHING:
                Word<I> prefix = accessQ.append(i);
                Word<O> oSeq = this.outputQuery(prefix);
                return Pair.of(prefix, oSeq);
            case SEPSEQ:
                Word<I> wit;
                if (basis.size() >= 2) {
                    List<Integer> ran = getSuccs(sample2(basis));
                    wit = ApartnessUtil.computeWitness(obsTree, ran.get(0), ran.get(1));
                    assert wit != null;
                } else {
                    wit = Word.epsilon();
                }
                Word<I> inputSeq = accessQ.append(i).concat(wit);
                Word<O> outputSeq = this.outputQuery(inputSeq);
                return Pair.of(inputSeq, outputSeq);
            default:
                throw new IllegalStateException("Shouldn't get here!");
        }
    }

    public List<Pair<Word<I>, List<Word<I>>>> exploreFrontier(Collection<Word<I>> basis) {
        List<Pair<Word<I>, List<Word<I>>>> frontier = new ArrayList<>();
        for (Word<I> b : basis) {
            for (I i : obsTree.getInputAlphabet()) {
                Integer bs = obsTree.getSucc(obsTree.defaultState(), b);
                assert bs != null;
                if (obsTree.getSucc(bs, Word.fromLetter(i)) == null) {
                    frontier.add(this.exploreFrontier(b, i, basis));
                }
            }
        }
        return frontier;
    }

    public Pair<Word<I>, List<Word<I>>> exploreFrontier(Word<I> accQ, I i, Collection<Word<I>> basis) {
        Word<I> accessQ = Word.fromWords(accQ);
        Integer q = obsTree.getSucc(obsTree.defaultState(), accQ);
        assert q != null;
        List<Integer> bss = getSuccs(basis);
        Pair<Word<I>, Word<O>> query = rule2IO(accessQ, i, bss, basis);
        Word<I> inputSeq = query.getFirst();
        Word<O> outputSeq = query.getSecond();

        obsTree.insertObservation(null, inputSeq, outputSeq);
        Integer fs = obsTree.getSucc(q, Word.fromLetter(i));
        assert fs != null;
        List<Word<I>> bsNotSep = new ArrayList<>(basis.size());
        for (Word<I> b : basis) {
            Integer bs = obsTree.getSucc(obsTree.defaultState(), b);
            assert bs != null;
            if (!ApartnessUtil.statesAreApart(obsTree, fs, bs)) {
                bsNotSep.add(b);
            }
        }
        return Pair.of(accQ.append(i), bsNotSep);
    }

    public Word<O> outputQuery(Word<I> inputSeq) {
        Word<O> out = obsTree.getObservation(null, inputSeq);
        if (out != null) {
            return out;
        }

        final WordAdaptiveQuery<I, O> query = new WordAdaptiveQuery<>(inputSeq);
        sul.processQuery(query);
        out = query.getOutput();

        if (sinkState == null && Objects.equals(out.lastSymbol(), sinkOutput)) {
            sinkState = inputSeq;
        }

        this.addObservation(inputSeq, out);
        return out;
    }

    public Pair<Word<I>, Word<O>> adaptiveOutputQuery(Word<I> prefix,
                                                      @Nullable I infix,
                                                      ADSTree<Integer, I, O> suffix) {
        return this.adaptiveOutputQuery(infix != null ? prefix.append(infix) : prefix, suffix);
    }

    public Pair<Word<I>, Word<O>> adaptiveOutputQuery(Word<I> prefix, ADSTree<Integer, I, O> suffix) {
        Pair<Word<I>, Word<O>> treeReply = null;
        Integer treeSucc = obsTree.getSucc(obsTree.defaultState(), prefix);
        if (treeSucc != null) {
            treeReply = this.answerADSFromTree(suffix, treeSucc);
        }

        suffix.resetToRoot();
        if (treeReply != null) {
            throw new IllegalStateException("ADS is not increasing the norm, we already knew this information.");
        }

        ADSTreeQuery query = new ADSTreeQuery(prefix, suffix);
        sul.processQuery(query);

        if (query.isSink()) {
            Word<O> output = query.getOutputSequence();
            Integer sink = this.addObservation(prefix, output);
            if (sinkState == null) {
                sinkState = prefix;
            }
            this.makeSink(sink);
            return Pair.of(prefix, output);
        }
        Word<I> inputSeq = query.getInputSequence();
        Word<O> outputSeq = query.getOutputSequence();
        this.addObservation(inputSeq, outputSeq);
        return Pair.of(inputSeq, outputSeq);
    }

    public @Nullable Pair<Word<I>, Word<O>> answerADSFromTree(ADSTree<Integer, I, O> ads, Integer fromState) {
        WordBuilder<I> inputsSent = new WordBuilder<>();
        WordBuilder<O> outputsReceived = new WordBuilder<>();
        Integer currState = fromState;

        O prevOutput;
        I nextInput = ads.nextInput(null);
        while (nextInput != null) {
            inputsSent.add(nextInput);
            Pair<O, Integer> pair = obsTree.getOutSucc(currState, nextInput);
            if (pair == null) {
                return null;
            }
            prevOutput = pair.getFirst();
            outputsReceived.add(pair.getFirst());
            currState = pair.getSecond();

            nextInput = ads.nextInput(prevOutput);
        }

        ads.resetToRoot();
        return Pair.of(inputsSent.toWord(), outputsReceived.toWord());
    }

    private class ADSTreeQuery implements AdaptiveQuery<I, O> {

        private final Word<I> prefix;
        private final ADSTree<Integer, I, O> ads;
        private final WordBuilder<I> input;
        private final WordBuilder<O> output;

        private int idx;
        private I adsSym;
        private boolean sink;

        ADSTreeQuery(Word<I> prefix, ADSTree<Integer, I, O> ads) {
            this.prefix = prefix;
            this.ads = ads;
            this.input = new WordBuilder<>();
            this.output = new WordBuilder<>();
        }

        @Override
        public I getInput() {
            if (idx < prefix.length()) {
                return prefix.getSymbol(idx);
            } else {
                return adsSym;
            }
        }

        @Override
        public Response processOutput(O out) {
            idx++;
            output.append(out);

            if (idx < prefix.length()) {
                return Response.SYMBOL;
            } else if (idx == prefix.length()) {
                if (Objects.equals(out, sinkOutput)) {
                    sink = true;
                    return Response.FINISHED;
                } else {
                    input.append(prefix);
                    return computeNext(null);
                }
            } else {
                return computeNext(out);
            }
        }

        private Response computeNext(@Nullable O out) {
            final I next = ads.nextInput(out);
            if (next == null) {
                return Response.FINISHED;
            } else {
                adsSym = next;
                input.append(next);
                return Response.SYMBOL;
            }
        }

        boolean isSink() {
            return sink;
        }

        Word<I> getInputSequence() {
            return input.toWord();
        }

        Word<O> getOutputSequence() {
            return output.toWord();
        }
    }

}
