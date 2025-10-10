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
import de.learnlib.query.DefaultQuery;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealySemanticInputSymbol;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealyOutputSymbol;
import net.automatalib.automaton.time.mmlt.LocalTimerMealy;
import net.automatalib.util.automaton.mmlt.LocalTimerMealyUtil;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A simulator oracle for MMLTs.
 *
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class LocalTimerMealySimulatorOracle<I, O> implements EquivalenceOracle.LocalTimerMealyEquivalenceOracle<I, O> {

    private final LocalTimerMealy<?, I, O> refModel;

    public LocalTimerMealySimulatorOracle(LocalTimerMealy<?, I, O> refModel) {
        this.refModel = refModel;
    }

    @Override
    public @Nullable DefaultQuery<LocalTimerMealySemanticInputSymbol<I>, Word<LocalTimerMealyOutputSymbol<O>>> findCounterExample(LocalTimerMealy<?, I, O> hypothesis, Collection<? extends LocalTimerMealySemanticInputSymbol<I>> inputs) {
        List<LocalTimerMealySemanticInputSymbol<I>> listInputs = new ArrayList<>(inputs);

        var separatingWord = LocalTimerMealyUtil.findSeparatingWord(refModel, hypothesis, listInputs);
        if (separatingWord != null) {
            var sulOutput = refModel.getSemantics().computeSuffixOutput(Word.epsilon(), separatingWord);
            return new DefaultQuery<>(Word.epsilon(), separatingWord, sulOutput);
        } else {
            return null;
        }
    }
}
