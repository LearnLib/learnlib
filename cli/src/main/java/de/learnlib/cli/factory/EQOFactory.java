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
package de.learnlib.cli.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

import de.learnlib.cli.option.EQOracle;
import de.learnlib.cli.option.Options;
import de.learnlib.oracle.AdaptiveMembershipOracle;
import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.equivalence.EQOracleChain;
import de.learnlib.oracle.equivalence.KWayStateCoverEQOracle;
import de.learnlib.oracle.equivalence.KWayTransitionCoverEQOracle;
import de.learnlib.oracle.equivalence.RandomWMethodEQOracle;
import de.learnlib.oracle.equivalence.RandomWordsEQOracle;
import de.learnlib.oracle.equivalence.RandomWpMethodEQOracle;
import de.learnlib.oracle.equivalence.SampleSetEQOracle;
import de.learnlib.oracle.equivalence.WMethodEQOracle;
import de.learnlib.oracle.equivalence.WpMethodEQOracle;
import de.learnlib.oracle.equivalence.vpa.RandomWellMatchedWordsEQOracle;
import de.learnlib.util.mealy.Adaptive2MembershipWrapper;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.UniversalDeterministicAutomaton.RegularAutomaton;
import net.automatalib.automaton.concept.SuffixOutput;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.NFA;
import net.automatalib.automaton.procedural.SBA;
import net.automatalib.automaton.procedural.SPA;
import net.automatalib.automaton.procedural.SPMM;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.vpa.OneSEVPA;
import net.automatalib.util.automaton.fsa.NFAs;
import net.automatalib.word.Word;

@FunctionalInterface
public interface EQOFactory<M, I, D, OR> extends BiFunction<Options, OR, EquivalenceOracle<M, I, D>> {

    int BATCH_SIZE = 10;

    EQOFactory<DFA<?, String>, String, Boolean, MembershipOracle<String, Boolean>> DFA_ORACLES =
            EQOFactory::getRegularOracles;
    EQOFactory<MealyMachine<?, String, ?, String>, String, Word<String>, MembershipOracle<String, Word<String>>>
            MEALY_ORACLES = EQOFactory::getRegularOracles;
    EQOFactory<MealyMachine<?, String, ?, String>, String, Word<String>, AdaptiveMembershipOracle<String, String>>
            ADAPTIVE_ORACLES = EQOFactory::getAdaptiveOracles;
    EQOFactory<NFA<?, String>, String, Boolean, MembershipOracle<String, Boolean>> NFA_ORACLES =
            EQOFactory::getNFAOracles;
    EQOFactory<SBA<?, String>, String, Boolean, MembershipOracle<String, Boolean>> SBA_ORACLES =
            EQOFactory::getSBAOracles;
    EQOFactory<SPA<?, String>, String, Boolean, MembershipOracle<String, Boolean>> SPA_ORACLES =
            EQOFactory::getSPAOracles;
    EQOFactory<SPMM<?, String, ?, String>, String, Word<String>, MembershipOracle<String, Word<String>>> SPMM_ORACLES =
            EQOFactory::getSPMMOracles;
    EQOFactory<OneSEVPA<?, String>, String, Boolean, MembershipOracle<String, Boolean>> VPA_ORACLES =
            EQOFactory::getVPAOracles;

