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
package de.learnlib.driver.api;

import de.learnlib.api.SUL;
import de.learnlib.mapper.ExecutableInputSUL;
import de.learnlib.mapper.SULMappers;
import de.learnlib.mapper.api.ExecutableInput;
import de.learnlib.mapper.api.SULMapper;

/**
 * A test driver executes.
 *
 * @param <AI>
 *         abstract input type
 * @param <CI>
 *         concrete input type
 * @param <AO>
 *         abstract output type
 * @param <CO>
 *         concrete output type
 */
public class TestDriver<AI, AO, CI extends ExecutableInput<CO>, CO> implements SUL<AI, AO> {

    private final SUL<AI, AO> sul;

    public TestDriver(SULMapper<AI, AO, CI, CO> mapper) {
        this(SULMappers.apply(mapper, new ExecutableInputSUL<>()));
    }

    private TestDriver(SUL<AI, AO> sul) {
        this.sul = sul;
    }

    @Override
    public void pre() {
        sul.pre();
    }

    @Override
    public void post() {
        sul.post();
    }

    @Override
    public AO step(AI i) {
        return sul.step(i);
    }

    @Override
    public boolean canFork() {
        return sul.canFork();
    }

    @Override
    public SUL<AI, AO> fork() {
        return new TestDriver<>(sul.fork());
    }

}
