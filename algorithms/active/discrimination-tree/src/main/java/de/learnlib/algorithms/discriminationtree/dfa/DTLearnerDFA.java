/* Copyright (C) 2013-2022 TU Dortmund
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
package de.learnlib.algorithms.discriminationtree.dfa;

import com.github.misberner.buildergen.annotations.GenerateBuilder;
import de.learnlib.algorithms.discriminationtree.AbstractDTLearner;
import de.learnlib.algorithms.discriminationtree.hypothesis.HState;
import de.learnlib.algorithms.discriminationtree.hypothesis.HTransition;
import de.learnlib.api.algorithm.LearningAlgorithm.DFALearner;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.AbstractQuery;
import de.learnlib.api.query.Query;
import de.learnlib.counterexamples.LocalSuffixFinder;
import de.learnlib.counterexamples.LocalSuffixFinders;
import de.learnlib.datastructure.discriminationtree.BinaryDTree;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Algorithm for learning DFA using the Discrimination Tree algorithm.
 *
 * @param <I>
 *         input symbol class
 *
 * @author Malte Isberner
 */
public class DTLearnerDFA<I> extends AbstractDTLearner<DFA<?, I>, I, Boolean, Boolean, Void> implements DFALearner<I> {

    /**
     * Constructor.
     *
     * @param alphabet
     *         the input alphabet
     * @param oracle
     *         the membership oracle
     * @param suffixFinder
     *         method to use for analyzing counterexamples
     * @param epsilonRoot
     *         whether or not to ensure the root of the discrimination tree is always labeled using the empty word.
     */
    @GenerateBuilder
    public DTLearnerDFA(Alphabet<I> alphabet,
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
    protected Query<I, Boolean> spQuery(final HState<I, Boolean, Boolean, Void> state) {
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

    public static final class BuilderDefaults {

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
