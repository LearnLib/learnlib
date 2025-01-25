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
package de.learnlib.algorithm.observationpack.mealy;

import de.learnlib.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.algorithm.observationpack.AbstractOPLearner;
import de.learnlib.algorithm.observationpack.hypothesis.HState;
import de.learnlib.algorithm.observationpack.hypothesis.HTransition;
import de.learnlib.counterexample.LocalSuffixFinder;
import de.learnlib.datastructure.discriminationtree.MultiDTree;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.AbstractQuery;
import de.learnlib.query.Query;
import de.learnlib.tooling.annotation.builder.GenerateBuilder;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A {@link MealyMachine}-based specialization of the {@link AbstractOPLearner}.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
public class OPLearnerMealy<I, O> extends AbstractOPLearner<MealyMachine<?, I, ?, O>, I, Word<O>, Void, O>
        implements MealyLearner<I, O> {

    /**
     * Constructor.
     *
     * @param alphabet
     *         the input alphabet
     * @param oracle
     *         the membership oracle
     * @param suffixFinder
     *         method to use for analyzing counterexamples
     * @param repeatedCounterexampleEvaluation
     *         a flag whether counterexamples should be analyzed exhaustively
     */
    @GenerateBuilder(defaults = AbstractOPLearner.BuilderDefaults.class)
    public OPLearnerMealy(Alphabet<I> alphabet,
                          MembershipOracle<I, Word<O>> oracle,
                          LocalSuffixFinder<? super I, ? super Word<O>> suffixFinder,
                          boolean repeatedCounterexampleEvaluation) {
        super(alphabet, oracle, suffixFinder, repeatedCounterexampleEvaluation, new MultiDTree<>(oracle));
    }

    @Override
    public MealyMachine<?, I, ?, O> getHypothesisModel() {
        return new HypothesisWrapperMealy<>(getHypothesisDS());
    }

    @Override
    protected @Nullable Query<I, Word<O>> spQuery(HState<I, Word<O>, Void, O> state) {
        return null;
    }

    @Override
    protected Query<I, Word<O>> tpQuery(HTransition<I, Word<O>, Void, O> transition) {
        return new AbstractQuery<I, Word<O>>(transition.getSource().getAccessSequence(),
                                             Word.fromLetter(transition.getSymbol())) {

            @Override
            public void answer(Word<O> output) {
                transition.setProperty(output.firstSymbol());
            }
        };
    }

}
