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

import static de.learnlib.examples.mealy.ExampleCoffeeMachine.Input.BUTTON;
import static de.learnlib.examples.mealy.ExampleCoffeeMachine.Input.CLEAN;
import static de.learnlib.examples.mealy.ExampleCoffeeMachine.Input.POD;
import static de.learnlib.examples.mealy.ExampleCoffeeMachine.Input.WATER;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.MutableMealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

/**
 * This example represents the Coffee Machine example from
 * Steffen et al. "Introduction to Active Automata Learning from
 * a Practical Perspective" (Figure 3)
 * 
 * @author Maik Merten <maikmerten@googlemail.com>
 */
public class ExampleCoffeeMachine {
	
	private static final class InstanceHolder {
		public static final MealyMachine<?,Input,?,String> INSTANCE;
		
		static {
			INSTANCE = constructMachine();
		}
	}
	
	public enum Input {
		WATER,
		POD,
		BUTTON,
		CLEAN
	}
	
	public final static String out_ok = "ok";
	public final static String out_error = "error";
	public final static String out_coffee = "coffee!";
	
	private final static Alphabet<Input> ALPHABET = Alphabets.fromEnum(Input.class);
	
	
	public static Alphabet<Input> getInputAlphabet() {
		return ALPHABET;
	}
	
	public static MealyMachine<?,Input,?,String> getInstance() {
		return InstanceHolder.INSTANCE;
	}
	
	
    /**
     * Construct and return a machine representation of this example
     * 
     * @return a Mealy machine representing the coffee machine example
     */   
    public static <S,A extends MutableMealyMachine<S,Input,?,String>>
    A constructMachine(A machine) {
		
		S a = machine.addInitialState(),
				b = machine.addState(),
				c = machine.addState(),
				d = machine.addState(),
				e = machine.addState(),
				f = machine.addState();
		
		machine.addTransition(a, WATER, c, out_ok);
		machine.addTransition(a, POD, b, out_ok);
		machine.addTransition(a, BUTTON, f, out_error);
		machine.addTransition(a, CLEAN, a, out_ok);

		machine.addTransition(b, WATER, d, out_ok);
		machine.addTransition(b, POD, b, out_ok);
		machine.addTransition(b, BUTTON, f, out_error);
		machine.addTransition(b, CLEAN, a, out_ok);

		machine.addTransition(c, WATER, c, out_ok);
		machine.addTransition(c, POD, d, out_ok);
		machine.addTransition(c, BUTTON, f, out_error);
		machine.addTransition(c, CLEAN, a, out_ok);

		machine.addTransition(d, WATER, d, out_ok);
		machine.addTransition(d, POD, d, out_ok);
		machine.addTransition(d, BUTTON, e, out_coffee);
		machine.addTransition(d, CLEAN, a, out_ok);

		machine.addTransition(e, WATER, f, out_error);
		machine.addTransition(e, POD, f, out_error);
		machine.addTransition(e, BUTTON, f, out_error);
		machine.addTransition(e, CLEAN, a, out_ok);
		
		machine.addTransition(f, WATER, f, out_error);
		machine.addTransition(f, POD, f, out_error);
		machine.addTransition(f, BUTTON, f, out_error);
		machine.addTransition(f, CLEAN, f, out_error);
		

        return machine;
    }
    
    public static CompactMealy<Input,String> constructMachine() {
    	return constructMachine(new CompactMealy<Input,String>(ALPHABET));
    }
	
}
