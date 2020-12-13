package net.automatalib.automata.transducers.impl.compact;

import java.util.Collection;
import java.util.Iterator;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.DetSuffixOutputAutomaton;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.automata.graphs.TransitionEdge.Property;
import net.automatalib.automata.graphs.UniversalAutomatonGraphView;
import net.automatalib.graphs.UniversalGraph;
import net.automatalib.visualization.VisualizationHelper;
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

    @Override
    default UniversalGraph<S, TransitionEdge<I, T>, Word<O>, Property<I, Word<O>>> transitionGraphView(Collection<? extends I> inputs) {
        return new SSTGraphView<>(this, inputs);
    }

    class SSTGraphView<S, I, T, O, A extends SubsequentialTransducer<S, I, T, O>>
            extends UniversalAutomatonGraphView<S, I, T, Word<O>, Word<O>, A> {

        public SSTGraphView(A automaton, Collection<? extends I> inputs) {
            super(automaton, inputs);
        }

        @Override
        public VisualizationHelper<S, TransitionEdge<I, T>> getVisualizationHelper() {
            return new SSTVisualizationHelper<>(automaton);
        }
    }
}
