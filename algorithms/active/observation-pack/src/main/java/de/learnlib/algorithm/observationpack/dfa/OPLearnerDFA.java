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
package de.learnlib.algorithm.observationpack.dfa;

import de.learnlib.algorithm.LearningAlgorithm.DFALearner;
import de.learnlib.algorithm.observationpack.AbstractOPLearner;
import de.learnlib.algorithm.observationpack.hypothesis.HState;
import de.learnlib.algorithm.observationpack.hypothesis.HTransition;
import de.learnlib.counterexample.LocalSuffixFinder;
import de.learnlib.counterexample.LocalSuffixFinders;
import de.learnlib.datastructure.discriminationtree.BinaryDTree;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.AbstractQuery;
import de.learnlib.query.Query;
import de.learnlib.tooling.annotation.builder.GenerateBuilder;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A {@link DFA}-based specialization of the {@link AbstractOPLearner}.
 *
 * @param <I>
 *         input symbol type
 */
public class OPLearnerDFA<I> extends AbstractOPLearner<DFA<?, I>, I, Boolean, Boolean, Void> implements DFALearner<I> {

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
     * @param epsilonRoot
     *         whether to ensure the root of the discrimination tree is always labeled using the empty word.
     */
    @GenerateBuilder(defaults = BuilderDefaults.class)
    public OPLearnerDFA(Alphabet<I> alphabet,
                        MembershipOracle<I, Boolean> oracle,
                        LocalSuffixFinder<? super I, ? super Boolean> suffixFinder,
                        boolean repeatedCounterexampleEvaluation,
                        boolean epsilonRoot) {
        super(alphabet, oracle, suffixFinder, repeatedCounterexampleEvaluation, new BinaryDTree<>(oracle, epsilonRoot));
    }

    @Override
    public DFA<?, I> getHypothesisModel() {
        return new HypothesisWrapperDFA<>(getHypothesisDS());
    }

    @Override
    protected Query<I, Boolean> spQuery(HState<I, Boolean, Boolean, Void> state) {
        return new AbstractQuery<I, Boolean>(state.getAccessSequence(), Word.epsilon()) {

            @Override
            public void answer(Boolean val) {
                state.setProperty(val);
            }
        };
    }

    @Override
    protected @Nullable Query<I, Boolean> tpQuery(HTransition<I, Boolean, Boolean, Void> transition) {
        return null;
    }

    static final class BuilderDefaults {

        private BuilderDefaults() {
            // prevent instantiation
        }

        public static boolean epsilonRoot() {
            return true;
        }

        public static <I, O> LocalSuffixFinder<? super I, ? super O> suffixFinder() {
            return LocalSuffixFinders.RIVEST_SCHAPIRE;
        }

        public static boolean repeatedCounterexampleEvaluation() {
            return true;
        }
    }

}
