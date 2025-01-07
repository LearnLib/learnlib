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
import java.lang.reflect.Method;

import de.learnlib.driver.ContextExecutableInputSUL;
import de.learnlib.sul.SUL;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.GrowingAlphabet;
import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.common.util.ReflectUtil;

/**
 * A test driver for plain old java objects. Given a constructor of a Java {@link Class}, this {@link SUL} creates
 * objects of the respective class and invokes methods on these objects as specified in the respective
 * {@link #addInput(String, Method, Object...) addInput} methods.
 */
public final class SimplePOJOTestDriver extends ContextExecutableInputSUL<MethodInput, MethodOutput, Object> {

    private final Class<?> instanceClass;
    private final GrowingAlphabet<MethodInput> inputs;

    public SimplePOJOTestDriver(Class<?> c) throws NoSuchMethodException {
        this(c.getConstructor());
    }

    public SimplePOJOTestDriver(Constructor<?> c, Object... cParams) {
        super(new InstanceConstructor(c, cParams));
        this.instanceClass = c.getDeclaringClass();
        this.inputs = new GrowingMapAlphabet<>();
    }

    public MethodInput addInput(String name, String methodName, Object... params) {
        Method m = ReflectUtil.findMatchingMethod(instanceClass, methodName, params);
        if (m == null) {
            throw new IllegalArgumentException();
        }
        return addInput(name, m, params);
    }

    public MethodInput addInput(String name, Method m, Object... params) {
        MethodInput i = new MethodInput(name, m, params);
        inputs.add(i);
        return i;
    }

    public Alphabet<MethodInput> getInputs() {
        return this.inputs;
    }

}
