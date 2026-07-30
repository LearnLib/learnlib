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
import de.learnlib.algorithm.LearningAlgorithm.DFALearner;
import de.learnlib.algorithm.kv.dfa.KearnsVaziraniDFA;
import de.learnlib.algorithm.lambda.lstar.LLambdaDFA;
import de.learnlib.algorithm.lambda.ttt.dfa.TTTLambdaDFA;
import de.learnlib.algorithm.lstar.dfa.ExtensibleLStarDFA;
import de.learnlib.algorithm.malerpnueli.MalerPnueliDFA;
import de.learnlib.algorithm.observationpack.dfa.OPLearnerDFA;
import de.learnlib.algorithm.rivestschapire.RivestSchapireDFA;
import de.learnlib.algorithm.ttt.dfa.TTTLearnerDFA;
import de.learnlib.oracle.MembershipOracle;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.SupportsGrowingAlphabet;

public interface ProceduralDFAAdapter<I>
        extends DFALearner<I>, SupportsGrowingAlphabet<I>, AccessSequenceTransformer<I> {

    final class KearnsVaziraniDFAAdapter<I> extends KearnsVaziraniDFA<I> implements ProceduralDFAAdapter<I> {

        public KearnsVaziraniDFAAdapter(Alphabet<I> alphabet, MembershipOracle<I, Boolean> oracle) {
            super(alphabet, oracle);
        }
    }

    final class LLambdaDFAAdapter<I> extends LLambdaDFA<I> implements ProceduralDFAAdapter<I> {

        public LLambdaDFAAdapter(Alphabet<I> alphabet, MembershipOracle<I, Boolean> oracle) {
            super(alphabet, oracle);
        }
    }

    final class ExtensibleLStarDFAAdapter<I> extends ExtensibleLStarDFA<I> implements ProceduralDFAAdapter<I> {

        public ExtensibleLStarDFAAdapter(Alphabet<I> alphabet, MembershipOracle<I, Boolean> oracle) {
            super(alphabet, oracle);
        }
    }

    final class MalerPnueliDFAAdapter<I> extends MalerPnueliDFA<I> implements ProceduralDFAAdapter<I> {

        public MalerPnueliDFAAdapter(Alphabet<I> alphabet, MembershipOracle<I, Boolean> oracle) {
            super(alphabet, oracle);
        }
    }

    final class OPLearnerDFAAdapter<I> extends OPLearnerDFA<I> implements ProceduralDFAAdapter<I> {

        public OPLearnerDFAAdapter(Alphabet<I> alphabet, MembershipOracle<I, Boolean> oracle) {
            super(alphabet, oracle);
        }
    }

    final class RivestSchapireDFAAdapter<I> extends RivestSchapireDFA<I> implements ProceduralDFAAdapter<I> {

        public RivestSchapireDFAAdapter(Alphabet<I> alphabet, MembershipOracle<I, Boolean> oracle) {
            super(alphabet, oracle);
        }
    }

    final class TTTLearnerDFAAdapter<I> extends TTTLearnerDFA<I> implements ProceduralDFAAdapter<I> {

        public TTTLearnerDFAAdapter(Alphabet<I> alphabet, MembershipOracle<I, Boolean> oracle) {
            super(alphabet, oracle);
        }
    }

    final class TTTLambdaDFAAdapter<I> extends TTTLambdaDFA<I> implements ProceduralDFAAdapter<I> {

        public TTTLambdaDFAAdapter(Alphabet<I> alphabet, MembershipOracle<I, Boolean> oracle) {
            super(alphabet, oracle);
        }
    }

}
