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
package de.learnlib.driver.reflect;

import de.learnlib.api.ContextExecutableInput;
import de.learnlib.api.SULMapper;
import de.learnlib.api.exception.MappedException;

/**
 * A mapper that wraps any kind of {@link RuntimeException} that occurs during
 * {@link SimplePOJOTestDriver#step(ContextExecutableInput) steps} of the {@link SimplePOJOTestDriver} into a
 * {@link MappedException} whose output is an {@link Error} object, followed by repeated {@link Unobserved} outputs.
 */
public class SimplePOJOExceptionMapper implements SULMapper<MethodInput, MethodOutput, MethodInput, MethodOutput> {

    @Override
    public MethodInput mapInput(MethodInput input) {
        return input;
    }

    @Override
    public MethodOutput mapOutput(MethodOutput output) {
        return output;
    }

    @Override
    public boolean canFork() {
        return true;
    }

    @Override
    public SimplePOJOExceptionMapper fork() {
        return this;
    }

    @Override
    public MappedException<? extends MethodOutput> mapUnwrappedException(RuntimeException exception) {
        final Throwable cause = exception.getCause();
        return MappedException.repeatOutput(new Error(cause != null ? cause : exception), Unobserved.INSTANCE);
    }

}
