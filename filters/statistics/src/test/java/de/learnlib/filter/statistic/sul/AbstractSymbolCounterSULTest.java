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
package de.learnlib.filter.statistic.sul;

import java.util.Collection;

import de.learnlib.filter.statistic.TestQueries;
import de.learnlib.query.Query;
import de.learnlib.statistic.StatisticSUL;
import net.automatalib.word.Word;

public abstract class AbstractSymbolCounterSULTest<S extends StatisticSUL<Integer, Character>>
        extends AbstractCounterSULTest<S> {

    private static final int QUERY_LENGTH = 5;

    @Override
    protected int getCountIncreasePerQuery() {
        return QUERY_LENGTH;
    }

    @Override
    protected Collection<Query<Integer, Word<Character>>> createQueries(int num) {
        return TestQueries.createNoopQueries(num, QUERY_LENGTH, TestQueries.INPUTS);
    }
}
