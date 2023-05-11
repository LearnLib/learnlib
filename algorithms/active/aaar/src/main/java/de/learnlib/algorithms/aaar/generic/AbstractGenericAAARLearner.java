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
package de.learnlib.algorithms.aaar.generic;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

import de.learnlib.algorithms.aaar.AbstractAAARLearner;
import de.learnlib.algorithms.aaar.LearnerProvider;
import de.learnlib.algorithms.aaar.abstraction.AbstractAbstractionTree;
import de.learnlib.algorithms.aaar.abstraction.GenericAbstractionTree;
import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.SupportsGrowingAlphabet;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

/**
 * @author fhowar
 * @author frohme
 */
public abstract class AbstractGenericAAARLearner<L extends LearningAlgorithm<CM, CI, D> & SupportsGrowingAlphabet<CI>, AM, CM, AI, CI, D>
        extends AbstractAAARLearner<L, AM, CM, AI, CI, D> {

    private final AI initialAbstract;
    private final CI initialConcrete;
    private final AbstractAbstractionTree<AI, CI, D> tree;

    public AbstractGenericAAARLearner(LearnerProvider<L, CM, CI, D> learnerProvider,
                                      MembershipOracle<CI, D> o,
                                      CI initialConcrete,
                                      Function<CI, AI> abstractor) {
        super(learnerProvider, o);

        this.initialConcrete = initialConcrete;
        this.initialAbstract = abstractor.apply(initialConcrete);
        this.tree = new GenericAbstractionTree<>(this.initialAbstract, initialConcrete, o, abstractor);
    }

    @Override
    public Alphabet<CI> getLearnerAlphabet() {
        return Alphabets.fromCollection(this.tree.getRepresentativeSymbols());
    }

    @Override
    protected AbstractAbstractionTree<AI, CI, D> getTreeForRepresentative(CI ci) {
        return this.tree;
    }

    @Override
    protected Collection<AI> getInitialAbstracts() {
        return Collections.singleton(this.initialAbstract);
    }

    @Override
    protected Collection<CI> getInitialConcretes() {
        return Collections.singleton(this.initialConcrete);
    }

    public AbstractAbstractionTree<AI, CI, D> getAbstractionTree() {
        return this.tree;
    }
}
