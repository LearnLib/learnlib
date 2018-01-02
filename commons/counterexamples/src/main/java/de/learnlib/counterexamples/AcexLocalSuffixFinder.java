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
package de.learnlib.counterexamples;

import de.learnlib.acex.AcexAnalyzer;
import de.learnlib.acex.analyzers.AbstractNamedAcexAnalyzer;
import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.Query;
import de.learnlib.counterexamples.acex.ClassicPrefixTransformAcex;
import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.words.Word;

/**
 * Wraps a {@link AbstractNamedAcexAnalyzer}. This class is both responsible for adapting it to the standard LearnLib
 * {@link LocalSuffixFinder} interface, and for maintaining statistics. Hence, a new object of this class should be
 * instantiated for every learning process.
 *
 * @author Malte Isberner
 */
public class AcexLocalSuffixFinder implements LocalSuffixFinder<Object, Object> {

    private final AcexAnalyzer analyzer;
    private final boolean reduce;
    private final String name;

    /**
     * Constructor.
     *
     * @param analyzer
     *         the analyzer to be wrapped
     * @param reduce
     *         whether or not to reduce counterexamples
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
