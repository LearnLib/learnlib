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

import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.oracle.MembershipOracle.MooreMembershipOracle;
import de.learnlib.query.Query;
import de.learnlib.statistic.Statistics;
import de.learnlib.statistic.StatsContainer;
import de.learnlib.tooling.annotation.refinement.GenerateRefinement;
import de.learnlib.tooling.annotation.refinement.Generic;
import de.learnlib.tooling.annotation.refinement.Interface;
import de.learnlib.tooling.annotation.refinement.Mapping;
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
                    generics = @Generic(value = "I", desc = "input symbol type"),
                    parentGenerics = {@Generic("I"), @Generic(clazz = Boolean.class)},
                    typeMappings = @Mapping(from = MembershipOracle.class,
                                            to = DFAMembershipOracle.class,
                                            generics = @Generic("I")),
                    interfaces = @Interface(clazz = DFAMembershipOracle.class, generics = @Generic("I")))
@GenerateRefinement(name = "MealyCounterOracle",
                    generics = {@Generic(value = "I", desc = "input symbol type"),
                                @Generic(value = "O", desc = "output symbol type")},
                    parentGenerics = {@Generic("I"), @Generic(clazz = Word.class, generics = "O")},
                    typeMappings = @Mapping(from = MembershipOracle.class,
                                            to = MealyMembershipOracle.class,
                                            generics = {@Generic("I"), @Generic("O")}),
                    interfaces = @Interface(clazz = MealyMembershipOracle.class,
                                            generics = {@Generic("I"), @Generic("O")}))
@GenerateRefinement(name = "MooreCounterOracle",
                    generics = {@Generic(value = "I", desc = "input symbol type"),
                                @Generic(value = "O", desc = "output symbol type")},
                    parentGenerics = {@Generic("I"), @Generic(clazz = Word.class, generics = "O")},
                    typeMappings = @Mapping(from = MembershipOracle.class,
                                            to = MooreMembershipOracle.class,
                                            generics = {@Generic("I"), @Generic("O")}),
                    interfaces = @Interface(clazz = MooreMembershipOracle.class,
                                            generics = {@Generic("I"), @Generic("O")}))
public class CounterOracle<I, D> implements MembershipOracle<I, D> {

    public static final String DUR_KEY = "-qry-dur";
    public static final String QUERY_KEY = "-qry-cnt";
    public static final String SYMBOL_KEY = "-sym-cnt";

    private final MembershipOracle<I, D> delegate;
    private final StatsContainer statistics;
    private final String prefix;

    public CounterOracle(MembershipOracle<I, D> delegate) {
        this(delegate, "");
    }

    public CounterOracle(MembershipOracle<I, D> delegate, String prefix) {
        this.delegate = delegate;
        this.prefix = prefix;
        this.statistics = Statistics.getContainer();
    }

    @Override
    public void processQueries(Collection<? extends Query<I, D>> queries) {
        statistics.increaseCounter(prefix + QUERY_KEY, "Number of queries", queries.size());
        for (Query<I, D> qry : queries) {
            statistics.increaseCounter(prefix + SYMBOL_KEY, "Number of symbols", qry.getPrefix().length() + qry.getSuffix().length());
        }
        statistics.startOrResumeClock(prefix + DUR_KEY, "Duration of queries");
        delegate.processQueries(queries);
        statistics.pauseClock(prefix + DUR_KEY);
    }
}
