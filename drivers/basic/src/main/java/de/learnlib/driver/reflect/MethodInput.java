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
package de.learnlib.driver.reflect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.learnlib.exception.SULException;
import de.learnlib.sul.ContextExecutableInput;

/**
 * An input symbol that represents a call to a method with a specific set of parameters.
 */
public class MethodInput implements ContextExecutableInput<MethodOutput, Object> {

    private final String displayName;
    private final Method method;
    private final Object[] parameters;

    public MethodInput(String displayName, Method method, Object[] parameters) {
        this.displayName = displayName;
        this.method = method;
        this.parameters = parameters;
    }

    @SuppressWarnings("PMD.PreserveStackTrace")
    @Override
    public MethodOutput execute(Object context) {
        try {
            final Object ret = this.method.invoke(context, parameters);
            if (Void.TYPE.equals(this.method.getReturnType())) {
                return VoidOutput.INSTANCE;
            } else {
                return new ReturnValue<>(ret);
            }
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new SULException(e);
        } catch (InvocationTargetException e) {
            throw new SULException(e.getCause());
        }
    }

    @Override
    public String toString() {
        return this.displayName;
    }

}
