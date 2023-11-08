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
package de.learnlib.oracle.emptiness;

import java.util.Collection;

import de.learnlib.buildtool.refinement.annotation.GenerateRefinement;
import de.learnlib.buildtool.refinement.annotation.Generic;
import de.learnlib.buildtool.refinement.annotation.Interface;
import de.learnlib.buildtool.refinement.annotation.Map;
import de.learnlib.oracle.LassoEmptinessOracle;
import de.learnlib.oracle.LassoEmptinessOracle.DFALassoEmptinessOracle;
import de.learnlib.oracle.LassoEmptinessOracle.MealyLassoEmptinessOracle;
import de.learnlib.oracle.LassoOracle;
import de.learnlib.oracle.LassoOracle.DFALassoOracle;
import de.learnlib.oracle.LassoOracle.MealyLassoOracle;
import de.learnlib.oracle.OmegaMembershipOracle;
import de.learnlib.oracle.OmegaMembershipOracle.DFAOmegaMembershipOracle;
import de.learnlib.oracle.OmegaMembershipOracle.MealyOmegaMembershipOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.query.OmegaQuery;
import net.automatalib.automaton.concept.Output;
import net.automatalib.modelchecking.Lasso;
import net.automatalib.modelchecking.Lasso.DFALasso;
import net.automatalib.modelchecking.Lasso.MealyLasso;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

@GenerateRefinement(name = "DFALassoEmptinessOracleImpl",
                    generics = {"S", "I"},
                    parentGenerics = {@Generic(clazz = DFALasso.class, generics = "I"),
                                      @Generic("S"),
                                      @Generic("I"),
                                      @Generic(clazz = Boolean.class)},
                    parameterMapping = @Map(from = OmegaMembershipOracle.class,
                                            to = DFAOmegaMembershipOracle.class,
                                            withGenerics = {"S", "I"}),
                    interfaces = {@Interface(clazz = DFALassoEmptinessOracle.class, generics = "I"),
                                  @Interface(clazz = DFALassoOracle.class, generics = "I")})
@GenerateRefinement(name = "MealyLassoEmptinessOracleImpl",
                    generics = {"S", "I", "O"},
                    parentGenerics = {@Generic(clazz = MealyLasso.class, generics = {"I", "O"}),
                                      @Generic("S"),
                                      @Generic("I"),
                                      @Generic(clazz = Word.class, generics = "O")},
                    parameterMapping = @Map(from = OmegaMembershipOracle.class,
                                            to = MealyOmegaMembershipOracle.class,
                                            withGenerics = {"S", "I", "O"}),
                    interfaces = {@Interface(clazz = MealyLassoEmptinessOracle.class, generics = {"I", "O"}),
                                  @Interface(clazz = MealyLassoOracle.class, generics = {"I", "O"})})
public class LassoEmptinessOracleImpl<L extends Lasso<I, D>, S, I, D>
        implements LassoEmptinessOracle<L, I, D>, LassoOracle<L, I, D> {

    /**
     * The {@link OmegaMembershipOracle} used to answer {@link OmegaQuery}s.
     */
    private final OmegaMembershipOracle<S, I, D> omegaMembershipOracle;

    public LassoEmptinessOracleImpl(OmegaMembershipOracle<S, I, D> omegaMembershipOracle) {
        this.omegaMembershipOracle = omegaMembershipOracle;
    }

    public OmegaMembershipOracle<S, I, D> getOmegaMembershipOracle() {
        return omegaMembershipOracle;
    }

    @Override
    public OmegaQuery<I, D> processInput(Word<I> prefix, Word<I> loop, int repeat) {
        final OmegaQuery<I, D> query = new OmegaQuery<>(prefix, loop, repeat);
        omegaMembershipOracle.processQuery(query);

        return query;
    }

    @Override
    public boolean isCounterExample(Output<I, D> hypothesis, Iterable<? extends I> input, D output) {
        return LassoEmptinessOracle.super.isCounterExample(hypothesis, input, output);
    }

    @Override
    public @Nullable DefaultQuery<I, D> findCounterExample(L hypothesis, Collection<? extends I> inputs) {
        return LassoOracle.super.findCounterExample(hypothesis, inputs);
    }
}
