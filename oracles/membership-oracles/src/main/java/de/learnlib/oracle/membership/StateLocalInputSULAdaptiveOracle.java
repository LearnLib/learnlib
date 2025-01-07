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
package de.learnlib.oracle.membership;

import java.util.Collection;
import java.util.Collections;

import de.learnlib.oracle.AdaptiveMembershipOracle;
import de.learnlib.query.AdaptiveQuery;
import de.learnlib.query.AdaptiveQuery.Response;
import de.learnlib.sul.StateLocalInputSUL;

/**
 * A {@link AdaptiveMembershipOracle} wrapper for {@link StateLocalInputSUL}s.
 * <p>
 * This oracle is <b>not</b> thread-safe.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 *
 * @see SULAdaptiveOracle
 */
public class StateLocalInputSULAdaptiveOracle<I, O> extends SULAdaptiveOracle<I, O>
        implements AdaptiveMembershipOracle<I, O> {

    private final StateLocalInputSUL<I, O> sul;
    private final O undefinedOutput;

    private boolean fetchRequired;

    public StateLocalInputSULAdaptiveOracle(StateLocalInputSUL<I, O> sul, O undefinedOutput) {
        super(sul);
        this.sul = sul;
        this.undefinedOutput = undefinedOutput;
        this.fetchRequired = true;
    }

    @Override
    public void processQuery(AdaptiveQuery<I, O> query) {

        sul.pre();

        Response response;

        do {
            final Collection<I> inputs = this.fetchRequired ? sul.currentlyEnabledInputs() : Collections.emptyList();
            final I in = query.getInput();

            final O out;

            if (inputs.contains(in)) {
                out = sul.step(in);
            } else {
                this.fetchRequired = false;
                out = undefinedOutput;
            }

            response = query.processOutput(out);

            if (response == Response.RESET) {
                fetchRequired = true;
                sul.post();
                sul.pre();
            }
        } while (response != Response.FINISHED);

        fetchRequired = true;
        sul.post();
    }
}
