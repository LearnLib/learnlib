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

import static de.learnlib.examples.mealy.ExampleStack.Input.POP;
import static de.learnlib.examples.mealy.ExampleStack.Input.PUSH;
import static de.learnlib.examples.mealy.ExampleStack.Output.EMPTY;
import static de.learnlib.examples.mealy.ExampleStack.Output.FULL;
import static de.learnlib.examples.mealy.ExampleStack.Output.OK;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.MutableMealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

/**
 * This example encodes a small stack with a capacity of three elements
 * and "push" and "pop" operations as Mealy machine. Outputs are
 * "ok", "empty" and "full".
 * 
 * @author Maik Merten <maikmerten@googlemail.com>
 */
public class ExampleStack {
	private static final class InstanceHolder {
		public static final MealyMachine<?,Input,?,Output> INSTANCE;
		
		static {
			INSTANCE = constructMachine();
		}
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

    
    private final static Alphabet<Input> ALPHABET = Alphabets.fromEnum(Input.class); 
    
    
    public static Alphabet<Input> getInputAlphabet() {
    	return ALPHABET;
    }
    
    public static MealyMachine<?,Input,?,Output> getInstance() {
    	return InstanceHolder.INSTANCE;
    }
    
    /**
     * Construct and return a machine representation of this example
     * 
     * @return machine instance of the example
     */
    public static <S,A extends MutableMealyMachine<S,Input,?,Output>> 
    A constructMachine(A fm) {
        S s0 = fm.addInitialState(),
                s1 = fm.addState(),
                s2 = fm.addState(),
                s3 = fm.addState();
        
        fm.addTransition(s0, PUSH, s1, OK);
        fm.addTransition(s0, POP, s0, EMPTY);
        
        fm.addTransition(s1, PUSH, s2, OK);
        fm.addTransition(s1, POP, s0, OK);
        
        fm.addTransition(s2, PUSH, s3, OK);
        fm.addTransition(s2, POP, s1, OK);
        
        fm.addTransition(s3, PUSH, s3, FULL);
        fm.addTransition(s3, POP, s2, OK);
        
        return fm;
    }
    
    public static CompactMealy<Input, Output> constructMachine() {
    	return constructMachine(new CompactMealy<Input,Output>(ALPHABET));
    }
    
}
