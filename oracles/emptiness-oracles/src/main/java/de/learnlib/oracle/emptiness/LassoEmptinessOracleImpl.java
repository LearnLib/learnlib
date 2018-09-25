/* Copyright (C) 2013-2018 TU Dortmund
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

import javax.annotation.Nullable;

import de.learnlib.api.oracle.LassoEmptinessOracle;
import de.learnlib.api.oracle.LassoOracle;
import de.learnlib.api.oracle.OmegaMembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.api.query.OmegaQuery;
import de.learnlib.util.AbstractBFOracle;
import net.automatalib.modelchecking.Lasso;
import net.automatalib.words.Word;

public class LassoEmptinessOracleImpl<L extends Lasso<I, D>, S, I, D>
        extends AbstractBFOracle<L, I, D>
        implements LassoEmptinessOracle<L, I, D>, LassoOracle<L, I, D> {

    /**
     * The {@link OmegaMembershipOracle} used to answer {@link OmegaQuery}s.
     */
    private final OmegaMembershipOracle<S, I, D> omegaMembershipOracle;

    public LassoEmptinessOracleImpl(OmegaMembershipOracle<S, I, D> omegaMembershipOracle) {
        super(omegaMembershipOracle.getMembershipOracle(), -1.0);
        this.omegaMembershipOracle = omegaMembershipOracle;
    }

    public OmegaMembershipOracle<S, I, D> getOmegaMembershipOracle() {
        return omegaMembershipOracle;
    }

    @Override
    public OmegaQuery<I, D> processOmegaQuery(OmegaQuery<I, D> query) {
        omegaMembershipOracle.processQuery(query);
        return query;
    }

    @Override
    public boolean isCounterExample(L hypothesis, Iterable<? extends I> inputs, @Nullable D output) {
        return LassoEmptinessOracle.super.isCounterExample(hypothesis, inputs, output);
    }

    @Nullable
    @Override
    public DefaultQuery<I, D> findCounterExample(L hypothesis, Collection<? extends I> inputs) {
        return LassoOracle.super.findCounterExample(hypothesis, inputs);
    }

    @Override
    public DefaultQuery<I, D> processInput(L hypothesis, Word<I> input) {
        return LassoOracle.super.processInput(hypothesis, input);
    }
}
