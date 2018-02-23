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
package de.learnlib.filter.cache.mealy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.Query;
import de.learnlib.filter.cache.LearningCacheOracle.MealyLearningCacheOracle;
import net.automatalib.commons.util.array.RichArray;
import net.automatalib.commons.util.comparison.CmpUtil;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.incremental.mealy.IncrementalMealyBuilder;
import net.automatalib.incremental.mealy.dag.IncrementalMealyDAGBuilder;
import net.automatalib.incremental.mealy.tree.IncrementalMealyTreeBuilder;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

/**
 * Mealy cache. This cache is implemented as a membership oracle: upon construction, it is provided with a delegate
 * oracle. Queries that can be answered from the cache are answered directly, others are forwarded to the delegate
 * oracle. When the delegate oracle has finished processing these remaining queries, the results are incorporated into
 * the cache.
 * <p>
 * This oracle additionally enables the user to define a Mealy-style prefix-closure filter: a {@link Mapping} from
 * output symbols to output symbols may be provided, with the following semantics: If in an output word a symbol for
 * which the given mapping has a non-null value is encountered, all symbols <i>after</i> this symbol are replaced by the
 * respective value. The rationale behind this is that the concrete error message (key in the mapping) is still
 * reflected in the learned model, it is forced to result in a sink state with only a single repeating output symbol
 * (value in the mapping).
 *
 * @param <I>
 *         input symbol class
 * @param <O>
 *         output symbol class
 *
 * @author Malte Isberner
 */
public class MealyCacheOracle<I, O> implements MealyLearningCacheOracle<I, O> {

    private final MembershipOracle<I, Word<O>> delegate;
    private final IncrementalMealyBuilder<I, O> incMealy;
    private final Lock incMealyLock;
    private final Comparator<? super Query<I, ?>> queryCmp;
    private final Mapping<? super O, ? extends O> errorSyms;

    MealyCacheOracle(IncrementalMealyBuilder<I, O> incrementalBuilder,
                     Mapping<? super O, ? extends O> errorSyms,
                     MembershipOracle<I, Word<O>> delegate) {
        this(incrementalBuilder, new ReentrantLock(), errorSyms, delegate);
    }

    MealyCacheOracle(IncrementalMealyBuilder<I, O> incrementalBuilder,
                     Lock lock,
                     Mapping<? super O, ? extends O> errorSyms,
                     MembershipOracle<I, Word<O>> delegate) {
        this.incMealy = incrementalBuilder;
        this.incMealyLock = lock;
        this.queryCmp = new ReverseLexCmp<>(incrementalBuilder.getInputAlphabet());
        this.errorSyms = errorSyms;
        this.delegate = delegate;
    }

    public static <I, O> MealyCacheOracle<I, O> createDAGCacheOracle(Alphabet<I> inputAlphabet,
                                                                     MembershipOracle<I, Word<O>> delegate) {
        return createDAGCacheOracle(inputAlphabet, null, delegate);
    }

    public static <I, O> MealyCacheOracle<I, O> createDAGCacheOracle(Alphabet<I> inputAlphabet,
                                                                     Mapping<? super O, ? extends O> errorSyms,
                                                                     MembershipOracle<I, Word<O>> delegate) {
        IncrementalMealyBuilder<I, O> incrementalBuilder = new IncrementalMealyDAGBuilder<>(inputAlphabet);
        return new MealyCacheOracle<>(incrementalBuilder, errorSyms, delegate);
    }

    public static <I, O> MealyCacheOracle<I, O> createTreeCacheOracle(Alphabet<I> inputAlphabet,
                                                                      MembershipOracle<I, Word<O>> delegate) {
        return createTreeCacheOracle(inputAlphabet, null, delegate);
    }

    public static <I, O> MealyCacheOracle<I, O> createTreeCacheOracle(Alphabet<I> inputAlphabet,
                                                                      Mapping<? super O, ? extends O> errorSyms,
                                                                      MembershipOracle<I, Word<O>> delegate) {
        IncrementalMealyBuilder<I, O> incrementalBuilder = new IncrementalMealyTreeBuilder<>(inputAlphabet);
        return new MealyCacheOracle<>(incrementalBuilder, errorSyms, delegate);
    }

    public int getCacheSize() {
        return incMealy.asGraph().size();
    }

    @Override
    public MealyCacheConsistencyTest<I, O> createCacheConsistencyTest() {
        return new MealyCacheConsistencyTest<>(incMealy, incMealyLock);
    }

    @Override
    public void processQueries(Collection<? extends Query<I, Word<O>>> queries) {
        if (queries.isEmpty()) {
            return;
        }

        RichArray<Query<I, Word<O>>> qrys = new RichArray<>(queries);
        qrys.parallelSort(queryCmp);

        List<MasterQuery<I, O>> masterQueries = new ArrayList<>();

        Iterator<Query<I, Word<O>>> it = qrys.iterator();
        Query<I, Word<O>> q = it.next();
        Word<I> ref = q.getInput();

        incMealyLock.lock();
        try {
            MasterQuery<I, O> master = createMasterQuery(ref);
            if (!master.isAnswered()) {
                masterQueries.add(master);
            }
            master.addSlave(q);

            while (it.hasNext()) {
                q = it.next();
                Word<I> curr = q.getInput();
                if (!curr.isPrefixOf(ref)) {
                    master = createMasterQuery(curr);
                    if (!master.isAnswered()) {
                        masterQueries.add(master);
                    }
                }

                master.addSlave(q);
                // Update ref to increase the effectiveness of the length check in
                // isPrefixOf
                ref = curr;
            }
        } finally {
            incMealyLock.unlock();
        }

        delegate.processQueries(masterQueries);

        incMealyLock.lock();
        try {
            for (MasterQuery<I, O> m : masterQueries) {
                postProcess(m);
            }
        } finally {
            incMealyLock.unlock();
        }
    }

    private MasterQuery<I, O> createMasterQuery(Word<I> word) {
        WordBuilder<O> wb = new WordBuilder<>(word.size());
        if (incMealy.lookup(word, wb)) {
            return new MasterQuery<>(word, wb.toWord());
        }

        if (errorSyms == null) {
            return new MasterQuery<>(word);
        }

        int wbSize = wb.size();

        if (wbSize == 0) {
            return new MasterQuery<>(word, errorSyms);
        }

        O repSym = errorSyms.get(wb.getSymbol(wbSize - 1));
        if (repSym == null) {
            return new MasterQuery<>(word, errorSyms);
        }

        wb.repeatAppend(word.length() - wbSize, repSym);
        return new MasterQuery<>(word, wb.toWord());
    }

    private void postProcess(MasterQuery<I, O> master) {
        Word<I> word = master.getSuffix();
        Word<O> answer = master.getAnswer();

        if (errorSyms == null) {
            incMealy.insert(word, answer);
            return;
        }

        int answLen = answer.length();
        int i = 0;
        while (i < answLen) {
            O sym = answer.getSymbol(i++);
            if (errorSyms.get(sym) != null) {
                break;
            }
        }

        if (i == answLen) {
            incMealy.insert(word, answer);
        } else {
            incMealy.insert(word.prefix(i), answer.prefix(i));
        }
    }

    private static final class ReverseLexCmp<I> implements Comparator<Query<I, ?>>, Serializable {

        private final Alphabet<I> alphabet;

        ReverseLexCmp(Alphabet<I> alphabet) {
            this.alphabet = alphabet;
        }

        @Override
        public int compare(Query<I, ?> o1, Query<I, ?> o2) {
            return -CmpUtil.lexCompare(o1.getInput(), o2.getInput(), alphabet);
        }
    }

}
