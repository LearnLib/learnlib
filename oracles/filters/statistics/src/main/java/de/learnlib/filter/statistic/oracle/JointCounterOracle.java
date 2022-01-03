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
package de.learnlib.filter.statistic.oracle;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.query.Query;
import de.learnlib.buildtool.refinement.annotation.GenerateRefinement;
import de.learnlib.buildtool.refinement.annotation.Generic;
import de.learnlib.buildtool.refinement.annotation.Interface;
import de.learnlib.buildtool.refinement.annotation.Map;
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
@GenerateRefinement(name = "DFAJointCounterOracle",
                    generics = "I",
                    parentGenerics = {@Generic("I"), @Generic(clazz = Boolean.class)},
                    parameterMapping = @Map(from = MembershipOracle.class,
                                            to = DFAMembershipOracle.class,
                                            withGenerics = "I"),
                    interfaces = @Interface(clazz = DFAMembershipOracle.class, generics = "I"))
@GenerateRefinement(name = "MealyJointCounterOracle",
                    generics = {"I", "O"},
                    parentGenerics = {@Generic("I"), @Generic(clazz = Word.class, generics = "O")},
                    parameterMapping = @Map(from = MembershipOracle.class,
                                            to = MealyMembershipOracle.class,
                                            withGenerics = {"I", "O"}),
                    interfaces = @Interface(clazz = MealyMembershipOracle.class, generics = {"I", "O"}))
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
}
