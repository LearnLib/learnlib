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
package de.learnlib.testsupport.example.mealy;

import de.learnlib.testsupport.example.DefaultLearningExample.DefaultMealyLearningExample;
import de.learnlib.testsupport.example.mealy.ExampleStack.Input;
import de.learnlib.testsupport.example.mealy.ExampleStack.Output;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.transducer.MutableMealyMachine;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.util.automaton.builder.AutomatonBuilders;

/**
 * This example encodes a small stack with a capacity of three elements and "push" and "pop" operations as Mealy
 * machine. Outputs are "ok", "empty" and "full".
 */
public class ExampleStack extends DefaultMealyLearningExample<Input, Output> {

    public ExampleStack() {
        super(constructMachine());
    }

    public static CompactMealy<Input, Output> constructMachine() {
        return constructMachine(new CompactMealy<>(createInputAlphabet()));
    }

    /**
     * Construct and return a machine representation of this example.
     *
     * @param fm
     *         the output object to write the contents to
     * @param <S>
     *         state type
     * @param <T>
     *         transition type
     * @param <A>
     *         automaton type
     *
     * @return machine instance of the example
     */
    public static <S, T, A extends MutableMealyMachine<S, ? super Input, T, ? super Output>> A constructMachine(A fm) {
        // @formatter:off
        return AutomatonBuilders.forMealy(fm)
                .withInitial("s0")
                .from("s0")
                    .on(Input.PUSH).withOutput(Output.OK).to("s1")
                    .on(Input.POP).withOutput(Output.EMPTY).loop()
                .from("s1")
                    .on(Input.PUSH).withOutput(Output.OK).to("s2")
                    .on(Input.POP).withOutput(Output.OK).to("s0")
                .from("s2")
                    .on(Input.PUSH).withOutput(Output.OK).to("s3")
                    .on(Input.POP).withOutput(Output.OK).to("s1")
                .from("s3")
                    .on(Input.PUSH).withOutput(Output.FULL).loop()
                    .on(Input.POP).withOutput(Output.OK).to("s2")
                .create();
        // @formatter:on
    }

    public static Alphabet<Input> createInputAlphabet() {
        return Alphabets.fromEnum(Input.class);
    }

    public static ExampleStack createExample() {
        return new ExampleStack();
    }

    public enum Input {
        PUSH,
        POP
    }

    public enum Output {
        OK,
        EMPTY,
        FULL
    }
}
