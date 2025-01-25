/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of LearnLib <https://learnlib.de>.
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
package de.learnlib.testsupport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.query.DefaultQuery;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.GrowingAlphabet;
import net.automatalib.alphabet.SupportsGrowingAlphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.automaton.concept.Output;
import net.automatalib.exception.GrowingAlphabetNotSupportedException;
import net.automatalib.util.automaton.Automata;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Simple (abstract super) test class that checks the basic workflow of a learning algorithm that implements {@link
 * SupportsGrowingAlphabet}.
 */
public abstract class AbstractGrowingAlphabetTest<L extends SupportsGrowingAlphabet<I> & LearningAlgorithm<M, I, D>, M extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & Output<I, D>, OR, I, D> {

    protected static final int RANDOM_SEED = 42;
    protected static final int DEFAULT_AUTOMATON_SIZE = 15;

    private M target;

    private OR oracle;

    private Alphabet<I> initialAlphabet;

    private List<I> alphabetExtensions;

    @BeforeClass
    public void setup() {
        initialAlphabet = getInitialAlphabet();
        alphabetExtensions = new ArrayList<>(getAlphabetExtensions());

        assert alphabetExtensions.size() > 2 : "At least 3 symbols need to be added for proper coverage";

        final List<I> compoundAlphabet = new ArrayList<>(initialAlphabet.size() + alphabetExtensions.size());
        compoundAlphabet.addAll(initialAlphabet);
        compoundAlphabet.addAll(alphabetExtensions);

        target = getTarget(Alphabets.fromList(compoundAlphabet));
        oracle = getOracle(target);
    }

    protected abstract Alphabet<I> getInitialAlphabet();

    protected abstract Collection<I> getAlphabetExtensions();

    protected abstract M getTarget(Alphabet<I> alphabet);

    protected abstract OR getOracle(M target);

    protected abstract L getLearner(OR oracle, Alphabet<I> alphabet);

    @Test(expectedExceptions = GrowingAlphabetNotSupportedException.class)
    public void testInitialAlphabet() {
        final L leaner = getLearner(oracle, initialAlphabet);

        testAlphabet(initialAlphabet, leaner);
    }

    /**
     * In case of passing a growing alphabet, the learners may use the existing {@link
     * GrowingAlphabet#addSymbol(Object)} functionality. Due to references, this may alter their
     * behavior. Check it!
     */
    @Test
    public void testGrowingAlphabet() {
        final GrowingAlphabet<I> alphabet = new GrowingMapAlphabet<>(initialAlphabet);
        final L learner = getLearner(oracle, alphabet);

        testAlphabet(alphabet, learner);
    }

    private void testAlphabet(Alphabet<I> alphabet, L learner) {

        // add the first symbol before actually starting the learning process
        learner.addAlphabetSymbol(alphabetExtensions.get(0));

        learner.startLearning();
        this.performLearnLoopAndCheck(learner, alphabet);

        final List<I> currentAlphabet = new ArrayList<>(alphabet.size() + alphabetExtensions.size());
        currentAlphabet.addAll(alphabet);

        boolean duplicateAdd = false;

        for (I i : alphabetExtensions) {
            currentAlphabet.add(i);
            learner.addAlphabetSymbol(i);

            if (duplicateAdd) {
                learner.addAlphabetSymbol(i);
            }
            // add every second symbol twice
            duplicateAdd = !duplicateAdd;

            final UniversalDeterministicAutomaton<?, I, ?, ?, ?> hyp = learner.getHypothesisModel();

            this.checkCompletenessOfHypothesis(hyp, currentAlphabet);
            this.performLearnLoopAndCheck(learner, currentAlphabet);
        }

    }

    private void performLearnLoopAndCheck(L learner, Collection<? extends I> effectiveAlphabet) {

        M hyp = learner.getHypothesisModel();
        Word<I> sepWord = Automata.findSeparatingWord(target, hyp, effectiveAlphabet);

        while (sepWord != null) {
            final DefaultQuery<I, D> ce = new DefaultQuery<>(sepWord, target.computeOutput(sepWord));

            while (learner.refineHypothesis(ce)) {
                // learn till the end
            }

            hyp = learner.getHypothesisModel();
            sepWord = Automata.findSeparatingWord(target, hyp, effectiveAlphabet);
        }

        Assert.assertTrue(Automata.testEquivalence(target, hyp, effectiveAlphabet));
    }

    private <S, T> void checkCompletenessOfHypothesis(UniversalDeterministicAutomaton<S, I, T, ?, ?> hypothesis,
                                                      Collection<? extends I> alphabet) {
        for (S s : hypothesis.getStates()) {
            for (I i : alphabet) {
                final T trans = hypothesis.getTransition(s, i);

                Assert.assertNotNull(trans);
                Assert.assertNotNull(hypothesis.getSuccessor(trans));
            }
        }
    }

}
