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
package de.learnlib.algorithm.aaar;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import de.learnlib.algorithm.LearnerConstructor;
import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.algorithm.aaar.abstraction.AbstractAbstractionTree;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.DefaultQuery;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.GrowingAlphabet;
import net.automatalib.alphabet.SupportsGrowingAlphabet;
import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.automaton.MutableDeterministic;
import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;

/**
 * Base implementation of the learner presented in "Automata Learning with Automated Alphabet Abstraction Refinement" by
 * Howar et al.
 * <p>
 * This implementation is a {@link LearningAlgorithm} for abstract models but operates on concrete systems with concrete
 * input symbols. Therefore, the learner receives concrete counterexamples which are transformed into abstract ones
 * (using the simultaneously inferred {@link AbstractAbstractionTree abstraction tree}) whose symbols are then mapped to
 * their concrete representatives again, prior to actual refinement. Since this concept is agnostic to the actual
 * learning process, this learner can be parameterized with an arbitrary (concrete) learning algorithm for the acutal
 * inference.
 * <p>
 * There exist several accessor for different views on the current hypothesis, e.g., the actual hypothesis of the
 * internal learner, an abstracted hypothesis, and a translating hypothesis which automatically performs the previously
 * mentioned translation steps for offering a model that is able to handle previously unobserved (concrete) input
 * symbols.
 *
 * @param <L>
 *         learner type
 * @param <AM>
 *         abstract model type
 * @param <CM>
 *         concrete model type
 * @param <AI>
 *         abstract input symbol type
 * @param <CI>
 *         concrete input symbol type
 * @param <D>
 *         output domain type
 */
public abstract class AbstractAAARLearner<L extends LearningAlgorithm<CM, CI, D> & SupportsGrowingAlphabet<CI>, AM, CM, AI, CI, D>
        implements LearningAlgorithm<AM, CI, D> {

    private final L learner;
    private final MembershipOracle<CI, D> oracle;

    private final GrowingAlphabet<CI> rep;
    private final GrowingAlphabet<AI> abs;

    public AbstractAAARLearner(LearnerConstructor<L, CI, D> learnerConstructor, MembershipOracle<CI, D> o) {
        this.oracle = o;
        this.rep = new GrowingMapAlphabet<>();
        this.abs = new GrowingMapAlphabet<>();

        this.learner = learnerConstructor.constructLearner(rep, oracle);
    }

    @Override
    public void startLearning() {
        for (AI ai : getInitialAbstracts()) {
            this.abs.addSymbol(ai);
        }
        for (CI ci : getInitialRepresentatives()) {
            this.rep.addSymbol(ci);
            this.learner.addAlphabetSymbol(ci);
        }
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

            if (Objects.equals(outOld, outNew)) {
                prefix = prefix.append(r);
                wb.append(r);
            } else { // add new abstraction
                final AI newA = tree.splitLeaf(r, cur, prefix, suffix, outOld);
                abs.addSymbol(newA);
                rep.addSymbol(cur);
                learner.addAlphabetSymbol(cur);
                return true;
            }
        }

        final int prefixLen = query.getPrefix().length();
        final DefaultQuery<CI, D> concreteCE =
                new DefaultQuery<>(wb.toWord(0, prefixLen), wb.toWord(prefixLen, wb.size()), query.getOutput());

        return learner.refineHypothesis(concreteCE);
    }

    /**
     * Returns the (abstract) alphabet of the current (abstract) hypothesis model (cf. {@link #getHypothesisModel()}).
     *
     * @return the (abstract) alphabet of the current (abstract) hypothesis model
     */
    public Alphabet<AI> getAbstractAlphabet() {
        return this.abs;
    }

    /**
     * Returns the (concrete) alphabet of the current (concrete) internal hypothesis model (cf.
     * {@link #getLearnerHypothesisModel()}).
     *
     * @return the (concrete) alphabet of the current (concrete) internal hypothesis model
     */
    public abstract Alphabet<CI> getLearnerAlphabet();

    /**
     * Returns the (concrete) hypothesis model form the provided internal learner.
     *
     * @return the (concrete) hypothesis model form the provided internal learner
     */
    public CM getLearnerHypothesisModel() {
        return learner.getHypothesisModel();
    }

    /**
     * Returns a model of the current internal hypothesis model (cf. {@link #getLearnerHypothesisModel()}) that
     * automatically transforms (concrete) input symbols to abstract ones and uses their representatives to actually
     * perform transitions. This allows the returned model to handle (concrete) input symbols that have not yet been
     * added to the hypothesis by previous abstraction refinements. Note that this model requires the
     * {@link MembershipOracle} passed to the constructor of this learner to still function in order to determine the
     * abstract input symbols.
     *
     * @return the (concrete) hypothesis model that automatically transforms input symbols
     */
    public abstract CM getTranslatingHypothesisModel();

    /**
     * Returns the created instance of the provided internal learner.
     *
     * @return the created instance of the provided internal learner
     */
    public L getLearner() {
        return this.learner;
    }

    protected <S1, S2, T, SP, TP> void copyAbstract(UniversalDeterministicAutomaton<S1, CI, T, SP, TP> src,
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
                final T trans = src.getTransition(e.getValue(), r);
                if (trans != null) {
                    final AbstractAbstractionTree<AI, CI, D> tree = getTreeForRepresentative(r);
                    final AI a = tree.getAbstractSymbol(r);
                    tgt.setTransition(e.getKey(),
                                      a,
                                      statesRev.get(src.getSuccessor(trans)),
                                      src.getTransitionProperty(trans));
                }
            }
        }
    }

    protected abstract AbstractAbstractionTree<AI, CI, D> getTreeForRepresentative(CI ci);

    protected abstract Collection<AI> getInitialAbstracts();

    protected abstract Collection<CI> getInitialRepresentatives();
}
