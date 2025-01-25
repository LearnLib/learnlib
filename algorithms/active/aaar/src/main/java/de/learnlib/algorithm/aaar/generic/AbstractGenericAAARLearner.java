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
package de.learnlib.algorithm.aaar.generic;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

import de.learnlib.algorithm.LearnerConstructor;
import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.algorithm.aaar.AbstractAAARLearner;
import de.learnlib.algorithm.aaar.abstraction.AbstractAbstractionTree;
import de.learnlib.algorithm.aaar.abstraction.GenericAbstractionTree;
import de.learnlib.algorithm.aaar.explicit.AbstractExplicitAAARLearner;
import de.learnlib.oracle.MembershipOracle;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.SupportsGrowingAlphabet;
import net.automatalib.alphabet.impl.Alphabets;

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
 */
public abstract class AbstractGenericAAARLearner<L extends LearningAlgorithm<CM, CI, D> & SupportsGrowingAlphabet<CI>, AM, CM, AI, CI, D>
        extends AbstractAAARLearner<L, AM, CM, AI, CI, D> {

    private final AI initialAbstract;
    private final CI initialConcrete;
    private final GenericAbstractionTree<AI, CI, D> tree;

    /**
     * Constructor.
     *
     * @param learnerConstructor
     *         the provider for constructing the internal (concrete) learner
     * @param oracle
     *         the (concrete) membership oracle
     * @param initialConcrete
     *         the initial (concrete) input symbol used for starting the learning process
     * @param abstractor
     *         the function for creating new abstract input symbols given concrete one. This function only receives
     *         input symbols from the provided (concrete) counterexamples
     */
    public AbstractGenericAAARLearner(LearnerConstructor<L, CI, D> learnerConstructor,
                                      MembershipOracle<CI, D> oracle,
                                      CI initialConcrete,
                                      Function<CI, AI> abstractor) {
        super(learnerConstructor, oracle);

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
