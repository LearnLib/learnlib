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
package de.learnlib.algorithm.procedural.spmm;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import de.learnlib.AccessSequenceTransformer;
import de.learnlib.algorithm.LearnerConstructor;
import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.algorithm.procedural.SymbolWrapper;
import de.learnlib.algorithm.procedural.spmm.manager.OptimizingATManager;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.util.MQUtil;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.alphabet.SupportsGrowingAlphabet;
import net.automatalib.alphabet.impl.DefaultProceduralInputAlphabet;
import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.automaton.procedural.SPMM;
import net.automatalib.automaton.procedural.impl.EmptySPMM;
import net.automatalib.automaton.procedural.impl.StackSPMM;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.common.util.HashUtil;
import net.automatalib.common.util.Pair;
import net.automatalib.common.util.mapping.Mapping;
import net.automatalib.util.automaton.Automata;
import net.automatalib.util.automaton.procedural.SPMMs;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;

/**
 * A learning algorithm for {@link SPMM}s.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 * @param <L>
 *         sub-learner type
 */
public class SPMMLearner<I, O, L extends MealyLearner<SymbolWrapper<I>, O> & SupportsGrowingAlphabet<SymbolWrapper<I>> & AccessSequenceTransformer<SymbolWrapper<I>>>
        implements LearningAlgorithm<SPMM<?, I, ?, O>, I, Word<O>> {

    private final ProceduralInputAlphabet<I> alphabet;
    private final O errorOutput;
    private final MembershipOracle<I, Word<O>> oracle;
    private final Mapping<I, LearnerConstructor<L, SymbolWrapper<I>, Word<O>>> learnerConstructors;
    private final ATManager<I, O> atManager;

    private final Map<I, L> learners;
    private I initialCallSymbol;
    private O initialOutputSymbol;

    private final Map<I, SymbolWrapper<I>> mapping;

    public SPMMLearner(ProceduralInputAlphabet<I> alphabet,
                       O errorOutput,
                       MembershipOracle<I, Word<O>> oracle,
                       LearnerConstructor<L, SymbolWrapper<I>, Word<O>> learnerConstructor) {
        this(alphabet,
             errorOutput,
             oracle,
             i -> learnerConstructor,
             new OptimizingATManager<>(alphabet, errorOutput));
    }

    public SPMMLearner(ProceduralInputAlphabet<I> alphabet,
                       O errorOutput,
                       MembershipOracle<I, Word<O>> oracle,
                       Mapping<I, LearnerConstructor<L, SymbolWrapper<I>, Word<O>>> learnerConstructors,
                       ATManager<I, O> atManager) {
        this.alphabet = alphabet;
        this.errorOutput = errorOutput;
        this.oracle = oracle;
        this.learnerConstructors = learnerConstructors;
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
    public boolean refineHypothesis(DefaultQuery<I, Word<O>> defaultQuery) {

        if (!MQUtil.isCounterexample(defaultQuery, getHypothesisModel())) {
            return false;
        }

        assert defaultQuery.getPrefix().isEmpty() : "Counterexamples need to provide full trace information";
        assert this.alphabet.isReturnMatched(defaultQuery.getInput()) : "Counterexample has unmatched return symbols";

        boolean changed = extractUsefulInformationFromCounterExample(defaultQuery);

        while (refineHypothesisInternal(defaultQuery)) {
            changed = true;
        }

        ensureReturnClosure();

        assert SPMMs.isValid(getHypothesisModel());

        return changed;
    }

    private boolean refineHypothesisInternal(DefaultQuery<I, Word<O>> defaultQuery) {

        final SPMM<?, I, ?, O> hypothesis = this.getHypothesisModel();

        if (!MQUtil.isCounterexample(defaultQuery, hypothesis)) {
            return false;
        }

        final Word<I> input = defaultQuery.getInput();
        final Word<O> output = defaultQuery.getOutput();

        final int mismatchIdx = detectMismatchingIdx(hypothesis, input, output);

        // extract local ce
        final int callIdx = alphabet.findCallIndex(input, mismatchIdx);
        final I procedure = input.getSymbol(callIdx);

        final Pair<Word<I>, Word<O>> localTraces = alphabet.project(input.subWord(callIdx + 1, mismatchIdx + 1),
                                                                    output.subWord(callIdx + 1, mismatchIdx + 1),
                                                                    0);
        final DefaultQuery<SymbolWrapper<I>, Word<O>> localCE =
                constructLocalCE(localTraces.getFirst(), localTraces.getSecond());
        final boolean localRefinement = this.learners.get(procedure).refineHypothesis(localCE);
        assert localRefinement;

        return true;
    }

    @Override
    public SPMM<?, I, ?, O> getHypothesisModel() {

        if (this.learners.isEmpty()) {
            return new EmptySPMM<>(this.alphabet, errorOutput);
        }

        final Alphabet<SymbolWrapper<I>> internalAlphabet = new GrowingMapAlphabet<>();
        final Alphabet<SymbolWrapper<I>> callAlphabet = new GrowingMapAlphabet<>();
        final SymbolWrapper<I> returnSymbol;

        final Map<I, MealyMachine<?, SymbolWrapper<I>, ?, O>> procedures = getSubModels();
        final Map<SymbolWrapper<I>, MealyMachine<?, SymbolWrapper<I>, ?, O>> mappedProcedures =
                new HashMap<>(HashUtil.capacity(procedures.size()));

        for (Entry<I, MealyMachine<?, SymbolWrapper<I>, ?, O>> e : procedures.entrySet()) {
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

        final StackSPMM<?, SymbolWrapper<I>, ?, O> delegate = new StackSPMM<>(mappedAlphabet,
                                                                              this.mapping.get(initialCallSymbol),
                                                                              initialOutputSymbol,
                                                                              errorOutput,
                                                                              mappedProcedures);

        return new MappingSPMM<>(alphabet, errorOutput, mapping, delegate);
    }

    private boolean extractUsefulInformationFromCounterExample(DefaultQuery<I, Word<O>> defaultQuery) {

        final Word<I> input = defaultQuery.getInput();
        final Word<O> output = defaultQuery.getOutput();

        // CEs should always be rooted at the main procedure
        this.initialCallSymbol = input.firstSymbol();
        this.initialOutputSymbol = output.firstSymbol();

        final Pair<Set<I>, Set<I>> newSeqs = atManager.scanCounterexample(defaultQuery);
        final Set<I> newCalls = newSeqs.getFirst();
        final Set<I> newTerms = newSeqs.getSecond();

        boolean update = false;

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
                                                                                                       errorOutput,
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

    private Map<I, MealyMachine<?, SymbolWrapper<I>, ?, O>> getSubModels() {
        final Map<I, MealyMachine<?, SymbolWrapper<I>, ?, O>> subModels =
                new HashMap<>(HashUtil.capacity(this.learners.size()));

        for (Map.Entry<I, L> entry : this.learners.entrySet()) {
            subModels.put(entry.getKey(), entry.getValue().getHypothesisModel());
        }

        return subModels;
    }

    private DefaultQuery<SymbolWrapper<I>, Word<O>> constructLocalCE(Word<I> input, Word<O> output) {

        final WordBuilder<SymbolWrapper<I>> wb = new WordBuilder<>(input.length());
        for (I i : input) {
            wb.append(mapping.get(i));
        }

        return new DefaultQuery<>(wb.toWord(), output);
    }

    private void ensureReturnClosure() {
        for (L learner : this.learners.values()) {
            boolean stable = false;

            while (!stable) {
                stable = ensureReturnClosure(learner.getHypothesisModel(), mapping.values(), learner);
            }
        }
    }

    private <S, T> boolean ensureReturnClosure(MealyMachine<S, SymbolWrapper<I>, T, O> hyp,
                                               Collection<SymbolWrapper<I>> inputs,
                                               L learner) {

        final Set<Word<SymbolWrapper<I>>> cover = new HashSet<>();
        for (Word<SymbolWrapper<I>> sc : Automata.stateCover(hyp, inputs)) {
            cover.add(learner.transformAccessSequence(sc));
        }

        for (Word<SymbolWrapper<I>> cov : cover) {
            final S state = hyp.getState(cov);

            for (SymbolWrapper<I> i : inputs) {
                if (Objects.equals(i.getDelegate(), alphabet.getReturnSymbol())) {

                    final S succ = hyp.getSuccessor(state, i);

                    for (SymbolWrapper<I> next : inputs) {
                        final O succOut = hyp.getOutput(succ, next);

                        if (!Objects.equals(errorOutput, succOut)) { // error closure is violated
                            // TODO split prefix/suffix? Issue with learners?
                            final Word<SymbolWrapper<I>> lp = cov.append(i);
                            final DefaultQuery<SymbolWrapper<I>, Word<O>> ce = new DefaultQuery<>(Word.epsilon(),
                                                                                                  lp.append(next),
                                                                                                  hyp.computeOutput(lp)
                                                                                                     .append(errorOutput));
                            final boolean refined = learner.refineHypothesis(ce);
                            assert refined;
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    private <S, T> int detectMismatchingIdx(SPMM<S, I, T, O> spmm, Word<I> input, Word<O> output) {

        final Iterator<I> inIter = input.iterator();
        final Iterator<O> outIter = output.iterator();

        S stateIter = spmm.getInitialState();
        int idx = 0;

        while (inIter.hasNext() && outIter.hasNext()) {
            final I i = inIter.next();
            final O o = outIter.next();

            T t = spmm.getTransition(stateIter, i);

            if (t == null || !Objects.equals(o, spmm.getTransitionOutput(t))) {
                return idx;
            }
            stateIter = spmm.getSuccessor(t);
            idx++;
        }

        throw new IllegalArgumentException("Non-counterexamples shouldn't be scanned for a mis-match");
    }
}
