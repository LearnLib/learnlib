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

import de.learnlib.api.oracle.LassoEmptinessOracle.DFALassoEmptinessOracle;
import de.learnlib.api.oracle.LassoOracle.DFALassoOracle;
import de.learnlib.api.oracle.OmegaMembershipOracle;
import net.automatalib.modelchecking.Lasso.DFALasso;

public class DFALassoEmptinessOracleImpl<S, I> extends LassoEmptinessOracleImpl<DFALasso<I>, S, I, Boolean>
        implements DFALassoEmptinessOracle<I>, DFALassoOracle<I> {

    public DFALassoEmptinessOracleImpl(OmegaMembershipOracle<S, I, Boolean> omegaMembershipOracle) {
        super(omegaMembershipOracle);
    }
}
