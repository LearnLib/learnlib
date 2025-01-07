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
package de.learnlib.oracle.property;

import java.util.Collection;

import de.learnlib.oracle.LassoEmptinessOracle.DFALassoEmptinessOracle;
import de.learnlib.oracle.PropertyOracle.DFAPropertyOracle;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.modelchecking.Lasso.DFALasso;
import net.automatalib.modelchecking.ModelCheckerLasso.DFAModelCheckerLasso;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A property oracle for DFAs that can check lassos from the model checker.
 *
 * @param <I>
 *         the input type
 * @param <P>
 *         the property type
 */
public class DFALassoPropertyOracle<I, P> extends AbstractPropertyOracle<I, DFA<?, I>, P, Boolean, DFALasso<I>>
        implements DFAPropertyOracle<I, P> {

    private final DFAModelCheckerLasso<I, P> modelChecker;

    public DFALassoPropertyOracle(P property,
                                  DFAInclusionOracle<I> inclusionOracle,
                                  DFALassoEmptinessOracle<I> emptinessOracle,
                                  DFAModelCheckerLasso<I, P> modelChecker) {
        super(property, inclusionOracle, emptinessOracle);
        this.modelChecker = modelChecker;
    }

    @Override
    protected @Nullable DFALasso<I> modelCheck(DFA<?, I> hypothesis, Collection<? extends I> inputs) {
        return modelChecker.findCounterExample(hypothesis, inputs, getProperty());
    }
}
