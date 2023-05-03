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
import java.util.List;
import java.util.function.Function;

import de.learnlib.acex.analyzers.AcexAnalyzers;
import de.learnlib.algorithms.aaar.AAARLearnerDFA;
import de.learnlib.algorithms.aaar.LearnerProvider;
import de.learnlib.algorithms.aaar.abstraction.AbstractionTree;
import de.learnlib.algorithms.discriminationtree.dfa.DTLearnerDFA;
import de.learnlib.algorithms.kv.dfa.KearnsVaziraniDFA;
import de.learnlib.algorithms.lstar.dfa.ClassicLStarDFA;
import de.learnlib.algorithms.oml.ttt.dfa.OptimalTTTDFA;
import de.learnlib.algorithms.rivestschapire.RivestSchapireDFA;
import de.learnlib.algorithms.ttt.dfa.TTTLearnerDFA;
import de.learnlib.api.algorithm.LearningAlgorithm.DFALearner;
import de.learnlib.api.oracle.EquivalenceOracle.DFAEquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.counterexamples.LocalSuffixFinders;
import de.learnlib.examples.LearningExample.DFALearningExample;
import de.learnlib.testsupport.it.learner.AbstractDFALearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList.DFALearnerVariantList;
import net.automatalib.SupportsGrowingAlphabet;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.conformance.WpMethodTestsIterator;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.testng.Assert;

public class AAARLearnerDFAIT extends AbstractDFALearnerIT {

    @Override
    protected <I> void addLearnerVariants(Alphabet<I> alphabet,
                                          int targetSize,
                                          DFAMembershipOracle<I> mqOracle,
                                          DFALearnerVariantList<I> variants) {

        final int maxRounds = alphabet.size() + targetSize;
        final I firstSym = alphabet.getSymbol(0);

        LearnerProvider<ClassicLStarDFA<I>, DFA<?, I>, I, Boolean> lstar = ClassicLStarDFA::new;
        LearnerProvider<RivestSchapireDFA<I>, DFA<?, I>, I, Boolean> rs = RivestSchapireDFA::new;
        LearnerProvider<KearnsVaziraniDFA<I>, DFA<?, I>, I, Boolean> kv =
                (alph, mqo) -> new KearnsVaziraniDFA<>(alph, mqo, true, AcexAnalyzers.BINARY_SEARCH_FWD);
        LearnerProvider<DTLearnerDFA<I>, DFA<?, I>, I, Boolean> dt =
                (alph, mqo) -> new DTLearnerDFA<>(alph, mqo, LocalSuffixFinders.RIVEST_SCHAPIRE, true, true);
        LearnerProvider<TTTLearnerDFA<I>, DFA<?, I>, I, Boolean> ttt =
                (alph, mqo) -> new TTTLearnerDFA<>(alph, mqo, AcexAnalyzers.BINARY_SEARCH_FWD);
        LearnerProvider<OptimalTTTDFA<I>, DFA<?, I>, I, Boolean> oml =
                (alph, mqo) -> new OptimalTTTDFA<>(alph, mqo, mqo);

        variants.addLearnerVariant("L*", new LearnerWrapper<>(lstar, mqOracle, firstSym), maxRounds);
        variants.addLearnerVariant("RS", new LearnerWrapper<>(rs, mqOracle, firstSym), maxRounds);
        variants.addLearnerVariant("KV", new LearnerWrapper<>(kv, mqOracle, firstSym), maxRounds);
        variants.addLearnerVariant("DT", new LearnerWrapper<>(dt, mqOracle, firstSym), maxRounds);
        variants.addLearnerVariant("TTT", new LearnerWrapper<>(ttt, mqOracle, firstSym), maxRounds);
        variants.addLearnerVariant("OML", new LearnerWrapper<>(oml, mqOracle, firstSym), maxRounds);
    }

    @Override
    protected <I> DFAEquivalenceOracle<I> getEquivalenceOracle(DFALearningExample<I> example) {
        return new EQWrapper<>(example);
    }

    /*
     * In order to generate counterexamples for the abstract hypothesis given a concrete source model, we need the
     * abstraction tree. The following wrappers make sure that we can access it. Note that in general one would have to
     * separate between abstract and concrete input symbols but in this specific situation we exploit the fact that both
     * domains coincide.
     */
    private static class LearnerWrapper<L extends DFALearner<I> & SupportsGrowingAlphabet<I>, I>
            extends AAARLearnerDFA<L, I, I> {

        LearnerWrapper(LearnerProvider<L, DFA<?, I>, I, Boolean> learnerProvider,
                       MembershipOracle<I, Boolean> mqo,
                       I initialConcrete) {
            super(learnerProvider, mqo, initialConcrete, Function.identity());
        }

        @Override
        public DFA<?, I> getHypothesisModel() {
            @SuppressWarnings("unchecked")
            final CompactDFA<I> hyp = (CompactDFA<I>) super.getHypothesisModel();
            return new HypothesisWrapper<>(hyp, super.getAbstractionTree());
        }
    }

    private static class HypothesisWrapper<I> extends CompactDFA<I> {

        private final AbstractionTree<I, I, Boolean> tree;

        HypothesisWrapper(CompactDFA<I> other, AbstractionTree<I, I, Boolean> tree) {
            super(other);
            this.tree = tree;
        }

        AbstractionTree<I, I, Boolean> getTree() {
            return tree;
        }
    }

    private static class EQWrapper<I> implements DFAEquivalenceOracle<I> {

        private final DFALearningExample<I> example;
        private final List<DefaultQuery<I, Boolean>> tests;

        EQWrapper(DFALearningExample<I> example) {
            this.example = example;
            final Alphabet<I> alphabet = example.getAlphabet();
            final DFA<?, I> dfa = example.getReferenceAutomaton();

            this.tests = new ArrayList<>();
            final WpMethodTestsIterator<I> iter = new WpMethodTestsIterator<>(dfa, alphabet);

            while (iter.hasNext()) {
                final Word<I> test = iter.next();
                this.tests.add(new DefaultQuery<>(Word.epsilon(), test, dfa.accepts(test)));
            }
        }

        @Override
        public @Nullable DefaultQuery<I, Boolean> findCounterExample(DFA<?, I> hypothesis,
                                                                     Collection<? extends I> inputs) {
            @SuppressWarnings("unchecked")
            final HypothesisWrapper<I> wrapper = (HypothesisWrapper<I>) hypothesis;
            final AbstractionTree<I, I, Boolean> tree = wrapper.getTree();

            for (DefaultQuery<I, Boolean> t : tests) {
                final Word<I> input = t.getInput();
                final Word<I> abstractTest = input.transform(tree::getAbstractSymbol);

                final boolean output = hypothesis.accepts(abstractTest);
                if (output != t.getOutput()) {
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
