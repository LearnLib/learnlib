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

import de.learnlib.exception.MappedException;
import de.learnlib.exception.SULException;
import de.learnlib.sul.SUL;
import de.learnlib.sul.SULMapper;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SULMapperCompositionTest {

    private static final char OUTER_EXCEPTION_TRIGGER_CHAR = 'X';
    private static final char OUTER_EXCEPTION_RESULT = OUTER_EXCEPTION_TRIGGER_CHAR;

    private static final char INNER_EXCEPTION_TRIGGER_CHAR = 'Y';
    private static final int INNER_EXCEPTION_RESULT = INNER_EXCEPTION_TRIGGER_CHAR;

    private static final char NESTED_EXCEPTION_TRIGGER_CHAR = 'Z';

    private InnerUnwrappedMapper innerUnwrappedMapper;
    private OuterWrappedMapper outerWrappedMapper;
    private SUL<Character, Character> mappedSUL;

    @BeforeClass
    public void setUp() {

        innerUnwrappedMapper = new InnerUnwrappedMapper();
        outerWrappedMapper = new OuterWrappedMapper();

        final SULMapper<Character, Character, Character, Character> toUpperCase =
                SULMappers.compose(outerWrappedMapper, innerUnwrappedMapper);

        this.mappedSUL = SULMappers.apply(toUpperCase, new MockSUL());
    }

    @Test
    public void testComposition() {

        mappedSUL.pre();

        // regular step
        Character result = mappedSUL.step('A');
        Assert.assertNotNull(result);
        Assert.assertEquals(result.charValue(), 'A');

        // exception that returns INNER_EXCEPTION_RESULT
        result = mappedSUL.step(INNER_EXCEPTION_TRIGGER_CHAR);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.charValue(), INNER_EXCEPTION_TRIGGER_CHAR);

        // check repeatedOutput
        result = mappedSUL.step('C');
        Assert.assertNotNull(result);
        Assert.assertEquals(result.charValue(), INNER_EXCEPTION_TRIGGER_CHAR);

        // check repeatedOutput (for other exceptions)
        result = mappedSUL.step(null);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.charValue(), INNER_EXCEPTION_TRIGGER_CHAR);

        // reset
        mappedSUL.post();
        mappedSUL.pre();

        // regular step
        result = mappedSUL.step('A');
        Assert.assertNotNull(result);
        Assert.assertEquals(result.charValue(), 'A');

        // exception that returns OUTER_EXCEPTION_RESULT
        result = mappedSUL.step(OUTER_EXCEPTION_TRIGGER_CHAR);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.charValue(), OUTER_EXCEPTION_RESULT);

        // check next output
        result = mappedSUL.step('C');
        Assert.assertNotNull(result);
        Assert.assertEquals(result.charValue(), 'C');

        // check next output
        result = mappedSUL.step(NESTED_EXCEPTION_TRIGGER_CHAR);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.charValue(), NESTED_EXCEPTION_TRIGGER_CHAR);

        // check unhandled exception
        Assert.assertThrows(NullPointerException.class, () -> mappedSUL.step(null));

        // check pre/post invocations
        mappedSUL.post();
        mappedSUL.pre();
        mappedSUL.pre();

        Assert.assertEquals(this.innerUnwrappedMapper.getPreCounter(), 4);
        Assert.assertEquals(this.outerWrappedMapper.getPreCounter(), 4);

        Assert.assertEquals(this.innerUnwrappedMapper.getPostCounter(), 2);
        Assert.assertEquals(this.outerWrappedMapper.getPostCounter(), 2);

        Assert.assertFalse(mappedSUL.canFork());
        Assert.assertThrows(mappedSUL::fork);
    }

    private static final class MockSUL implements SUL<Character, Character> {

        @Override
        public void pre() {}

        @Override
        public void post() {}

        @Override
        public Character step(Character in) {
            switch (in) {
                case OUTER_EXCEPTION_TRIGGER_CHAR:
                    throw new OuterWrappedException(new IllegalArgumentException());
                case INNER_EXCEPTION_TRIGGER_CHAR:
                    throw new InnerUnwrappedException();
                case NESTED_EXCEPTION_TRIGGER_CHAR:
                    throw new NestedUnwrappedException();
                default:
                    return in;
            }
        }
    }

    private static final class InnerUnwrappedMapper implements SULMapper<Integer, Integer, Character, Character> {

        private int preCounter;
        private int postCounter;

        @Override
        public void pre() {
            preCounter++;
        }

        @Override
        public void post() {
            postCounter++;
        }

        @Override
        public Character mapInput(Integer abstractInput) {
            return (char) abstractInput.intValue();
        }

        @Override
        public Integer mapOutput(Character concreteOutput) {
            return (int) concreteOutput;
        }

        @Override
        public MappedException<? extends Integer> mapUnwrappedException(RuntimeException exception) {
            if (exception instanceof InnerUnwrappedException) {
                return MappedException.repeatOutput(INNER_EXCEPTION_RESULT, INNER_EXCEPTION_RESULT);
            }

            throw exception;
        }

        int getPreCounter() {
            return preCounter;
        }

        int getPostCounter() {
            return postCounter;
        }
    }

    private static final class OuterWrappedMapper implements SULMapper<Character, Character, Integer, Integer> {

        private int preCounter;
        private int postCounter;

        @Override
        public void pre() {
            preCounter++;
        }

        @Override
        public void post() {
            postCounter++;
        }

        @Override
        public Integer mapInput(Character abstractInput) {
            return (int) abstractInput;
        }

        @Override
        public Character mapOutput(Integer concreteOutput) {
            return (char) concreteOutput.intValue();
        }

        @Override
        public MappedException<? extends Character> mapWrappedException(SULException exception) {
            if (exception instanceof OuterWrappedException) {
                return MappedException.ignoreAndContinue(OUTER_EXCEPTION_RESULT);
            }

            return MappedException.pass(exception);
        }

        @Override
        public MappedException<? extends Character> mapUnwrappedException(RuntimeException exception) {
            if (exception instanceof NestedUnwrappedException) {
                return MappedException.ignoreAndContinue(NESTED_EXCEPTION_TRIGGER_CHAR);
            }

            throw exception;
        }

        int getPreCounter() {
            return preCounter;
        }

        int getPostCounter() {
            return postCounter;
        }
    }

    private static final class InnerUnwrappedException extends RuntimeException {}

    private static final class NestedUnwrappedException extends RuntimeException {}

    private static final class OuterWrappedException extends SULException {

        OuterWrappedException(Throwable cause) {
            super(cause);
        }
    }
}
