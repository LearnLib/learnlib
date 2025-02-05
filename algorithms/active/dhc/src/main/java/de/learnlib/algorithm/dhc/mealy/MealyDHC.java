/* Copyright (C) 2013-2025 TU Dortmund University
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
package de.learnlib.algorithm.dhc.mealy;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import de.learnlib.AccessSequenceTransformer;
import de.learnlib.Resumable;
import de.learnlib.algorithm.GlobalSuffixLearner.GlobalSuffixLearnerMealy;
import de.learnlib.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.counterexample.GlobalSuffixFinder;
import de.learnlib.counterexample.GlobalSuffixFinders;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.tooling.annotation.builder.GenerateBuilder;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.SupportsGrowingAlphabet;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.common.util.HashUtil;
import net.automatalib.common.util.mapping.MapMapping;
import net.automatalib.common.util.mapping.MutableMapping;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The DHC learner.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
public class MealyDHC<I, O> implements MealyLearner<I, O>,
                                       AccessSequenceTransformer<I>,
                                       GlobalSuffixLearnerMealy<I, O>,
                                       SupportsGrowingAlphabet<I>,
                                       Resumable<MealyDHCState<I, O>> {

    private final MembershipOracle<I, Word<O>> oracle;
    private final Alphabet<I> alphabet;
    private Set<Word<I>> splitters = new LinkedHashSet<>();
    private CompactMealy<I, O> hypothesis;
    private MutableMapping<Integer, QueueElement<I, O>> accessSequences;
    private final GlobalSuffixFinder<? super I, ? super Word<O>> suffixFinder;

    /**
     * Constructor, provided for backwards compatibility reasons.
     *
     * @param alphabet
     *         the learning alphabet
     * @param oracle
     *         the learning membership oracle
     */
    public MealyDHC(Alphabet<I> alphabet, MembershipOracle<I, Word<O>> oracle) {
        this(alphabet, oracle, GlobalSuffixFinders.RIVEST_SCHAPIRE, Collections.emptyList());
    }

    /**
     * Constructor.
     *
     * @param alphabet
     *         the learning alphabet
     * @param oracle
     *         the learning membership oracle
     * @param suffixFinder
     *         the {@link GlobalSuffixFinder suffix finder} to use for analyzing counterexamples
     * @param initialSplitters
     *         the initial set of splitters, {@code null} or an empty collection will result in the set of splitters
     *         being initialized as the set of alphabet symbols (interpreted as {@link Word}s)
     */
    @GenerateBuilder(defaults = BuilderDefaults.class)
    public MealyDHC(Alphabet<I> alphabet,
                    MembershipOracle<I, Word<O>> oracle,
                    GlobalSuffixFinder<? super I, ? super Word<O>> suffixFinder,
                    Collection<? extends Word<I>> initialSplitters) {
        this.alphabet = alphabet;
        this.oracle = oracle;
        this.suffixFinder = suffixFinder;
        // ensure that the first k splitters are the k alphabet symbols,
        // in correct order (this is required by scheduleSuccessors)
        for (I symbol : alphabet) {
            splitters.add(Word.fromLetter(symbol));
        }
        if (initialSplitters != null) {
            splitters.addAll(initialSplitters);
        }
    }

    @Override
    public Collection<Word<I>> getGlobalSuffixes() {
        return Collections.unmodifiableCollection(splitters);
    }

    @Override
    public boolean addGlobalSuffixes(Collection<? extends Word<I>> newGlobalSuffixes) {
        checkInternalState();

        return addSuffixesUnchecked(newGlobalSuffixes);
    }

    private void checkInternalState() {
        if (hypothesis == null) {
            throw new IllegalStateException("No hypothesis learned yet");
        }
    }

    protected boolean addSuffixesUnchecked(Collection<? extends Word<I>> newSuffixes) {
        int oldSize = hypothesis.size();

        splitters.addAll(newSuffixes);

        startLearning();

        return hypothesis.size() != oldSize;
    }

    @Override
    public void startLearning() {
        // initialize structure to store state output signatures
        Map<List<Word<O>>, Integer> signatures = new HashMap<>();

        // set up new hypothesis machine
        hypothesis = new CompactMealy<>(alphabet);

        // initialize exploration queue
        Queue<QueueElement<I, O>> queue = new ArrayDeque<>();

        // initialize storage for access sequences
        accessSequences = hypothesis.createDynamicStateMapping();

        // first element to be explored represents the initial state with no predecessor
        queue.add(new QueueElement<>(null, null, null, null));

        while (!queue.isEmpty()) {
            // get element to be explored from queue
            @SuppressWarnings("nullness") // false positive https://github.com/typetools/checker-framework/issues/399
            @NonNull QueueElement<I, O> elem = queue.poll();

            // determine access sequence for state
            Word<I> access = assembleAccessSequence(elem);

            // assemble queries
            ArrayList<DefaultQuery<I, Word<O>>> queries = new ArrayList<>(splitters.size());
            for (Word<I> suffix : splitters) {
                queries.add(new DefaultQuery<>(access, suffix));
            }

            // retrieve answers
            oracle.processQueries(queries);

            // assemble output signature
            List<Word<O>> sig = new ArrayList<>(splitters.size());
            for (DefaultQuery<I, Word<O>> query : queries) {
                sig.add(query.getOutput());
            }

            Integer sibling = signatures.get(sig);

            if (sibling != null) {
                // this element does not possess a new output signature
                // create a transition from parent state to sibling
                hypothesis.addTransition(elem.parentState, elem.transIn, sibling, elem.transOut);
            } else {
                // this is actually an observably distinct state! Progress!
                // Create state and connect via transition to parent
                Integer state = elem.parentElement == null ? hypothesis.addInitialState() : hypothesis.addState();
                if (elem.parentElement != null) {
                    hypothesis.addTransition(elem.parentState, elem.transIn, state, elem.transOut);
                }
                signatures.put(sig, state);
                accessSequences.put(state, elem);

                scheduleSuccessors(elem, state, queue, sig);
            }
        }
    }

    private Word<I> assembleAccessSequence(QueueElement<I, O> elem) {
        List<I> word = new ArrayList<>(elem.depth);

        QueueElement<I, O> pre = elem.parentElement;
        I sym = elem.transIn;
        while (pre != null && sym != null) {
            word.add(sym);
            sym = pre.transIn;
            pre = pre.parentElement;
        }

        Collections.reverse(word);
        return Word.fromList(word);
    }

    private void scheduleSuccessors(QueueElement<I, O> elem,
                                    Integer state,
                                    Queue<QueueElement<I, O>> queue,
                                    List<Word<O>> sig) {
        for (int i = 0; i < alphabet.size(); ++i) {
            // retrieve I/O for transition
            I input = alphabet.getSymbol(i);
            O output = sig.get(i).getSymbol(0);

            // create successor element and schedule for exploration
            queue.add(new QueueElement<>(state, elem, input, output));
        }
    }

    @Override
    public boolean refineHypothesis(DefaultQuery<I, Word<O>> ceQuery) {
        checkInternalState();

        if (hypothesis.computeSuffixOutput(ceQuery.getPrefix(), ceQuery.getSuffix()).equals(ceQuery.getOutput())) {
            return false;
        }

        Collection<Word<I>> ceSuffixes = suffixFinder.findSuffixes(ceQuery, this, hypothesis, oracle);

        return addSuffixesUnchecked(ceSuffixes);
    }

    @Override
    public CompactMealy<I, O> getHypothesisModel() {
        checkInternalState();
        return hypothesis;
    }

    @Override
    public void addAlphabetSymbol(I symbol) {

        if (!this.alphabet.containsSymbol(symbol)) {
            this.alphabet.asGrowingAlphabetOrThrowException().addSymbol(symbol);
        }

        if (!this.splitters.contains(Word.fromLetter(symbol))) {
            final Iterator<Word<I>> splitterIterator = this.splitters.iterator();
            final LinkedHashSet<Word<I>> newSplitters =
                    new LinkedHashSet<>(HashUtil.capacity(this.splitters.size() + 1));

            // see initial initialization of the splitters
            for (int i = 0; i < this.alphabet.size() - 1; i++) {
                newSplitters.add(splitterIterator.next());
            }

            newSplitters.add(Word.fromLetter(symbol));

            while (splitterIterator.hasNext()) {
                newSplitters.add(splitterIterator.next());
            }

            this.splitters = newSplitters;

            this.startLearning();
        }
    }

    @Override
    public MealyDHCState<I, O> suspend() {
        return new MealyDHCState<>(splitters, hypothesis, accessSequences);
    }

    @Override
    public void resume(MealyDHCState<I, O> state) {
        this.splitters = state.getSplitters();
        this.accessSequences = new MapMapping<>(state.getAccessSequences());
        this.hypothesis = state.getHypothesis();
    }

    @Override
    public Word<I> transformAccessSequence(Word<I> word) {
        checkInternalState();
        Integer state = hypothesis.getSuccessor(hypothesis.getInitialState(), word);
        return assembleAccessSequence(accessSequences.get(state));
    }

    static final class BuilderDefaults {

        private BuilderDefaults() {
            // prevent instantiation
        }

        public static <I, O> GlobalSuffixFinder<? super I, ? super Word<O>> suffixFinder() {
            return GlobalSuffixFinders.RIVEST_SCHAPIRE;
        }

        public static <I> Collection<Word<I>> initialSplitters() {
            return Collections.emptyList();
        }
    }

    static final class QueueElement<I, O> {

        private final @Nullable Integer parentState;
        private final @Nullable QueueElement<I, O> parentElement;
        private final @Nullable I transIn;
        private final @Nullable O transOut;
        private final int depth;

        QueueElement(@Nullable Integer parentState,
                     @Nullable QueueElement<I, O> parentElement,
                     @Nullable I transIn,
                     @Nullable O transOut) {
            this.parentState = parentState;
            this.parentElement = parentElement;
            this.transIn = transIn;
            this.transOut = transOut;
            this.depth = (parentElement != null) ? parentElement.depth + 1 : 0;
        }
    }
}
