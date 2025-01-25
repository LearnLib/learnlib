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
import java.util.concurrent.LinkedBlockingDeque;

import de.learnlib.exception.SULException;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class SimplePOJOTestDriverTest {

    @Test
    public void testDriverWithException() throws Exception {

        final Constructor<?> c = LinkedBlockingDeque.class.getConstructor(int.class);
        final SimplePOJOTestDriver driver = new SimplePOJOTestDriver(c, 2);

        final MethodInput pu1 = driver.addInput("push_1", "addFirst", 1);
        final MethodInput pu2 = driver.addInput("push_2", "addFirst", 2);
        final MethodInput pop = driver.addInput("pop", "removeFirst");

        driver.pre();
        Assert.assertEquals(driver.step(pu1), VoidOutput.INSTANCE);
        Assert.assertEquals(driver.step(pu2), VoidOutput.INSTANCE);
        Assert.assertThrows(SULException.class, () -> driver.step(pu1));
        Assert.assertEquals(driver.step(pop), new ReturnValue<>(2));
        Assert.assertEquals(driver.step(pop), new ReturnValue<>(1));
        Assert.assertThrows(SULException.class, () -> driver.step(pop));
        driver.post();
    }

    @Test
    public void testDriverWithNulls() throws Exception {

        final Constructor<?> c = LinkedBlockingDeque.class.getConstructor(int.class);
        final SimplePOJOTestDriver driver = new SimplePOJOTestDriver(c, 2);

        final MethodInput pu1 = driver.addInput("push_1", "offerFirst", 1);
        final MethodInput pu2 = driver.addInput("push_2", "offerFirst", 2);
        final MethodInput pop = driver.addInput("pop", "pollFirst");

        driver.pre();
        Assert.assertEquals(driver.step(pu1), new ReturnValue<>(true));
        Assert.assertEquals(driver.step(pu2), new ReturnValue<>(true));
        Assert.assertEquals(driver.step(pu1), new ReturnValue<>(false));
        Assert.assertEquals(driver.step(pop), new ReturnValue<>(2));
        Assert.assertEquals(driver.step(pop), new ReturnValue<>(1));
        Assert.assertEquals(driver.step(pop), new ReturnValue<>(null));
        driver.post();
    }

}
