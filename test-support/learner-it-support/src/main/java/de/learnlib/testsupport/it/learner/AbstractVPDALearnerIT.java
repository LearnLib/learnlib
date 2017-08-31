/* Copyright (C) 2013-2017 TU Dortmund
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
package de.learnlib.testsupport.it.learner;

import java.util.Random;

import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.eqtests.basic.vpda.SimulatorEQOracle;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.oracles.SimulatorOracle;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.impl.Alphabets;
import net.automatalib.words.impl.DefaultVPDAlphabet;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Abstract integration test for VPDA learning algorithms.
 * <p>
 * //TODO: Integrate into existing IT landscape. (Needs refactoring, because VPDAs are no {@link
 * net.automatalib.automata.UniversalAutomaton})
 *
 * @author frohme
 */
public abstract class AbstractVPDALearnerIT {

    private static final Random RANDOM = new Random(123);

    private static final int LOC_COUNT = 100;
    private static final double ACCEPTANCE_PROB = 0.5;
    private static final double RETURN_PROB = 0.5;

    private static final Alphabet<Character> CALL_ALPHABET = Alphabets.fromArray('1', '2', '3');
    private static final Alphabet<Character> INTERNAL_ALPHABET = Alphabets.characters('a', 'f');
    private static final Alphabet<Character> RETURN_ALPHABET = Alphabets.fromArray('7', '8', '9');

    private static final VPDAlphabet<Character> VPD_ALPHABET =
            new DefaultVPDAlphabet<>(INTERNAL_ALPHABET, CALL_ALPHABET, RETURN_ALPHABET);

    @Test
    public void testRandomVPDA() {
        final OneSEVPA<?, Character> target =
                RandomAutomata.randomOneSEVPA(RANDOM, LOC_COUNT, VPD_ALPHABET, ACCEPTANCE_PROB, RETURN_PROB, true);
        checkAllVariants(target, VPD_ALPHABET);
    }

    private <I> void checkAllVariants(final OneSEVPA<?, I> target, final VPDAlphabet<I> alphabet) {

        final MembershipOracle<I, Boolean> mqOracle = new SimulatorOracle<>(target);
        final EquivalenceOracle<OneSEVPA<?, I>, I, Boolean> eqOracle = new SimulatorEQOracle<>(target, alphabet);
        final LearnerVariantListImpl.OneSEVPALearnerVariantListImpl<I> variants =
                new LearnerVariantListImpl.OneSEVPALearnerVariantListImpl<>();

        addLearnerVariants(alphabet, mqOracle, variants);

        for (LearnerVariant<OneSEVPA<?, I>, I, Boolean> v : variants.getLearnerVariants()) {
            final LearningAlgorithm<? extends OneSEVPA<?, I>, I, Boolean> learner = v.getLearner();

            learner.startLearning();
            DefaultQuery<I, Boolean> ceQuery;

            while ((ceQuery = eqOracle.findCounterExample(learner.getHypothesisModel(), alphabet)) != null) {
                boolean refined = learner.refineHypothesis(ceQuery);
                Assert.assertTrue(refined, "Real counterexample " + ceQuery.getInput() + " did not refine hypothesis");
            }

            Assert.assertTrue(Automata.testEquivalence(target, learner.getHypothesisModel(), alphabet),
                              "Final hypothesis does not match reference automaton");

        }
    }

    /**
     * Adds, for a given setup, all the variants of the OneSEVPA learner to be tested to the specified {@link
     * LearnerVariantList variant list}.
     *
     * @param alphabet
     *         the input alphabet
     * @param mqOracle
     *         the membership oracle
     * @param variants
     *         list to add the learner variants to
     */
    protected abstract <I> void addLearnerVariants(VPDAlphabet<I> alphabet,
                                                   MembershipOracle<I, Boolean> mqOracle,
                                                   LearnerVariantList.OneSEVPALearnerVariantList<I> variants);
}
