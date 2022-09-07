package de.learnlib.algorithms.discriminationtree.moore;

import com.github.misberner.buildergen.annotations.GenerateBuilder;
import de.learnlib.algorithms.discriminationtree.AbstractDTLearner;
import de.learnlib.algorithms.discriminationtree.DTLearnerState;
import de.learnlib.algorithms.discriminationtree.hypothesis.HState;
import de.learnlib.algorithms.discriminationtree.hypothesis.HTransition;
import de.learnlib.api.algorithm.LearningAlgorithm.MooreLearner;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.AbstractQuery;
import de.learnlib.api.query.Query;
import de.learnlib.counterexamples.LocalSuffixFinder;
import de.learnlib.counterexamples.LocalSuffixFinders;
import de.learnlib.datastructure.discriminationtree.MultiDTree;
import de.learnlib.datastructure.discriminationtree.model.AbstractWordBasedDiscriminationTree;
import net.automatalib.automata.transducers.MooreMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

public class DTLearnerMoore<I, O> extends AbstractDTLearner<MooreMachine<?, I, ?, O>, I, Word<O>, O, Void>
        implements MooreLearner<I, O> {

    private HypothesisWrapperMoore<I, O> hypWrapper;

    @GenerateBuilder(defaults = AbstractDTLearner.BuilderDefaults.class)
    protected DTLearnerMoore(Alphabet<I> alphabet,
                             MembershipOracle<I, Word<O>> oracle,
                             LocalSuffixFinder<? super I, ? super Word<O>> suffixFinder,
                             boolean repeatedCounterexampleEvaluation) {

        super(alphabet,
                oracle,
                suffixFinder,
                repeatedCounterexampleEvaluation, new MultiDTree<>(oracle));
        this.hypWrapper = new HypothesisWrapperMoore<>(getHypothesisDS());

    }

    @Override
    protected @Nullable Query<I, Word<O>> spQuery(HState<I, Word<O>, O, Void> state) {
        return new AbstractQuery<I, Word<O>>(state.getAccessSequence(), Word.epsilon()) {

            @Override
            public void answer(Word<O> output) {
                state.setProperty(output.firstSymbol());
            }
        } ;
    }

    @Override
    protected @Nullable Query<I, Word<O>> tpQuery(HTransition<I, Word<O>, O, Void> transition) {
        return null;
    }


    @Override
    public MooreMachine<?, I, ?, O> getHypothesisModel() {
        return hypWrapper;
    }

    @Override
    public void resume(DTLearnerState<I, Word<O>, O, Void> state){
        super.resume(state);
        this.hypWrapper = new HypothesisWrapperMoore<>(getHypothesisDS());
    }


}