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
package de.learnlib.filter.statistic.queries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.learnlib.api.query.Query;
import net.automatalib.words.Word;

public abstract class AbstractTestQueries {

    private AbstractTestQueries() {
        throw new AssertionError("Constructor should not be invoked");
    }

    public static <I, D> Collection<Query<I, D>> createNoopQueries(int numQueries) {
        List<Query<I, D>> result = new ArrayList<>(numQueries);
        for (int i = 0; i < numQueries; i++) {
            result.add(new NoopQuery<>(Word.epsilon()));
        }
        return result;
    }

}
