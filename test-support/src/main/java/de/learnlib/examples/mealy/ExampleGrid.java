/* Copyright (C) 2013 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 * 
 * AutomataLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 * 
 * AutomataLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with AutomataLib; if not, see
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package de.learnlib.examples.mealy;

import java.io.IOException;
import java.io.Writer;

import net.automatalib.automata.transout.impl.FastMealy;
import net.automatalib.automata.transout.impl.FastMealyState;
import net.automatalib.commons.dotutil.DOT;
import net.automatalib.util.graphs.dot.GraphDOT;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.FastAlphabet;
import net.automatalib.words.impl.Symbol;

/**
 * This class generates a Mealy machine consisting of a two-dimensional grid of
 * states. Each transition has unique output.
 *
 * @author Maik Merten <maikmerten@googlemail.com>
 */
public class ExampleGrid {

    public final static Symbol in_x = new Symbol("x");
    public final static Symbol in_y = new Symbol("y");
    public final static Alphabet<Symbol> alphabet = new FastAlphabet<>(in_x, in_y);

    /**
     * Construct and return a machine representation of this example
     * 
     * @param xsize number of states in x direction
     * @param ysize number of states in y direction
     * @return a Mealy machine with (xsize * ysize) states
     */
    @SuppressWarnings("unchecked")
    public static FastMealy<Symbol, Integer> constructMachine(int xsize, int ysize) {

        FastMealy<Symbol, Integer> fm = new FastMealy<>(alphabet);

        // create 2D grid of states
        FastMealyState<Integer>[][] stategrid
        	= (FastMealyState<Integer>[][])new FastMealyState<?>[xsize][ysize];
        for (int x = 0; x < xsize; ++x) {
            for (int y = 0; y < ysize; ++y) {
                stategrid[x][y] = (x == 0 && y == 0) ? fm.addInitialState() : fm.addState();
            }
        }

        // create transitions with unique output
        int output = 0;
        for (int x = 0; x < xsize; ++x) {
            for (int y = 0; y < ysize; ++y) {
                // determine successor states in x and y direction
                int succ_x = x < (xsize - 1) ? x + 1 : x;
                int succ_y = y < (ysize - 1) ? y + 1 : y;

                // transition in x direction
                fm.addTransition(stategrid[x][y], in_x, stategrid[succ_x][y], output++);
                // transition in y direction
                fm.addTransition(stategrid[x][y], in_y, stategrid[x][succ_y], output++);
            }
        }

        return fm;
    }

    public static void main(String[] args) throws IOException {

        FastMealy<Symbol, Integer> fm = constructMachine(6, 3);

        Writer w = DOT.createDotWriter(true);
        GraphDOT.write(fm, fm.getInputAlphabet(), w);
        w.close();
    }
}
