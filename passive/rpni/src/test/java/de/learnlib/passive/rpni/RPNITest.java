package de.learnlib.passive.rpni;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

public class RPNITest {

	public RPNITest() {
		// TODO Auto-generated constructor stub
	}
	
	public static void main(String[] args) {
		Alphabet<Integer> alphabet = Alphabets.integers(0, 4);
		BlueFringeRPNIDFA<Integer> rpni = new BlueFringeRPNIDFA<>(alphabet);
		rpni.addPositiveSample(Word.fromSymbols(0, 1, 2, 3));
		rpni.addPositiveSample(Word.fromSymbols(0, 1, 2, 4));
		rpni.addNegativeSample(Word.fromLetter(1));
		
		DFA<?,Integer> result = rpni.computeModel();
		
		Visualization.visualizeAutomaton(result, alphabet, true);
	}

}
