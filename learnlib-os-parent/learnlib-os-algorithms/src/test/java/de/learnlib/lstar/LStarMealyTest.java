package de.learnlib.lstar;

import java.util.Collections;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.FastMealy;
import net.automatalib.automata.transout.impl.FastMealyState;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.FastAlphabet;
import net.automatalib.words.impl.Symbol;
import net.automatalib.words.util.Words;

import org.junit.Assert;
import org.junit.Test;

import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.lstar.mealy.ClassicLStarMealy;
import de.learnlib.lstar.mealy.OptimizedLStarMealy;
import de.learnlib.oracles.SimulatorOracle;

public class LStarMealyTest {

	private final static Symbol in_a = new Symbol("a");
    private final static Symbol in_b = new Symbol("b");
    
    private final static String out_ok = "ok";
    private final static String out_error = "error";
    
    private FastMealy<Symbol, String> constructMachine() {
        Alphabet<Symbol> alpha = new FastAlphabet<>();
        alpha.add(in_a);
        alpha.add(in_b);
    
        
        FastMealy<Symbol, String> fm = new FastMealy<>(alpha);
        
        FastMealyState<String> s0 = fm.addInitialState(),
                s1 = fm.addState(),
                s2 = fm.addState();
        
        fm.addTransition(s0, in_a, s1, out_ok);
        fm.addTransition(s0, in_b, s0, out_error);
        
        fm.addTransition(s1, in_a, s2, out_ok);
        fm.addTransition(s1, in_b, s0, out_ok);
        
        fm.addTransition(s2, in_a, s2, out_error);
        fm.addTransition(s2, in_b, s1, out_ok);
        
        return fm;
    }
    
	@Test
	public void testClassicLStarMealy() {
		FastMealy<Symbol,String> mealy = constructMachine();
		Alphabet<Symbol> alphabet = mealy.getInputAlphabet();
		
		MembershipOracle<Symbol,Word<String>> oracle
			= new SimulatorOracle<>(mealy);
			
		LearningAlgorithm<MealyMachine<?,Symbol,?,String>,Symbol,String> learner
			= ClassicLStarMealy.createForWordOracle(alphabet, oracle);
		
		learner.startLearning();
		
		MealyMachine<?,Symbol,?,String> hyp = learner.getHypothesisModel();
		
		Assert.assertEquals(mealy.size(), hyp.size());
	}
	
	@Test
	public void testOptimizedLStarMealy() {
		FastMealy<Symbol,String> mealy = constructMachine();
		Alphabet<Symbol> alphabet = mealy.getInputAlphabet();
		
		MembershipOracle<Symbol,Word<String>> oracle
			= new SimulatorOracle<>(mealy);
			
		LearningAlgorithm<MealyMachine<?,Symbol,?,String>,Symbol,Word<String>> learner
			= new OptimizedLStarMealy<>(alphabet, oracle, Collections.singletonList(Words.asWord(in_a)));
		
		learner.startLearning();
		MealyMachine<?,Symbol,?,String> hyp = learner.getHypothesisModel();
		
		Assert.assertEquals(1, hyp.size());
		
		
		Word<Symbol> ce = Automata.findSeparatingWord(mealy, hyp, alphabet);
		Query<Symbol,Word<String>> qry = new Query<>(ce);
		oracle.processQueries(Collections.singleton(qry));
		
		learner.refineHypothesis(qry);
		hyp = learner.getHypothesisModel();
		
		Assert.assertEquals(mealy.size(), hyp.size());
	}

}
