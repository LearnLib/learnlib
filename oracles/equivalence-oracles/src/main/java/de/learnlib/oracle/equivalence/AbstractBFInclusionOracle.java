/* Copyright (C) 2013-2019 TU Dortmund
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
package de.learnlib.oracle.equivalence;

import java.util.Collection;

import javax.annotation.Nullable;

import de.learnlib.api.oracle.InclusionOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.util.AbstractBFOracle;
import net.automatalib.automata.concepts.DetOutputAutomaton;

/**
 * An {@link InclusionOracle} that generates words in a breadth-first manner.
 *
 * @author Jeroen Meijer
 *
 * @see InclusionOracle
 * @see AbstractBFOracle
 */
public abstract class AbstractBFInclusionOracle<A extends DetOutputAutomaton<?, I, ?, D>, I, D>
        extends AbstractBFOracle<A, I, D> implements InclusionOracle<A, I, D> {

    public AbstractBFInclusionOracle(MembershipOracle<I, D> membershipOracle, double multiplier) {
        super(membershipOracle, multiplier);
    }

    @Override
    public boolean isCounterExample(A hypothesis, Iterable<? extends I> inputs, @Nullable D output) {
        return InclusionOracle.super.isCounterExample(hypothesis, inputs, output);
    }

    @Nullable
    @Override
    public DefaultQuery<I, D> findCounterExample(A hypothesis, Collection<? extends I> inputs) {
        return super.findCounterExample(hypothesis, inputs);
    }
}
