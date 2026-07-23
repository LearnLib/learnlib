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

import java.util.function.Function;

import de.learnlib.algorithm.adt.learner.ADTLearner;
import de.learnlib.algorithm.lsharp.LSharpMealy;
import de.learnlib.algorithm.nlstar.NLStarLearner;
import de.learnlib.algorithm.observationpack.vpa.OPLearnerVPABuilder;
import de.learnlib.algorithm.procedural.SymbolWrapper;
import de.learnlib.algorithm.procedural.sba.SBALearner;
import de.learnlib.algorithm.procedural.spa.SPALearner;
import de.learnlib.algorithm.procedural.spmm.SPMMLearner;
import de.learnlib.algorithm.ttt.vpa.TTTLearnerVPABuilder;
import de.learnlib.cli.adapter.ProceduralDFAAdapter.ExtensibleLStarDFAAdapter;
import de.learnlib.cli.adapter.ProceduralDFAAdapter.KearnsVaziraniDFAAdapter;
import de.learnlib.cli.adapter.ProceduralDFAAdapter.LLambdaDFAAdapter;
import de.learnlib.cli.adapter.ProceduralDFAAdapter.OPLearnerDFAAdapter;
import de.learnlib.cli.adapter.ProceduralDFAAdapter.TTTLambdaDFAAdapter;
import de.learnlib.cli.adapter.ProceduralDFAAdapter.TTTLearnerDFAAdapter;
import de.learnlib.cli.adapter.ProceduralMealyAdapter.ExtensibleLStarMealyAdapter;
import de.learnlib.cli.adapter.ProceduralMealyAdapter.KearnsVaziraniMealyAdapter;
import de.learnlib.cli.adapter.ProceduralMealyAdapter.LLambdaMealyAdapter;
import de.learnlib.cli.adapter.ProceduralMealyAdapter.MealyDHCAdapter;
import de.learnlib.cli.adapter.ProceduralMealyAdapter.OPLearnerMealyAdapter;
import de.learnlib.cli.adapter.ProceduralMealyAdapter.SparseLearnerAdapter;
import de.learnlib.cli.adapter.ProceduralMealyAdapter.TTTLambdaMealyAdapter;
import de.learnlib.cli.adapter.ProceduralMealyAdapter.TTTLearnerMealyAdapter;
import de.learnlib.cli.option.Learner;
import de.learnlib.cli.option.Options;
import de.learnlib.cli.util.Constructor;
import de.learnlib.cli.util.Constructor.AdaptiveConstructor;
import de.learnlib.cli.util.Constructor.DFAConstructor;
import de.learnlib.cli.util.Constructor.MealyConstructor;
import de.learnlib.cli.util.Constructor.PresetConstructor;
import de.learnlib.oracle.AdaptiveMembershipOracle;
import de.learnlib.oracle.MembershipOracle;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.alphabet.VPAlphabet;
import net.automatalib.automaton.concept.FiniteRepresentation;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.NFA;
import net.automatalib.automaton.procedural.SBA;
import net.automatalib.automaton.procedural.SPA;
import net.automatalib.automaton.procedural.SPMM;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.vpa.OneSEVPA;
import net.automatalib.word.Word;

