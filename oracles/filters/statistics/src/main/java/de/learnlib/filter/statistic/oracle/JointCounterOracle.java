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
package de.learnlib.filter.statistic.oracle;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.Query;
import net.automatalib.words.Word;

/**
 * A {@link MembershipOracle} that counts both the number of queries, as well as the total number of symbols occurring
 * in all those queries.
 *
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 *
 * @author Malte Isberner
 */
public class JointCounterOracle<I, D> implements MembershipOracle<I, D> {

    private final MembershipOracle<I, D> delegate;
    private final AtomicLong queryCounter = new AtomicLong();
    private final AtomicLong symbolCounter = new AtomicLong();

    public JointCounterOracle(MembershipOracle<I, D> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void processQueries(Collection<? extends Query<I, D>> queries) {
        queryCounter.addAndGet(queries.size());
        for (Query<I, D> qry : queries) {
            symbolCounter.addAndGet(qry.getInput().length());
        }
        delegate.processQueries(queries);
    }

    /**
     * Retrieves the number of queries posed to this oracle.
     *
     * @return the number of queries
     */
    public long getQueryCount() {
        return queryCounter.get();
    }

    /**
     * Retrieves the number of symbols in all queries posed to this oracle.
     *
     * @return the number of symbols
     */
    public long getSymbolCount() {
        return symbolCounter.get();
    }

    public static class DFAJointCounterOracle<I> extends JointCounterOracle<I, Boolean>
            implements DFAMembershipOracle<I> {

        public DFAJointCounterOracle(MembershipOracle<I, Boolean> delegate) {
            super(delegate);
        }
    }

    public static class MealyJointCounterOracle<I, O> extends JointCounterOracle<I, Word<O>>
            implements MealyMembershipOracle<I, O> {

        public MealyJointCounterOracle(MembershipOracle<I, Word<O>> delegate) {
            super(delegate);
        }
    }

}