    private static <M extends RegularAutomaton<?, String, ?, ?, ?> & SuffixOutput<String, D>, D> EQOracleChain<M, String, D> getRegularOracles(
            Options options,
            MembershipOracle<String, D> mqo) {
        final EQOracleChain<M, String, D> chain = new EQOracleChain<>();

        for (EQOracle e : options.eqos) {
            EquivalenceOracle<? super M, String, D> eqo = switch (e) {
                case W -> new WMethodEQOracle<>(mqo,
                                                options.eqoParams.wMethod.lookahead,
                                                options.eqoParams.wMethod.expectedSize,
                                                computeBatchSize(options));
                case WP -> new WpMethodEQOracle<>(mqo,
                                                  options.eqoParams.wpMethod.lookahead,
                                                  options.eqoParams.wpMethod.expectedSize,
                                                  computeBatchSize(options));
                case RANDOM -> new RandomWordsEQOracle<>(mqo,
                                                         options.eqoParams.random.minLength,
                                                         options.eqoParams.random.maxLength,
                                                         options.eqoParams.random.maxTests,
                                                         new Random(options.eqoParams.random.seed),
                                                         computeBatchSize(options));
                case RANDOM_W -> new RandomWMethodEQOracle<>(mqo,
                                                             options.eqoParams.randomWMethod.minimalSize,
                                                             options.eqoParams.randomWMethod.rndLength,
                                                             options.eqoParams.randomWMethod.bound,
                                                             new Random(options.eqoParams.randomWMethod.seed),
                                                             computeBatchSize(options));
                case RANDOM_WP -> new RandomWpMethodEQOracle<>(mqo,
                                                               options.eqoParams.randomWpMethod.minimalSize,
                                                               options.eqoParams.randomWpMethod.rndLength,
                                                               options.eqoParams.randomWpMethod.bound,
                                                               new Random(options.eqoParams.randomWpMethod.seed),
                                                               computeBatchSize(options));
                case KWAY_S -> new KWayStateCoverEQOracle<>(mqo,
                                                            new Random(options.eqoParams.kWayState.seed),
                                                            options.eqoParams.kWayState.randomWalkLen,
                                                            options.eqoParams.kWayState.k,
                                                            options.eqoParams.kWayState.combinationMethod,
                                                            computeBatchSize(options));
                case KWAY_T -> new KWayTransitionCoverEQOracle<>(mqo,
                                                                 new Random(options.eqoParams.kWayTransition.seed),
                                                                 options.eqoParams.kWayTransition.randomWalkLen,
                                                                 options.eqoParams.kWayTransition.numGeneratePaths,
                                                                 options.eqoParams.kWayTransition.maxPathLen,
                                                                 options.eqoParams.kWayTransition.maxNumberOfSteps,
                                                                 options.eqoParams.kWayTransition.k,
                                                                 options.eqoParams.kWayTransition.optimizationMetric,
                                                                 options.eqoParams.kWayTransition.generationMethod,
                                                                 computeBatchSize(options));
                case SAMPLE -> buildSampleSetOracle(options, mqo);
            };
            chain.addOracle(eqo);
        }

        return chain;
    }

    private static <M extends RegularAutomaton<?, String, ?, ?, ?> & SuffixOutput<String, Word<O>>, O> EQOracleChain<M, String, Word<O>> getAdaptiveOracles(
            Options options,
            AdaptiveMembershipOracle<String, O> mqo) {
        return getRegularOracles(options, new Adaptive2MembershipWrapper<>(mqo));
    }

    private static EquivalenceOracle<NFA<?, String>, String, Boolean> getNFAOracles(Options options,
                                                                                    MembershipOracle<String, Boolean> mqo) {
        final EQOracleChain<? super DFA<?, String>, String, Boolean> chain = getRegularOracles(options, mqo);
        final EQOracleChain<NFA<?, String>, String, Boolean> result = new EQOracleChain<>();

        for (EquivalenceOracle<? super DFA<?, String>, String, Boolean> eqo : chain.getOracles()) {
            result.addOracle((hyp, inputs) -> eqo.findCounterExample(NFAs.determinize(hyp,
                                                                                      Alphabets.fromCollection(inputs)),
                                                                     inputs));
        }

        return result;
    }

    private static EquivalenceOracle<SBA<?, String>, String, Boolean> getSBAOracles(Options options,
                                                                                    MembershipOracle<String, Boolean> mqo) {
        final EQOracleChain<SBA<?, String>, String, Boolean> chain = new EQOracleChain<>();

        for (EQOracle e : options.eqos) {
            EquivalenceOracle<? super SBA<?, String>, String, Boolean> eqo = switch (e) {
                case W -> new de.learnlib.oracle.equivalence.sba.WMethodEQOracle<>(mqo,
                                                                                   options.eqoParams.wMethod.lookahead,
                                                                                   options.eqoParams.wMethod.expectedSize);
                case RANDOM -> buildRandomWellMatchedOracle(options, mqo);
                case SAMPLE -> buildSampleSetOracle(options, mqo);
                default -> throw new UnsupportedCombinationException(options, e);
            };
            chain.addOracle(eqo);
        }

        return chain;
    }

