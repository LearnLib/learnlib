package de.learnlib.algorithms.ttt.moore;

import de.learnlib.algorithms.ttt.base.AbstractTTTHypothesis;
import de.learnlib.algorithms.ttt.base.TTTState;
import de.learnlib.algorithms.ttt.base.TTTTransition;
import de.learnlib.algorithms.ttt.dfa.TTTStateDFA;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.UniversalDeterministicAutomaton.FullIntAbstraction;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.automata.transducers.MooreMachine;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.UniversalGraph;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;


public class TTTHypothesisMoore<I, O> extends AbstractTTTHypothesis<I, Word<O>, TTTState<I, Word<O>>> implements
        MooreMachine<TTTState<I, Word<O>>, I, TTTState<I, Word<O>>, O>,
        FullIntAbstraction<TTTState<I, Word<O>>, O, Void> {

    /**
     * Constructor.
     *
     * @param alphabet the input alphabet
     */
    public TTTHypothesisMoore(Alphabet<I> alphabet) {
        super(alphabet);
    }

    @Override
    protected TTTState<I, Word<O>> mapTransition(TTTTransition<I, Word<O>> internalTransition) {
        return internalTransition.getTarget();
    }



    @Override
    public O getStateProperty(int state) {
        TTTStateMoore<I, O> mooreState = (TTTStateMoore<I, O>) states.get(state);
        return mooreState.getOutput();

    }

    @Override
    public TTTStateMoore<I, O> newState(int alphabetSize, TTTTransition<I, Word<O>> parent, int id){
        return new TTTStateMoore<>(alphabetSize, parent, id);
    }

    @Override
    public UniversalDeterministicAutomaton.FullIntAbstraction fullIntAbstraction(
            Alphabet<I> alphabet) {
        if (alphabet.equals(getInputAlphabet())) {
            return this;
        }
        return MooreMachine.super.fullIntAbstraction(alphabet);
    }


    @Override
    public O getStateOutput(TTTState<I, Word<O>> state) {
        TTTStateMoore<I, O> newState = (TTTStateMoore<I,O>) state;
        return newState.getOutput();
    }

    @Override
    public TTTState<I, Word<O>> getSuccessor(TTTState<I, Word<O>> transition) {
        return transition;
    }

    @Override
    public Void getTransitionProperty(TTTState<I, Word<O>> state){
        return null;
    }
}