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

import java.util.HashMap;
import java.util.Map;

import de.learnlib.sul.SULMapper;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.GrowingMapAlphabet;

public class StringMapper<CI> implements SULMapper<String, String, CI, Object> {

    private final Map<String, CI> inputs = new HashMap<>();
    private final Alphabet<String> mappedInputs = new GrowingMapAlphabet<>();

    public StringMapper(Alphabet<CI> alphabet) {
        for (CI input : alphabet) {
            String str = String.valueOf(input);
            inputs.put(str, input);
            mappedInputs.add(str);
        }
    }

    @Override
    public CI mapInput(String abstractInput) {
        return inputs.get(abstractInput);
    }

    @Override
    public String mapOutput(Object concreteOutput) {
        return concreteOutput.toString();
    }

    public Alphabet<String> getMappedInputs() {
        return mappedInputs;
    }
}
