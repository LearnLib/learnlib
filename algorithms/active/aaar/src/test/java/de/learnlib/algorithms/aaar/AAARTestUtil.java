/* Copyright (C) 2013-2023 TU Dortmund
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
package de.learnlib.algorithms.aaar;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.learnlib.acex.analyzers.AcexAnalyzers;
import de.learnlib.algorithms.discriminationtree.dfa.DTLearnerDFA;
import de.learnlib.algorithms.discriminationtree.mealy.DTLearnerMealy;
import de.learnlib.algorithms.discriminationtree.moore.DTLearnerMoore;
import de.learnlib.algorithms.kv.dfa.KearnsVaziraniDFA;
import de.learnlib.algorithms.kv.mealy.KearnsVaziraniMealy;
import de.learnlib.algorithms.lstar.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstar.closing.ClosingStrategies;
import de.learnlib.algorithms.lstar.dfa.ClassicLStarDFA;
import de.learnlib.algorithms.lstar.mealy.ExtensibleLStarMealy;
import de.learnlib.algorithms.lstar.moore.ExtensibleLStarMoore;
import de.learnlib.algorithms.oml.ttt.dfa.OptimalTTTDFA;
import de.learnlib.algorithms.oml.ttt.mealy.OptimalTTTMealy;
import de.learnlib.algorithms.rivestschapire.RivestSchapireDFA;
import de.learnlib.algorithms.rivestschapire.RivestSchapireMealy;
import de.learnlib.algorithms.rivestschapire.RivestSchapireMoore;
import de.learnlib.algorithms.ttt.dfa.TTTLearnerDFA;
import de.learnlib.algorithms.ttt.mealy.TTTLearnerMealy;
import de.learnlib.algorithms.ttt.moore.TTTLearnerMoore;
import de.learnlib.api.algorithm.LearningAlgorithm.DFALearner;
import de.learnlib.api.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.api.algorithm.LearningAlgorithm.MooreLearner;
import de.learnlib.counterexamples.LocalSuffixFinders;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.Word;

public final class AAARTestUtil {

    private AAARTestUtil() {
        // prevent instantiation
    }

    public static <I> List<Pair<String, ComboConstructor<? extends DFALearner<I>, I, Boolean>>> getDFALearners() {

        final ComboConstructor<ClassicLStarDFA<I>, I, Boolean> lstar = ClassicLStarDFA::new;
        final ComboConstructor<RivestSchapireDFA<I>, I, Boolean> rs = RivestSchapireDFA::new;
        final ComboConstructor<KearnsVaziraniDFA<I>, I, Boolean> kv =
                (alph, mqo) -> new KearnsVaziraniDFA<>(alph, mqo, true, AcexAnalyzers.BINARY_SEARCH_FWD);
        final ComboConstructor<DTLearnerDFA<I>, I, Boolean> dt =
                (alph, mqo) -> new DTLearnerDFA<>(alph, mqo, LocalSuffixFinders.RIVEST_SCHAPIRE, true, true);
        final ComboConstructor<TTTLearnerDFA<I>, I, Boolean> ttt =
                (alph, mqo) -> new TTTLearnerDFA<>(alph, mqo, AcexAnalyzers.BINARY_SEARCH_FWD);
        final ComboConstructor<OptimalTTTDFA<I>, I, Boolean> oml = (alph, mqo) -> new OptimalTTTDFA<>(alph, mqo, mqo);

        return Arrays.asList(Pair.of("L*", lstar),
                             Pair.of("RS", rs),
                             Pair.of("KV", kv),
                             Pair.of("DT", dt),
                             Pair.of("TTT", ttt),
                             Pair.of("OML", oml));
    }

    public static <I, O> List<Pair<String, ComboConstructor<? extends MealyLearner<I, O>, I, Word<O>>>> getMealyLearners() {

        final ComboConstructor<ExtensibleLStarMealy<I, O>, I, Word<O>> lstar =
                (alph, mqo) -> new ExtensibleLStarMealy<>(alph,
                                                          mqo,
                                                          Collections.emptyList(),
                                                          ObservationTableCEXHandlers.CLASSIC_LSTAR,
                                                          ClosingStrategies.CLOSE_FIRST);
        final ComboConstructor<RivestSchapireMealy<I, O>, I, Word<O>> rs = RivestSchapireMealy::new;
        final ComboConstructor<KearnsVaziraniMealy<I, O>, I, Word<O>> kv =
                (alph, mqo) -> new KearnsVaziraniMealy<>(alph, mqo, true, AcexAnalyzers.BINARY_SEARCH_FWD);
        final ComboConstructor<DTLearnerMealy<I, O>, I, Word<O>> dt =
                (alph, mqo) -> new DTLearnerMealy<>(alph, mqo, LocalSuffixFinders.RIVEST_SCHAPIRE, true);
        final ComboConstructor<TTTLearnerMealy<I, O>, I, Word<O>> ttt =
                (alph, mqo) -> new TTTLearnerMealy<>(alph, mqo, AcexAnalyzers.BINARY_SEARCH_FWD);
        final ComboConstructor<OptimalTTTMealy<I, O>, I, Word<O>> oml =
                (alph, mqo) -> new OptimalTTTMealy<>(alph, mqo, mqo);

        return Arrays.asList(Pair.of("L*", lstar),
                             Pair.of("RS", rs),
                             Pair.of("KV", kv),
                             Pair.of("DT", dt),
                             Pair.of("TTT", ttt),
                             Pair.of("OML", oml));
    }

    public static <I, O> List<Pair<String, ComboConstructor<? extends MooreLearner<I, O>, I, Word<O>>>> getMooreLearners() {

        final ComboConstructor<ExtensibleLStarMoore<I, O>, I, Word<O>> lstar =
                (alph, mqo) -> new ExtensibleLStarMoore<>(alph,
                                                          mqo,
                                                          Collections.emptyList(),
                                                          ObservationTableCEXHandlers.CLASSIC_LSTAR,
                                                          ClosingStrategies.CLOSE_FIRST);
        final ComboConstructor<RivestSchapireMoore<I, O>, I, Word<O>> rs = RivestSchapireMoore::new;
        final ComboConstructor<DTLearnerMoore<I, O>, I, Word<O>> dt =
                (alph, mqo) -> new DTLearnerMoore<>(alph, mqo, LocalSuffixFinders.RIVEST_SCHAPIRE, true);
        final ComboConstructor<TTTLearnerMoore<I, O>, I, Word<O>> ttt =
                (alph, mqo) -> new TTTLearnerMoore<>(alph, mqo, AcexAnalyzers.BINARY_SEARCH_FWD);

        return Arrays.asList(Pair.of("L*", lstar), Pair.of("RS", rs), Pair.of("DT", dt), Pair.of("TTT", ttt));
    }

}
