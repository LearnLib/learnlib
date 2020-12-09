package net.automatalib.automata.transducers.impl.compact;

import java.util.Collection;
import java.util.Iterator;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.DetSuffixOutputAutomaton;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

public interface SubsequentialTransducer<S, I, T, O>
        extends DetSuffixOutputAutomaton<S, I, T, Word<O>>, UniversalDeterministicAutomaton<S, I, T, Word<O>, Word<O>> {

    @Override
    default Word<O> computeStateOutput(S state, Iterable<? extends I> input) {
        final WordBuilder<O> result;
        if (input instanceof Word) {
            result = new WordBuilder<>(((Word<?>) input).length());
        } else if (input instanceof Collection) {
            result = new WordBuilder<>(((Collection<?>) input).size());
        } else {
            result = new WordBuilder<>();
        }

        final Iterator<? extends I> inputIter = input.iterator();
        S stateIter = state;

        while (inputIter.hasNext()) {
            final I i = inputIter.next();
            final T t = getTransition(stateIter, i);

            if (t != null) {
                result.append(getTransitionProperty(t));
                stateIter = getSuccessor(t);
            }
        }

        result.append(getStateProperty(stateIter));

        return result.toWord();
    }

}
