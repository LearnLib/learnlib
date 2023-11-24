package de.learnlib.algorithm.lsharp;

import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.common.util.Pair;
import net.automatalib.word.Word;

public interface ObservationTree<ST extends Comparable<ST>, I, O> {
    public ST defaultState();

    public ST insertObservation(@Nullable ST start, Word<I> input, Word<O> output);

    public Word<I> getAccessSeq(ST state);

    public Word<I> getTransferSeq(ST toState, ST fromState);

    public @Nullable Word<O> getObservation(@Nullable ST start, Word<I> input);

    public @Nullable Pair<O, ST> getOutSucc(ST src, I input);

    public default @Nullable O getOut(ST src, I input) {
        @Nullable
        Pair<O, ST> out = this.getOutSucc(src, input);
        if (out == null) {
            return null;
        }

        return out.getFirst();
    }

    public @Nullable ST getSucc(ST src, Word<I> input);

    public List<Pair<ST, I>> noSuccDefined(List<ST> basis, boolean sort);

    public Integer size();

    public boolean treeAndHypStatesApartSink(ST st, LSState sh, MealyMachine<LSState, I, ?, O> fsm, O sinkOutput,
            Integer depth);

    public Alphabet<I> getInputAlphabet();

}
