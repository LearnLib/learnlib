package de.learnlib.algorithm.lsharp;

import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.common.util.Pair;
import net.automatalib.word.Word;

public interface ObservationTree<S extends Comparable<S>, I, O> {
    S defaultState();

    S insertObservation(@Nullable S start, Word<I> input, Word<O> output);

    Word<I> getAccessSeq(S state);

    Word<I> getTransferSeq(S toState, S fromState);

    @Nullable
    Word<O> getObservation(@Nullable S start, Word<I> input);

    @Nullable
    Pair<O, S> getOutSucc(S src, I input);

    default @Nullable O getOut(S src, I input) {
        @Nullable
        Pair<O, S> out = this.getOutSucc(src, input);
        if (out == null) {
            return null;
        }

        return out.getFirst();
    }

    @Nullable
    S getSucc(S src, Word<I> input);

    List<Pair<S, I>> noSuccDefined(List<S> basis, boolean sort);

    Integer size();

    boolean treeAndHypStatesApartSink(S st, LSState sh, MealyMachine<LSState, I, ?, O> fsm, O sinkOutput,
            Integer depth);

    Alphabet<I> getInputAlphabet();

}
