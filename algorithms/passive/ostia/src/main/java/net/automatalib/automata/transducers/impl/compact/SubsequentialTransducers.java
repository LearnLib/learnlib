package net.automatalib.automata.transducers.impl.compact;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import net.automatalib.commons.util.Pair;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.util.automata.copy.AutomatonCopyMethod;
import net.automatalib.util.automata.copy.AutomatonLowLevelCopy;
import net.automatalib.words.Word;

public final class SubsequentialTransducers {

    private SubsequentialTransducers() {
        // prevent initialization
    }

    public static <S1, S2, I, T1, T2, O, A extends MutableOnwardSubsequentialTransducer<S2, I, T2, O>> A toOSST(
            SubsequentialTransducer<S1, I, T1, O> sst,
            Collection<? extends I> inputs,
            A out) {

        AutomatonLowLevelCopy.copy(AutomatonCopyMethod.STATE_BY_STATE, sst, inputs, out);

        final Mapping<S2, Set<Pair<S2, I>>> incomingTransitions = getIncomingTransitions(out, inputs);
        final Deque<S2> queue = new ArrayDeque<>(out.getStates());

        while (!queue.isEmpty()) {
            final S2 s = queue.pop();
            final Word<O> lcp = computeLCP(out, inputs, s);

            System.err.println("lcp: " + lcp);

            if (!lcp.isEmpty()) {
                final Word<O> oldStateProperty = out.getStateProperty(s);
                final Word<O> newStateProperty = oldStateProperty.subWord(lcp.length());

                out.setStateProperty(s, newStateProperty);

                for (I i : inputs) {
                    final T2 t = out.getTransition(s, i);
                    if (t != null) {
                        final Word<O> oldTransitionProperty = out.getTransitionProperty(t);
                        final Word<O> newTransitionProperty = oldTransitionProperty.subWord(lcp.length());

                        out.setTransitionProperty(t, newTransitionProperty);
                    }
                }

                for (Pair<S2, I> incoming : incomingTransitions.get(s)) {
                    final T2 t = out.getTransition(incoming.getFirst(), incoming.getSecond());

                    final Word<O> oldTransitionProperty = out.getTransitionProperty(t);
                    final Word<O> newTransitionProperty = oldTransitionProperty.concat(lcp);

                    out.setTransitionProperty(t, newTransitionProperty);
                    queue.add(incoming.getFirst());
                }
            }
        }

        return out;
    }

    private static <S, I, T> Mapping<S, Set<Pair<S, I>>> getIncomingTransitions(SubsequentialTransducer<S, I, T, ?> sst,
                                                                                Collection<? extends I> inputs) {

        final MutableMapping<S, Set<Pair<S, I>>> result = sst.createStaticStateMapping();

        for (S s : sst) {
            result.put(s, new HashSet<>());
        }

        for (S s : sst) {
            for (I i : inputs) {
                final T t = sst.getTransition(s, i);

                if (t != null) {
                    final S succ = sst.getSuccessor(t);
                    result.get(succ).add(Pair.of(s, i));
                }
            }
        }

        return result;
    }

    private static <S, I, O> Word<O> computeLCP(SubsequentialTransducer<S, I, ?, O> sst,
                                                Collection<? extends I> inputs,
                                                S s) {

        Word<O> lcp = sst.getStateProperty(s);

        for (I i : inputs) {
            lcp = lcp.longestCommonPrefix(sst.getTransitionProperty(s, i));
        }

        return lcp;
    }

}
