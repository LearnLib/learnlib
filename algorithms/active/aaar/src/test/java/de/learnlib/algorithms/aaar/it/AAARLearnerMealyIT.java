package de.learnlib.algorithms.aaar.it;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import de.learnlib.acex.analyzers.AcexAnalyzers;
import de.learnlib.algorithms.aaar.AAARLearnerMealy;
import de.learnlib.algorithms.aaar.LearnerProvider;
import de.learnlib.algorithms.aaar.abstraction.AbstractionTree;
import de.learnlib.algorithms.discriminationtree.mealy.DTLearnerMealy;
import de.learnlib.algorithms.ttt.mealy.TTTLearnerMealy;
import de.learnlib.api.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.api.oracle.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.counterexamples.LocalSuffixFinders;
import de.learnlib.examples.LearningExample.DFALearningExample;
import de.learnlib.examples.LearningExample.MealyLearningExample;
import de.learnlib.examples.LearningExample.StateLocalInputMealyLearningExample;
import de.learnlib.examples.LearningExample.UniversalDeterministicLearningExample;
import de.learnlib.testsupport.it.learner.AbstractMealyLearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList.MealyLearnerVariantList;
import net.automatalib.SupportsGrowingAlphabet;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.conformance.WpMethodTestsIterator;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.testng.Assert;

public class AAARLearnerMealyIT extends AbstractMealyLearnerIT {

    @Override
    protected <I, O> void addLearnerVariants(Alphabet<I> alphabet,
                                             MealyMembershipOracle<I, O> mqOracle,
                                             MealyLearnerVariantList<I, O> variants) {

        LearnerProvider<DTLearnerMealy<I, O>, MealyMachine<?, I, ?, O>, I, Word<O>> dt =
                (alph, mqo) -> new DTLearnerMealy<>(alph, mqo, LocalSuffixFinders.RIVEST_SCHAPIRE, true);
        LearnerProvider<TTTLearnerMealy<I, O>, MealyMachine<?, I, ?, O>, I, Word<O>> ttt =
                (alph, mqo) -> new TTTLearnerMealy<>(alph, mqo, AcexAnalyzers.BINARY_SEARCH_FWD);

        variants.addLearnerVariant("DT",
                                   new LearnerWrapper<>(dt, mqOracle, alphabet.getSymbol(0), Function.identity()));
        variants.addLearnerVariant("TTT",
                                   new LearnerWrapper<>(ttt, mqOracle, alphabet.getSymbol(0), Function.identity()));
    }

    @Override
    protected <I, O> MealyEquivalenceOracle<I, O> getEquivalenceOracle(MealyLearningExample<I, O> example) {
        return new EQWrapper<>(example);
    }

    protected <I, O> MealyEquivalenceOracle<I, O> getEquivalenceOracle(StateLocalInputMealyLearningExample<I, O> example) {
        return new EQWrapper<>(example);
    }

    /*
     * In order to generate counterexamples for the abstract hypothesis given a concrete source model, we need the
     * abstraction tree. The following wrappers make sure that we can access it. Note that in general one would have to
     * separate between abstract and concrete input symbols but in this specific situation we exploit the fact that both
     *  domains coincide.
     */
    private static class LearnerWrapper<L extends MealyLearner<I, O> & SupportsGrowingAlphabet<I>, I, O>
            extends AAARLearnerMealy<L, I, I, O> {

        public LearnerWrapper(LearnerProvider<L, MealyMachine<?, I, ?, O>, I, Word<O>> learnerProvider,
                              MembershipOracle<I, Word<O>> o,
                              I initialConcrete,
                              Function<I, I> abstractor) {
            super(learnerProvider, o, initialConcrete, abstractor);
        }

        @Override
        public MealyMachine<?, I, ?, O> getHypothesisModel() {
            @SuppressWarnings("unchecked")
            final CompactMealy<I, O> hyp = (CompactMealy<I, O>) super.getHypothesisModel();
            return new HypothesisWrapper<>(hyp, super.getAbstractionTree());
        }
    }

    private static class HypothesisWrapper<I, O> extends CompactMealy<I, O> {

        private final AbstractionTree<I, I, Word<O>> tree;

        public HypothesisWrapper(CompactMealy<I, O> other, AbstractionTree<I, I, Word<O>> tree) {
            super(other);
            this.tree = tree;
        }

        public AbstractionTree<I, I, Word<O>> getTree() {
            return tree;
        }
    }

    private static class EQWrapper<I, O> implements MealyEquivalenceOracle<I, O> {

        private final UniversalDeterministicLearningExample<I, ? extends MealyMachine<?, I, ?, O>> example;
        private final List<DefaultQuery<I, Word<O>>> tests;

        public EQWrapper(UniversalDeterministicLearningExample<I, ? extends MealyMachine<?, I, ?, O>> example) {
            this.example = example;
            final Alphabet<I> alphabet = example.getAlphabet();
            final MealyMachine<?, I, ?, O> mealy = example.getReferenceAutomaton();

            this.tests = new ArrayList<>();
            final WpMethodTestsIterator<I> iter = new WpMethodTestsIterator<>(mealy, alphabet);

            while (iter.hasNext()) {
                final Word<I> test = iter.next();
                this.tests.add(new DefaultQuery<>(Word.epsilon(), test, mealy.computeOutput(test)));
            }
        }

        @Override
        public @Nullable DefaultQuery<I, Word<O>> findCounterExample(MealyMachine<?, I, ?, O> hypothesis,
                                                                     Collection<? extends I> inputs) {
            @SuppressWarnings("unchecked")
            final HypothesisWrapper<I, O> wrapper = (HypothesisWrapper<I, O>) hypothesis;
            final AbstractionTree<I, I, Word<O>> tree = wrapper.getTree();

            for (DefaultQuery<I, Word<O>> t : tests) {
                final Word<I> input = t.getInput();
                final Word<I> abstractTest = input.transform(tree::getAbstractSymbol);

                final Word<O> output = hypothesis.computeOutput(abstractTest);
                if (!output.equals(t.getOutput())) {
                    return t;
                }
            }

            Assert.assertTrue(Automata.testEquivalence(example.getReferenceAutomaton(),
                                                       hypothesis,
                                                       example.getAlphabet()));
            return null;
        }
    }
}
