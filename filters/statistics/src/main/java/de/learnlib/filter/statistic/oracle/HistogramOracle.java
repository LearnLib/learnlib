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
package de.learnlib.filter.statistic.oracle;

import java.util.Collection;

import de.learnlib.filter.statistic.HistogramDataSet;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.oracle.MembershipOracle.MooreMembershipOracle;
import de.learnlib.query.Query;
import de.learnlib.statistic.StatisticOracle;
import de.learnlib.tooling.annotation.refinement.GenerateRefinement;
import de.learnlib.tooling.annotation.refinement.Generic;
import de.learnlib.tooling.annotation.refinement.Interface;
import de.learnlib.tooling.annotation.refinement.Mapping;
import net.automatalib.word.Word;

/**
 * Collects a histogram of passed query lengths.
 *
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output symbol type
 */
@GenerateRefinement(name = "DFAHistogramOracle",
                    generics = @Generic(value = "I", desc = "input symbol type"),
                    parentGenerics = {@Generic("I"), @Generic(clazz = Boolean.class)},
                    typeMappings = @Mapping(from = MembershipOracle.class,
                                            to = DFAMembershipOracle.class,
                                            generics = @Generic("I")),
                    interfaces = @Interface(clazz = DFAMembershipOracle.class, generics = @Generic("I")))
@GenerateRefinement(name = "MealyHistogramOracle",
                    generics = {@Generic(value = "I", desc = "input symbol type"),
                                @Generic(value = "O", desc = "output symbol type")},
                    parentGenerics = {@Generic("I"), @Generic(clazz = Word.class, generics = "O")},
                    typeMappings = @Mapping(from = MembershipOracle.class,
                                            to = MealyMembershipOracle.class,
                                            generics = {@Generic("I"), @Generic("O")}),
                    interfaces = @Interface(clazz = MealyMembershipOracle.class,
                                            generics = {@Generic("I"), @Generic("O")}))
@GenerateRefinement(name = "MooreHistogramOracle",
                    generics = {@Generic(value = "I", desc = "input symbol type"),
                                @Generic(value = "O", desc = "output symbol type")},
                    parentGenerics = {@Generic("I"), @Generic(clazz = Word.class, generics = "O")},
                    typeMappings = @Mapping(from = MembershipOracle.class,
                                            to = MooreMembershipOracle.class,
                                            generics = {@Generic("I"), @Generic("O")}),
                    interfaces = @Interface(clazz = MooreMembershipOracle.class,
                                            generics = {@Generic("I"), @Generic("O")}))
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
     * Default constructor.
     *
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

    @Override
    public final HistogramDataSet getStatisticalData() {
        return this.dataSet;
    }
}
