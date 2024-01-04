/* Copyright (C) 2013-2024 TU Dortmund University
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
package de.learnlib.algorithm.observationpack.moore;

import de.learnlib.algorithm.LearningAlgorithm.MooreLearner;
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
import net.automatalib.automaton.transducer.MooreMachine;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A {@link MooreMachine}-based specialization of the DT learner.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
public class OPLearnerMoore<I, O> extends AbstractOPLearner<MooreMachine<?, I, ?, O>, I, Word<O>, O, Void>
        implements MooreLearner<I, O> {

    @GenerateBuilder(defaults = AbstractOPLearner.BuilderDefaults.class)
    public OPLearnerMoore(Alphabet<I> alphabet,
                          MembershipOracle<I, Word<O>> oracle,
                          LocalSuffixFinder<? super I, ? super Word<O>> suffixFinder,
                          boolean repeatedCounterexampleEvaluation) {
        super(alphabet, oracle, suffixFinder, repeatedCounterexampleEvaluation, new MultiDTree<>(oracle));
    }

    @Override
    protected @Nullable Query<I, Word<O>> spQuery(HState<I, Word<O>, O, Void> state) {
        return new AbstractQuery<I, Word<O>>(state.getAccessSequence(), Word.epsilon()) {

            @Override
            public void answer(Word<O> output) {
                state.setProperty(output.firstSymbol());
            }
        };
    }

    @Override
    protected @Nullable Query<I, Word<O>> tpQuery(HTransition<I, Word<O>, O, Void> transition) {
        return null;
    }

    @Override
    public MooreMachine<?, I, ?, O> getHypothesisModel() {
        return new HypothesisWrapperMoore<>(getHypothesisDS());
    }
}
