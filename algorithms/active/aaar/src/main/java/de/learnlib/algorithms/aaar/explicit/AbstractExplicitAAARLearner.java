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
package de.learnlib.algorithms.aaar.explicit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.Maps;
import de.learnlib.algorithms.aaar.AbstractAAARLearner;
import de.learnlib.algorithms.aaar.ExplicitInitialAbstraction;
import de.learnlib.algorithms.aaar.LearnerProvider;
import de.learnlib.algorithms.aaar.abstraction.AbstractAbstractionTree;
import de.learnlib.algorithms.aaar.abstraction.ExplicitAbstractionTree;
import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.SupportsGrowingAlphabet;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

/**
 * @author fhowar
 * @author frohme
 */
public abstract class AbstractExplicitAAARLearner<L extends LearningAlgorithm<CM, CI, D> & SupportsGrowingAlphabet<CI>, AM, CM, AI, CI, D>
        extends AbstractAAARLearner<L, AM, CM, AI, CI, D> {

    private final ExplicitInitialAbstraction<AI, CI> explicitInitialAbstraction;
    private final Map<AI, AbstractAbstractionTree<AI, CI, D>> trees;

    public AbstractExplicitAAARLearner(LearnerProvider<L, CM, CI, D> learnerProvider,
                                       MembershipOracle<CI, D> o,
                                       ExplicitInitialAbstraction<AI, CI> explicitInitialAbstraction,
                                       Function<AI, AI> incrementor) {
        super(learnerProvider, o);

        this.explicitInitialAbstraction = explicitInitialAbstraction;
        this.trees = Maps.newHashMapWithExpectedSize(explicitInitialAbstraction.getSigmaA().size());

        for (AI a : explicitInitialAbstraction.getSigmaA()) {
            final CI rep = explicitInitialAbstraction.getRepresentative(a);
            this.trees.put(a, new ExplicitAbstractionTree<>(a, rep, o, incrementor));
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
        return this.explicitInitialAbstraction.getSigmaA();
    }

    @Override
    protected Collection<CI> getInitialConcretes() {

        final Collection<AI> sigmaA = this.explicitInitialAbstraction.getSigmaA();
        final Collection<CI> sigmaC = new ArrayList<>(sigmaA.size());

        for (AI ai : sigmaA) {
            sigmaC.add(this.explicitInitialAbstraction.getRepresentative(ai));
        }

        return sigmaC;
    }

    public Map<AI, AbstractAbstractionTree<AI, CI, D>> getAbstractionTrees() {
        return this.trees;
    }

}
