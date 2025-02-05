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
package de.learnlib.algorithm.procedural.spa;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import de.learnlib.AccessSequenceTransformer;
import de.learnlib.acex.AbstractBaseCounterexample;
import de.learnlib.acex.AcexAnalyzer;
import de.learnlib.acex.AcexAnalyzers;
import de.learnlib.algorithm.LearnerConstructor;
import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.algorithm.LearningAlgorithm.DFALearner;
import de.learnlib.algorithm.procedural.spa.manager.OptimizingATRManager;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.util.MQUtil;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.alphabet.SupportsGrowingAlphabet;
import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.procedural.SPA;
import net.automatalib.automaton.procedural.impl.EmptySPA;
import net.automatalib.automaton.procedural.impl.StackSPA;
import net.automatalib.common.util.HashUtil;
import net.automatalib.common.util.mapping.Mapping;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A learning algorithm for {@link SPA}s.
 *
 * @param <I>
 *         input symbol type
 * @param <L>
 *         sub-learner type
 */
public class SPALearner<I, L extends DFALearner<I> & SupportsGrowingAlphabet<I> & AccessSequenceTransformer<I>>
        implements LearningAlgorithm<SPA<?, I>, I, Boolean> {

    private final ProceduralInputAlphabet<I> alphabet;
    private final MembershipOracle<I, Boolean> oracle;
    private final Mapping<I, LearnerConstructor<L, I, Boolean>> learnerConstructors;
    private final AcexAnalyzer analyzer;
    private final ATRManager<I> atrManager;

    private final Map<I, L> subLearners;
    private final Set<I> activeAlphabet;
    private I initialCallSymbol;

    public SPALearner(ProceduralInputAlphabet<I> alphabet,
                      MembershipOracle<I, Boolean> oracle,
                      LearnerConstructor<L, I, Boolean> learnerConstructor) {
        this(alphabet,
             oracle,
             i -> learnerConstructor,
             AcexAnalyzers.BINARY_SEARCH_FWD,
             new OptimizingATRManager<>(alphabet));
    }

    public SPALearner(ProceduralInputAlphabet<I> alphabet,
                      MembershipOracle<I, Boolean> oracle,
                      Mapping<I, LearnerConstructor<L, I, Boolean>> learnerConstructors,
                      AcexAnalyzer analyzer,
                      ATRManager<I> atrManager) {
        this.alphabet = alphabet;
        this.oracle = oracle;
        this.learnerConstructors = learnerConstructors;
        this.analyzer = analyzer;
        this.atrManager = atrManager;

        this.subLearners = new HashMap<>(HashUtil.capacity(this.alphabet.getNumCalls()));
        this.activeAlphabet = new HashSet<>(HashUtil.capacity(alphabet.getNumCalls() + alphabet.getNumInternals()));
        this.activeAlphabet.addAll(alphabet.getInternalAlphabet());
    }

    @Override
    public void startLearning() {
        // do nothing, as we have to wait for evidence that the potential main procedure actually terminates
    }

    @Override
    public boolean refineHypothesis(DefaultQuery<I, Boolean> defaultQuery) {

        if (!MQUtil.isCounterexample(defaultQuery, getHypothesisModel())) {
            return false;
        }

        assert this.alphabet.isWellMatched(defaultQuery.getInput());

        boolean changed = extractUsefulInformationFromCounterExample(defaultQuery);

        while (refineHypothesisInternal(defaultQuery)) {
            changed = true;
        }

        return changed;
    }

    private boolean refineHypothesisInternal(DefaultQuery<I, Boolean> defaultQuery) {

        final SPA<?, I> hypothesis = this.getHypothesisModel();

        if (!MQUtil.isCounterexample(defaultQuery, hypothesis)) {
            return false;
        }

        // look for better sequences and ensure TS conformance prior to CE analysis
        boolean localRefinement = updateATRAndCheckTSConformance(hypothesis);

        if (!MQUtil.isCounterexample(defaultQuery, hypothesis)) {
            return localRefinement;
        }

        final Word<I> input = defaultQuery.getInput();
        final List<Integer> returnIndices = determineReturnIndices(input);
        final int idx = analyzer.analyzeAbstractCounterexample(new Acex(input,
                                                                        defaultQuery.getOutput() ?
                                                                                hypothesis::accepts :
                                                                                this.oracle::answerQuery,
                                                                        returnIndices));
        final int returnIdx = returnIndices.get(idx);

        // extract local ce
        final int callIdx = this.alphabet.findCallIndex(input, returnIdx);
        final I procedure = input.getSymbol(callIdx);

        final Word<I> localTrace = this.alphabet.project(input.subWord(callIdx + 1, returnIdx), 0);
        final DefaultQuery<I, Boolean> localCE = new DefaultQuery<>(localTrace, defaultQuery.getOutput());

        localRefinement |= this.subLearners.get(procedure).refineHypothesis(localCE);
        assert localRefinement;

        return true;
    }

    @Override
    public SPA<?, I> getHypothesisModel() {

        if (this.subLearners.isEmpty()) {
            return new EmptySPA<>(this.alphabet);
        }

        return new StackSPA<>(alphabet, initialCallSymbol, getSubModels());
    }

    private boolean extractUsefulInformationFromCounterExample(DefaultQuery<I, Boolean> defaultQuery) {

        if (!defaultQuery.getOutput()) {
            return false;
        }

        final Word<I> input = defaultQuery.getInput();

        // positive CEs should always be rooted at the main procedure
        this.initialCallSymbol = input.firstSymbol();

        final Set<I> newProcedures = atrManager.scanPositiveCounterexample(input);

        for (I sym : newProcedures) {
            final L newLearner = learnerConstructors.get(sym)
                                                    .constructLearner(new GrowingMapAlphabet<>(alphabet.getInternalAlphabet()),
                                                                      new ProceduralMembershipOracle<>(alphabet,
                                                                                                       oracle,
                                                                                                       sym,
                                                                                                       atrManager));
            // add existing procedures (without itself) to new learner
            for (I call : this.subLearners.keySet()) {
                newLearner.addAlphabetSymbol(call);
            }

            newLearner.startLearning();

            // add new learner here, so that we have an AccessSequenceTransformer available when scanning for shorter ts
            this.subLearners.put(sym, newLearner);

            // try to find a shorter terminating sequence for 'sym' before procedure is added to other hypotheses
            this.atrManager.scanProcedures(Collections.singletonMap(sym, newLearner.getHypothesisModel()),
                                           subLearners,
                                           activeAlphabet);
            this.activeAlphabet.add(sym);

            // add the new procedure (with a possibly shorter ts) to all learners (including the new one)
            for (L learner : this.subLearners.values()) {
                learner.addAlphabetSymbol(sym);
            }
        }

        if (newProcedures.isEmpty()) {
            return false;
        } else {
            this.atrManager.scanProcedures(getSubModels(), subLearners, activeAlphabet);
            return true;
        }
    }

    private Map<I, DFA<?, I>> getSubModels() {
        final Map<I, DFA<?, I>> subModels = new HashMap<>(HashUtil.capacity(this.subLearners.size()));

        for (Map.Entry<I, L> entry : this.subLearners.entrySet()) {
            subModels.put(entry.getKey(), entry.getValue().getHypothesisModel());
        }

        return subModels;
    }

    private boolean updateATRAndCheckTSConformance(SPA<?, I> hypothesis) {
        boolean refinement = false;
        Map<I, DFA<?, I>> subModels = hypothesis.getProcedures();

        while (checkAndEnsureTSConformance(subModels)) {
            refinement = true;
            subModels = getSubModels();
            this.atrManager.scanProcedures(subModels, subLearners, activeAlphabet);
        }

        return refinement;
    }

    private List<Integer> determineReturnIndices(Word<I> input) {

        final List<Integer> returnIndices = new ArrayList<>();

        for (int i = 0; i < input.length(); i++) {
            if (this.alphabet.isReturnSymbol(input.getSymbol(i))) {
                returnIndices.add(i);
            }
        }

        return returnIndices;
    }

    private boolean checkAndEnsureTSConformance(Map<I, DFA<?, I>> subModels) {
        boolean refinement = false;

        for (I procedure : this.subLearners.keySet()) {
            final Word<I> terminatingSequence = this.atrManager.getTerminatingSequence(procedure);
            final WordBuilder<I> embeddedTS = new WordBuilder<>(terminatingSequence.size() + 2);
            embeddedTS.append(procedure);
            embeddedTS.append(terminatingSequence);
            embeddedTS.append(alphabet.getReturnSymbol());
            refinement |= checkSingleTerminatingSequence(embeddedTS.toWord(), subModels);
        }

        return refinement;
    }

    private boolean checkSingleTerminatingSequence(Word<I> input, Map<I, DFA<?, I>> hypotheses) {
        boolean refinement = false;

        for (int i = 0; i < input.size(); i++) {
            final I sym = input.getSymbol(i);

            if (this.alphabet.isCallSymbol(sym)) {
                final int returnIdx = this.alphabet.findReturnIndex(input, i + 1);
                final Word<I> projectedRun = this.alphabet.project(input.subWord(i + 1, returnIdx), 0);
                // whenever we extract a terminating sequence, we can also instantiate a learner.
                // Therefore, the existence of the hypothesis is guaranteed.
                @SuppressWarnings("assignment.type.incompatible")
                final @NonNull DFA<?, I> hyp = hypotheses.get(sym);

                if (!hyp.accepts(projectedRun)) {
                    refinement = true;
                    subLearners.get(sym).refineHypothesis(new DefaultQuery<>(projectedRun, true));
                }
            }
        }

        return refinement;
    }

    private final class Acex extends AbstractBaseCounterexample<Boolean> {

        private final Word<I> input;
        private final Predicate<? super Word<I>> oracle;
        private final List<Integer> returnIndices;

        Acex(Word<I> input, Predicate<? super Word<I>> oracle, List<Integer> returnIndices) {
            super(returnIndices.size() + 1);
            this.input = input;
            this.oracle = oracle;
            this.returnIndices = returnIndices;

            setEffect(returnIndices.size(), true);
            setEffect(0, false);
        }

        @Override
        protected Boolean computeEffect(int index) {
            final Deque<Word<I>> wordStack = new ArrayDeque<>();
            int idx = this.returnIndices.get(index);

            while (idx > 0) {
                final int callIdx = alphabet.findCallIndex(input, idx);
                final I callSymbol = input.getSymbol(callIdx);
                final Word<I> normalized = alphabet.project(input.subWord(callIdx + 1, idx), 0);
                final Word<I> expanded = alphabet.expand(normalized, atrManager::getTerminatingSequence);

                wordStack.push(expanded.prepend(callSymbol));

                idx = callIdx;
            }

            final WordBuilder<I> builder = new WordBuilder<>();
            wordStack.forEach(builder::append);
            builder.append(input.subWord(this.returnIndices.get(index)));

            return oracle.test(builder.toWord());
        }

        @Override
        public boolean checkEffects(Boolean eff1, Boolean eff2) {
            return Objects.equals(eff1, eff2);
        }
    }
}
