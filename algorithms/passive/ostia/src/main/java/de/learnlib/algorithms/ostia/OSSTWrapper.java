package de.learnlib.algorithms.ostia;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.stream.StreamSupport;

import net.automatalib.automata.transducers.impl.compact.SubsequentialTransducer;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

class OSSTWrapper<I, O> implements SubsequentialTransducer<State, I, Edge, O> {

    private final State root;
    private final Alphabet<I> inputAlphabet;
    private final Alphabet<O> outputAlphabet;

    public OSSTWrapper(State root, Alphabet<I> inputAlphabet, Alphabet<O> outputAlphabet) {
        this.root = root;
        this.inputAlphabet = inputAlphabet;
        this.outputAlphabet = outputAlphabet;
    }

    @Override
    public Word<O> computeOutput(Iterable<? extends I> input) {
        final Iterator<Integer> transformedInput =
                StreamSupport.stream(input.spliterator(), false).mapToInt(inputAlphabet).iterator();
        final ArrayList<Integer> output = OSTIA.run(root, transformedInput);

        return output != null ?
                output.stream().map(outputAlphabet::getSymbol).collect(Word.collector()) :
                Word.epsilon();
    }

    @Override
    public Collection<State> getStates() {
        final Set<State> cache = new HashSet<>();
        final Queue<State> queue = new ArrayDeque<>();

        queue.add(root);

        while (!queue.isEmpty()) {
            State s = queue.poll();
            cache.add(s);

            for (Edge transition : s.transitions) {
                if (transition != null) {
                    State succ = transition.target;

                    if (succ != null && !cache.contains(succ)) {
                        queue.add(succ);
                        cache.add(succ);
                    }
                }
            }
        }

        return cache;
    }

    @Override
    public @Nullable Edge getTransition(State state, I input) {
        return state.transitions[inputAlphabet.getSymbolIndex(input)];
    }

    @Override
    public State getInitialState() {
        return root;
    }

    @Override
    public Word<O> getStateProperty(State state) {
        return outToWord(state.out);
    }

    @Override
    public Word<O> getTransitionProperty(Edge transition) {
        return outToWord(transition.out);
    }

    @Override
    public State getSuccessor(Edge transition) {
        return transition.target;
    }

    private Word<O> outToWord(Out out) {
        return outToWord(out == null ? null : out.str);
    }

    private Word<O> outToWord(IntQueue out) {
        if (out == null) {
            return Word.epsilon();
        }

        final WordBuilder<O> wb = new WordBuilder<>();

        while (out != null) {
            wb.add(outputAlphabet.getSymbol(out.value));
            out = out.next;
        }

        return wb.toWord();
    }
}
