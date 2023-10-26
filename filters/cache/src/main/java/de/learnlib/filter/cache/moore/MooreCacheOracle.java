/* Copyright (C) 2013-2023 TU Dortmund
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
package de.learnlib.filter.cache.moore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import de.learnlib.api.Resumable;
import de.learnlib.api.logging.Category;
import de.learnlib.api.oracle.EquivalenceOracle.MooreEquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.Query;
import de.learnlib.filter.cache.LearningCacheOracle.MooreLearningCacheOracle;
import de.learnlib.filter.cache.moore.MooreCacheOracle.MooreCacheOracleState;
import net.automatalib.SupportsGrowingAlphabet;
import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.common.util.comparison.CmpUtil;
import net.automatalib.common.util.mapping.Mapping;
import net.automatalib.incremental.moore.IncrementalMooreBuilder;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Moore cache. This cache is implemented as a membership oracle: upon construction, it is provided with a delegate
 * oracle. Queries that can be answered from the cache are answered directly, others are forwarded to the delegate
 * oracle. When the delegate oracle has finished processing these remaining queries, the results are incorporated into
 * the cache.
 * <p>
 * This oracle additionally enables the user to define a Moore-style prefix-closure filter: a {@link Mapping} from
 * output symbols to output symbols may be provided, with the following semantics: If in an output word a symbol for
 * which the given mapping has a non-null value is encountered, all symbols <i>after</i> this symbol are replaced by the
 * respective value. The rationale behind this is that the concrete error message (key in the mapping) is still
 * reflected in the learned model, it is forced to result in a sink state with only a single repeating output symbol
 * (value in the mapping).
 * <p>
 * <b>Note:</b> this implementation is <b>not</b> thread-safe. If you require a cache that is usable in a parallel
 * environment. use the {@code ThreadSafeMooreCacheOracle} (or rather the {@code ThreadSafeMooreCaches} factory) from
 * the {@code learnlib-parallelism} artifact.
 *
 * @param <I>
 *         input symbol class
 * @param <O>
 *         output symbol class
 */
public class MooreCacheOracle<I, O>
        implements MooreLearningCacheOracle<I, O>, SupportsGrowingAlphabet<I>, Resumable<MooreCacheOracleState<I, O>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MooreCacheOracle.class);

    private final MembershipOracle<I, Word<O>> delegate;
    private IncrementalMooreBuilder<I, O> incMoore;
    private final Comparator<? super Query<I, ?>> queryCmp;
    private final @Nullable Mapping<? super O, ? extends O> errorSyms;


    MooreCacheOracle(IncrementalMooreBuilder<I, O> incrementalBuilder,
                     @Nullable Mapping<? super O, ? extends O> errorSyms,
                     MembershipOracle<I, Word<O>> delegate) {
        this(incrementalBuilder, errorSyms, delegate, new DynamicSymbolComparator<>());
    }

    MooreCacheOracle(IncrementalMooreBuilder<I, O> incrementalBuilder,
                     @Nullable Mapping<? super O, ? extends O> errorSyms,
                     MembershipOracle<I, Word<O>> delegate,
                     Comparator<I> comparator) {
        this.incMoore = incrementalBuilder;
        this.queryCmp = new ReverseLexCmp<>(comparator);
        this.errorSyms = errorSyms;
        this.delegate = delegate;
    }

    @Override
    public MooreEquivalenceOracle<I, O> createCacheConsistencyTest() {
        return new MooreCacheConsistencyTest<>(incMoore);
    }

    @Override
    public void processQueries(Collection<? extends Query<I, Word<O>>> queries) {
        if (queries.isEmpty()) {
            return;
        }

        List<? extends Query<I, Word<O>>> qrys = new ArrayList<>(queries);
        qrys.sort(queryCmp);

        List<MasterQuery<I, O>> masterQueries = queryCache(qrys);

        delegate.processQueries(masterQueries);

        updateCache(masterQueries);
    }

    @Override
    public void addAlphabetSymbol(I symbol) {
        incMoore.addAlphabetSymbol(symbol);
    }

    @Override
    public MooreCacheOracleState<I, O> suspend() {
        return new MooreCacheOracleState<>(incMoore);
    }

    @Override
    public void resume(MooreCacheOracleState<I, O> state) {
        final Class<?> thisClass = this.incMoore.getClass();
        final Class<?> stateClass = state.getBuilder().getClass();

        if (!thisClass.equals(stateClass)) {
            LOGGER.warn(Category.DATASTRUCTURE,
                        "You currently plan to use a '{}', but the state contained a '{}'. This may yield unexpected behavior.",
                        thisClass,
                        stateClass);
        }

        this.incMoore = state.getBuilder();
    }

    protected List<MasterQuery<I, O>> queryCache(Collection<? extends Query<I, Word<O>>> queries) {
        List<MasterQuery<I, O>> masterQueries = new ArrayList<>();

        Iterator<? extends Query<I, Word<O>>> it = queries.iterator();
        Query<I, Word<O>> q = it.next();
        Word<I> ref = q.getInput();

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

        return masterQueries;
    }

    private MasterQuery<I, O> createMasterQuery(Word<I> word) {
        WordBuilder<O> wb = new WordBuilder<>(word.size());
        if (incMoore.lookup(word, wb)) {
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

        wb.repeatAppend(word.length() - wbSize + 1, repSym);
        return new MasterQuery<>(word, wb.toWord());
    }

    protected void updateCache(Collection<? extends MasterQuery<I, O>> queries) {
        for (MasterQuery<I, O> m : queries) {
            postProcess(m);
        }
    }

    private void postProcess(MasterQuery<I, O> master) {
        Word<I> word = master.getSuffix();
        Word<O> answer = master.getAnswer();

        if (errorSyms == null) {
            incMoore.insert(word, answer);
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
            incMoore.insert(word, answer);
        } else {
            incMoore.insert(word.prefix(i - 1), answer.prefix(i));
        }
    }

    private static final class ReverseLexCmp<I> implements Comparator<Query<I, ?>> {

        private final Comparator<I> comparator;

        ReverseLexCmp(Comparator<I> comparator) {
            this.comparator = comparator;
        }

        @Override
        public int compare(Query<I, ?> o1, Query<I, ?> o2) {
            return -CmpUtil.lexCompare(o1.getInput(), o2.getInput(), comparator);
        }
    }

    private static final class DynamicSymbolComparator<I> implements Comparator<I> {

        private final GrowingMapAlphabet<I> alphabet;

        DynamicSymbolComparator() {
            this.alphabet = new GrowingMapAlphabet<>();
        }

        @Override
        public int compare(I i1, I i2) {
            alphabet.addSymbol(i1);
            alphabet.addSymbol(i2);
            return alphabet.compare(i1, i2);
        }
    }

    public static class MooreCacheOracleState<I, O> {

        private final IncrementalMooreBuilder<I, O> builder;

        MooreCacheOracleState(IncrementalMooreBuilder<I, O> builder) {
            this.builder = builder;
        }

        IncrementalMooreBuilder<I, O> getBuilder() {
            return builder;
        }
    }

}
