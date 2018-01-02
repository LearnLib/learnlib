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

import de.learnlib.api.query.AbstractQuery;
import de.learnlib.api.query.Query;
import net.automatalib.words.Word;
import org.testng.Assert;

public class UnanswerableQuery<I, D> extends AbstractQuery<I, D> {

    public UnanswerableQuery(Query<I, ?> query) {
        super(query);
    }

    public UnanswerableQuery(Word<I> queryWord) {
        super(queryWord);
    }

    public UnanswerableQuery(Word<I> prefix, Word<I> suffix) {
        super(prefix, suffix);
    }

    @Override
    public void answer(D output) {
        Assert.fail("Query should not be answered");
    }

}
