package de.learnlib.filter.cache.mmlt;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.learnlib.algorithm.LocalTimerMealyModelParams;
import de.learnlib.driver.simulator.LocalTimerMealySimulatorSUL;
import de.learnlib.oracle.membership.TimedQueryOracle;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.automaton.mmlt.impl.CompactMMLT;
import net.automatalib.automaton.mmlt.impl.StringSymbolCombiner;
import net.automatalib.common.util.random.RandomUtil;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimeoutSymbol;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class LocalTimerMealyCacheTest {
    private CompactMMLT<String, String> buildBaseModel() {
        var alphabet = Alphabets.fromArray("p1", "p2", "abort", "collect");
        var model = new CompactMMLT<>(alphabet, "void", StringSymbolCombiner.getInstance());

        var s0 = model.addState();
        var s1 = model.addState();
        var s2 = model.addState();
        var s3 = model.addState();

        model.setInitialState(s0);

        model.addTransition(s0, "p1", s1, "go");
        model.addTransition(s1, "abort", s1, "ok");
        model.addLocalReset(s1, "abort");

        model.addPeriodicTimer(s1, "a", 3, "part");
        model.addPeriodicTimer(s1, "b", 6, "noise");
        model.addOneShotTimer(s1, "c", 40, "done", s3);

        model.addTransition(s0, "p2", s2, "go");
        model.addTransition(s2, "abort", s3, "void");
        model.addOneShotTimer(s2, "d", 4, "done", s3);

        model.addTransition(s3, "collect", s0, "void");

        return model;
    }

    /**
     * Tests if the information in the cache is consistent with the output of the SUL.
     */
    public void testCacheAndSULConsistency() {
        Random random = new Random(100);

        var automaton = buildBaseModel();
        var params = new LocalTimerMealyModelParams<>("void", 4, 80, StringSymbolCombiner.getInstance());

        var sul = new LocalTimerMealySimulatorSUL<>(automaton.getSemantics());
        var cacheSUL = new LocalTimerMealyTreeSULCache<>(sul, params);
        var timeOracleWithCache = new TimedQueryOracle<>(cacheSUL, params);
        var timeOracleWithoutCache = new TimedQueryOracle<>(sul, params);


        var listAlphabet = new ArrayList<>(automaton.getSemantics().getInputAlphabet());

        // Generate some random words and compare outputs of the cache, SUL, and automaton:
        List<Word<TimedInput<String>>> words = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            int maxLength = random.nextInt(1, 500);
            var symbols = RandomUtil.sample(random, listAlphabet, maxLength);
            var word = Word.fromList(symbols);
            words.add(word);

            var cacheOutput = timeOracleWithCache.querySuffixOutput(Word.epsilon(), word);
            var sulOutput = timeOracleWithoutCache.querySuffixOutput(Word.epsilon(), word);
            var automatonOutput = automaton.getSemantics().computeSuffixOutput(Word.epsilon(), word);

            Assert.assertEquals(sulOutput, automatonOutput, "Automaton output does not match SUL output for word " + word);
            Assert.assertEquals(cacheOutput, sulOutput, "Cache output does not match SUL output for word " + word);
        }

        // Now that the cache contents have changed, ensure that the results are still correct:
        for (var word : words) {
            var cacheOutput = timeOracleWithCache.querySuffixOutput(Word.epsilon(), word);
            var sulOutput = timeOracleWithoutCache.querySuffixOutput(Word.epsilon(), word);

            Assert.assertEquals(sulOutput, cacheOutput, "Cache output does not match SUL output for word " + word);
        }
    }

    @Test
    public void testCacheConsistencyTest() {
        // Test if the cache consistency test works correctly:
        var refAutomaton = buildBaseModel();
        var params = new LocalTimerMealyModelParams<>("void", 4, 80, StringSymbolCombiner.getInstance());

        var sul = new LocalTimerMealySimulatorSUL<>(refAutomaton.getSemantics());
        var cacheSUL = new LocalTimerMealyTreeSULCache<>(sul, params);
        var timeOracleWithCache = new TimedQueryOracle<>(cacheSUL, params);

        // Add word to cache:
        Word<TimedInput<String>> testWord = Word.fromSymbols(TimedInput.input("p2"), TimedInput.timeout(), TimedInput.step(), TimedInput.timeout());
        timeOracleWithCache.querySuffixOutput(Word.epsilon(), testWord);

        // Create a bad hypothesis:
        var badAutomaton = buildBaseModel();
        badAutomaton.removeTimer(2, "d");
        badAutomaton.addPeriodicTimer(2, "d", 4, "done");

        // Query the cache for a counterexample:
        Word<TimedInput<String>> expectedCex = Word.fromSymbols(
                new InputSymbol<>("p2"), new TimeoutSymbol<>(), new TimeoutSymbol<>()
        );

        var cacheConsistencyTest = cacheSUL.createCacheConsistencyTest();
        var cex = cacheConsistencyTest.findCounterExample(badAutomaton, refAutomaton.getSemantics().getInputAlphabet());
        Assert.assertNotNull(cex);
        Assert.assertEquals(cex.getInput(), expectedCex);

        // Now test with a reduced alphabet:
        var symbols = List.of("p1", "abort", "collect"); // not p1
        GrowingMapAlphabet<TimedInput<String>> reducedAlphabet = new GrowingMapAlphabet<>();
        symbols.forEach(s -> reducedAlphabet.add(new InputSymbol<>(s)));
        reducedAlphabet.add(new TimeoutSymbol<>());

        // The only counterexample in the cache has the prefix p2, which is now omitted:
        Assert.assertNull(cacheConsistencyTest.findCounterExample(badAutomaton, reducedAlphabet));
    }
}
