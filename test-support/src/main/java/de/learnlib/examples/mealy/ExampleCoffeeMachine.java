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

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.MutableMealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.FastAlphabet;
import net.automatalib.words.impl.Symbol;

/**
 * This example represents the Coffee Machine example from
 * Steffen et al. "Introduction to Active Automata Learning from
 * a Practical Perspective" (Figure 3)
 * 
 * @author Maik Merten <maikmerten@googlemail.com>
 */
public class ExampleCoffeeMachine {
	
	private static final class InstanceHolder {
		public static final MealyMachine<?,Symbol,?,String> INSTANCE;
		
		static {
			INSTANCE = constructMachine();
		}
	}
	
	
	public final static Symbol in_water = new Symbol("water");
	public final static Symbol in_pod = new Symbol("pod");
	public final static Symbol in_button = new Symbol("button");
	public final static Symbol in_clean = new Symbol("clean");
	
	public final static String out_ok = "ok";
	public final static String out_error = "error";
	public final static String out_coffee = "coffee!";
	
	private final static Alphabet<Symbol> ALPHABET = new FastAlphabet<>(in_water, in_pod, in_button, in_clean);
	
	
	public static Alphabet<Symbol> getInputAlphabet() {
		return ALPHABET;
	}
	
	public static MealyMachine<?,Symbol,?,String> getInstance() {
		return InstanceHolder.INSTANCE;
	}
	
	
    /**
     * Construct and return a machine representation of this example
     * 
     * @return a Mealy machine representing the coffee machine example
     */   
    public static <S,A extends MutableMealyMachine<S,Symbol,?,String>>
    A constructMachine(A machine) {
		
		S a = machine.addInitialState(),
				b = machine.addState(),
				c = machine.addState(),
				d = machine.addState(),
				e = machine.addState(),
				f = machine.addState();
		
		machine.addTransition(a, in_water, c, out_ok);
		machine.addTransition(a, in_pod, b, out_ok);
		machine.addTransition(a, in_button, f, out_error);
		machine.addTransition(a, in_clean, a, out_ok);

		machine.addTransition(b, in_water, d, out_ok);
		machine.addTransition(b, in_pod, b, out_ok);
		machine.addTransition(b, in_button, f, out_error);
		machine.addTransition(b, in_clean, a, out_ok);

		machine.addTransition(c, in_water, c, out_ok);
		machine.addTransition(c, in_pod, d, out_ok);
		machine.addTransition(c, in_button, f, out_error);
		machine.addTransition(c, in_clean, a, out_ok);

		machine.addTransition(d, in_water, d, out_ok);
		machine.addTransition(d, in_pod, d, out_ok);
		machine.addTransition(d, in_button, e, out_coffee);
		machine.addTransition(d, in_clean, a, out_ok);

		machine.addTransition(e, in_water, f, out_error);
		machine.addTransition(e, in_pod, f, out_error);
		machine.addTransition(e, in_button, f, out_error);
		machine.addTransition(e, in_clean, a, out_ok);
		
		machine.addTransition(f, in_water, f, out_error);
		machine.addTransition(f, in_pod, f, out_error);
		machine.addTransition(f, in_button, f, out_error);
		machine.addTransition(f, in_clean, f, out_error);
		

        return machine;
    }
    
    public static CompactMealy<Symbol,String> constructMachine() {
    	return constructMachine(new CompactMealy<Symbol,String>(ALPHABET));
    }
	
}
