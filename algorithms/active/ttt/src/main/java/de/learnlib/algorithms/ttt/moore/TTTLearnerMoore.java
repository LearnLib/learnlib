package de.learnlib.algorithms.ttt.moore;

import com.github.misberner.buildergen.annotations.GenerateBuilder;
import de.learnlib.acex.AcexAnalyzer;
import de.learnlib.algorithms.ttt.base.*;
import de.learnlib.algorithms.ttt.dfa.TTTStateDFA;
import de.learnlib.algorithms.ttt.mealy.TTTDTNodeMealy;
import de.learnlib.algorithms.ttt.mealy.TTTHypothesisMealy;
import de.learnlib.algorithms.ttt.mealy.TTTTransitionMealy;
import de.learnlib.api.algorithm.LearningAlgorithm.MooreLearner;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.counterexamples.acex.MealyOutInconsPrefixTransformAcex;
import de.learnlib.counterexamples.acex.MooreOutInconsPrefixTransformAcex;
import de.learnlib.counterexamples.acex.OutInconsPrefixTransformAcex;
import de.learnlib.util.mealy.MealyUtil;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.MooreMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

public class TTTLearnerMoore<I, O> extends AbstractTTTLearner<MooreMachine<?, I, ?, O>, I, Word<O>>
        implements MooreLearner<I, O> {

    @GenerateBuilder(defaults = AbstractTTTLearner.BuilderDefaults.class)
    protected TTTLearnerMoore(Alphabet<I> alphabet, MembershipOracle<I, Word<O>> oracle, AcexAnalyzer analyzer) {
        super(alphabet,
                oracle,
                new TTTHypothesisMoore<>(alphabet),
                new BaseTTTDiscriminationTree<>(oracle, TTTDTNodeMoore::new),
                analyzer);

        dtree.getRoot().split(Word.epsilon() ,(oracle.answerQuery(Word.epsilon())));

    }

    @Override
    protected Word<O> succEffect(Word<O> effect) {
        return effect.subWord(1);
    }

    @Override
    protected Word<O> predictSuccOutcome(TTTTransition<I, Word<O>> trans, AbstractBaseDTNode<I, Word<O>> succSeparator) {
        TTTStateMoore<I, O> curr = (TTTStateMoore<I, O>) trans.getSource();
        return succSeparator.subtreeLabel(trans.getDTTarget()).prepend(curr.getOutput());
    }


    @Override
    protected void initializeState(TTTState<I, Word<O>> state) {
        super.initializeState(state);

        TTTStateMoore<I, O> mooreState = (TTTStateMoore<I, O>) state;
        O output = dtree.getRoot().subtreeLabel(mooreState.getDTLeaf()).firstSymbol();
        assert output != null;
        mooreState.setOutput(output);
    }

    @Override
    protected OutInconsPrefixTransformAcex<I, Word<O>> deriveAcex(OutputInconsistency<I, Word<O>> outIncons) {
        TTTState<I, Word<O>> source = outIncons.srcState;
        Word<I> suffix = outIncons.suffix;

        OutInconsPrefixTransformAcex<I, Word<O>> acex = new MooreOutInconsPrefixTransformAcex<>(suffix,
                oracle,
                w -> getDeterministicState(
                        source,
                        w).getAccessSequence());

        acex.setEffect(0, outIncons.targetOut);
        Word<O> lastHypOut = computeHypothesisOutput(getAnySuccessor(source, suffix), Word.epsilon());
        acex.setEffect(suffix.length(), lastHypOut);

        return acex;
    }


    @Override
    @SuppressWarnings("unchecked")
    protected boolean refineHypothesisSingle(DefaultQuery<I, Word<O>> ceQuery) {
        DefaultQuery<I, Word<O>> shortenedCeQuery = shortenCounterExample((TTTHypothesisMoore<I, O>) hypothesis, ceQuery);
        return shortenedCeQuery != null && super.refineHypothesisSingle(shortenedCeQuery);
    }



    @Override
    protected Word<O> computeHypothesisOutput(TTTState<I, Word<O>> state, Word<I> suffix) {

        TTTStateMoore<I, O> curr = (TTTStateMoore<I, O>) state;

        WordBuilder<O> wb = new WordBuilder<>(suffix.length());

        wb.append(curr.output);
        if(suffix.length()==0){

            return wb.toWord();
        }
        for (I sym : suffix) {
            curr = (TTTStateMoore<I, O>) getAnySuccessor(curr, sym);
            wb.append(curr.output);

        }
        return wb.toWord();

    }

    @Override
    protected AbstractBaseDTNode<I, Word<O>> createNewNode(AbstractBaseDTNode<I, Word<O>> parent, Word<O> parentOutput) {
        return new TTTDTNodeMoore<>(parent, parentOutput);
    }

    @Override
    public MooreMachine<?, I, ?, O> getHypothesisModel() {
        return (TTTHypothesisMoore<I, O>) hypothesis;
    }


    /*
    help Methods
     */

    private static <I, O> @Nullable DefaultQuery<I, Word<O>> shortenCounterExample(MooreMachine<?, I, ?, O> hypothesis,
                                                                                   DefaultQuery<I, Word<O>> ceQuery) {
        Word<I> cePrefix = ceQuery.getPrefix(), ceSuffix = ceQuery.getSuffix();
        Word<O> hypOut = hypothesis.computeSuffixOutput(cePrefix, ceSuffix);
        Word<O> ceOut = ceQuery.getOutput();
        assert ceOut.length() == hypOut.length();


        int mismatchIdx = MealyUtil.findMismatch(hypOut, ceOut);
        if (mismatchIdx == -1) {
            return null;
        }
        return new DefaultQuery<>(cePrefix, ceSuffix.prefix(mismatchIdx), ceOut.prefix(mismatchIdx + 1));
    }


}