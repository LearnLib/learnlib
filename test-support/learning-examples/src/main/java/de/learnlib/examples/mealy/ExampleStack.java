/* Copyright (C) 2013-2014 TU Dortmund
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
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import de.learnlib.examples.DefaultLearningExample.DefaultMealyLearningExample;
import de.learnlib.examples.mealy.ExampleStack.Input;
import de.learnlib.examples.mealy.ExampleStack.Output;

/**
 * This example encodes a small stack with a capacity of three elements
 * and "push" and "pop" operations as Mealy machine. Outputs are
 * "ok", "empty" and "full".
 * 
 * @author Maik Merten <maikmerten@googlemail.com>
 */
public class ExampleStack extends DefaultMealyLearningExample<Input, Output> {

	public static enum Input {
		PUSH,
		POP
	}
	
	public static enum Output {
		OK,
		EMPTY,
		FULL
	}

    
    public static Alphabet<Input> createInputAlphabet() {
    	return Alphabets.fromEnum(Input.class);
    }
    
    /**
     * Construct and return a machine representation of this example
     * 
     * @return machine instance of the example
     */
    public static <S,T,A extends MutableMealyMachine<S,? super Input,T,? super Output>> 
    A constructMachine(A fm) {
    	fm = AutomatonBuilders.forMealy(fm)
    			.withInitial("s0")
    			.from("s0")
    				.on(PUSH).withOutput(OK).to("s1")
    				.on(POP).withOutput(EMPTY).loop()
    			.from("s1")
    				.on(PUSH).withOutput(OK).to("s2")
    				.on(POP).withOutput(OK).to("s0")
    			.from("s2")
    				.on(PUSH).withOutput(OK).to("s3")
    				.on(POP).withOutput(OK).to("s1")
    			.from("s3")
    				.on(PUSH).withOutput(FULL).loop()
    				.on(POP).withOutput(OK).to("s2")
    		.create();
    	
    	/*
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
        */
        
        return fm;
    }
    
    public static CompactMealy<Input, Output> constructMachine() {
    	return constructMachine(new CompactMealy<Input,Output>(createInputAlphabet()));
    }
    
    public static ExampleStack createExample() {
    	CompactMealy<Input,Output> mealy = constructMachine();
    	return new ExampleStack(mealy.getInputAlphabet(), mealy);
    }
    
	private ExampleStack(Alphabet<Input> alphabet,
			MealyMachine<?, Input, ?, Output> referenceAutomaton) {
		super(alphabet, referenceAutomaton);
	}

    
}
