/* Copyright (C) 2013-2018 TU Dortmund
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.oracle.membership.SimulatorOracle;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.impl.Alphabets;
import net.automatalib.words.impl.DefaultVPDAlphabet;
import org.testng.annotations.Factory;

/**
 * Abstract integration test for VPDA learning algorithms.
 * <p>
 * //TODO: Integrate into existing IT landscape. (Needs refactoring, because VPDAs are no {@link
 * net.automatalib.automata.UniversalAutomaton})
 *
 * @author frohme
 */
public abstract class AbstractVPDALearnerIT {

    private static final Random RANDOM = new Random(42);

    private static final int LOC_COUNT = 80;
    private static final double ACCEPTANCE_PROB = 0.3;
    private static final double RETURN_PROB = 0.3;

    private static final Alphabet<Character> CALL_ALPHABET = Alphabets.fromArray('1', '2', '3');
    private static final Alphabet<Character> INTERNAL_ALPHABET = Alphabets.characters('a', 'f');
    private static final Alphabet<Character> RETURN_ALPHABET = Alphabets.fromArray('7', '8', '9');

    private static final VPDAlphabet<Character> VPD_ALPHABET =
            new DefaultVPDAlphabet<>(INTERNAL_ALPHABET, CALL_ALPHABET, RETURN_ALPHABET);

    @Factory
    public Object[] testRandomVPDA() {
        final OneSEVPA<?, Character> target =
                RandomAutomata.randomOneSEVPA(RANDOM, LOC_COUNT, VPD_ALPHABET, ACCEPTANCE_PROB, RETURN_PROB, true);

        return buildAllVariants(target, VPD_ALPHABET);
    }

    private <I> Object[] buildAllVariants(final OneSEVPA<?, I> target, final VPDAlphabet<I> alphabet) {

        final MembershipOracle<I, Boolean> mqOracle = new SimulatorOracle<>(target);
        final LearnerVariantListImpl.OneSEVPALearnerVariantListImpl<I> variants =
                new LearnerVariantListImpl.OneSEVPALearnerVariantListImpl<>();

        addLearnerVariants(alphabet, mqOracle, variants);

        final List<VPDALearnerITCase<I>> result = new ArrayList<>();

        for (LearnerVariant<OneSEVPA<?, I>, I, Boolean> v : variants.getLearnerVariants()) {
            result.add(new VPDALearnerITCase<>(v, target, alphabet));
        }

        return result.toArray();
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
