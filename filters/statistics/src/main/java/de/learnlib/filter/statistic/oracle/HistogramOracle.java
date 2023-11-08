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

import java.util.Collection;

import de.learnlib.buildtool.refinement.annotation.GenerateRefinement;
import de.learnlib.buildtool.refinement.annotation.Generic;
import de.learnlib.buildtool.refinement.annotation.Interface;
import de.learnlib.buildtool.refinement.annotation.Map;
import de.learnlib.filter.statistic.HistogramDataSet;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.oracle.MembershipOracle.MooreMembershipOracle;
import de.learnlib.query.Query;
import de.learnlib.statistic.StatisticOracle;
import net.automatalib.word.Word;

/**
 * Collects a histogram of passed query lengths.
 *
 * @param <I>
 *         input symbol class
 * @param <D>
 *         output symbol class
 */
@GenerateRefinement(name = "DFAHistogramOracle",
                    generics = "I",
                    parentGenerics = {@Generic("I"), @Generic(clazz = Boolean.class)},
                    parameterMapping = @Map(from = MembershipOracle.class,
                                            to = DFAMembershipOracle.class,
                                            withGenerics = "I"),
                    interfaces = @Interface(clazz = DFAMembershipOracle.class, generics = "I"))
@GenerateRefinement(name = "MealyHistogramOracle",
                    generics = {"I", "O"},
                    parentGenerics = {@Generic("I"), @Generic(clazz = Word.class, generics = "O")},
                    parameterMapping = @Map(from = MembershipOracle.class,
                                            to = MealyMembershipOracle.class,
                                            withGenerics = {"I", "O"}),
                    interfaces = @Interface(clazz = MealyMembershipOracle.class, generics = {"I", "O"}))
@GenerateRefinement(name = "MooreOutputHistogramOracle",
                    generics = {"I", "O"},
                    parentGenerics = {@Generic("I"), @Generic(clazz = Word.class, generics = "O")},
                    parameterMapping = @Map(from = MembershipOracle.class,
                                            to = MooreMembershipOracle.class,
                                            withGenerics = {"I", "O"}),
                    interfaces = @Interface(clazz = MooreMembershipOracle.class, generics = {"I", "O"}))
public class HistogramOracle<I, D> implements StatisticOracle<I, D> {

    /**
     * dataset to be collected.
     */
    private final HistogramDataSet dataSet;

    /**
     * oracle used to answer queries.
     */
    private final MembershipOracle<I, D> delegate;

    /**
     * @param next
     *         real oracle
     * @param name
     *         name of the collected data set
     */
    public HistogramOracle(MembershipOracle<I, D> next, String name) {
        this.delegate = next;
        this.dataSet = new HistogramDataSet(name, "query length");
    }

    @Override
    public final void processQueries(Collection<? extends Query<I, D>> queries) {
        for (Query<I, D> q : queries) {
            this.dataSet.addDataPoint((long) q.getPrefix().size() + q.getSuffix().size());
        }
        this.delegate.processQueries(queries);
    }

    /**
     * @return the data set collected by this oracle.
     */
    @Override
    public final HistogramDataSet getStatisticalData() {
        return this.dataSet;
    }
}
