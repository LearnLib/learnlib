/* Copyright (C) 2013-2026 TU Dortmund University
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

import java.util.Collection;
import java.util.Collections;
import java.util.Random;

import de.learnlib.AccessSequenceTransformer;
import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.query.DefaultQuery;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.automaton.concept.SuffixOutput;
import net.automatalib.common.util.mapping.Mapping;
import net.automatalib.util.automaton.Automata;
import net.automatalib.util.automaton.cover.Covers;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class that checks whether a learning algorithm which implements the {@link AccessSequenceTransformer} interface
 * returns the correct representatives.
 *
 * @param <L>
 *         learner type
 * @param <M>
 *         hypothesis type
 * @param <OR>
 *         oracle type
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 */
public abstract class AbstractLearnerASTTest<L extends AccessSequenceTransformer<I> & LearningAlgorithm<M, I, D>, M extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & SuffixOutput<I, D>, OR, I, D> {

    protected static final int RANDOM_SEED = 42;

    private M sul;

    protected L learner;

    private Alphabet<I> inputAlphabet;

    @BeforeClass
    public void setup() {
        this.inputAlphabet = getInitialAlphabet();
        this.sul = getSUL(inputAlphabet);

        final OR oracle = getOracle(sul);

        this.learner = getLearner(oracle, inputAlphabet);
    }

    protected abstract Alphabet<I> getInitialAlphabet();

    protected abstract M getSUL(Alphabet<I> alphabet);

    protected abstract OR getOracle(M sul);

    protected abstract L getLearner(OR oracle, Alphabet<I> alphabet);

    protected Collection<Word<I>> getTrueRepresentatives() {
        return Collections.emptyList();
    }

    @Test
    public void testExceptionBeforeStart() {

        final Random random = new Random(RANDOM_SEED);
        final int size = 5;
        final WordBuilder<I> wb = new WordBuilder<>(size);

        for (int i = 0; i < size; i++) {
            wb.append(inputAlphabet.getSymbol(random.nextInt(inputAlphabet.size())));
        }

        Assert.assertThrows(IllegalStateException.class, () -> learner.transformAccessSequence(wb.toWord()));
    }

    @Test(dependsOnMethods = "testExceptionBeforeStart")
    public void testCorrectTransformations() {
        learner.startLearning();

        M hyp = learner.getHypothesisModel();
        Word<I> ce = Automata.findSeparatingWord(sul, hyp, inputAlphabet);

        while (ce != null) {
            learner.refineHypothesis(new DefaultQuery<>(Word.epsilon(), ce, sul.computeOutput(ce)));
            hyp = learner.getHypothesisModel();
            ce = Automata.findSeparatingWord(sul, hyp, inputAlphabet);
        }

        checkStateMapping((UniversalDeterministicAutomaton<?, I, ?, ?, ?>) hyp);
    }

    private <S> void checkStateMapping(UniversalDeterministicAutomaton<S, I, ?, ?, ?> hyp) {

        final Mapping<S, @Nullable Word<I>> mapping = Covers.cover(hyp, inputAlphabet, hyp.getInitialState(), t -> {}, s -> {});

        // check that transformed sequences reach the same state
        for (S s : hyp) {
            final Word<I> cover = mapping.get(s);
            Assert.assertNotNull(cover);

            final Word<I> as = learner.transformAccessSequence(cover);
            Assert.assertNotNull(as);

            final S reached = hyp.getState(as);
            Assert.assertNotNull(reached);

            Assert.assertEquals(s, reached);
        }

        // check that results are actually the correct representatives
        // note that sub-classes may return an empty mapping, if the true values cannot be extracted faithfully
        for (Word<I> rep : getTrueRepresentatives()) {
            Assert.assertEquals(rep, learner.transformAccessSequence(rep), rep.toString());
        }
    }
}
