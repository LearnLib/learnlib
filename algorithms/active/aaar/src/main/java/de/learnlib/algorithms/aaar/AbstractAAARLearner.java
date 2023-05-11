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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import de.learnlib.algorithms.aaar.abstraction.AbstractAbstractionTree;
import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.SupportsGrowingAlphabet;
import net.automatalib.automata.MutableDeterministic;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.words.Alphabet;
import net.automatalib.words.GrowingAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import net.automatalib.words.impl.GrowingMapAlphabet;

/**
 * @author fhowar
 * @author frohme
 */
public abstract class AbstractAAARLearner<L extends LearningAlgorithm<CM, CI, D> & SupportsGrowingAlphabet<CI>, AM, CM, AI, CI, D>
        implements LearningAlgorithm<AM, CI, D> {

    private final L learner;
    private final MembershipOracle<CI, D> oracle;

    private final GrowingAlphabet<CI> rep;
    private final GrowingAlphabet<AI> abs;

    public AbstractAAARLearner(LearnerProvider<L, CM, CI, D> learnerProvider, MembershipOracle<CI, D> o) {
        this.oracle = o;
        this.rep = new GrowingMapAlphabet<>();
        this.abs = new GrowingMapAlphabet<>();

        this.learner = learnerProvider.createLearner(rep, oracle);
    }

    @Override
    public void startLearning() {
        getInitialAbstracts().forEach(this.abs::addSymbol);
        getInitialConcretes().forEach(this.rep::addSymbol);
        getInitialConcretes().forEach(((SupportsGrowingAlphabet<CI>) this.learner)::addAlphabetSymbol);
        learner.startLearning();
    }

    @Override
    public boolean refineHypothesis(DefaultQuery<CI, D> query) {

        final Word<CI> input = query.getInput();
        final WordBuilder<CI> wb = new WordBuilder<>(input.size());

        Word<CI> prefix = Word.epsilon();

        for (int i = 0; i < input.size(); i++) {
            final CI cur = input.getSymbol(i);
            // lift & lower
            final AbstractAbstractionTree<AI, CI, D> tree = getTreeForRepresentative(cur);
            final AI a = tree.getAbstractSymbol(cur);
            final CI r = tree.getRepresentative(a);

            final Word<CI> suffix = input.suffix(input.size() - i - 1);

            final Word<CI> testOld = prefix.append(r).concat(suffix);
            final Word<CI> testNew = prefix.append(cur).concat(suffix);

            final D outOld = oracle.answerQuery(testOld);
            final D outNew = oracle.answerQuery(testNew);

            if (!Objects.equals(outOld, outNew)) { // add new abstraction
                final AI newA = tree.splitLeaf(r, cur, prefix, suffix, outOld);
                abs.addSymbol(newA);
                rep.addSymbol(cur);
                learner.addAlphabetSymbol(cur);
                return true;
            } else {
                prefix = prefix.append(r);
                wb.append(r);
            }
        }

        final int prefixLen = query.getPrefix().length();
        final DefaultQuery<CI, D> concreteCE =
                new DefaultQuery<>(wb.toWord(0, prefixLen), wb.toWord(prefixLen, wb.size()), query.getOutput());

        return learner.refineHypothesis(concreteCE);
    }

    protected <S1, S2, SP, TP> void copyAbstract(UniversalDeterministicAutomaton<S1, CI, ?, SP, TP> src,
                                                 MutableDeterministic<S2, AI, ?, SP, TP> tgt) {
        // states
        final Map<S2, S1> states = new HashMap<>();
        final Map<S1, S2> statesRev = new HashMap<>();

        for (S1 s : src.getStates()) {
            final SP sp = src.getStateProperty(s);
            final S2 n = tgt.addState(sp);
            tgt.setInitial(n, Objects.equals(s, src.getInitialState()));

            states.put(n, s);
            statesRev.put(s, n);
        }

        // transitions
        for (Entry<S2, S1> e : states.entrySet()) {
            for (CI r : rep) {
                final AbstractAbstractionTree<AI, CI, D> tree = getTreeForRepresentative(r);
                final AI a = tree.getAbstractSymbol(r);
                tgt.setTransition(e.getKey(),
                                  a,
                                  statesRev.get(src.getSuccessor(e.getValue(), r)),
                                  src.getTransitionProperty(e.getValue(), r));
            }
        }
    }

    public Alphabet<AI> getAbstractAlphabet() {
        return this.abs;
    }

    public L getLearner() {
        return this.learner;
    }

    public CM getLearnerHypothesisModel() {
        return learner.getHypothesisModel();
    }

    public abstract Alphabet<CI> getLearnerAlphabet();

    public abstract CM getTranslatingHypothesisModel();

    protected abstract AbstractAbstractionTree<AI, CI, D> getTreeForRepresentative(CI ci);

    protected abstract Collection<AI> getInitialAbstracts();

    protected abstract Collection<CI> getInitialConcretes();
}