@FunctionalInterface
public interface LearnerFactory<A extends Alphabet<String>, M extends FiniteRepresentation, D, OR>
        extends Function<Options, Constructor<A, M, String, D, OR>> {

    LearnerFactory<Alphabet<String>, DFA<?, String>, Boolean, MembershipOracle<String, Boolean>> DFA_LEARNER = LearnerFactory::getDFALearner;
    LearnerFactory<Alphabet<String>, MealyMachine<?, String, ?, String>, Word<String>, MembershipOracle<String, Word<String>>> MEALY_LEARNER =
            LearnerFactory::getMealyLearner;
    LearnerFactory<Alphabet<String>, MealyMachine<?, String, ?, String>, Word<String>, AdaptiveMembershipOracle<String, String>> ADAPTIVE_LEARNER =
            LearnerFactory::getAdaptiveLearner;
    LearnerFactory<Alphabet<String>, NFA<?, String>, Boolean, MembershipOracle<String, Boolean>> NFA_LEARNER = LearnerFactory::getNFALearner;
    LearnerFactory<ProceduralInputAlphabet<String>, SBA<?, String>, Boolean, MembershipOracle<String, Boolean>> SBA_LEARNER =
            LearnerFactory::getSBALearner;
    LearnerFactory<ProceduralInputAlphabet<String>, SPA<?, String>, Boolean, MembershipOracle<String, Boolean>> SPA_LEARNER =
            LearnerFactory::getSPALearner;
    LearnerFactory<ProceduralInputAlphabet<String>, SPMM<?, String, ?, String>, Word<String>, MembershipOracle<String, Word<String>>> SPMM_LEARNER =
            LearnerFactory::getSPMMLearner;
    LearnerFactory<VPAlphabet<String>, OneSEVPA<?, String>, Boolean, MembershipOracle<String, Boolean>> VPA_LEARNER = LearnerFactory::getVPALearner;

    private static <I> DFAConstructor<Alphabet<I>, I> getDFALearner(Options options) {
        return switch (options.learner) {
            case KV -> KearnsVaziraniDFAAdapter::new;
            case LLAMBDA -> LLambdaDFAAdapter::new;
            case LSTAR -> ExtensibleLStarDFAAdapter::new;
            case OP -> OPLearnerDFAAdapter::new;
            case TTT -> TTTLearnerDFAAdapter::new;
            case TTTLAMBDA -> TTTLambdaDFAAdapter::new;
            default -> throw new UnsupportedCombinationException(options);
        };
    }

    private static <I, O> MealyConstructor<Alphabet<I>, I, O> getMealyLearner(Options options) {
        return switch (options.learner) {
            case DHC -> MealyDHCAdapter::new;
            case KV -> KearnsVaziraniMealyAdapter::new;
            case LLAMBDA -> LLambdaMealyAdapter::new;
            case LSTAR -> ExtensibleLStarMealyAdapter::new;
            case SPARSE -> SparseLearnerAdapter::new;
            case OP -> OPLearnerMealyAdapter::new;
            case TTT -> TTTLearnerMealyAdapter::new;
            case TTTLAMBDA -> TTTLambdaMealyAdapter::new;
            default -> throw new UnsupportedCombinationException(options);
        };
    }

    private static <I, O> AdaptiveConstructor<Alphabet<I>, MealyMachine<?, I, ?, O>, I, O> getAdaptiveLearner(Options options) {
        return switch (options.learner) {
            case ADT -> ADTLearner::new;
            case LSHARP -> LSharpMealy::new;
            default -> throw new UnsupportedCombinationException(options);
        };
    }

    private static <I> PresetConstructor<Alphabet<I>, NFA<?, I>, I, Boolean> getNFALearner(Options options) {
        if (options.learner == Learner.NLSTAR) {
            return NLStarLearner::new;
        }
        throw new UnsupportedCombinationException(options);
    }

    private static <I> PresetConstructor<ProceduralInputAlphabet<I>, SBA<?, I>, I, Boolean> getSBALearner(Options options) {
        final DFAConstructor<Alphabet<SymbolWrapper<I>>, SymbolWrapper<I>> learner = getDFALearner(options);
        return (alph, mqo) -> new SBALearner<>(alph, mqo, learner::constructLearner);
    }

    private static <I> PresetConstructor<ProceduralInputAlphabet<I>, SPA<?, I>, I, Boolean> getSPALearner(Options options) {
        final DFAConstructor<Alphabet<I>, I> learner = getDFALearner(options);
        return (alph, mqo) -> new SPALearner<>(alph, mqo, learner::constructLearner);
    }

    private static <I> PresetConstructor<ProceduralInputAlphabet<I>, SPMM<?, I, ?, String>, I, Word<String>> getSPMMLearner(
            Options options) {
        final MealyConstructor<Alphabet<SymbolWrapper<I>>, SymbolWrapper<I>, String> learner = getMealyLearner(options);
        return (alph, mqo) -> new SPMMLearner<>(alph, "error", mqo, learner::constructLearner);
    }

    private static <I> PresetConstructor<VPAlphabet<I>, OneSEVPA<?, I>, I, Boolean> getVPALearner(Options options) {
        return switch (options.learner) {
            case OP -> (alphabet, mqo) -> new OPLearnerVPABuilder<I>().withAlphabet(alphabet).withOracle(mqo).create();
            case TTT ->
                    (alphabet, mqo) -> new TTTLearnerVPABuilder<I>().withAlphabet(alphabet).withOracle(mqo).create();
            default -> throw new UnsupportedCombinationException(options);
        };
    }

    class UnsupportedCombinationException extends IllegalArgumentException {

        UnsupportedCombinationException(Options options) {
            super(String.format("Learner '%s' does not support type '%s'", options.learner, options.type));
        }
    }
}
