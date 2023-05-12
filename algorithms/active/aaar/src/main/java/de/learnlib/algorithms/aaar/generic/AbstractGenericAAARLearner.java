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
import de.learnlib.algorithms.aaar.explicit.AbstractExplicitAAARLearner;
import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.SupportsGrowingAlphabet;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

/**
 * A "generic" refinement of the {@link AbstractAAARLearner}. This implementation uses a single
 * {@link GenericAbstractionTree} for transforming concrete input symbols to abstract ones. This may be useful if no
 * prior knowledge about abstract symbol classes is available (cf. {@link AbstractExplicitAAARLearner}). This
 * implementation only requires a single concrete input symbol to start in the inference process and a rather generic
 * {@link Function abstractor} to create new abstract input symbols.
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
 *
 * @author fhowar
 * @author frohme
 */
public abstract class AbstractGenericAAARLearner<L extends LearningAlgorithm<CM, CI, D> & SupportsGrowingAlphabet<CI>, AM, CM, AI, CI, D>
        extends AbstractAAARLearner<L, AM, CM, AI, CI, D> {

    private final AI initialAbstract;
    private final CI initialConcrete;
    private final GenericAbstractionTree<AI, CI, D> tree;

    /**
     * Constructor.
     *
     * @param learnerProvider
     *         the provider for constructing the internal (concrete) learner
     * @param oracle
     *         the (concrete) membership oracle
     * @param initialConcrete
     *         the initial (concrete) input symbol used for starting the learning process
     * @param abstractor
     *         the function for creating new abstract input symbols given concrete one. This function only receives
     *         input symbols from the provided (concrete) counterexamples
     */
    public AbstractGenericAAARLearner(LearnerProvider<L, CM, CI, D> learnerProvider,
                                      MembershipOracle<CI, D> oracle,
                                      CI initialConcrete,
                                      Function<CI, AI> abstractor) {
        super(learnerProvider, oracle);

        this.initialConcrete = initialConcrete;
        this.initialAbstract = abstractor.apply(initialConcrete);
        this.tree = new GenericAbstractionTree<>(this.initialAbstract, initialConcrete, oracle, abstractor);
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
    protected Collection<CI> getInitialRepresentatives() {
        return Collections.singleton(this.initialConcrete);
    }

    public GenericAbstractionTree<AI, CI, D> getAbstractionTree() {
        return this.tree;
    }
}
