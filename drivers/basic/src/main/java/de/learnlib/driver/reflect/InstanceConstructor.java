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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import de.learnlib.exception.SULException;
import de.learnlib.sul.ContextHandler;

final class InstanceConstructor implements ContextHandler<Object> {

    private final Constructor<?> constructor;
    private final Object[] params;

    InstanceConstructor(Constructor<?> constructor, Object[] params) {
        this.constructor = constructor;
        this.params = params;
    }

    @SuppressWarnings("PMD.PreserveStackTrace")
    @Override
    public Object createContext() {
        try {
            return constructor.newInstance(params);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new SULException(e);
        } catch (InvocationTargetException e) {
            throw new SULException(e.getCause());
        }
    }

    @Override
    public void disposeContext(Object context) {
        // do nothing
    }
}
