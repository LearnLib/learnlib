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

import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.oracle.MembershipOracle.MooreMembershipOracle;
import de.learnlib.api.query.Query;
import de.learnlib.api.statistic.StatisticOracle;
import de.learnlib.buildtool.refinement.annotation.GenerateRefinement;
import de.learnlib.buildtool.refinement.annotation.Generic;
import de.learnlib.buildtool.refinement.annotation.Interface;
import de.learnlib.buildtool.refinement.annotation.Map;
import de.learnlib.filter.statistic.Counter;
import net.automatalib.words.Word;

/**
 * Counts queries.
 *
 * @author falkhowar
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

    private final Counter counter;
    private MembershipOracle<I, D> nextOracle;

    public CounterOracle(MembershipOracle<I, D> nextOracle, String name) {
        this.nextOracle = nextOracle;
        this.counter = new Counter(name, "queries");
    }

    @Override
    public void processQueries(Collection<? extends Query<I, D>> queries) {
        this.counter.increment(queries.size());
        nextOracle.processQueries(queries);
    }

    @Override
    public Counter getStatisticalData() {
        return this.counter;
    }

    public Counter getCounter() {
        return this.counter;
    }

    public long getCount() {
        return counter.getCount();
    }

    @Override
    public void setNext(MembershipOracle<I, D> next) {
        this.nextOracle = next;
    }
}
