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
package de.learnlib.algorithms.procedural.spmm;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.Maps;
import de.learnlib.algorithms.procedural.AlphabetMapper;
import de.learnlib.algorithms.procedural.SymbolWrapper;
import de.learnlib.algorithms.procedural.spmm.manager.OptimizingATManager;
import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.api.algorithm.LearnerConstructor;
import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.util.MQUtil;
import net.automatalib.SupportsGrowingAlphabet;
import net.automatalib.automata.spmm.EmptySPMM;
import net.automatalib.automata.spmm.SPMM;
import net.automatalib.automata.spmm.StackSPMM;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.commons.util.Pair;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.spmm.SPMMUtil;
import net.automatalib.words.Alphabet;
import net.automatalib.words.SPAAlphabet;
import net.automatalib.words.SPAOutputAlphabet;
import net.automatalib.words.VPDAlphabet.SymbolType;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import net.automatalib.words.impl.DefaultSPAAlphabet;
import net.automatalib.words.impl.GrowingMapAlphabet;

public class SPMMLearner<I, O, L extends MealyLearner<SymbolWrapper<I>, O> & SupportsGrowingAlphabet<SymbolWrapper<I>> & AccessSequenceTransformer<SymbolWrapper<I>>>
        implements LearningAlgorithm<SPMM<?, I, ?, O>, I, Word<O>> {

    private final SPAAlphabet<I> inputAlphabet;
    private final SPAOutputAlphabet<O> outputAlphabet;
    private final MembershipOracle<I, Word<O>> oracle;
    private final Mapping<I, LearnerConstructor<L, SymbolWrapper<I>, Word<O>>> learnerConstructors;
    private final ATManager<I, O> atManager;

    private final Map<I, L> learners;
    private I initialCallSymbol;
    private O initialOutputSymbol;

    private final AlphabetMapper<I> mapper;

    public SPMMLearner(SPAAlphabet<I> inputAlphabet,
                       SPAOutputAlphabet<O> outputAlphabet,
                       MembershipOracle<I, Word<O>> oracle,
                       LearnerConstructor<L, SymbolWrapper<I>, Word<O>> learnerConstructor) {
        this(inputAlphabet,
             outputAlphabet,
             oracle,
             (i) -> learnerConstructor,
             new OptimizingATManager<>(inputAlphabet, outputAlphabet));
    }

    public SPMMLearner(SPAAlphabet<I> inputAlphabet,
                       SPAOutputAlphabet<O> outputAlphabet,
                       MembershipOracle<I, Word<O>> oracle,
                       Mapping<I, LearnerConstructor<L, SymbolWrapper<I>, Word<O>>> learnerConstructors,
                       ATManager<I, O> atManager) {
        this.inputAlphabet = inputAlphabet;
        this.outputAlphabet = outputAlphabet;
        this.oracle = oracle;
        this.learnerConstructors = learnerConstructors;
        this.atManager = atManager;

        this.learners = Maps.newHashMapWithExpectedSize(this.inputAlphabet.getNumCalls());
        this.mapper = new AlphabetMapper<>(inputAlphabet);

        for (I i : this.inputAlphabet.getCallAlphabet()) {
            final SymbolWrapper<I> wrapper = new SymbolWrapper<>(i, false, SymbolType.CALL);
            this.mapper.set(i, wrapper);
        }
        for (I i : this.inputAlphabet.getInternalAlphabet()) {
            final SymbolWrapper<I> wrapper = new SymbolWrapper<>(i, false, SymbolType.INTERNAL);
            this.mapper.set(i, wrapper);
        }

        final SymbolWrapper<I> wrapper =
                new SymbolWrapper<>(this.inputAlphabet.getReturnSymbol(), false, SymbolType.RETURN);
        this.mapper.set(this.inputAlphabet.getReturnSymbol(), wrapper);
    }

    @Override
    public void startLearning() {
        // do nothing, as we have to wait for evidence that the potential main procedure actually terminates
    }

    @Override
    public boolean refineHypothesis(DefaultQuery<I, Word<O>> defaultQuery) {

        boolean changed = this.extractUsefulInformationFromCounterExample(defaultQuery);

        while (refineHypothesisInternal(defaultQuery)) {
            changed = true;
        }

        ensureReturnClosure();

        assert SPMMUtil.isValid(getHypothesisModel());

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
        final int callIdx = inputAlphabet.findCallIndex(input, mismatchIdx);
        final I procedure = input.getSymbol(callIdx);

        final Pair<Word<I>, Word<O>> localTraces = inputAlphabet.project(input.subWord(callIdx + 1, mismatchIdx + 1),
                                                                         output.subWord(callIdx + 1, mismatchIdx + 1),
                                                                         0);
        final DefaultQuery<SymbolWrapper<I>, Word<O>> localCE =
                constructLocalCE(localTraces.getFirst(), localTraces.getSecond());
        final boolean localRefinement = this.learners.get(procedure).refineHypothesis(localCE);

        if (!localRefinement) {
            throw new AssertionError();
        }

        return true;
    }

    @Override
    public SPMM<?, I, ?, O> getHypothesisModel() {

        if (this.learners.isEmpty()) {
            return new EmptySPMM<>(this.inputAlphabet, outputAlphabet);
        }

        final Map<I, MealyMachine<?, SymbolWrapper<I>, ?, O>> procedures = getSubModels();
        final Alphabet<SymbolWrapper<I>> internalAlphabet = new GrowingMapAlphabet<>();
        final Alphabet<SymbolWrapper<I>> callAlphabet = new GrowingMapAlphabet<>();
        final Map<SymbolWrapper<I>, MealyMachine<?, SymbolWrapper<I>, ?, O>> mappedProcedures =
                Maps.newHashMapWithExpectedSize(procedures.size());

        for (I i : this.inputAlphabet) {
            final SymbolWrapper<I> mappedI = this.mapper.get(i);

            if (this.inputAlphabet.isCallSymbol(i)) {
                callAlphabet.add(mappedI);
                final MealyMachine<?, SymbolWrapper<I>, ?, O> p = procedures.get(i);
                if (p != null) {
                    mappedProcedures.put(mappedI, p);
                }
            } else if (inputAlphabet.isInternalSymbol(i)) {
                internalAlphabet.add(mappedI);
            }
        }

        final SPAAlphabet<SymbolWrapper<I>> mappedAlphabet = new DefaultSPAAlphabet<>(internalAlphabet,
                                                                                      callAlphabet,
                                                                                      this.mapper.get(inputAlphabet.getReturnSymbol()));

        final StackSPMM<?, SymbolWrapper<I>, ?, O> delegate = new StackSPMM<>(mappedAlphabet,
                                                                              outputAlphabet,
                                                                              this.mapper.get(initialCallSymbol),
                                                                              initialOutputSymbol,
                                                                              mappedProcedures);

        return new MappingSPMM<>(inputAlphabet, outputAlphabet, mapper, delegate);
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
            final SymbolWrapper<I> sym = new SymbolWrapper<>(call, true, SymbolType.CALL);
            this.mapper.set(call, sym);
            for (L learner : this.learners.values()) {
                learner.addAlphabetSymbol(sym);
                update = true;
            }
        }

        for (I sym : newCalls) {
            update = true;
            final L newLearner = learnerConstructors.get(sym)
                                                    .constructLearner(new GrowingMapAlphabet<>(this.mapper.values()),
                                                                      new ProceduralMembershipOracle<>(inputAlphabet,
                                                                                                       oracle,
                                                                                                       sym,
                                                                                                       outputAlphabet.getErrorSymbol(),
                                                                                                       atManager));

            newLearner.startLearning();

            // add new learner here, so that we have an AccessSequenceTransformer available when scanning for shorter ts
            this.learners.put(sym, newLearner);

            // try to find a shorter terminating sequence for 'sym' before procedure is added to other hypotheses
            final Set<I> newTS =
                    this.atManager.scanRefinedProcedures(Collections.singletonMap(sym, newLearner.getHypothesisModel()),
                                                         learners,
                                                         mapper.values());

            for (I call : newTS) {
                final SymbolWrapper<I> wrapper = new SymbolWrapper<>(call, true, SymbolType.CALL);
                this.mapper.set(call, wrapper);
                for (L learner : this.learners.values()) {
                    learner.addAlphabetSymbol(wrapper);
                }
            }
        }

        return update;
    }

    private Map<I, MealyMachine<?, SymbolWrapper<I>, ?, O>> getSubModels() {
        final Map<I, MealyMachine<?, SymbolWrapper<I>, ?, O>> subModels =
                Maps.newHashMapWithExpectedSize(this.learners.size());

        for (final Map.Entry<I, L> entry : this.learners.entrySet()) {
            subModels.put(entry.getKey(), entry.getValue().getHypothesisModel());
        }

        return subModels;
    }

    private DefaultQuery<SymbolWrapper<I>, Word<O>> constructLocalCE(Word<I> input, Word<O> output) {

        final WordBuilder<SymbolWrapper<I>> wb = new WordBuilder<>(input.length());
        for (I i : input) {
            wb.append(mapper.get(i));
        }

        return new DefaultQuery<>(wb.toWord(), output);
    }

    private void ensureReturnClosure() {
        for (L learner : this.learners.values()) {
            boolean stable = false;

            while (!stable) {
                stable = ensureReturnClosure(learner.getHypothesisModel(), mapper.values(), learner);
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
                if (i.getType() == SymbolType.RETURN) {

                    final S succ = hyp.getSuccessor(state, i);

                    for (SymbolWrapper<I> next : inputs) {
                        final O succOut = hyp.getOutput(succ, next);

                        if (!outputAlphabet.isErrorSymbol(succOut)) { // error closure is violated
                            // TODO split prefix/suffix? Issue with learners?
                            final Word<SymbolWrapper<I>> lp = cov.append(i);
                            final DefaultQuery<SymbolWrapper<I>, Word<O>> ce = new DefaultQuery<>(Word.epsilon(),
                                                                                                  lp.append(next),
                                                                                                  hyp.computeOutput(lp)
                                                                                                     .append(outputAlphabet.getErrorSymbol()));
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

        return -1;
    }
}
