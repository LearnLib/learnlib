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
package de.learnlib.algorithm.procedural.sba;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
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
import de.learnlib.algorithm.procedural.SymbolWrapper;
import de.learnlib.algorithm.procedural.sba.manager.OptimizingATManager;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.util.MQUtil;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.alphabet.SupportsGrowingAlphabet;
import net.automatalib.alphabet.impl.DefaultProceduralInputAlphabet;
import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.procedural.SBA;
import net.automatalib.automaton.procedural.impl.EmptySBA;
import net.automatalib.automaton.procedural.impl.StackSBA;
import net.automatalib.common.util.HashUtil;
import net.automatalib.common.util.Pair;
import net.automatalib.common.util.mapping.Mapping;
import net.automatalib.util.automaton.Automata;
import net.automatalib.util.automaton.procedural.SBAs;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;

/**
 * A learning algorithm for {@link SBA}s.
 *
 * @param <I>
 *         input symbol type
 * @param <L>
 *         sub-learner type
 */
public class SBALearner<I, L extends DFALearner<SymbolWrapper<I>> & SupportsGrowingAlphabet<SymbolWrapper<I>> & AccessSequenceTransformer<SymbolWrapper<I>>>
        implements LearningAlgorithm<SBA<?, I>, I, Boolean> {

    private final ProceduralInputAlphabet<I> alphabet;
    private final MembershipOracle<I, Boolean> oracle;
    private final Mapping<I, LearnerConstructor<L, SymbolWrapper<I>, Boolean>> learnerConstructors;
    private final AcexAnalyzer analyzer;
    private final ATManager<I> atManager;

    private final Map<I, L> learners;
    private I initialCallSymbol;

    private final Map<I, SymbolWrapper<I>> mapping;

    public SBALearner(ProceduralInputAlphabet<I> alphabet,
                      MembershipOracle<I, Boolean> oracle,
                      LearnerConstructor<L, SymbolWrapper<I>, Boolean> learnerConstructor) {
        this(alphabet,
             oracle,
             i -> learnerConstructor,
             AcexAnalyzers.BINARY_SEARCH_BWD,
             new OptimizingATManager<>(alphabet));
    }

    public SBALearner(ProceduralInputAlphabet<I> alphabet,
                      MembershipOracle<I, Boolean> oracle,
                      Mapping<I, LearnerConstructor<L, SymbolWrapper<I>, Boolean>> learnerConstructors,
                      AcexAnalyzer analyzer,
                      ATManager<I> atManager) {
        this.alphabet = alphabet;
        this.oracle = oracle;
        this.learnerConstructors = learnerConstructors;
        this.analyzer = analyzer;
        this.atManager = atManager;

        this.learners = new HashMap<>(HashUtil.capacity(this.alphabet.getNumCalls()));
        this.mapping = new HashMap<>(HashUtil.capacity(this.alphabet.size()));

        for (I i : this.alphabet.getInternalAlphabet()) {
            final SymbolWrapper<I> wrapper = new SymbolWrapper<>(i, true);
            this.mapping.put(i, wrapper);
        }

        final SymbolWrapper<I> wrapper = new SymbolWrapper<>(this.alphabet.getReturnSymbol(), false);
        this.mapping.put(this.alphabet.getReturnSymbol(), wrapper);
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

        assert this.alphabet.isReturnMatched(defaultQuery.getInput());

        boolean changed = extractUsefulInformationFromCounterExample(defaultQuery);

        while (refineHypothesisInternal(defaultQuery)) {
            changed = true;
        }

        ensureCallAndReturnClosure();

        assert SBAs.isValid(getHypothesisModel());

        return changed;
    }

    private boolean refineHypothesisInternal(DefaultQuery<I, Boolean> defaultQuery) {

        final SBA<?, I> hypothesis = this.getHypothesisModel();

        if (!MQUtil.isCounterexample(defaultQuery, hypothesis)) {
            return false;
        }

        final Word<I> input = defaultQuery.getInput();
        final int mismatchIdx = analyzer.analyzeAbstractCounterexample(new Acex<>(input,
                                                                                  defaultQuery.getOutput() ?
                                                                                          hypothesis::accepts :
                                                                                          this.oracle::answerQuery));

        // extract local ce
        final int callIdx = this.alphabet.findCallIndex(input, mismatchIdx);
        final I procedure = input.getSymbol(callIdx);

        final Word<I> localTrace =
                this.alphabet.project(input.subWord(callIdx + 1, mismatchIdx), 0).append(input.getSymbol(mismatchIdx));
        final DefaultQuery<SymbolWrapper<I>, Boolean> localCE = constructLocalCE(localTrace, defaultQuery.getOutput());

        boolean localRefinement = this.learners.get(procedure).refineHypothesis(localCE);
        assert localRefinement;

        return true;
    }

    @Override
    public SBA<?, I> getHypothesisModel() {

        if (this.learners.isEmpty()) {
            return new EmptySBA<>(this.alphabet);
        }

        final Alphabet<SymbolWrapper<I>> internalAlphabet = new GrowingMapAlphabet<>();
        final Alphabet<SymbolWrapper<I>> callAlphabet = new GrowingMapAlphabet<>();
        final SymbolWrapper<I> returnSymbol;

        final Map<I, DFA<?, SymbolWrapper<I>>> procedures = getSubModels();
        final Map<SymbolWrapper<I>, DFA<?, SymbolWrapper<I>>> mappedProcedures =
                new HashMap<>(HashUtil.capacity(procedures.size()));

        for (Entry<I, DFA<?, SymbolWrapper<I>>> e : procedures.entrySet()) {
            final SymbolWrapper<I> w = this.mapping.get(e.getKey());
            assert w != null;
            mappedProcedures.put(w, e.getValue());
            callAlphabet.add(w);
        }

        for (I i : this.alphabet.getInternalAlphabet()) {
            final SymbolWrapper<I> w = this.mapping.get(i);
            assert w != null;
            internalAlphabet.add(w);
        }

        returnSymbol = this.mapping.get(alphabet.getReturnSymbol());
        assert returnSymbol != null;

        final ProceduralInputAlphabet<SymbolWrapper<I>> mappedAlphabet =
                new DefaultProceduralInputAlphabet<>(internalAlphabet, callAlphabet, returnSymbol);

        final StackSBA<?, SymbolWrapper<I>> delegate =
                new StackSBA<>(mappedAlphabet, this.mapping.get(initialCallSymbol), mappedProcedures);

        return new MappingSBA<>(alphabet, mapping, delegate);
    }

    private boolean extractUsefulInformationFromCounterExample(DefaultQuery<I, Boolean> defaultQuery) {

        if (!defaultQuery.getOutput()) {
            return false;
        }

        boolean update = false;
        final Word<I> input = defaultQuery.getInput();

        // positive CEs should always be rooted at the main procedure
        this.initialCallSymbol = input.firstSymbol();

        final Pair<Set<I>, Set<I>> newSeqs = atManager.scanPositiveCounterexample(input);
        final Set<I> newCalls = newSeqs.getFirst();
        final Set<I> newTerms = newSeqs.getSecond();

        for (I call : newTerms) {
            final SymbolWrapper<I> sym = new SymbolWrapper<>(call, true);
            this.mapping.put(call, sym);
            for (L learner : this.learners.values()) {
                learner.addAlphabetSymbol(sym);
                update = true;
            }
        }

        for (I sym : newCalls) {
            update = true;
            final L newLearner = learnerConstructors.get(sym)
                                                    .constructLearner(new GrowingMapAlphabet<>(this.mapping.values()),
                                                                      new ProceduralMembershipOracle<>(alphabet,
                                                                                                       oracle,
                                                                                                       sym,
                                                                                                       atManager));

            newLearner.startLearning();

            // add new learner here, so that we have an AccessSequenceTransformer available when scanning for shorter ts
            this.learners.put(sym, newLearner);

            // try to find a shorter terminating sequence for 'sym' before procedure is added to other hypotheses
            final Set<I> newTS =
                    this.atManager.scanProcedures(Collections.singletonMap(sym, newLearner.getHypothesisModel()),
                                                  learners,
                                                  mapping.values());

            for (I call : newTS) {
                final SymbolWrapper<I> wrapper = new SymbolWrapper<>(call, true);
                this.mapping.put(call, wrapper);
                for (L learner : this.learners.values()) {
                    learner.addAlphabetSymbol(wrapper);
                }
            }

            // add non-terminating version for new call
            if (!this.mapping.containsKey(sym)) {
                final SymbolWrapper<I> wrapper = new SymbolWrapper<>(sym, false);
                this.mapping.put(sym, wrapper);
                for (L learner : this.learners.values()) {
                    learner.addAlphabetSymbol(wrapper);
                }
            }
        }

        return update;
    }

    private Map<I, DFA<?, SymbolWrapper<I>>> getSubModels() {
        final Map<I, DFA<?, SymbolWrapper<I>>> subModels = new HashMap<>(HashUtil.capacity(this.learners.size()));

        for (Map.Entry<I, L> entry : this.learners.entrySet()) {
            subModels.put(entry.getKey(), entry.getValue().getHypothesisModel());
        }

        return subModels;
    }

    private DefaultQuery<SymbolWrapper<I>, Boolean> constructLocalCE(Word<I> input, boolean output) {

        final WordBuilder<SymbolWrapper<I>> wb = new WordBuilder<>(input.length());
        for (I i : input) {
            wb.append(mapping.get(i));
        }

        return new DefaultQuery<>(wb.toWord(), output);
    }

    private void ensureCallAndReturnClosure() {

        final Set<SymbolWrapper<I>> nonContinuableSymbols = new HashSet<>();
        for (SymbolWrapper<I> mapped : mapping.values()) {
            if (!mapped.isContinuable()) {
                nonContinuableSymbols.add(mapped);
            }
        }

        for (L learner : this.learners.values()) {
            boolean stable = false;

            while (!stable) {
                stable = ensureCallAndReturnClosure(learner.getHypothesisModel(), nonContinuableSymbols, learner);
            }
        }
    }

    private <S> boolean ensureCallAndReturnClosure(DFA<S, SymbolWrapper<I>> hyp,
                                                   Collection<SymbolWrapper<I>> nonContinuableSymbols,
                                                   L learner) {

        final Set<Word<SymbolWrapper<I>>> cover = new HashSet<>();
        for (Word<SymbolWrapper<I>> sc : Automata.stateCover(hyp, mapping.values())) {
            cover.add(learner.transformAccessSequence(sc));
        }

        for (Word<SymbolWrapper<I>> cov : cover) {
            final S state = hyp.getState(cov);

            for (SymbolWrapper<I> i : nonContinuableSymbols) {
                final S succ = hyp.getSuccessor(state, i);

                for (SymbolWrapper<I> next : mapping.values()) {

                    if (hyp.isAccepting(hyp.getSuccessor(succ, next))) { // closure is violated

                        final DefaultQuery<SymbolWrapper<I>, Boolean> ce =
                                new DefaultQuery<>(cov.append(i).append(next), false);
                        final boolean refined = learner.refineHypothesis(ce);

                        assert refined;
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private static class Acex<I> extends AbstractBaseCounterexample<Boolean> {

        private final Word<I> input;
        private final Predicate<? super Word<I>> oracle;

        Acex(Word<I> input, Predicate<? super Word<I>> oracle) {
            super(input.size() + 1);
            this.input = input;
            this.oracle = oracle;
        }

        @Override
        protected Boolean computeEffect(int index) {
            return oracle.test(input.prefix(index));
        }

        @Override
        public boolean checkEffects(Boolean eff1, Boolean eff2) {
            return Objects.equals(eff1, eff2);
        }
    }
}
