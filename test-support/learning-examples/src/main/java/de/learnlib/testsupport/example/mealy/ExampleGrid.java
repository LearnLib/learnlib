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
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.transducer.MutableMealyMachine;
import net.automatalib.automaton.transducer.impl.CompactMealy;

/**
 * This class generates a Mealy machine consisting of a two-dimensional grid of states. Each transition has unique
 * output.
 */
public class ExampleGrid extends DefaultMealyLearningExample<Character, Integer> {

    public ExampleGrid(int xsize, int ysize) {
        super(constructMachine(xsize, ysize));
    }

    public static CompactMealy<Character, Integer> constructMachine(int xsize, int ysize) {
        return constructMachine(new CompactMealy<>(createInputAlphabet()), xsize, ysize);
    }

    /**
     * Construct and return a machine representation of this example.
     *
     * @param fm
     *         the output object to write the contents to
     * @param xsize
     *         number of states in x direction
     * @param ysize
     *         number of states in y direction
     * @param <S>
     *         state type
     * @param <A>
     *         automaton type
     *
     * @return a Mealy machine with (xsize * ysize) states
     */
    public static <S, A extends MutableMealyMachine<S, Character, ?, Integer>> A constructMachine(A fm,
                                                                                                  int xsize,
                                                                                                  int ysize) {

        // create 2D grid of states
        @SuppressWarnings("unchecked")
        S[][] stategrid = (S[][]) new Object[xsize][ysize];
        for (int x = 0; x < xsize; ++x) {
            for (int y = 0; y < ysize; ++y) {
                stategrid[x][y] = x == 0 && y == 0 ? fm.addInitialState() : fm.addState();
            }
        }

        // create transitions with unique output
        int output = 0;
        for (int x = 0; x < xsize; ++x) {
            for (int y = 0; y < ysize; ++y) {
                // determine successor states in x and y direction
                int succX = x < (xsize - 1) ? x + 1 : x;
                int succY = y < (ysize - 1) ? y + 1 : y;

                // transition in x direction
                fm.addTransition(stategrid[x][y], 'x', stategrid[succX][y], output++);
                // transition in y direction
                fm.addTransition(stategrid[x][y], 'y', stategrid[x][succY], output++);
            }
        }

        return fm;
    }

    public static Alphabet<Character> createInputAlphabet() {
        return Alphabets.characters('x', 'y');
    }

    public static ExampleGrid createExample(int xsize, int ysize) {
        return new ExampleGrid(xsize, ysize);
    }

}
