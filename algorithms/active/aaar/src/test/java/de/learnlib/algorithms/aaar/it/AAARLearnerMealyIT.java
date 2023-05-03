/* Copyright (C) 2013-2023 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.learnlib.algorithms.aaar.it;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import de.learnlib.acex.analyzers.AcexAnalyzers;
import de.learnlib.algorithms.aaar.AAARLearnerMealy;
import de.learnlib.algorithms.aaar.LearnerProvider;
import de.learnlib.algorithms.aaar.abstraction.AbstractionTree;
import de.learnlib.algorithms.discriminationtree.mealy.DTLearnerMealy;
import de.learnlib.algorithms.kv.mealy.KearnsVaziraniMealy;
import de.learnlib.algorithms.lstar.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstar.closing.ClosingStrategies;
import de.learnlib.algorithms.lstar.mealy.ExtensibleLStarMealy;
import de.learnlib.algorithms.oml.ttt.mealy.OptimalTTTMealy;
import de.learnlib.algorithms.rivestschapire.RivestSchapireMealy;
import de.learnlib.algorithms.ttt.mealy.TTTLearnerMealy;
import de.learnlib.api.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.api.oracle.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.counterexamples.LocalSuffixFinders;
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
                                             int targetSize,
                                             MealyMembershipOracle<I, O> mqOracle,
                                             MealyLearnerVariantList<I, O> variants) {

        final int maxRounds = alphabet.size() + targetSize;
        final I firstSym = alphabet.getSymbol(0);

        LearnerProvider<ExtensibleLStarMealy<I, O>, MealyMachine<?, I, ?, O>, I, Word<O>> lstar =
                (alph, mqo) -> new ExtensibleLStarMealy<>(alph,
                                                          mqo,
                                                          Collections.emptyList(),
                                                          ObservationTableCEXHandlers.CLASSIC_LSTAR,
                                                          ClosingStrategies.CLOSE_FIRST);
        LearnerProvider<RivestSchapireMealy<I, O>, MealyMachine<?, I, ?, O>, I, Word<O>> rs = RivestSchapireMealy::new;
        LearnerProvider<KearnsVaziraniMealy<I, O>, MealyMachine<?, I, ?, O>, I, Word<O>> kv =
                (alph, mqo) -> new KearnsVaziraniMealy<>(alph, mqo, true, AcexAnalyzers.BINARY_SEARCH_FWD);
        LearnerProvider<DTLearnerMealy<I, O>, MealyMachine<?, I, ?, O>, I, Word<O>> dt =
                (alph, mqo) -> new DTLearnerMealy<>(alph, mqo, LocalSuffixFinders.RIVEST_SCHAPIRE, true);
        LearnerProvider<TTTLearnerMealy<I, O>, MealyMachine<?, I, ?, O>, I, Word<O>> ttt =
                (alph, mqo) -> new TTTLearnerMealy<>(alph, mqo, AcexAnalyzers.BINARY_SEARCH_FWD);
        LearnerProvider<OptimalTTTMealy<I, O>, MealyMachine<?, I, ?, O>, I, Word<O>> oml =
                (alph, mqo) -> new OptimalTTTMealy<>(alph, mqo, mqo);

        variants.addLearnerVariant("L*", new LearnerWrapper<>(lstar, mqOracle, firstSym), maxRounds);
        variants.addLearnerVariant("RS", new LearnerWrapper<>(rs, mqOracle, firstSym), maxRounds);
        variants.addLearnerVariant("KV", new LearnerWrapper<>(kv, mqOracle, firstSym), maxRounds);
        variants.addLearnerVariant("DT", new LearnerWrapper<>(dt, mqOracle, firstSym), maxRounds);
        variants.addLearnerVariant("TTT", new LearnerWrapper<>(ttt, mqOracle, firstSym), maxRounds);
        variants.addLearnerVariant("OML", new LearnerWrapper<>(oml, mqOracle, firstSym), maxRounds);
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
     * domains coincide.
     */
    private static class LearnerWrapper<L extends MealyLearner<I, O> & SupportsGrowingAlphabet<I>, I, O>
            extends AAARLearnerMealy<L, I, I, O> {

        LearnerWrapper(LearnerProvider<L, MealyMachine<?, I, ?, O>, I, Word<O>> learnerProvider,
                       MembershipOracle<I, Word<O>> mqo,
                       I initialConcrete) {
            super(learnerProvider, mqo, initialConcrete, Function.identity());
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

        HypothesisWrapper(CompactMealy<I, O> other, AbstractionTree<I, I, Word<O>> tree) {
            super(other);
            this.tree = tree;
        }

        AbstractionTree<I, I, Word<O>> getTree() {
            return tree;
        }
    }

    private static class EQWrapper<I, O> implements MealyEquivalenceOracle<I, O> {

        private final UniversalDeterministicLearningExample<I, ? extends MealyMachine<?, I, ?, O>> example;
        private final List<DefaultQuery<I, Word<O>>> tests;

        EQWrapper(UniversalDeterministicLearningExample<I, ? extends MealyMachine<?, I, ?, O>> example) {
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
