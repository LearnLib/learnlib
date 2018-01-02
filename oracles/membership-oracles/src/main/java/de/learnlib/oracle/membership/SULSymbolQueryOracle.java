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
package de.learnlib.oracle.membership;

import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.api.SUL;
import de.learnlib.api.oracle.SymbolQueryOracle;

/**
 * A wrapper that allows to use a {@link SUL} where a {@link SymbolQueryOracle} is expected.
 *
 * @param <I>
 *         input alphabet type
 * @param <O>
 *         output alphabet type
 *
 * @author frohme
 */
@ParametersAreNonnullByDefault
public class SULSymbolQueryOracle<I, O> implements SymbolQueryOracle<I, O> {

    private final SUL<I, O> sul;

    public SULSymbolQueryOracle(final SUL<I, O> sul) {
        this.sul = sul;
        this.sul.pre();
    }

    @Override
    public O query(I i) {
        return this.sul.step(i);
    }

    @Override
    public void reset() {
        this.sul.post();
        this.sul.pre();
    }
}
