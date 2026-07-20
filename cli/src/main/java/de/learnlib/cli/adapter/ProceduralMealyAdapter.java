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
package de.learnlib.cli.adapter;

import de.learnlib.AccessSequenceTransformer;
import de.learnlib.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.algorithm.dhc.mealy.MealyDHC;
import de.learnlib.algorithm.kv.mealy.KearnsVaziraniMealy;
import de.learnlib.algorithm.lambda.lstar.LLambdaMealy;
import de.learnlib.algorithm.lambda.ttt.mealy.TTTLambdaMealy;
import de.learnlib.algorithm.lstar.mealy.ExtensibleLStarMealy;
import de.learnlib.algorithm.observationpack.mealy.OPLearnerMealy;
import de.learnlib.algorithm.sparse.SparseLearner;
import de.learnlib.algorithm.ttt.mealy.TTTLearnerMealy;
import de.learnlib.oracle.MembershipOracle;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.SupportsGrowingAlphabet;
import net.automatalib.word.Word;

public interface ProceduralMealyAdapter<I, O>
        extends MealyLearner<I, O>, SupportsGrowingAlphabet<I>, AccessSequenceTransformer<I> {

    final class MealyDHCAdapter<I, O> extends MealyDHC<I, O> implements ProceduralMealyAdapter<I, O> {

        public MealyDHCAdapter(Alphabet<I> alphabet, MembershipOracle<I, Word<O>> oracle) {
            super(alphabet, oracle);
        }
    }

    final class KearnsVaziraniMealyAdapter<I, O> extends KearnsVaziraniMealy<I, O>
            implements ProceduralMealyAdapter<I, O> {

        public KearnsVaziraniMealyAdapter(Alphabet<I> alphabet, MembershipOracle<I, Word<O>> oracle) {
            super(alphabet, oracle);
        }
    }

    final class LLambdaMealyAdapter<I, O> extends LLambdaMealy<I, O> implements ProceduralMealyAdapter<I, O> {

        public LLambdaMealyAdapter(Alphabet<I> alphabet, MembershipOracle<I, Word<O>> oracle) {
            super(alphabet, oracle);
        }
    }

    final class ExtensibleLStarMealyAdapter<I, O> extends ExtensibleLStarMealy<I, O>
            implements ProceduralMealyAdapter<I, O> {

        public ExtensibleLStarMealyAdapter(Alphabet<I> alphabet, MembershipOracle<I, Word<O>> oracle) {
            super(alphabet, oracle);
        }
    }

    final class SparseLearnerAdapter<I, O> extends SparseLearner<I, O> implements ProceduralMealyAdapter<I, O> {

        public SparseLearnerAdapter(Alphabet<I> alphabet, MembershipOracle<I, Word<O>> oracle) {
            super(alphabet, oracle);
        }
    }

    final class OPLearnerMealyAdapter<I, O> extends OPLearnerMealy<I, O> implements ProceduralMealyAdapter<I, O> {

        public OPLearnerMealyAdapter(Alphabet<I> alphabet, MembershipOracle<I, Word<O>> oracle) {
            super(alphabet, oracle);
        }
    }

    final class TTTLearnerMealyAdapter<I, O> extends TTTLearnerMealy<I, O> implements ProceduralMealyAdapter<I, O> {

        public TTTLearnerMealyAdapter(Alphabet<I> alphabet, MembershipOracle<I, Word<O>> oracle) {
            super(alphabet, oracle);
        }
    }

    final class TTTLambdaMealyAdapter<I, O> extends TTTLambdaMealy<I, O> implements ProceduralMealyAdapter<I, O> {

        public TTTLambdaMealyAdapter(Alphabet<I> alphabet, MembershipOracle<I, Word<O>> oracle) {
            super(alphabet, oracle);
        }
    }

}
