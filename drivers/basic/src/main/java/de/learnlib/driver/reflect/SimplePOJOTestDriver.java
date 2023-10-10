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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;

import de.learnlib.driver.api.TestDriver;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.GrowingAlphabet;
import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.common.util.ReflectUtil;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Simple test driver for plain java objects. Uses a very simple data mapper without state or storage. Inputs cannot
 * have abstract parameters.
 */
public final class SimplePOJOTestDriver
        extends TestDriver<MethodInput, MethodOutput, ConcreteMethodInput, @Nullable Object> {

    private final GrowingAlphabet<MethodInput> inputs = new GrowingMapAlphabet<>();

    private final Class<?> instanceClass;

    public SimplePOJOTestDriver(Class<?> c) throws NoSuchMethodException {
        this(c.getConstructor());
    }

    public SimplePOJOTestDriver(Constructor<?> c, Object... cParams) {
        super(new SimplePOJODataMapper(c, cParams));
        this.instanceClass = c.getDeclaringClass();
    }

    public MethodInput addInput(String name, String methodName, Object... params) {
        Method m = ReflectUtil.findMatchingMethod(instanceClass, methodName, params);
        if (m == null) {
            throw new IllegalArgumentException();
        }
        return addInput(name, m, params);
    }

    public MethodInput addInput(String name, Method m, Object... params) {
        MethodInput i = new MethodInput(name, m, new HashMap<>(), params);
        inputs.add(i);
        return i;
    }

    /**
     * @return the inputs
     */
    public Alphabet<MethodInput> getInputs() {
        return this.inputs;
    }

}
