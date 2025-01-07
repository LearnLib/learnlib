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
package de.learnlib.algorithm.aaar.explicit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import de.learnlib.algorithm.LearnerConstructor;
import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.algorithm.aaar.AbstractAAARLearner;
import de.learnlib.algorithm.aaar.ExplicitInitialAbstraction;
import de.learnlib.algorithm.aaar.abstraction.AbstractAbstractionTree;
import de.learnlib.algorithm.aaar.abstraction.ExplicitAbstractionTree;
import de.learnlib.algorithm.aaar.generic.AbstractGenericAAARLearner;
import de.learnlib.oracle.MembershipOracle;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.SupportsGrowingAlphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.common.util.HashUtil;

/**
 * An "explicit" refinement of the {@link AbstractAAARLearner}. This implementation requires a prior partition of (all)
 * concrete input symbols into abstract symbol classes. Concrete input symbols are only distinguished within their
 * initially provided abstract class (using multiple {@link ExplicitAbstractionTree}s). This may improve performance
 * because the individual discrimination trees may be smaller than a globally shared one (cf.
 * {@link AbstractGenericAAARLearner}). This class requires an {@link ExplicitInitialAbstraction} to provide information
 * about the initial partitioning and an {@link Function incrementor} to increment the initially specified abstract
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
public abstract class AbstractExplicitAAARLearner<L extends LearningAlgorithm<CM, CI, D> & SupportsGrowingAlphabet<CI>, AM, CM, AI, CI, D>
        extends AbstractAAARLearner<L, AM, CM, AI, CI, D> {

    private final ExplicitInitialAbstraction<AI, CI> explicitInitialAbstraction;
    private final Map<AI, ExplicitAbstractionTree<AI, CI, D>> trees;

    /**
     * Constructor.
     *
     * @param learnerConstructor
     *         the provider for constructing the internal (concrete) learner
     * @param oracle
     *         the (concrete) membership oracle
     * @param explicitInitialAbstraction
     *         the initial mapping between concrete and abstract input symbols
     * @param incrementor
     *         the function for creating new abstract input symbols given concrete one. This function only receives
     *         input symbols from the provided explicitInitialAbstraction
     */
    public AbstractExplicitAAARLearner(LearnerConstructor<L, CI, D> learnerConstructor,
                                       MembershipOracle<CI, D> oracle,
                                       ExplicitInitialAbstraction<AI, CI> explicitInitialAbstraction,
                                       Function<AI, AI> incrementor) {
        super(learnerConstructor, oracle);

        this.explicitInitialAbstraction = explicitInitialAbstraction;
        this.trees = new HashMap<>(HashUtil.capacity(explicitInitialAbstraction.getInitialAbstracts().size()));

        for (AI a : explicitInitialAbstraction.getInitialAbstracts()) {
            final CI rep = explicitInitialAbstraction.getRepresentative(a);
            this.trees.put(a, new ExplicitAbstractionTree<>(a, rep, oracle, incrementor));
        }
    }

    @Override
    public Alphabet<CI> getLearnerAlphabet() {
        final Set<CI> symbols = new HashSet<>();

        for (AbstractAbstractionTree<AI, CI, D> t : this.trees.values()) {
            symbols.addAll(t.getRepresentativeSymbols());
        }

        return Alphabets.fromCollection(symbols);
    }

    @Override
    protected AbstractAbstractionTree<AI, CI, D> getTreeForRepresentative(CI ci) {
        final AbstractAbstractionTree<AI, CI, D> tree =
                this.trees.get(this.explicitInitialAbstraction.getAbstractSymbol(ci));
        assert tree != null;
        return tree;
    }

    @Override
    protected Collection<AI> getInitialAbstracts() {
        return this.explicitInitialAbstraction.getInitialAbstracts();
    }

    @Override
    protected Collection<CI> getInitialRepresentatives() {

        final Collection<AI> abs = this.explicitInitialAbstraction.getInitialAbstracts();
        final Collection<CI> rep = new ArrayList<>(abs.size());

        for (AI ai : abs) {
            rep.add(this.explicitInitialAbstraction.getRepresentative(ai));
        }

        return rep;
    }

    public Map<AI, ExplicitAbstractionTree<AI, CI, D>> getAbstractionTrees() {
        return this.trees;
    }

}
