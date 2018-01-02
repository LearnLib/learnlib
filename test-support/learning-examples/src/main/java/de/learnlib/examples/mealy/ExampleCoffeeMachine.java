/* Copyright (C) 2013-2018 TU Dortmund
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
package de.learnlib.examples.mealy;

import de.learnlib.examples.DefaultLearningExample.DefaultMealyLearningExample;
import de.learnlib.examples.mealy.ExampleCoffeeMachine.Input;
import net.automatalib.automata.transout.MutableMealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

/**
 * This example represents the Coffee Machine example from Steffen et al. "Introduction to Active Automata Learning from
 * a Practical Perspective" (Figure 3)
 *
 * @author Maik Merten
 */
public class ExampleCoffeeMachine extends DefaultMealyLearningExample<Input, String> {

    public static final String OUT_OK = "ok";
    public static final String OUT_ERROR = "error";
    public static final String OUT_COFFEE = "coffee!";

    public ExampleCoffeeMachine() {
        super(constructMachine());
    }

    public static CompactMealy<Input, String> constructMachine() {
        return constructMachine(new CompactMealy<>(createInputAlphabet()));
    }

    /**
     * Construct and return a machine representation of this example.
     *
     * @return a Mealy machine representing the coffee machine example
     */
    public static <S, T, A extends MutableMealyMachine<S, ? super Input, T, ? super String>> A constructMachine(A machine) {

        // @formatter:off
        return AutomatonBuilders.forMealy(machine)
                .withInitial("a")
                .from("a")
                    .on(Input.WATER).withOutput(OUT_OK).to("c")
                    .on(Input.POD).withOutput(OUT_OK).to("b")
                    .on(Input.BUTTON).withOutput(OUT_ERROR).to("f")
                    .on(Input.CLEAN).withOutput(OUT_OK).loop()
                .from("b")
                    .on(Input.WATER).withOutput(OUT_OK).to("d")
                    .on(Input.POD).withOutput(OUT_OK).loop()
                    .on(Input.BUTTON).withOutput(OUT_ERROR).to("f")
                    .on(Input.CLEAN).withOutput(OUT_OK).to("a")
                .from("c")
                    .on(Input.WATER).withOutput(OUT_OK).loop()
                    .on(Input.POD).withOutput(OUT_OK).to("d")
                    .on(Input.BUTTON).withOutput(OUT_ERROR).to("f")
                    .on(Input.CLEAN).withOutput(OUT_OK).to("a")
                .from("d")
                    .on(Input.WATER, Input.POD).withOutput(OUT_OK).loop()
                    .on(Input.BUTTON).withOutput(OUT_COFFEE).to("e")
                    .on(Input.CLEAN).withOutput(OUT_OK).to("a")
                .from("e")
                    .on(Input.WATER, Input.POD, Input.BUTTON).withOutput(OUT_ERROR).to("f")
                    .on(Input.CLEAN).withOutput(OUT_OK).to("a")
                .from("f")
                    .on(Input.WATER, Input.POD, Input.BUTTON, Input.CLEAN).withOutput(OUT_ERROR).loop()
                .create();
        // @formatter:on
    }

    public static Alphabet<Input> createInputAlphabet() {
        return Alphabets.fromEnum(Input.class);
    }

    public static ExampleCoffeeMachine createExample() {
        return new ExampleCoffeeMachine();
    }

    public enum Input {
        WATER,
        POD,
        BUTTON,
        CLEAN
    }

}
