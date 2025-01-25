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
package de.learnlib.mapper;

import java.lang.reflect.Constructor;
import java.util.concurrent.LinkedBlockingDeque;

import de.learnlib.driver.reflect.Error;
import de.learnlib.driver.reflect.MethodInput;
import de.learnlib.driver.reflect.MethodOutput;
import de.learnlib.driver.reflect.ReturnValue;
import de.learnlib.driver.reflect.SimplePOJOExceptionMapper;
import de.learnlib.driver.reflect.SimplePOJOTestDriver;
import de.learnlib.driver.reflect.Unobserved;
import de.learnlib.driver.reflect.VoidOutput;
import de.learnlib.exception.SULException;
import de.learnlib.sul.SUL;
import net.automatalib.alphabet.Alphabet;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MappedSULTest {

    @Test
    public void testMapperWithException() throws Exception {
        final Constructor<?> c = LinkedBlockingDeque.class.getConstructor(int.class);
        final SimplePOJOTestDriver driver = new SimplePOJOTestDriver(c, 2);

        final String pu1 = "push_1";
        final String pu2 = "push_2";
        final String pop = "pop";

        driver.addInput(pu1, "addFirst", 1);
        driver.addInput(pu2, "addFirst", 2);
        driver.addInput(pop, "removeFirst");

        final Alphabet<MethodInput> alphabet = driver.getInputs();

        final SUL<String, String> mapper = SULMappers.apply(new StringMapper<>(alphabet), driver);

        driver.pre();
        Assert.assertEquals(mapper.step(pu1), "void");
        Assert.assertEquals(mapper.step(pop), "1");
        Assert.assertEquals(mapper.step(pu1), "void");
        Assert.assertEquals(mapper.step(pu2), "void");
        Assert.assertThrows(SULException.class, () -> mapper.step(pu2));
        driver.post();
    }

    @Test
    public void testMapperWithMappedExceptions() throws Exception {
        final Constructor<?> c = LinkedBlockingDeque.class.getConstructor(int.class);
        final SimplePOJOTestDriver driver = new SimplePOJOTestDriver(c, 2);

        final MethodInput pu1 = driver.addInput("push_1", "addFirst", 1);
        final MethodInput pu2 = driver.addInput("push_2", "addFirst", 2);
        final MethodInput pop = driver.addInput("pop", "removeFirst");

        final SUL<MethodInput, MethodOutput> mapper = SULMappers.apply(new SimplePOJOExceptionMapper(), driver);

        driver.pre();
        Assert.assertEquals(mapper.step(pu1), VoidOutput.INSTANCE);
        Assert.assertEquals(mapper.step(pop), new ReturnValue<>(1));
        Assert.assertEquals(mapper.step(pu2), VoidOutput.INSTANCE);
        Assert.assertEquals(mapper.step(pu2), VoidOutput.INSTANCE);
        Assert.assertEquals(mapper.step(pu1), new Error(new IllegalStateException()));
        Assert.assertEquals(mapper.step(pop), Unobserved.INSTANCE);
        driver.post();
    }

}
