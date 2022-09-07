package de.learnlib.algorithms.discriminationtree.moore;

import de.learnlib.algorithms.discriminationtree.hypothesis.DTLearnerHypothesis;
import de.learnlib.algorithms.discriminationtree.hypothesis.HState;
import de.learnlib.algorithms.discriminationtree.hypothesis.HTransition;
import net.automatalib.automata.transducers.MooreMachine;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;

public class HypothesisWrapperMoore <I, O> implements MooreMachine<HState<I, Word<O>, O, Void>, I, HTransition<I, Word<O>, O, Void>, O> {

    private final DTLearnerHypothesis<I, Word<O>, O, Void> dtHypothesis;

    HypothesisWrapperMoore(DTLearnerHypothesis<I, Word<O>, O, Void> dtHypothesis) {
        this.dtHypothesis = dtHypothesis;
    }


    @Override
    public O getStateOutput(HState<I, Word<O>, O, Void> state) {
        return state.getProperty();
    }

    @Override
    public Collection<HState<I, Word<O>, O, Void>> getStates() {
        return dtHypothesis.getStates();
    }


    @Override
    public @Nullable HState<I, Word<O>, O, Void> getInitialState() {
        return dtHypothesis.getInitialState();
    }

    @Override
    public @Nullable HTransition<I, Word<O>, O, Void> getTransition(HState<I, Word<O>, O, Void> state, I input) {
        return dtHypothesis.getTransition(state, input);
    }

    @Override
    public HState<I, Word<O>, O, Void> getSuccessor(HTransition<I, Word<O>, O, Void> transition) {
        return dtHypothesis.getSuccessor(transition);
    }
}