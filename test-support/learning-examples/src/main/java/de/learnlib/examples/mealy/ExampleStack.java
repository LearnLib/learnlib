/* Copyright (C) 2013-2015 TU Dortmund
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

import static de.learnlib.examples.mealy.ExampleStack.Input.POP;
import static de.learnlib.examples.mealy.ExampleStack.Input.PUSH;
import static de.learnlib.examples.mealy.ExampleStack.Output.EMPTY;
import static de.learnlib.examples.mealy.ExampleStack.Output.FULL;
import static de.learnlib.examples.mealy.ExampleStack.Output.OK;
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
 * @author Maik Merten 
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
    	return new ExampleStack();
    }
    
	public ExampleStack() {
		super(constructMachine());
	}
}
