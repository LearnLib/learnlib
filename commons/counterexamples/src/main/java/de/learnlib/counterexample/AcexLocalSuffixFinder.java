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
package de.learnlib.counterexample;

import de.learnlib.AccessSequenceTransformer;
import de.learnlib.acex.AbstractNamedAcexAnalyzer;
import de.learnlib.acex.AcexAnalyzer;
import de.learnlib.acex.ClassicPrefixTransformAcex;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.Query;
import net.automatalib.automaton.concept.SuffixOutput;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Wraps a {@link AbstractNamedAcexAnalyzer}. This class is both responsible for adapting it to the standard LearnLib
 * {@link LocalSuffixFinder} interface, and for maintaining statistics. Hence, a new object of this class should be
 * instantiated for every learning process.
 */
public class AcexLocalSuffixFinder implements LocalSuffixFinder<@Nullable Object, @Nullable Object> {

    private final AcexAnalyzer analyzer;
    private final boolean reduce;
    private final String name;

    /**
     * Constructor.
     *
     * @param analyzer
     *         the analyzer to be wrapped
     * @param reduce
     *         whether to reduce counterexamples
     * @param name
     *         the display name of the suffix finder
     */
    public AcexLocalSuffixFinder(AcexAnalyzer analyzer, boolean reduce, String name) {
        this.analyzer = analyzer;
        this.reduce = reduce;
        this.name = name;
    }

    @Override
    public <RI, RO> int findSuffixIndex(Query<RI, RO> ceQuery,
                                        AccessSequenceTransformer<RI> asTransformer,
                                        SuffixOutput<RI, RO> hypOutput,
                                        MembershipOracle<RI, RO> oracle) {

        return findSuffixIndex(analyzer, reduce, ceQuery, asTransformer, hypOutput, oracle);
    }

    public static <RI, RO> int findSuffixIndex(AcexAnalyzer analyzer,
                                               boolean reduce,
                                               Query<RI, RO> ceQuery,
                                               AccessSequenceTransformer<RI> asTransformer,
                                               SuffixOutput<RI, RO> hypOutput,
                                               MembershipOracle<RI, RO> oracle) {

        Word<RI> counterexample = ceQuery.getInput();

        // Create the view of an abstract counterexample
        ClassicPrefixTransformAcex<RI, RO> acex = new ClassicPrefixTransformAcex<>(counterexample,
                                                                                   oracle,
                                                                                   hypOutput,
                                                                                   asTransformer::transformAccessSequence);

        int start = 0;

        if (reduce) {
            start = asTransformer.longestASPrefix(counterexample).length();
        }

        int idx = analyzer.analyzeAbstractCounterexample(acex, start);

        // Note: There is an off-by-one mismatch between the old and the new interface
        return idx + 1;
    }

    @Override
    public String toString() {
        return name;
    }
}
