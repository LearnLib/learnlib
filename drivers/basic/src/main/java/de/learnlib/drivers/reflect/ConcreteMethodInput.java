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
package de.learnlib.drivers.reflect;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

import de.learnlib.api.exception.SULException;
import de.learnlib.mapper.api.ExecutableInput;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A concrete inputs contains the information for one specific method call.
 *
 * @author falkhowar
 */
public class ConcreteMethodInput implements ExecutableInput<@Nullable Object> {

    /**
     * corresponding abstract input.
     */
    private final MethodInput input;

    /**
     * parameter values.
     */
    private final Map<String, Object> values;

    /**
     * invocation target.
     */
    private final Object target;

    public ConcreteMethodInput(MethodInput input, Map<String, Object> values, Object target) {
        this.input = input;
        this.values = values;
        this.target = target;
    }

    @Override
    public String toString() {
        return target + "." + this.input.getMethod().getName() + Arrays.toString(getParameterValues());
    }

    private Object[] getParameterValues() {
        return this.input.getParameters(values);
    }

    // RuntimeExceptions are the type of exceptions we allow to handle, therefore we should throw them
    @SuppressWarnings({"PMD.AvoidThrowingRawExceptionTypes", "PMD.PreserveStackTrace"})
    @Override
    public @Nullable Object execute() {
        Object out;
        try {
            Object ret = this.input.getMethod().invoke(this.target, getParameterValues());
            if (this.input.getMethod().getReturnType().equals(Void.TYPE)) {
                out = Void.TYPE;
            } else {
                out = ret;
            }
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new SULException(e.getCause());
        }
        return out;
    }

}
