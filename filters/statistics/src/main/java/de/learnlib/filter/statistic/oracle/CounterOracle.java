/* Copyright (C) 2013-2023 TU Dortmund
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

import java.util.Arrays;
import java.util.Collection;

import de.learnlib.buildtool.refinement.annotation.GenerateRefinement;
import de.learnlib.buildtool.refinement.annotation.Generic;
import de.learnlib.buildtool.refinement.annotation.Interface;
import de.learnlib.buildtool.refinement.annotation.Map;
import de.learnlib.filter.statistic.Counter;
import de.learnlib.filter.statistic.CounterCollection;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.oracle.MembershipOracle.MooreMembershipOracle;
import de.learnlib.query.Query;
import de.learnlib.statistic.StatisticData;
import de.learnlib.statistic.StatisticOracle;
import net.automatalib.word.Word;

/**
 * A {@link MembershipOracle} that counts both the number of queries and the total number of symbols occurring in all
 * those queries.
 *
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 */
@GenerateRefinement(name = "DFACounterOracle",
                    generics = "I",
                    parentGenerics = {@Generic("I"), @Generic(clazz = Boolean.class)},
                    parameterMapping = @Map(from = MembershipOracle.class,
                                            to = DFAMembershipOracle.class,
                                            withGenerics = "I"),
                    interfaces = @Interface(clazz = DFAMembershipOracle.class, generics = "I"))
@GenerateRefinement(name = "MealyCounterOracle",
                    generics = {"I", "O"},
                    parentGenerics = {@Generic("I"), @Generic(clazz = Word.class, generics = "O")},
                    parameterMapping = @Map(from = MembershipOracle.class,
                                            to = MealyMembershipOracle.class,
                                            withGenerics = {"I", "O"}),
                    interfaces = @Interface(clazz = MealyMembershipOracle.class, generics = {"I", "O"}))
@GenerateRefinement(name = "MooreCounterOracle",
                    generics = {"I", "O"},
                    parentGenerics = {@Generic("I"), @Generic(clazz = Word.class, generics = "O")},
                    parameterMapping = @Map(from = MembershipOracle.class,
                                            to = MooreMembershipOracle.class,
                                            withGenerics = {"I", "O"}),
                    interfaces = @Interface(clazz = MooreMembershipOracle.class, generics = {"I", "O"}))
public class CounterOracle<I, D> implements StatisticOracle<I, D> {

    private final MembershipOracle<I, D> delegate;
    private final Counter queryCounter;
    private final Counter symbolCounter;

    public CounterOracle(MembershipOracle<I, D> delegate) {
        this.delegate = delegate;
        this.queryCounter = new Counter("Queries", "#");
        this.symbolCounter = new Counter("Symbols", "#");
    }

    @Override
    public void processQueries(Collection<? extends Query<I, D>> queries) {
        queryCounter.increment(queries.size());
        for (Query<I, D> qry : queries) {
            symbolCounter.increment(qry.getPrefix().length() + qry.getSuffix().length());
        }
        delegate.processQueries(queries);
    }

    /**
     * Retrieves {@link Counter} for the number of queries posed to this oracle.
     *
     * @return the counter of queries
     */
    public Counter getQueryCounter() {
        return queryCounter;
    }

    /**
     * Retrieves the {@link Counter} for the number of symbols in all queries posed to this oracle.
     *
     * @return the counter of symbols
     */
    public Counter getSymbolCounter() {
        return symbolCounter;
    }

    @Override
    public StatisticData getStatisticalData() {
        return new CounterCollection(Arrays.asList(queryCounter, symbolCounter));
    }
}
