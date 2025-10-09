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

package de.learnlib.oracle.symbol_filters.mmlt;


import de.learnlib.symbol_filter.SymbolFilter;
import de.learnlib.symbol_filter.SymbolFilterResponse;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealySemanticInputSymbol;
import net.automatalib.alphabet.time.mmlt.NonDelayingInput;
import net.automatalib.automaton.time.mmlt.LocalTimerMealy;
import net.automatalib.word.Word;

/**
 * A symbol filter that correctly accepts and ignores all transitions.
 *
 * @param <S> Location type
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class PerfectSymbolFilter<S, I, O> implements SymbolFilter<I, O> {

    private final LocalTimerMealy<S, I, O> sulModel;

    public PerfectSymbolFilter(LocalTimerMealy<S, I, O> sulModel) {
        this.sulModel = sulModel;
    }

    @Override
    public SymbolFilterResponse query(Word<LocalTimerMealySemanticInputSymbol<I>> prefix, NonDelayingInput<I> symbol) {
        // Check if silent self-loop:

        var targetConfig = this.sulModel.getSemantics().traceInputs(prefix);
        var trans = this.sulModel.getSemantics().getTransition(targetConfig, symbol);

        if (trans.output().equals(sulModel.getSemantics().getSilentOutput()) && targetConfig.equals(trans.target())) {
            return SymbolFilterResponse.IGNORE;
        } else {
            return SymbolFilterResponse.ACCEPT;
        }
    }

    @Override
    public void update(Word<LocalTimerMealySemanticInputSymbol<I>> prefix, NonDelayingInput<I> symbol, SymbolFilterResponse response) {
        throw new IllegalStateException("Not supported.");
    }
}
