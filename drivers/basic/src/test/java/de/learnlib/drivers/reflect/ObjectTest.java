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
import java.lang.reflect.Method;

import de.learnlib.oracle.membership.SULOracle;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author falk
 */
@Test
public class ObjectTest {

    @Test
    public void testDriver() throws Exception {

        final StackData testCase = buildStackData(StackWithException.class);

        // push1, push2, pop, pop, pop, pop
        final Word<MethodOutput> output1 = answerFirstQuery(testCase);
        // push1, push2, push1, pop
        final Word<MethodOutput> output2 = answerSecondQuery(testCase);

        Assert.assertTrue(output1.getSymbol(0) instanceof ReturnValue);
        Assert.assertTrue(output1.getSymbol(1) instanceof ReturnValue);
        Assert.assertTrue(output1.getSymbol(2) instanceof ReturnValue);
        Assert.assertTrue(output1.getSymbol(3) instanceof ReturnValue);
        Assert.assertTrue(output1.getSymbol(4) instanceof Error);
        Assert.assertTrue(output1.getSymbol(5) instanceof Unobserved);

        Assert.assertTrue(output2.getSymbol(0) instanceof ReturnValue);
        Assert.assertTrue(output2.getSymbol(1) instanceof ReturnValue);
        Assert.assertTrue(output2.getSymbol(2) instanceof Error);
        Assert.assertTrue(output2.getSymbol(3) instanceof Unobserved);
    }

    @Test
    public void testDriver2() throws Exception {

        final StackData testCase = buildStackData(StackWithNull.class);

        // push1, push2, pop, pop, pop, pop
        final Word<MethodOutput> output1 = answerFirstQuery(testCase);
        // push1, push2, push1, pop
        final Word<MethodOutput> output2 = answerSecondQuery(testCase);

        for (MethodOutput out : output1) {
            Assert.assertTrue(out instanceof ReturnValue);
        }

        for (MethodOutput out : output2) {
            Assert.assertTrue(out instanceof ReturnValue);
        }
    }

    private Word<MethodOutput> answerFirstQuery(StackData stackData) {
        // push1, push2, pop, pop, pop, pop
        return answerQuery(stackData, 0, 1, 2, 2, 2, 2);

    }

    private Word<MethodOutput> answerSecondQuery(StackData stackData) {
        // push1, push2, push1, pop
        return answerQuery(stackData, 0, 1, 0, 2);

    }

    private Word<MethodOutput> answerQuery(StackData stackData, int... inputIndexes) {

        final Alphabet<MethodInput> alphabet = stackData.driver.getInputs();
        SULOracle<MethodInput, MethodOutput> oracle = stackData.oracle;

        final WordBuilder<MethodInput> wb = new WordBuilder<>(inputIndexes.length);

        for (int i : inputIndexes) {
            wb.add(alphabet.apply(i));
        }

        return oracle.answerQuery(wb.toWord());
    }

    private StackData buildStackData(final Class<?> stackClass) throws NoSuchMethodException {
        Constructor<?> c = stackClass.getConstructor(int.class);
        SimplePOJOTestDriver driver = new SimplePOJOTestDriver(c, 2);
        SULOracle<MethodInput, MethodOutput> oracle = new SULOracle<>(driver);

        Method push = stackClass.getMethod("push", Object.class);
        driver.addInput("push_1", push, 1);
        driver.addInput("push_2", push, 2);

        Method popMethod = stackClass.getMethod("pop");
        driver.addInput("pop", popMethod);

        return new StackData(driver, oracle);
    }

    private static class StackData {

        private final SimplePOJOTestDriver driver;
        private final SULOracle<MethodInput, MethodOutput> oracle;

        StackData(SimplePOJOTestDriver driver, SULOracle<MethodInput, MethodOutput> oracle) {
            this.driver = driver;
            this.oracle = oracle;
        }
    }

}