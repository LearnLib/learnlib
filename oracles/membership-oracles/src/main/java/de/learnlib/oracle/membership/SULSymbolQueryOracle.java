/* Copyright (C) 2013-2022 TU Dortmund
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

import de.learnlib.api.SUL;
import de.learnlib.api.oracle.SymbolQueryOracle;

/**
 * A wrapper that allows to use a {@link SUL} where a {@link SymbolQueryOracle} is expected.
 * <p>
 * <b>Implementation note</b>: The contract of {@link SymbolQueryOracle} does not make any assumptions about when its
 * {@link SymbolQueryOracle#reset() reset} method is called. However, from a {@link SUL} perspective it is desirable to
 * call its {@link SUL#post() post} method once querying is done. Therefore, multiple calls to {@code this.}{@link
 * SULSymbolQueryOracle#reset()} will {@link SUL#post() close} the underlying {@link SUL} only once, so that the {@link
 * SUL} can be shutdown by {@code this} oracle from outside, after the learning process has finished.
 * <p>
 * This oracle is <b>not</b> thread-safe.
 *
 * @param <I>
 *         input alphabet type
 * @param <O>
 *         output alphabet type
 *
 * @author frohme
 */
public class SULSymbolQueryOracle<I, O> implements SymbolQueryOracle<I, O> {

    private final SUL<I, O> sul;

    private boolean preRequired;
    private boolean postRequired;

    public SULSymbolQueryOracle(final SUL<I, O> sul) {
        this.sul = sul;
        this.preRequired = true;
    }

    @Override
    public O query(I i) {
        if (preRequired) {
            this.sul.pre();
            this.preRequired = false;
            this.postRequired = true;
        }

        return queryInternal(i);
    }

    @Override
    public void reset() {
        if (postRequired) {
            this.sul.post();
            this.postRequired = false;
        }
        this.preRequired = true;
    }

    protected O queryInternal(I i) {
        return this.sul.step(i);
    }
}
