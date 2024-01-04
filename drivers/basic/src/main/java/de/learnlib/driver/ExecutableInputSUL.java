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
package de.learnlib.driver;

import de.learnlib.sul.ExecutableInput;
import de.learnlib.sul.SUL;

/**
 * A {@link SUL} that executes {@link ExecutableInput} symbols.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
public class ExecutableInputSUL<I extends ExecutableInput<? extends O>, O> implements SUL<I, O> {

    @Override
    public void pre() {}

    @Override
    public void post() {}

    @Override
    public O step(I in) {
        return in.execute();
    }

    @Override
    public boolean canFork() {
        return true;
    }

    @Override
    public SUL<I, O> fork() {
        return this;
    }
}
