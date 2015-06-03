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

import net.automatalib.automata.transout.MutableMealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import de.learnlib.examples.DefaultLearningExample.DefaultMealyLearningExample;

/**
 * This class provides the example used in the paper ''Inferring Mealy Machines'' 
 * by Muzammil Shahbaz and Roland Groz (see Figure 1).
 * 
 * @author Oliver Bauer 
 */
public class ExampleShahbazGroz extends DefaultMealyLearningExample<Character,String> {
	

	public static Alphabet<Character> createInputAlphabet() {
    	return Alphabets.characters('a', 'b');
    }
    
    /**
     * Construct and return a machine representation of this example
     * 
     * @return machine instance of the example
     */
    public static <S,T,A extends MutableMealyMachine<S,? super Character,T,? super String>>
    A constructMachine(A fm) {
//
//		S q0 = fm.addInitialState();
//		S q1 = fm.addState();
//		S q2 = fm.addState();
//		S q3 = fm.addState();
//
//		fm.addTransition(q0, 'a', q1, out_x);
//		fm.addTransition(q0, 'b', q3, out_x);
//
//		fm.addTransition(q1, 'a', q1, out_y);
//		fm.addTransition(q1, 'b', q2, out_x);
//
//		fm.addTransition(q2, 'a', q3, out_x);
//		fm.addTransition(q2, 'b', q3, out_x);
//
//		fm.addTransition(q3, 'a', q0, out_x);
//		fm.addTransition(q3, 'b', q0, out_x);

    	fm = AutomatonBuilders.forMealy(fm)
    			.withInitial("q0")
    			.from("q0")
    				.on('a').withOutput("x").to("q1")
    				.on('b').withOutput("x").to("q3")
    			.from("q1")
    				.on('a').withOutput("y").loop()
    				.on('b').withOutput("x").to("q2")
    			.from("q2")
    				.on('a', 'b').withOutput("x").to("q3")
    			.from("q3")
    				.on('a', 'b').withOutput("x").to("q0")
    		.create();
    	
        /*
         * In the paper the authors use the following counterexample
         * to refine the first conjecture from an angluin for mealy machines:
         * a b a b b a a
         */
    	
    	return fm;
    }
    
    public static CompactMealy<Character, String> constructMachine() {
    	return constructMachine(new CompactMealy<Character,String>(createInputAlphabet()));
    }
    
    public static ExampleShahbazGroz createExample() {
    	return new ExampleShahbazGroz();
    }
    
    public ExampleShahbazGroz() {
		super(constructMachine());
	}

}
