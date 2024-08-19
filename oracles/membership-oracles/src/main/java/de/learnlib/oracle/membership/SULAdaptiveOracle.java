/* Copyright (C) 2013-2024 TU Dortmund University
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
package de.learnlib.oracle.membership;

import de.learnlib.oracle.SingleAdaptiveMembershipOracle;
import de.learnlib.query.AdaptiveQuery;
import de.learnlib.query.AdaptiveQuery.Response;
import de.learnlib.sul.SUL;

public class SULAdaptiveOracle<I, O> implements SingleAdaptiveMembershipOracle<I, O> {

    private final SUL<I, O> sul;

    public SULAdaptiveOracle(SUL<I, O> sul) {
        this.sul = sul;
    }

    @Override
    public void processQuery(AdaptiveQuery<I, O> query) {
        sul.pre();

        Response response;

        do {
            final I in = query.getInput();
            final O out = sul.step(in);

            response = query.processOutput(out);

            if (response == Response.RESET) {
                sul.post();
                sul.pre();
            }
        } while (response != Response.FINISHED);

        sul.post();
    }
}

