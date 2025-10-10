/*
 * Copyright (C) 2023-2024 Paul Kogel, TU Berlin
 *
 * THIS SOFTWARE IS INTENDED FOR ACADEMIC, RESEARCH AND EDUCATIONAL PURPOSES ONLY,
 * MOST PROMINENTLY TO FACILITATE RESEARCH ON AUTOMATA LEARNING. FOR-PROFIT USE OF
 * THIS SOFTWARE, INCLUDING, BUT NOT LIMITED TO, SELLING THE SOFTWARE ON ITS OWN OR
 * AS PART OF TOOLS AND/OR SERVICES IS PROHIBITED. COMMERCIAL USE OR COMMERCIAL
 * DISTRIBUTION OF THIS SOFTWARE OR OF ANY DERIVATES IS PROHIBITED.
 *
 * Apart from that, this software is licensed under the
 * GNU Affero Public License version 3 (AGPLv3).
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.learnlib.oracle.equivalence.mmlt;

import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.oracle.AbstractTimedQueryOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.statistic.container.DummyStatsContainer;
import de.learnlib.statistic.container.LearnerStatsProvider;
import de.learnlib.statistic.container.StatsContainer;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealyOutputSymbol;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealySemanticInputSymbol;
import net.automatalib.alphabet.time.mmlt.TimeStepSymbol;
import net.automatalib.alphabet.time.mmlt.TimeoutSymbol;
import net.automatalib.automaton.time.impl.mmlt.ReducedLocalTimerMealySemantics;
import net.automatalib.automaton.time.mmlt.LocalTimerMealy;
import net.automatalib.common.util.string.AbstractPrintable;
import net.automatalib.util.automaton.Automata;
import net.automatalib.util.automaton.cover.LocalTimerMealyCover;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * RandomWP counterexample search for MMLT learning.
 * Key modification: samples prefix from entry prefixes instead of all state prefixes.
 *
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class LocalTimerMealyRandomWpOracle<I, O> implements EquivalenceOracle.LocalTimerMealyEquivalenceOracle<I, O>, LearnerStatsProvider {
    private static final Logger logger = LoggerFactory.getLogger(LocalTimerMealyRandomWpOracle.class);
    private final AbstractTimedQueryOracle<I, O> timeOracle;

    private StatsContainer stats = new DummyStatsContainer();

    private final Random random;
    private final int minSize;
    private final int rndLen;
    private final int bound;

    public LocalTimerMealyRandomWpOracle(AbstractTimedQueryOracle<I, O> timeOracle,
                                         long randomSeed,
                                         int minSize, int rndAddLength, int bound) {

        this.timeOracle = timeOracle;

        this.random = new Random(randomSeed);

        this.minSize = minSize;
        this.rndLen = rndAddLength;
        this.bound = bound;
    }

    @Override
    public @Nullable DefaultQuery<LocalTimerMealySemanticInputSymbol<I>, Word<LocalTimerMealyOutputSymbol<O>>> findCounterExample(LocalTimerMealy<?, I, O> hypothesis, @Nullable Collection<? extends LocalTimerMealySemanticInputSymbol<I>> ignored) {
        return findCounterExampleInternal(hypothesis);
    }

    private <S> DefaultQuery<LocalTimerMealySemanticInputSymbol<I>, Word<LocalTimerMealyOutputSymbol<O>>> findCounterExampleInternal(LocalTimerMealy<S, I, O> hypothesis) {
        // Make expanded form of hypothesis:
        var hypSemModel = ReducedLocalTimerMealySemantics.forLocalTimerMealy(hypothesis);

        // Create a list of symbols (for faster access):
        List<LocalTimerMealySemanticInputSymbol<I>> listAlphabet = new ArrayList<>(hypothesis.getUntimedAlphabet());
        listAlphabet.add(new TimeoutSymbol<>());
        listAlphabet.add(new TimeStepSymbol<>());

        // Identify global suffixes:
        var globalSuffixes = Automata.characterizingSet(hypSemModel, hypSemModel.getInputAlphabet());

        // Get list of prefixes in deterministic order (so we can reproduce experiments easily):
        var locationCover = LocalTimerMealyCover.getLocalTimerMealyLocationCover(hypothesis);
        var prefixList = locationCover
                .values()
                .stream()
                .sorted(Comparator.comparing(AbstractPrintable::toString))
                .toList();

        // Generate test words:
        for (int i = 0; i < this.bound; i++) {
            stats.increaseCounter("WP_TESTED_WORD", "RandomWpOracle: tested words");

            var sulAnswer = this.generateTestword(prefixList, globalSuffixes, hypothesis, hypSemModel, listAlphabet);
            Word<LocalTimerMealyOutputSymbol<O>> hypAnswer = hypothesis.getSemantics().computeSuffixOutput(sulAnswer.getPrefix(), sulAnswer.getSuffix());

            // Found inconsistency if outputs do no match:
            if (!sulAnswer.getOutput().equals(hypAnswer)) {
                return sulAnswer;
            }
        }

        return null;
    }

    private <S> DefaultQuery<LocalTimerMealySemanticInputSymbol<I>, Word<LocalTimerMealyOutputSymbol<O>>> generateTestword(List<Word<LocalTimerMealySemanticInputSymbol<I>>> prefixes,
                                                                                                                           List<Word<LocalTimerMealySemanticInputSymbol<I>>> globalSuffixes,
                                                                                                                           LocalTimerMealy<S, I, O> hypothesis,
                                                                                                                           ReducedLocalTimerMealySemantics<S, I, O> hypSemModel,
                                                                                                                           List<LocalTimerMealySemanticInputSymbol<I>> alphabet) {

        WordBuilder<LocalTimerMealySemanticInputSymbol<I>> wbTestWord = new WordBuilder<>();

        // 1. Pick a random entry config prefix:
        Word<LocalTimerMealySemanticInputSymbol<I>> prefix = prefixes.get(this.random.nextInt(prefixes.size()));
        wbTestWord.append(prefix);

        // 2. Add random middle part:
        int size = minSize;
        while ((size > 0) || (this.random.nextDouble() > 1 / (this.rndLen + 1.0))) {
            var nextSymbol = alphabet.get(this.random.nextInt(alphabet.size()));
            wbTestWord.append(nextSymbol);

            if (size > 0) {
                size--;
            }
        }

        // 3. Pick a random suffix for this state:
        // 50% chance for state testing, 50% chance for transition testing
        Word<LocalTimerMealySemanticInputSymbol<I>> suffix = Word.epsilon();
        if (this.random.nextBoolean()) {
            if (!globalSuffixes.isEmpty()) {
                suffix = globalSuffixes.get(random.nextInt(globalSuffixes.size()));
            }
        } else {
            // Identify configuration reached by prefix:
            var currentConfig = hypothesis.getSemantics().traceInputs(wbTestWord.toWord());
            var state = hypSemModel.getStateForConfiguration(currentConfig, true);
            var localSuffixes = Automata.stateCharacterizingSet(hypSemModel, hypSemModel.getInputAlphabet(), state);

            if (!localSuffixes.isEmpty()) {
                suffix = localSuffixes.get(random.nextInt(localSuffixes.size()));
            }
        }
        wbTestWord.append(suffix);

        // Query SUL:
        var testWord = wbTestWord.toWord();
        var sulAnswer = timeOracle.answerQuery(testWord);
        return new DefaultQuery<>(testWord, sulAnswer);
    }


    @Override
    public void setStatsContainer(StatsContainer container) {
        this.stats = container;
    }
}
