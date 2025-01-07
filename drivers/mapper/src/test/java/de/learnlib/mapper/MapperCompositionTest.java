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

import de.learnlib.Mapper;
import net.automatalib.alphabet.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class MapperCompositionTest {

    private ToUpperCaseMapper toUpperCaseMapper;

    private Mapper<String, String, Character, Character> mapper;

    @BeforeClass
    public void setUp() {
        toUpperCaseMapper = new ToUpperCaseMapper();
        StringMapper<Character> toCharacterMapper = new StringMapper<>(Alphabets.characters('A', 'z'));
        mapper = Mappers.compose(toCharacterMapper, toUpperCaseMapper);
    }

    @Test
    public void testComposition() {
        mapper.pre();

        // small 'a' to big 'A'
        Character mappedInput = mapper.mapInput("a");
        Assert.assertNotNull(mappedInput);
        Assert.assertEquals(mappedInput.charValue(), 'A');

        // big 'B' to big 'B'
        mappedInput = mapper.mapInput("B");
        Assert.assertNotNull(mappedInput);
        Assert.assertEquals(mappedInput.charValue(), 'B');

        // small 'a' to big 'A'
        String mappedOutput = mapper.mapOutput('a');
        Assert.assertNotNull(mappedOutput);
        Assert.assertEquals(mappedOutput, "A");

        // big 'B' to big 'B'
        mappedOutput = mapper.mapOutput('B');
        Assert.assertNotNull(mappedOutput);
        Assert.assertEquals(mappedOutput, "B");

        mapper.post();
        mapper.post();
        mapper.pre();
        mapper.pre();

        Assert.assertEquals(this.toUpperCaseMapper.getPreCounter(), 3);
        Assert.assertEquals(this.toUpperCaseMapper.getPostCounter(), 2);
    }

    private static final class ToUpperCaseMapper implements Mapper<Character, Character, Character, Character> {

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
        public Character mapInput(Character abstractInput) {
            return Character.toUpperCase(abstractInput);
        }

        @Override
        public Character mapOutput(Character concreteOutput) {
            return Character.toUpperCase(concreteOutput);
        }

        int getPreCounter() {
            return preCounter;
        }

        int getPostCounter() {
            return postCounter;
        }
    }
}
