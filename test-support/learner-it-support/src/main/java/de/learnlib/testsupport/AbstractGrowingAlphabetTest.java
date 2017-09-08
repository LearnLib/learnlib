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
package de.learnlib.testsupport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.algorithm.feature.SupportsGrowingAlphabet;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.Output;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import net.automatalib.words.impl.Alphabets;
import net.automatalib.words.impl.SimpleAlphabet;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Simple (abstract super) test class that checks the basic workflow of a learning algorithm that implements {@link
 * SupportsGrowingAlphabet}.
 *
 * @author frohme
 */
public abstract class AbstractGrowingAlphabetTest<L extends SupportsGrowingAlphabet<I> & LearningAlgorithm<M, I, D>, M extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & Output<I, D>, OR, I, D> {

    private static final int MAX_VERIFICATION_TRIES = 100;
    protected static final int RANDOM_SEED = 42;
    protected static final int DEFAULT_AUTOMATON_SIZE = 15;

    private Random random;

    private M target;

    private L learner;

    private Alphabet<I> initialAlphabet;

    private Collection<I> alphabetExtensions;

    @BeforeClass
    public void setup() {
        random = new Random(RANDOM_SEED);

        initialAlphabet = getInitialAlphabet();
        alphabetExtensions = getAlphabetExtensions();

        final List<I> compoundAlphabet = new ArrayList<>(initialAlphabet.size() + alphabetExtensions.size());
        compoundAlphabet.addAll(initialAlphabet);
        compoundAlphabet.addAll(alphabetExtensions);

        target = getTarget(Alphabets.fromList(compoundAlphabet));
        final OR oracle = getOracle(target);

        learner = getLearner(oracle, initialAlphabet);
    }

    protected abstract Alphabet<I> getInitialAlphabet();

    protected abstract Collection<I> getAlphabetExtensions();

    protected abstract M getTarget(Alphabet<I> alphabet);

    protected abstract OR getOracle(M target);

    protected abstract L getLearner(OR oracle, Alphabet<I> alphabet);

    @Test
    public void testInitialAlphabet() {
        learner.startLearning();
        this.performLearnLoop(initialAlphabet);
    }

    private void performLearnLoop(final Alphabet<I> effectiveAlphabet) {

        M hyp = learner.getHypothesisModel();
        Word<I> sepWord = Automata.findSeparatingWord(target, hyp, effectiveAlphabet);

        while (sepWord != null) {
            final DefaultQuery<I, D> ce = new DefaultQuery<>(sepWord, target.computeOutput(sepWord));

            while (learner.refineHypothesis(ce)) {
            }

            hyp = learner.getHypothesisModel();
            sepWord = Automata.findSeparatingWord(target, hyp, effectiveAlphabet);
        }

        Assert.assertTrue(Automata.testEquivalence(target, hyp, effectiveAlphabet));
    }

    @Test(dependsOnMethods = "testInitialAlphabet")
    public void testAddingAlphabetSymbols() {
        final Alphabet<I> currentAlphabet = new SimpleAlphabet<>(initialAlphabet);

        for (final I i : alphabetExtensions) {
            currentAlphabet.add(i);
            learner.addAlphabetSymbol(i);

            this.checkCompletenessOfHypothesis(currentAlphabet);
            this.performLearnLoop(currentAlphabet);
        }
    }

    private void checkCompletenessOfHypothesis(final Alphabet<I> alphabet) {

        final int alphabetSize = alphabet.size();
        final M hypothesis = learner.getHypothesisModel();

        for (int i = 0; i < MAX_VERIFICATION_TRIES; i++) {
            final WordBuilder<I> testTraceBuilder = new WordBuilder<>(i);

            for (int j = 0; j < i; j++) {
                testTraceBuilder.add(alphabet.getSymbol(random.nextInt(alphabetSize)));
            }

            // simply try to compute output without an exception
            hypothesis.computeOutput(testTraceBuilder.toWord());
        }
    }

}