    private static EquivalenceOracle<SPA<?, String>, String, Boolean> getSPAOracles(Options options,
                                                                                    MembershipOracle<String, Boolean> mqo) {
        final EQOracleChain<SPA<?, String>, String, Boolean> chain = new EQOracleChain<>();

        for (EQOracle e : options.eqos) {
            EquivalenceOracle<? super SPA<?, String>, String, Boolean> eqo = switch (e) {
                case W -> new de.learnlib.oracle.equivalence.spa.WMethodEQOracle<>(mqo,
                                                                                   options.eqoParams.wMethod.lookahead,
                                                                                   options.eqoParams.wMethod.expectedSize);
                case WP -> new de.learnlib.oracle.equivalence.spa.WpMethodEQOracle<>(mqo,
                                                                                     options.eqoParams.wpMethod.lookahead,
                                                                                     options.eqoParams.wpMethod.expectedSize);
                case RANDOM -> buildRandomWellMatchedOracle(options, mqo);
                case SAMPLE -> buildSampleSetOracle(options, mqo);
                default -> throw new UnsupportedCombinationException(options, e);
            };
            chain.addOracle(eqo);
        }

        return chain;
    }

    private static <O> EquivalenceOracle<SPMM<?, String, ?, O>, String, Word<O>> getSPMMOracles(Options options,
                                                                                                MembershipOracle<String, Word<O>> mqo) {
        final EQOracleChain<SPMM<?, String, ?, O>, String, Word<O>> chain = new EQOracleChain<>();

        for (EQOracle e : options.eqos) {
            EquivalenceOracle<? super SPMM<?, String, ?, O>, String, Word<O>> eqo = switch (e) {
                case W -> new de.learnlib.oracle.equivalence.spmm.WMethodEQOracle<>(mqo,
                                                                                    options.eqoParams.wMethod.lookahead,
                                                                                    options.eqoParams.wMethod.expectedSize);
                case SAMPLE -> buildSampleSetOracle(options, mqo);
                default -> throw new UnsupportedCombinationException(options, e);
            };
            chain.addOracle(eqo);
        }

        return chain;
    }

    private static EquivalenceOracle<OneSEVPA<?, String>, String, Boolean> getVPAOracles(Options options,
                                                                                         MembershipOracle<String, Boolean> mqo) {
        final EQOracleChain<OneSEVPA<?, String>, String, Boolean> chain = new EQOracleChain<>();

        for (EQOracle e : options.eqos) {
            EquivalenceOracle<? super OneSEVPA<?, String>, String, Boolean> eqo = switch (e) {
                case RANDOM -> buildRandomWellMatchedOracle(options, mqo);
                case SAMPLE -> buildSampleSetOracle(options, mqo);
                default -> throw new UnsupportedCombinationException(options, e);
            };
            chain.addOracle(eqo);
        }

        return chain;
    }

    private static <I> RandomWellMatchedWordsEQOracle<I> buildRandomWellMatchedOracle(Options options,
                                                                                      MembershipOracle<I, Boolean> oracle) {
        final double callProb = 0.5;
        return new RandomWellMatchedWordsEQOracle<>(new Random(options.eqoParams.random.seed),
                                                    oracle,
                                                    callProb,
                                                    options.eqoParams.random.maxTests,
                                                    options.eqoParams.random.minLength,
                                                    options.eqoParams.random.maxLength);
    }

    private static <D> SampleSetEQOracle<String, D> buildSampleSetOracle(Options options,
                                                                         MembershipOracle<String, D> oracle) {
        final List<String> samples = options.eqoParams.samples.samples;
        final List<Word<String>> tmp = new ArrayList<>(samples.size());

        for (String s : options.eqoParams.samples.samples) {
            String[] words = s.split(options.eqoParams.samples.split);
            tmp.add(Word.fromArray(words, 0, words.length));
        }

        return new SampleSetEQOracle<String, D>().addAll(oracle, tmp);
    }

    private static int computeBatchSize(Options options) {
        if (options.sul.size() == 1) {
            return 1;
        } else {
            return options.sul.size() * BATCH_SIZE;
        }
    }

    class UnsupportedCombinationException extends IllegalArgumentException {

        UnsupportedCombinationException(Options options, EQOracle eqo) {
            super(String.format("Type '%s' does not support oracle '%s'", options.type, eqo));
        }
    }

}
