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

package de.learnlib.oracle.membership;

import java.util.Collection;
import java.util.Collections;

import de.learnlib.oracle.SymbolQueryOracle;
import de.learnlib.sul.StateLocalInputSUL;

/**
 * A {@link SymbolQueryOracle} wrapper for {@link StateLocalInputSUL}s. See {@link SULSymbolQueryOracle}.
 * <p>
 * This oracle is <b>not</b> thread-safe.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 *
 * @see SULSymbolQueryOracle
 */
public class StateLocalInputSULSymbolQueryOracle<I, O> extends SULSymbolQueryOracle<I, O>
        implements SymbolQueryOracle<I, O> {

    private final StateLocalInputSUL<I, O> sul;
    private final O undefinedOutput;

    private boolean fetchRequired;

    public StateLocalInputSULSymbolQueryOracle(StateLocalInputSUL<I, O> sul, O undefinedOutput) {
        super(sul);
        this.sul = sul;
        this.undefinedOutput = undefinedOutput;
        this.fetchRequired = true;
    }

    @Override
    public void reset() {
        super.reset();
        this.fetchRequired = true;
    }

    @Override
    protected O queryInternal(I i) {
        final Collection<I> enabledInputs = this.fetchRequired ? sul.currentlyEnabledInputs() : Collections.emptyList();

        if (enabledInputs.contains(i)) {
            return sul.step(i);
        } else {
            this.fetchRequired = false;
            return undefinedOutput;
        }
    }
}
