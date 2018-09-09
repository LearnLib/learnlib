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

import de.learnlib.api.oracle.LassoEmptinessOracle;
import de.learnlib.api.oracle.LassoOracle;
import de.learnlib.api.oracle.OmegaMembershipOracle;
import net.automatalib.modelchecking.Lasso;
import net.automatalib.words.Word;

public class MealyLassoEmptinessOracleImpl<S, I, O>
        extends LassoEmptinessOracleImpl<Lasso.MealyLasso<I, O>, S, I, Word<O>>
        implements LassoEmptinessOracle.MealyLassoEmptinessOracle<I, O>, LassoOracle.MealyLassoOracle<I, O> {

    public MealyLassoEmptinessOracleImpl(OmegaMembershipOracle<S, I, Word<O>> omegaMembershipOracle) {
        super(omegaMembershipOracle);
    }
}
