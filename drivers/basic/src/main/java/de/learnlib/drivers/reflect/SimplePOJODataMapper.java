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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import de.learnlib.api.exception.SULException;
import de.learnlib.mapper.api.SULMapper;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Basic stateless data mapper for objects.
 *
 * @author falkhowar
 */
public class SimplePOJODataMapper
        implements SULMapper<MethodInput, MethodOutput, ConcreteMethodInput, @Nullable Object> {

    private final Constructor<?> initMethod;
    private final Object[] initParams;

    protected @Nullable Object delegate;

    protected SimplePOJODataMapper(Constructor<?> initMethod, Object... initParams) {
        this.initMethod = initMethod;
        this.initParams = initParams;
    }

    // RuntimeExceptions are the type of exceptions we allow to handle, therefore we should throw them
    @SuppressWarnings({"PMD.AvoidThrowingRawExceptionTypes", "PMD.PreserveStackTrace"})
    @Override
    public void pre() {
        try {
            delegate = initMethod.newInstance(initParams);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new SULException(e.getCause());
        }
    }

    @Override
    public void post() {
        delegate = null;
    }

    @Override
    public MappedException<? extends MethodOutput> mapUnwrappedException(RuntimeException exception) {
        final Throwable cause = exception.getCause();
        return MappedException.repeatOutput(new Error(cause != null ? cause : exception), Unobserved.INSTANCE);
    }

    @Override
    public ConcreteMethodInput mapInput(MethodInput abstractInput) {
        assert delegate != null;
        return new ConcreteMethodInput(abstractInput, new HashMap<>(), delegate);
    }

    @Override
    public MethodOutput mapOutput(@Nullable Object concreteOutput) {
        return new ReturnValue(concreteOutput);
    }

    @Override
    public boolean canFork() {
        return true;
    }

    @Override
    public SULMapper<MethodInput, MethodOutput, ConcreteMethodInput, @Nullable Object> fork() {
        return new SimplePOJODataMapper(initMethod, initParams);
    }

}
