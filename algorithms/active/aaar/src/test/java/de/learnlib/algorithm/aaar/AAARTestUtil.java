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
package de.learnlib.algorithm.aaar;

import java.util.Arrays;
import java.util.List;

import de.learnlib.algorithm.LearningAlgorithm.DFALearner;
import de.learnlib.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.algorithm.LearningAlgorithm.MooreLearner;
import de.learnlib.algorithm.dhc.mealy.MealyDHC;
import de.learnlib.algorithm.kv.dfa.KearnsVaziraniDFA;
import de.learnlib.algorithm.kv.mealy.KearnsVaziraniMealy;
import de.learnlib.algorithm.lambda.ttt.dfa.TTTLambdaDFA;
import de.learnlib.algorithm.lambda.ttt.mealy.TTTLambdaMealy;
import de.learnlib.algorithm.lstar.dfa.ClassicLStarDFA;
import de.learnlib.algorithm.lstar.mealy.ExtensibleLStarMealy;
import de.learnlib.algorithm.lstar.moore.ExtensibleLStarMoore;
import de.learnlib.algorithm.observationpack.dfa.OPLearnerDFA;
import de.learnlib.algorithm.observationpack.mealy.OPLearnerMealy;
import de.learnlib.algorithm.observationpack.moore.OPLearnerMoore;
import de.learnlib.algorithm.rivestschapire.RivestSchapireDFA;
import de.learnlib.algorithm.rivestschapire.RivestSchapireMealy;
import de.learnlib.algorithm.rivestschapire.RivestSchapireMoore;
import de.learnlib.algorithm.sparse.SparseLearner;
import de.learnlib.algorithm.ttt.dfa.TTTLearnerDFA;
import de.learnlib.algorithm.ttt.mealy.TTTLearnerMealy;
import de.learnlib.algorithm.ttt.moore.TTTLearnerMoore;
import net.automatalib.common.util.Pair;
import net.automatalib.word.Word;

public final class AAARTestUtil {

    private AAARTestUtil() {
        // prevent instantiation
    }

    public static <I> List<Pair<String, ComboConstructor<? extends DFALearner<I>, I, Boolean>>> getDFALearners() {

        final ComboConstructor<ClassicLStarDFA<I>, I, Boolean> lstar = ClassicLStarDFA::new;
        final ComboConstructor<RivestSchapireDFA<I>, I, Boolean> rs = RivestSchapireDFA::new;
        final ComboConstructor<KearnsVaziraniDFA<I>, I, Boolean> kv = KearnsVaziraniDFA::new;
        final ComboConstructor<OPLearnerDFA<I>, I, Boolean> op = OPLearnerDFA::new;
        final ComboConstructor<TTTLearnerDFA<I>, I, Boolean> ttt = TTTLearnerDFA::new;
        final ComboConstructor<TTTLambdaDFA<I>, I, Boolean> lambda = TTTLambdaDFA::new;

        return Arrays.asList(Pair.of("L*", lstar),
                             Pair.of("RS", rs),
                             Pair.of("KV", kv),
                             Pair.of("OP", op),
                             Pair.of("TTT", ttt),
                             Pair.of("TTTLambda", lambda));
    }

    public static <I, O> List<Pair<String, ComboConstructor<? extends MealyLearner<I, O>, I, Word<O>>>> getMealyLearners() {

        final ComboConstructor<ExtensibleLStarMealy<I, O>, I, Word<O>> lstar = ExtensibleLStarMealy::new;
        final ComboConstructor<RivestSchapireMealy<I, O>, I, Word<O>> rs = RivestSchapireMealy::new;
        final ComboConstructor<KearnsVaziraniMealy<I, O>, I, Word<O>> kv = KearnsVaziraniMealy::new;
        final ComboConstructor<MealyDHC<I, O>, I, Word<O>> dhc = MealyDHC::new;
        final ComboConstructor<OPLearnerMealy<I, O>, I, Word<O>> op = OPLearnerMealy::new;
        final ComboConstructor<SparseLearner<I, O>, I, Word<O>> sparse = SparseLearner::new;
        final ComboConstructor<TTTLearnerMealy<I, O>, I, Word<O>> ttt = TTTLearnerMealy::new;
        final ComboConstructor<TTTLambdaMealy<I, O>, I, Word<O>> lambda = TTTLambdaMealy::new;

        return Arrays.asList(Pair.of("L*", lstar),
                             Pair.of("RS", rs),
                             Pair.of("KV", kv),
                             Pair.of("DHC", dhc),
                             Pair.of("OP", op),
                             Pair.of("Sparse", sparse),
                             Pair.of("TTT", ttt),
                             Pair.of("TTTLambda", lambda));
    }

    public static <I, O> List<Pair<String, ComboConstructor<? extends MooreLearner<I, O>, I, Word<O>>>> getMooreLearners() {

        final ComboConstructor<ExtensibleLStarMoore<I, O>, I, Word<O>> lstar = ExtensibleLStarMoore::new;
        final ComboConstructor<RivestSchapireMoore<I, O>, I, Word<O>> rs = RivestSchapireMoore::new;
        final ComboConstructor<OPLearnerMoore<I, O>, I, Word<O>> op = OPLearnerMoore::new;
        final ComboConstructor<TTTLearnerMoore<I, O>, I, Word<O>> ttt = TTTLearnerMoore::new;

        return Arrays.asList(Pair.of("L*", lstar), Pair.of("RS", rs), Pair.of("OP", op), Pair.of("TTT", ttt));
    }

}
