/* Copyright (C) 2013-2018 TU Dortmund
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
package de.learnlib.algorithms.lstar.mealy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.misberner.buildergen.annotations.GenerateBuilder;
import de.learnlib.algorithms.lstar.AbstractExtensibleAutomatonLStar;
import de.learnlib.algorithms.lstar.ce.ObservationTableCEXHandler;
import de.learnlib.algorithms.lstar.closing.ClosingStrategy;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.datastructure.observationtable.OTLearner.OTLearnerMealy;
import de.learnlib.datastructure.observationtable.ObservationTable;
import de.learnlib.datastructure.observationtable.Row;
import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.automata.transout.impl.compact.CompactMealyTransition;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public class ExtensibleLStarMealy<I, O>
        extends AbstractExtensibleAutomatonLStar<MealyMachine<?, I, ?, O>, I, Word<O>, Integer, CompactMealyTransition<O>, Void, O, CompactMealy<I, O>>
        implements OTLearnerMealy<I, O> {

    private final List<O> outputTable = new ArrayList<>();

    public ExtensibleLStarMealy(Alphabet<I> alphabet,
                                MembershipOracle<I, Word<O>> oracle,
                                List<Word<I>> initialSuffixes,
                                ObservationTableCEXHandler<? super I, ? super Word<O>> cexHandler,
                                ClosingStrategy<? super I, ? super Word<O>> closingStrategy) {
        this(alphabet, oracle, Collections.singletonList(Word.epsilon()), initialSuffixes, cexHandler, closingStrategy);
    }

    @GenerateBuilder(defaults = AbstractExtensibleAutomatonLStar.BuilderDefaults.class)
    public ExtensibleLStarMealy(Alphabet<I> alphabet,
                                MembershipOracle<I, Word<O>> oracle,
                                List<Word<I>> initialPrefixes,
                                List<Word<I>> initialSuffixes,
                                ObservationTableCEXHandler<? super I, ? super Word<O>> cexHandler,
                                ClosingStrategy<? super I, ? super Word<O>> closingStrategy) {
        super(alphabet,
              oracle,
              new CompactMealy<>(alphabet),
              initialPrefixes,
              LStarMealyUtil.ensureSuffixCompliancy(initialSuffixes, alphabet, cexHandler.needsConsistencyCheck()),
              cexHandler,
              closingStrategy);
    }

    @Override
    protected List<Word<I>> initialSuffixes() {
        return initialSuffixes;
    }

    @Override
    public CompactMealy<I, O> getHypothesisModel() {
        return internalHyp;
    }

    @Override
    protected MealyMachine<?, I, ?, O> exposeInternalHypothesis() {
        return internalHyp;
    }

    @Override
    protected void updateInternalHypothesis() {
        updateOutputs();
        super.updateInternalHypothesis();
    }

    @Override
    protected Void stateProperty(ObservationTable<I, Word<O>> table, Row<I> stateRow) {
        return null;
    }

    @Override
    protected O transitionProperty(ObservationTable<I, Word<O>> table, Row<I> stateRow, int inputIdx) {
        Row<I> transRow = stateRow.getSuccessor(inputIdx);
        return outputTable.get(transRow.getRowId() - 1);
    }

    protected void updateOutputs() {
        int numOutputs = outputTable.size();
        int numTransRows = table.numberOfRows() - 1;

        int newOutputs = numTransRows - numOutputs;
        if (newOutputs == 0) {
            return;
        }

        List<DefaultQuery<I, Word<O>>> outputQueries = new ArrayList<>(numOutputs);

        for (int i = numOutputs + 1; i <= numTransRows; i++) {
            Row<I> row = table.getRow(i);
            Word<I> rowPrefix = row.getLabel();
            int prefixLen = rowPrefix.size();
            outputQueries.add(new DefaultQuery<>(rowPrefix.prefix(prefixLen - 1), rowPrefix.suffix(1)));
        }

        oracle.processQueries(outputQueries);

        for (int i = 0; i < newOutputs; i++) {
            DefaultQuery<I, Word<O>> query = outputQueries.get(i);
            O outSym = query.getOutput().getSymbol(0);
            outputTable.add(outSym);
        }
    }

    @Override
    protected SuffixOutput<I, Word<O>> hypothesisOutput() {
        return internalHyp;
    }

}
