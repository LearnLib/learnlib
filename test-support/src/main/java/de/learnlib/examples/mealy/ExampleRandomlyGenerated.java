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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import net.automatalib.automata.transout.impl.FastMealy;
import net.automatalib.automata.transout.impl.FastMealyState;
import net.automatalib.commons.dotutil.DOT;
import net.automatalib.util.graphs.dot.GraphDOT;
import net.automatalib.util.minimizer.Block;
import net.automatalib.util.minimizer.MinimizationResult;
import net.automatalib.util.minimizer.Minimizer;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.FastAlphabet;
import net.automatalib.words.impl.Symbol;

/**
 * Generates a random minimized Mealy machine with a specified input
 * alphabet, output alphabet, and maximum size.
 * 
 * @author Maik Merten <maikmerten@googlemail.com>
 */
public class ExampleRandomlyGenerated {
	
	/**
	 * Construct and return a machine representation of this example.
	 * 
	 * @param inalpha Alphabet of input symbols
	 * @param outalpha Alphabet of output symbols
	 * @param rand instance of a random number generator
	 * @param maxsize upper bound for number of states
	 * @return a Mealy machine instance
	 */
	public static FastMealy<Symbol, Symbol> constructMachine(Alphabet<Symbol> inalpha,
			Alphabet<Symbol> outalpha,
			Random rand,
			int maxsize) {
		
		FastMealy<Symbol, Symbol> fm = new FastMealy<>(inalpha);
		
		// create states
		List<FastMealyState<Symbol>> states = new ArrayList<>(maxsize);
		for(int i = 0; i < maxsize; ++i) {
			states.add(i == 0 ? fm.addInitialState() : fm.addState());
		}
		
		// connect states with random transitions
		for(int i = 0; i < maxsize; ++i) {
			FastMealyState<Symbol> from = states.get(i);
			
			for(Symbol input : inalpha) {
				// random output symbol
				Symbol output = outalpha.getSymbol(rand.nextInt(outalpha.size()));
				// random target state
				FastMealyState<Symbol> to = states.get(rand.nextInt(maxsize));
				// add transition
				fm.addTransition(from, input, to, output);
				
			}
		}
		
		// minimize automaton
		MinimizationResult result = Minimizer.minimize(fm);
		for(Object o : result.getBlocks()) {
			Block b = (Block) o;
			FastMealyState representative = (FastMealyState) result.getRepresentative(b);
			Collection<FastMealyState> statesInBlock = result.getStatesInBlock(b);
			for(FastMealyState s : statesInBlock) {
				if(s != representative) {
					fm.removeState(s, representative);
				}
			}
		}
		
		return fm;
	}
	
	
	public static void main(String[] args) throws IOException {
		
		Alphabet<Symbol> inputs = new FastAlphabet<>(
				new Symbol("a"),
				new Symbol("b"),
				new Symbol("c"));
		
		Alphabet<Symbol> outputs = new FastAlphabet<>(
				new Symbol("hi"),
				new Symbol("hello"),
				new Symbol("cya"),
				new Symbol("bye"));

        FastMealy<Symbol, Symbol> fm = constructMachine(
				inputs,
				outputs,
				new Random(1337),
				5);

        Writer w = DOT.createDotWriter(true);
        GraphDOT.write(fm, fm.getInputAlphabet(), w);
        w.close();		
	}
	
}
