/* Copyright (C) 2013-2026 TU Dortmund University
 * This file is part of LearnLib <https://learnlib.de>.
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
package de.learnlib.filter.cache.mmlt;

import java.util.List;

import de.learnlib.driver.simulator.MMLTSimulatorSUL;
import de.learnlib.filter.cache.AbstractCacheTest;
import de.learnlib.filter.cache.CacheTestUtils;
import de.learnlib.filter.cache.TimedSULLearningCacheOracle;
import de.learnlib.filter.cache.sul.SULCaches;
import de.learnlib.filter.statistic.sul.CounterTimedSUL;
import de.learnlib.statistic.Statistics;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.symbol.time.TimeoutSymbol;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class MMLTCacheTest
        extends AbstractCacheTest<TimedSULLearningCacheOracle<String, String, TimedSULTreeCache<String, String>>, MMLT<?, String, ?, String>, TimedInput<String>, Word<TimedOutput<String>>> {

    private final CounterTimedSUL<String, String> counter;

    public MMLTCacheTest() {
        counter = new CounterTimedSUL<>(new MMLTSimulatorSUL<>(CacheTestUtils.MMLT));
    }

    @Override
    protected Alphabet<TimedInput<String>> getAlphabet() {
        return CacheTestUtils.MMLT.getSemantics().getInputAlphabet();
    }

    @Override
    protected Alphabet<TimedInput<String>> getExtensionAlphabet() {
        return Alphabets.fromArray();
    }

    @Override
    protected MMLT<?, String, ?, String> getTargetModel() {
        return CacheTestUtils.MMLT;
    }

    @Override
    protected MMLT<?, String, ?, String> getInvalidTargetModel() {
        return CacheTestUtils.MMLT_INVALID;
    }

    @Override
    protected TimedSULLearningCacheOracle<String, String, TimedSULTreeCache<String, String>> getCachedOracle() {
        return TimedSULLearningCacheOracle.fromTimedSULCache(SULCaches.createTimedCache(counter,
                                                                                        CacheTestUtils.MMLT_PARAMS),
                                                             CacheTestUtils.MMLT_PARAMS);
    }

    @Override
    protected TimedSULLearningCacheOracle<String, String, TimedSULTreeCache<String, String>> getResumedOracle(
            TimedSULLearningCacheOracle<String, String, TimedSULTreeCache<String, String>> original) {
        return original;
    }

    @Override
    protected Word<TimedOutput<String>> computeOutput(MMLT<?, String, ?, String> model,
                                                      Word<TimedInput<String>> input) {
        return model.computeOutput(input);
    }

    @Override
    protected long getNumberOfPosedQueries() {
        return Statistics.getService().getCount(CounterTimedSUL.KEY_QUERY).orElse(0L);
    }

    @Override
    protected boolean supportsPrefixes() {
        return true;
    }

    @Override
    protected boolean supportsGrowing() {
        return false;
    }

    @Override
    @Test(dependsOnMethods = "testPrefix")
    public void testCacheConsistency() {
        // Add word to cache to ensure counter example
        Word<TimedInput<String>> testWord =
                Word.fromSymbols(TimedInput.input("p2"), TimedInput.timeout(), TimedInput.step(), TimedInput.timeout());
        super.oracle.getOracle().answerQuery(testWord);

        super.testCacheConsistency();
    }

    @Test(dependsOnMethods = "testCacheConsistency")
    public void testReducedAlphabet() {
        // Now test with a reduced alphabet:
        var symbols = List.of("p1", "abort", "collect"); // not p1
        var reducedAlphabet = symbols.stream().<TimedInput<String>>map(InputSymbol::new).collect(Alphabets.collector());
        reducedAlphabet.add(new TimeoutSymbol<>());

        // The only counterexample in the cache has the prefix p2, which is now omitted:
        Assert.assertNull(super.oracle.createCacheConsistencyTest()
                                      .findCounterExample(CacheTestUtils.MMLT_INVALID, reducedAlphabet));
    }
}
