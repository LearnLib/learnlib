/* Copyright (C) 2013-2026 TU Dortmund University
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
import de.learnlib.statistic.StatisticsKey;
import de.learnlib.statistic.StatisticsService;
import de.learnlib.tooling.annotation.refinement.GenerateRefinement;
import de.learnlib.tooling.annotation.refinement.Generic;
import de.learnlib.tooling.annotation.refinement.Interface;
import de.learnlib.tooling.annotation.refinement.Mapping;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

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

    /**
     * The {@link StatisticsKey} this class uses for counting the number of
     * {@link MembershipOracle#processQueries(Collection) queries} executed on the membership oracle.
     */
    public static final StatisticsKey KEY_QUERY = new StatisticsKey("mq-qry-cnt", "Number of queries");

    /**
     * The {@link StatisticsKey} this class uses for counting the number of {@link Query#length() symbols} contained in
     * the executed queries.
     */
    public static final StatisticsKey KEY_SYMBOL = new StatisticsKey("mq-sym-cnt", "Number of symbols");

    private final MembershipOracle<I, D> delegate;
    private final StatisticsService statistics;
    private final StatisticsKey keyQuery;
    private final StatisticsKey keySymbol;

    /**
     * Convenience constructor for {@link CounterOracle#CounterOracle(MembershipOracle, String)} which uses {@code null}
     * as {@code id}.
     *
     * @param delegate
     *         the oracle to delegate calls to
     */
    public CounterOracle(MembershipOracle<I, D> delegate) {
        this(delegate, null);
    }

    /**
     * Constructs a new counter oracle that writes statistical data to a {@link StatisticsService}. The provided
     * {@code id} is used to refine the supported {@link StatisticsKey}s and allows for using multiple instances of this
     * class for different purposes.
     *
     * @param delegate
     *         the oracle to delegate calls to
     * @param id
     *         the id used for specializing the statistics keys
     */
    public CounterOracle(MembershipOracle<I, D> delegate, @Nullable String id) {
        this.delegate = delegate;

        this.statistics = Statistics.getService();
        this.keyQuery = KEY_QUERY.withId(id);
        this.keySymbol = KEY_SYMBOL.withId(id);
    }

    @Override
    public void processQueries(Collection<? extends Query<I, D>> queries) {
        long symCounter = 0;
        for (Query<I, D> qry : queries) {
            symCounter += qry.length();
        }
        statistics.increaseCounter(keyQuery, queries.size(), this);
        statistics.increaseCounter(keySymbol, symCounter, this);
        delegate.processQueries(queries);
    }
}
