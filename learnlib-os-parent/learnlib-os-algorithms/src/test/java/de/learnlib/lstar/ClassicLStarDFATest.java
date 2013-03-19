package de.learnlib.lstar;

import java.util.Collections;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.FastDFA;
import net.automatalib.automata.fsa.impl.FastDFAState;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.FastAlphabet;
import net.automatalib.words.impl.Symbol;
import net.automatalib.words.util.Words;

import org.junit.Assert;
import org.junit.Test;

import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.lstar.dfa.ClassicLStarDFA;
import de.learnlib.oracles.SimulatorOracle;

public class ClassicLStarDFATest {

	private final static Symbol in_paul = new Symbol("Paul");
    private final static Symbol in_loves = new Symbol("loves");
    private final static Symbol in_mary = new Symbol("Mary");

    public FastDFA<Symbol> constructMachine() {
        
        Alphabet<Symbol> alpha = new FastAlphabet<>();
        alpha.add(in_paul);
        alpha.add(in_loves);
        alpha.add(in_mary);
        
        FastDFA<Symbol> dfa = new FastDFA<>(alpha);
        
        FastDFAState s0 = dfa.addInitialState(false),
                s1 = dfa.addState(false),
                s2 = dfa.addState(false),
                s3 = dfa.addState(true),
                s4 = dfa.addState(false);

        dfa.addTransition(s0, in_paul, s1);
        dfa.addTransition(s0, in_loves, s4);
        dfa.addTransition(s0, in_mary, s4);
        
        dfa.addTransition(s1, in_paul, s4);
        dfa.addTransition(s1, in_loves, s2);
        dfa.addTransition(s1, in_mary, s4);
        
        dfa.addTransition(s2, in_paul, s4);
        dfa.addTransition(s2, in_loves, s4);
        dfa.addTransition(s2, in_mary, s3);
        
        dfa.addTransition(s3, in_paul, s4);
        dfa.addTransition(s3, in_loves, s4);
        dfa.addTransition(s3, in_mary, s4);
        
        dfa.addTransition(s4, in_paul, s4);
        dfa.addTransition(s4, in_loves, s4);
        dfa.addTransition(s4, in_mary, s4);

        return dfa;
    }
    
	@Test
	public void testLStar() {
		FastDFA<Symbol> targetDFA = constructMachine();
		Alphabet<Symbol> alphabet = targetDFA.getInputAlphabet();
		
		MembershipOracle<Symbol, Boolean> dfaOracle = new SimulatorOracle<>(targetDFA);
		
		ClassicLStarDFA<Symbol> lstar = new ClassicLStarDFA<>(alphabet, dfaOracle);
		
		lstar.startLearning();
		DFA<?,Symbol> hyp = lstar.getHypothesisModel();
		Assert.assertEquals(1, hyp.size());
		
		Word<Symbol> ce = Words.asWord(in_paul, in_loves, in_mary);
		Query<Symbol,Boolean> qry = new Query<>(ce);
		dfaOracle.processQueries(Collections.singleton(qry));
		
		lstar.refineHypothesis(qry);
		hyp = lstar.getHypothesisModel();
		Assert.assertEquals(targetDFA.size() - 1, hyp.size());
		
		ce = Automata.findSeparatingWord(targetDFA, hyp, alphabet);
		System.err.println("CE is " + ce);
		qry = new Query<>(ce);
		dfaOracle.processQueries(Collections.singleton(qry));
		
		lstar.refineHypothesis(qry);
		hyp = lstar.getHypothesisModel();
		Assert.assertEquals(targetDFA.size(), hyp.size());
	}
	

}
