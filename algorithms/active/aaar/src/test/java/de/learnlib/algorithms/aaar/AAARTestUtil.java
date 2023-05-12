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
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.MooreMachine;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.Word;

/**
 * @author frohme
 */
public final class AAARTestUtil {

    private AAARTestUtil() {
        // prevent instantiation
    }

    public static <I> List<Pair<String, LearnerProvider<? extends DFALearner<I>, DFA<?, I>, I, Boolean>>> getDFALearners() {

        final LearnerProvider<ClassicLStarDFA<I>, DFA<?, I>, I, Boolean> lstar = ClassicLStarDFA::new;
        final LearnerProvider<RivestSchapireDFA<I>, DFA<?, I>, I, Boolean> rs = RivestSchapireDFA::new;
        final LearnerProvider<KearnsVaziraniDFA<I>, DFA<?, I>, I, Boolean> kv =
                (alph, mqo) -> new KearnsVaziraniDFA<>(alph, mqo, true, AcexAnalyzers.BINARY_SEARCH_FWD);
        final LearnerProvider<DTLearnerDFA<I>, DFA<?, I>, I, Boolean> dt =
                (alph, mqo) -> new DTLearnerDFA<>(alph, mqo, LocalSuffixFinders.RIVEST_SCHAPIRE, true, true);
        final LearnerProvider<TTTLearnerDFA<I>, DFA<?, I>, I, Boolean> ttt =
                (alph, mqo) -> new TTTLearnerDFA<>(alph, mqo, AcexAnalyzers.BINARY_SEARCH_FWD);
        final LearnerProvider<OptimalTTTDFA<I>, DFA<?, I>, I, Boolean> oml =
                (alph, mqo) -> new OptimalTTTDFA<>(alph, mqo, mqo);

        return Arrays.asList(Pair.of("L*", lstar),
                             Pair.of("RS", rs),
                             Pair.of("KV", kv),
                             Pair.of("DT", dt),
                             Pair.of("TTT", ttt),
                             Pair.of("OML", oml));
    }

    public static <I, O> List<Pair<String, LearnerProvider<? extends MealyLearner<I, O>, MealyMachine<?, I, ?, O>, I, Word<O>>>> getMealyLearners() {

        final LearnerProvider<ExtensibleLStarMealy<I, O>, MealyMachine<?, I, ?, O>, I, Word<O>> lstar =
                (alph, mqo) -> new ExtensibleLStarMealy<>(alph,
                                                          mqo,
                                                          Collections.emptyList(),
                                                          ObservationTableCEXHandlers.CLASSIC_LSTAR,
                                                          ClosingStrategies.CLOSE_FIRST);
        final LearnerProvider<RivestSchapireMealy<I, O>, MealyMachine<?, I, ?, O>, I, Word<O>> rs =
                RivestSchapireMealy::new;
        final LearnerProvider<KearnsVaziraniMealy<I, O>, MealyMachine<?, I, ?, O>, I, Word<O>> kv =
                (alph, mqo) -> new KearnsVaziraniMealy<>(alph, mqo, true, AcexAnalyzers.BINARY_SEARCH_FWD);
        final LearnerProvider<DTLearnerMealy<I, O>, MealyMachine<?, I, ?, O>, I, Word<O>> dt =
                (alph, mqo) -> new DTLearnerMealy<>(alph, mqo, LocalSuffixFinders.RIVEST_SCHAPIRE, true);
        final LearnerProvider<TTTLearnerMealy<I, O>, MealyMachine<?, I, ?, O>, I, Word<O>> ttt =
                (alph, mqo) -> new TTTLearnerMealy<>(alph, mqo, AcexAnalyzers.BINARY_SEARCH_FWD);
        final LearnerProvider<OptimalTTTMealy<I, O>, MealyMachine<?, I, ?, O>, I, Word<O>> oml =
                (alph, mqo) -> new OptimalTTTMealy<>(alph, mqo, mqo);

        return Arrays.asList(Pair.of("L*", lstar),
                             Pair.of("RS", rs),
                             Pair.of("KV", kv),
                             Pair.of("DT", dt),
                             Pair.of("TTT", ttt),
                             Pair.of("OML", oml));
    }

    public static <I, O> List<Pair<String, LearnerProvider<? extends MooreLearner<I, O>, MooreMachine<?, I, ?, O>, I, Word<O>>>> getMooreLearners() {

        final LearnerProvider<ExtensibleLStarMoore<I, O>, MooreMachine<?, I, ?, O>, I, Word<O>> lstar =
                (alph, mqo) -> new ExtensibleLStarMoore<>(alph,
                                                          mqo,
                                                          Collections.emptyList(),
                                                          ObservationTableCEXHandlers.CLASSIC_LSTAR,
                                                          ClosingStrategies.CLOSE_FIRST);
        final LearnerProvider<RivestSchapireMoore<I, O>, MooreMachine<?, I, ?, O>, I, Word<O>> rs =
                RivestSchapireMoore::new;
        final LearnerProvider<DTLearnerMoore<I, O>, MooreMachine<?, I, ?, O>, I, Word<O>> dt =
                (alph, mqo) -> new DTLearnerMoore<>(alph, mqo, LocalSuffixFinders.RIVEST_SCHAPIRE, true);
        final LearnerProvider<TTTLearnerMoore<I, O>, MooreMachine<?, I, ?, O>, I, Word<O>> ttt =
                (alph, mqo) -> new TTTLearnerMoore<>(alph, mqo, AcexAnalyzers.BINARY_SEARCH_FWD);

        return Arrays.asList(Pair.of("L*", lstar), Pair.of("RS", rs), Pair.of("DT", dt), Pair.of("TTT", ttt));
    }

}
