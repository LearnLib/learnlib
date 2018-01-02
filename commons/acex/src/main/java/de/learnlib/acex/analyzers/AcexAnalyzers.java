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
package de.learnlib.acex.analyzers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.learnlib.acex.AbstractCounterexample;

/**
 * This is a utility class, acting as a container for several {@link AbstractNamedAcexAnalyzer}s.
 *
 * @author Malte Isberner
 */
public final class AcexAnalyzers {

    /**
     * Analyzer that linearly scans through the abstract counterexample in ascending order.
     */
    public static final AbstractNamedAcexAnalyzer LINEAR_FWD = new AbstractNamedAcexAnalyzer("LinearFwd") {

        @Override
        public int analyzeAbstractCounterexample(AbstractCounterexample<?> acex, int low, int high) {
            return AcexAnalysisAlgorithms.linearSearchFwd(acex, low, high);
        }
    };

    /**
     * Analyzer that linearly scans through the abstract counterexample in descending order.
     */
    public static final AbstractNamedAcexAnalyzer LINEAR_BWD = new AbstractNamedAcexAnalyzer("LinearBwd") {

        @Override
        public int analyzeAbstractCounterexample(AbstractCounterexample<?> acex, int low, int high) {
            return AcexAnalysisAlgorithms.linearSearchBwd(acex, low, high);
        }
    };
    /**
     * Analyzer that searches for a suffix index using binary search.
     */
    public static final AbstractNamedAcexAnalyzer BINARY_SEARCH_BWD = new AbstractNamedAcexAnalyzer("BinarySearchBwd") {

        @Override
        public int analyzeAbstractCounterexample(AbstractCounterexample<?> acex, int low, int high) {
            return AcexAnalysisAlgorithms.binarySearchRight(acex, low, high);
        }
    };
    public static final AbstractNamedAcexAnalyzer BINARY_SEARCH_FWD = new AbstractNamedAcexAnalyzer("BinarySearchFwd") {

        @Override
        public int analyzeAbstractCounterexample(AbstractCounterexample<?> acex, int low, int high) {
            return AcexAnalysisAlgorithms.binarySearchLeft(acex, low, high);
        }
    };
    /**
     * Analyzer that searches for a suffix index using exponential search.
     */
    public static final AbstractNamedAcexAnalyzer EXPONENTIAL_BWD = new AbstractNamedAcexAnalyzer("ExponentialBwd") {

        @Override
        public int analyzeAbstractCounterexample(AbstractCounterexample<?> acex, int low, int high) {
            return AcexAnalysisAlgorithms.exponentialSearchBwd(acex, low, high);
        }
    };
    public static final AbstractNamedAcexAnalyzer EXPONENTIAL_FWD = new AbstractNamedAcexAnalyzer("ExponentialFwd") {

        @Override
        public int analyzeAbstractCounterexample(AbstractCounterexample<?> acex, int low, int high) {
            return AcexAnalysisAlgorithms.exponentialSearchFwd(acex, low, high);
        }
    };
    public static final Map<String, AbstractNamedAcexAnalyzer> FWD_ANALYZERS =
            createMap(LINEAR_FWD, EXPONENTIAL_FWD, BINARY_SEARCH_FWD);

    public static final Map<String, AbstractNamedAcexAnalyzer> BWD_ANALYZERS =
            createMap(LINEAR_BWD, EXPONENTIAL_BWD, BINARY_SEARCH_BWD);
    public static final Map<String, AbstractNamedAcexAnalyzer> ALL_ANALYZERS = createMap(FWD_ANALYZERS, BWD_ANALYZERS);

    private AcexAnalyzers() {
        throw new AssertionError("Class should not be instantiated");
    }

    private static Map<String, AbstractNamedAcexAnalyzer> createMap(AbstractNamedAcexAnalyzer... analyzers) {
        Map<String, AbstractNamedAcexAnalyzer> analyzerMap = new HashMap<>(analyzers.length * 3 / 2);
        for (AbstractNamedAcexAnalyzer a : analyzers) {
            analyzerMap.put(a.getName(), a);
        }
        return Collections.unmodifiableMap(analyzerMap);
    }

    @SafeVarargs
    private static Map<String, AbstractNamedAcexAnalyzer> createMap(Map<String, AbstractNamedAcexAnalyzer>... maps) {
        Map<String, AbstractNamedAcexAnalyzer> result = new HashMap<>();
        for (Map<String, AbstractNamedAcexAnalyzer> map : maps) {
            result.putAll(map);
        }
        return result;
    }

    public static Collection<AbstractNamedAcexAnalyzer> getAnalyzers(Direction dir) {
        switch (dir) {
            case FORWARD:
                return getForwardAnalyzers();
            case BACKWARD:
                return getBackwardAnalyzers();
            default:
                throw new IllegalArgumentException();
        }
    }

    public static Collection<AbstractNamedAcexAnalyzer> getForwardAnalyzers() {
        return FWD_ANALYZERS.values();
    }

    public static Collection<AbstractNamedAcexAnalyzer> getBackwardAnalyzers() {
        return BWD_ANALYZERS.values();
    }

    public static Collection<AbstractNamedAcexAnalyzer> getAllAnalyzers() {
        return ALL_ANALYZERS.values();
    }

    public enum Direction {
        FORWARD,
        BACKWARD
    }

}
