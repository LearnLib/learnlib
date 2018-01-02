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

import java.util.Collection;

import de.learnlib.api.oracle.Filter;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.Query;

/**
 * A chain of oracles.
 *
 * @author falkhowar
 */
public class FilterChain<I, D> implements MembershipOracle<I, D> {

    private final MembershipOracle<I, D> oracle;

    @SafeVarargs
    public FilterChain(MembershipOracle<I, D> endpoint, Filter<I, D>... chain) {
        if (chain.length < 1) {
            this.oracle = endpoint;
            return;
        }

        this.oracle = chain[0];
        for (int i = 0; i < chain.length - 1; i++) {
            chain[i].setNext(chain[i + 1]);
        }
        chain[chain.length - 1].setNext(endpoint);
    }

    @Override
    public void processQueries(Collection<? extends Query<I, D>> queries) {
        this.oracle.processQueries(queries);
    }

}
