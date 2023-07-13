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
package de.learnlib.algorithms.sba;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import de.learnlib.algorithms.sba.manager.OptimizingATManager;
import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.api.algorithm.LearnerConstructor;
import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.algorithm.LearningAlgorithm.DFALearner;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.util.MQUtil;
import net.automatalib.SupportsGrowingAlphabet;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.sba.EmptySBA;
import net.automatalib.automata.sba.SBA;
import net.automatalib.automata.sba.StackSBA;
import net.automatalib.commons.util.Pair;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.sba.SBAUtil;
import net.automatalib.words.Alphabet;
import net.automatalib.words.SPAAlphabet;
import net.automatalib.words.VPDAlphabet.SymbolType;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import net.automatalib.words.impl.DefaultSPAAlphabet;
import net.automatalib.words.impl.GrowingMapAlphabet;

public class SBALearner<I, L extends DFALearner<SymbolWrapper<I>> & SupportsGrowingAlphabet<SymbolWrapper<I>> & AccessSequenceTransformer<SymbolWrapper<I>>>
        implements LearningAlgorithm<SBA<?, I>, I, Boolean> {

    private final SPAAlphabet<I> alphabet;
    private final MembershipOracle<I, Boolean> oracle;
    private final Mapping<I, LearnerConstructor<L, SymbolWrapper<I>, Boolean>> learnerConstructors;
    private final ATManager<I> atManager;

    private final Map<I, L> subLearners;
    private I initialCallSymbol;

    private final AlphabetMapper<I> mapper;

    public SBALearner(final SPAAlphabet<I> alphabet,
                      final MembershipOracle<I, Boolean> oracle,
                      final LearnerConstructor<L, SymbolWrapper<I>, Boolean> learnerConstructor) {
        this(alphabet, oracle, (i) -> learnerConstructor, new OptimizingATManager<>(alphabet));
    }

    public SBALearner(final SPAAlphabet<I> alphabet,
                      final MembershipOracle<I, Boolean> oracle,
                      final Mapping<I, LearnerConstructor<L, SymbolWrapper<I>, Boolean>> learnerConstructors,
                      final ATManager<I> atManager) {
        this.alphabet = alphabet;
        this.oracle = oracle;
        this.learnerConstructors = learnerConstructors;
        this.atManager = atManager;

        this.subLearners = Maps.newHashMapWithExpectedSize(this.alphabet.getNumCalls());
        this.mapper = new AlphabetMapper<>(alphabet);

        for (I i : this.alphabet.getCallAlphabet()) {
            final SymbolWrapper<I> wrapper = new SymbolWrapper<>(i, false, SymbolType.CALL);
            this.mapper.set(i, wrapper);
        }
        for (I i : this.alphabet.getInternalAlphabet()) {
            final SymbolWrapper<I> wrapper = new SymbolWrapper<>(i, false, SymbolType.INTERNAL);
            this.mapper.set(i, wrapper);
        }

        final SymbolWrapper<I> wrapper = new SymbolWrapper<>(this.alphabet.getReturnSymbol(), false, SymbolType.RETURN);
        this.mapper.set(this.alphabet.getReturnSymbol(), wrapper);
    }

    @Override
    public void startLearning() {
        // do nothing, as we have to wait for evidence that the potential main procedure actually terminates
    }

    @Override
    public boolean refineHypothesis(DefaultQuery<I, Boolean> defaultQuery) {

        boolean changed = this.extractUsefulInformationFromCounterExample(defaultQuery);

        while (refineHypothesisInternal(defaultQuery)) {
            changed = true;
        }

        ensureCallAndReturnClosure();

        assert SBAUtil.isValid(this.getHypothesisModel());

        return changed;
    }

    private boolean refineHypothesisInternal(DefaultQuery<I, Boolean> defaultQuery) {

        final SBA<?, I> hypothesis = this.getHypothesisModel();

        if (!MQUtil.isCounterexample(defaultQuery, hypothesis)) {
            return false;
        }

        final Word<I> input = defaultQuery.getInput();
        final int mismatchIdx = detectMismatchingIdx(hypothesis, input, defaultQuery.getOutput());

        // extract local ce
        final int callIdx = this.alphabet.findCallIndex(input, mismatchIdx);
        final I procedure = input.getSymbol(callIdx);

        final Word<I> localTrace =
                this.alphabet.project(input.subWord(callIdx + 1, mismatchIdx), 0).append(input.getSymbol(mismatchIdx));
        final DefaultQuery<SymbolWrapper<I>, Boolean> localCE = constructLocalCE(localTrace, defaultQuery.getOutput());

        boolean localRefinement = this.subLearners.get(procedure).refineHypothesis(localCE);
        assert localRefinement;

        return true;
    }

    @Override
    public SBA<?, I> getHypothesisModel() {

        if (this.subLearners.isEmpty()) {
            return new EmptySBA<>(this.alphabet);
        }

        final Map<I, DFA<?, SymbolWrapper<I>>> procedures = getSubModels();
        final Alphabet<SymbolWrapper<I>> internalAlphabet = new GrowingMapAlphabet<>();
        final Alphabet<SymbolWrapper<I>> callAlphabet = new GrowingMapAlphabet<>();
        final Map<SymbolWrapper<I>, DFA<?, SymbolWrapper<I>>> mappedProcedures =
                Maps.newHashMapWithExpectedSize(procedures.size());

        for (I i : this.alphabet) {
            final SymbolWrapper<I> mappedI = this.mapper.get(i);

            if (this.alphabet.isCallSymbol(i)) {
                callAlphabet.add(mappedI);
                final DFA<?, SymbolWrapper<I>> p = procedures.get(i);
                if (p != null) {
                    mappedProcedures.put(mappedI, p);
                }
            } else if (alphabet.isInternalSymbol(i)) {
                internalAlphabet.add(mappedI);
            }
        }

        final SPAAlphabet<SymbolWrapper<I>> mappedAlphabet =
                new DefaultSPAAlphabet<>(internalAlphabet, callAlphabet, this.mapper.get(alphabet.getReturnSymbol()));

        final StackSBA<?, SymbolWrapper<I>> delegate =
                new StackSBA<>(mappedAlphabet, this.mapper.get(initialCallSymbol), mappedProcedures);

        return new MappingSBA<>(alphabet, mapper, delegate);
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
            final SymbolWrapper<I> sym = new SymbolWrapper<>(call, true, SymbolType.CALL);
            this.mapper.set(call, sym);
            for (L learner : this.subLearners.values()) {
                learner.addAlphabetSymbol(sym);
                update = true;
            }
        }

        for (I sym : newCalls) {
            update = true;
            final L newLearner = learnerConstructors.get(sym)
                                                    .constructLearner(new GrowingMapAlphabet<>(this.mapper.values()),
                                                                      new ProceduralMembershipOracle<>(alphabet,
                                                                                                       oracle,
                                                                                                       sym,
                                                                                                       atManager));

            newLearner.startLearning();

            // add new learner here, so that we have an AccessSequenceTransformer available when scanning for shorter ts
            this.subLearners.put(sym, newLearner);

            // try to find a shorter terminating sequence for 'sym' before procedure is added to other hypotheses
            final Set<I> newTS =
                    this.atManager.scanRefinedProcedures(Collections.singletonMap(sym, newLearner.getHypothesisModel()),
                                                         subLearners,
                                                         mapper.values());

            for (I call : newTS) {
                final SymbolWrapper<I> wrapper = new SymbolWrapper<>(call, true, SymbolType.CALL);
                this.mapper.set(call, wrapper);
                for (L learner : this.subLearners.values()) {
                    learner.addAlphabetSymbol(wrapper);
                }
            }
        }

        return update;
    }

    private Map<I, DFA<?, SymbolWrapper<I>>> getSubModels() {
        final Map<I, DFA<?, SymbolWrapper<I>>> subModels = Maps.newHashMapWithExpectedSize(this.subLearners.size());

        for (final Map.Entry<I, L> entry : this.subLearners.entrySet()) {
            subModels.put(entry.getKey(), entry.getValue().getHypothesisModel());
        }

        return subModels;
    }

    private <S> int detectMismatchingIdx(SBA<S, I> sba, Word<I> input, boolean output) {

        if (output) {
            S stateIter = sba.getInitialState();
            int idx = 0;

            for (I i : input) {
                final S succ = sba.getSuccessor(stateIter, i);

                if (succ == null || !sba.isAccepting(succ)) {
                    return idx;
                }
                stateIter = succ;
                idx++;
            }
        } else {
            int lower = 0;
            int upper = input.size() - 1;
            int result = input.size();

            while (upper - lower > -1) {
                int mid = lower + (upper - lower) / 2;
                boolean answer = this.oracle.answerQuery(input.prefix(mid));
                if (answer) {
                    lower = mid + 1;
                } else {
                    result = mid;
                    upper = mid - 1;
                }
            }

            return result - 1;
        }

        throw new IllegalStateException("Could not properly analyze CE");
    }

    private DefaultQuery<SymbolWrapper<I>, Boolean> constructLocalCE(Word<I> input, boolean output) {

        final WordBuilder<SymbolWrapper<I>> wb = new WordBuilder<>(input.length());
        for (I i : input) {
            wb.append(mapper.get(i));
        }

        return new DefaultQuery<>(wb.toWord(), output);
    }

    private void ensureCallAndReturnClosure() {

        final Set<SymbolWrapper<I>> nonContinuableSymbols = new HashSet<>();
        nonContinuableSymbols.add(mapper.get(alphabet.getReturnSymbol()));
        for (I i : alphabet.getCallAlphabet()) {
            final SymbolWrapper<I> mapped = mapper.get(i);
            if (!mapped.isTerminating()) {
                nonContinuableSymbols.add(mapped);
            }
        }

        for (L learner : this.subLearners.values()) {
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
        for (Word<SymbolWrapper<I>> sc : Automata.stateCover(hyp, mapper.values())) {
            cover.add(learner.transformAccessSequence(sc));
        }

        for (Word<SymbolWrapper<I>> cov : cover) {
            final S state = hyp.getState(cov);

            for (SymbolWrapper<I> i : nonContinuableSymbols) {
                final S succ = hyp.getSuccessor(state, i);

                for (SymbolWrapper<I> next : mapper.values()) {

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
}
