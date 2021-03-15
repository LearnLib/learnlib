/* Copyright (C) 2013-2021 TU Dortmund
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
import de.learnlib.testsupport.it.learner.LearnerVariantList.SPALearnerVariantList;
import de.learnlib.testsupport.it.learner.LearnerVariantListImpl.SPALearnerVariantListImpl;
import net.automatalib.automata.spa.SPA;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.SPAAlphabet;
import net.automatalib.words.impl.Alphabets;
import net.automatalib.words.impl.DefaultSPAAlphabet;
import org.testng.annotations.Factory;

/**
 * Abstract integration test for VPDA learning algorithms.
 * <p>
 * //TODO: Integrate into existing IT landscape. (Needs refactoring, because SPAs are no {@link
 * net.automatalib.automata.UniversalAutomaton})
 *
 * @author frohme
 */
public abstract class AbstractSPALearnerIT {

    private static final Random RANDOM = new Random(69);
    private static final int SIZE = 10;

    private static final Alphabet<Character> CALL_ALPHABET = Alphabets.characters('A', 'F');
    private static final Alphabet<Character> INTERNAL_ALPHABET = Alphabets.characters('a', 'f');
    private static final SPAAlphabet<Character> SPA_ALPHABET =
            new DefaultSPAAlphabet<>(INTERNAL_ALPHABET, CALL_ALPHABET, 'R');

    @Factory
    public Object[] testRandomSPA() {
        final SPA<?, Character> target = RandomAutomata.randomSPA(RANDOM, SPA_ALPHABET, SIZE);

        return buildAllVariants(target, SPA_ALPHABET);
    }

    private <I> Object[] buildAllVariants(SPA<?, I> target, SPAAlphabet<I> alphabet) {

        final MembershipOracle<I, Boolean> mqOracle = new SimulatorOracle<>(target);
        final SPALearnerVariantListImpl<I> variants = new SPALearnerVariantListImpl<>();

        addLearnerVariants(alphabet, mqOracle, variants);

        final List<SPALearnerITCase<I>> result = new ArrayList<>();

        for (LearnerVariant<SPA<?, I>, I, Boolean> v : variants.getLearnerVariants()) {
            result.add(new SPALearnerITCase<>(v, target, alphabet));
        }

        return result.toArray();
    }

    /**
     * Adds, for a given setup, all the variants of the DFA learner to be tested to the specified {@link
     * LearnerVariantList variant list}.
     *
     * @param alphabet
     *         the input alphabet
     * @param mqOracle
     *         the membership oracle
     * @param variants
     *         list to add the learner variants to
     */
    protected abstract <I> void addLearnerVariants(SPAAlphabet<I> alphabet,
                                                   MembershipOracle<I, Boolean> mqOracle,
                                                   SPALearnerVariantList<I> variants);
}
