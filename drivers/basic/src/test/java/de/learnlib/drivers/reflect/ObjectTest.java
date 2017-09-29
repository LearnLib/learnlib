/* Copyright (C) 2013-2017 TU Dortmund
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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import de.learnlib.api.query.DefaultQuery;
import de.learnlib.api.query.Query;
import de.learnlib.oracle.membership.SULOracle;
import net.automatalib.words.Word;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * @author falk
 */
@Test
public class ObjectTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectTest.class);

    @Test
    public void testDriver() throws Exception {

        Constructor<?> c = Stack.class.getConstructor(int.class);
        SimplePOJOTestDriver driver = new SimplePOJOTestDriver(c, 2);
        SULOracle<MethodInput, AbstractMethodOutput> oracle = new SULOracle<>(driver);

        Method push = Stack.class.getMethod("push", Object.class);
        MethodInput push1 = driver.addInput("push_1", push, 1);
        MethodInput push2 = driver.addInput("push_2", push, 2);

        Method popMethod = Stack.class.getMethod("pop");
        MethodInput pop = driver.addInput("pop", popMethod);

        DefaultQuery<MethodInput, Word<AbstractMethodOutput>> query1 =
                new DefaultQuery<>(Word.fromSymbols(push1, push2, pop, pop, pop, pop));
        DefaultQuery<MethodInput, Word<AbstractMethodOutput>> query2 =
                new DefaultQuery<>(Word.fromSymbols(push1, push2, push1, pop));

        Collection<Query<MethodInput, Word<AbstractMethodOutput>>> queries = new ArrayList<>();
        queries.add(query1);
        queries.add(query2);

        oracle.processQueries(queries);

        LOGGER.debug("{}  :  {}", query1.getInput(), query1.getOutput());
        LOGGER.debug("{}  :  {}", query2.getInput(), query2.getOutput());
    }

}