/* Copyright (C) 2015 TU Dortmund
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
 * This is a utility class, acting as a container for several {@link NamedAcexAnalyzer}s.
 * 
 * @author Malte Isberner
 *
 */
public abstract class AcexAnalyzers {
	
	public enum Direction {
		FORWARD,
		BACKWARD
	}
	
	/**
	 * Analyzer that linearly scans through the abstract counterexample in ascending
	 * order.
	 */
	public static final NamedAcexAnalyzer LINEAR_FWD = new NamedAcexAnalyzer("LinearFwd") {
		@Override
		public int analyzeAbstractCounterexample(AbstractCounterexample<?> acex, int low, int high) {
			return AcexAnalysisAlgorithms.linearSearchFwd(acex, low, high);
		}
	};
	
	/**
	 * Analyzer that linearly scans through the abstract counterexample in descending
	 * order.
	 */
	public static final NamedAcexAnalyzer LINEAR_BWD = new NamedAcexAnalyzer("LinearBwd") {
		@Override
		public int analyzeAbstractCounterexample(AbstractCounterexample<?> acex, int low, int high) {
			return AcexAnalysisAlgorithms.linearSearchBwd(acex, low, high);
		}
	};
	
	/**
	 * Analyzer that searches for a suffix index using binary search.
	 */
	public static final NamedAcexAnalyzer BINARY_SEARCH_BWD = new NamedAcexAnalyzer("BinarySearchBwd") {
		@Override
		public int analyzeAbstractCounterexample(AbstractCounterexample<?> acex, int low, int high) {
			return AcexAnalysisAlgorithms.binarySearchRight(acex, low, high);
		}
	};
	
	public static final NamedAcexAnalyzer BINARY_SEARCH_FWD = new NamedAcexAnalyzer("BinarySearchFwd") {
		@Override
		public int analyzeAbstractCounterexample(AbstractCounterexample<?> acex, int low, int high) {
			return AcexAnalysisAlgorithms.binarySearchLeft(acex, low, high);
		}
	};
	
	/**
	 * Analyzer that searches for a suffix index using exponential search.
	 */
	public static final NamedAcexAnalyzer EXPONENTIAL_BWD = new NamedAcexAnalyzer("ExponentialBwd") {
		@Override
		public int analyzeAbstractCounterexample(AbstractCounterexample<?> acex, int low, int high) {
			return AcexAnalysisAlgorithms.exponentialSearchBwd(acex, low, high);
		}
	};
	
	public static final NamedAcexAnalyzer EXPONENTIAL_FWD = new NamedAcexAnalyzer("ExponentialFwd") {
		@Override
		public int analyzeAbstractCounterexample(AbstractCounterexample<?> acex, int low, int high) {
			return AcexAnalysisAlgorithms.exponentialSearchFwd(acex, low, high);
		}
	};
	
//	/**
//	 * Analyzer that searches for a suffix index using partition search.
//	 */
//	public static final NamedAcex2Analyzer PARTITION_BWD = new NamedAcex2Analyzer("PartitionBwd") {
//		@Override
//		public int analyzeAbstractCounterexample(AbstractCounterexample2<?> acex, int low, int high) {
//			return AcexAnalysisAlgorithms.partitionSearchBwd(acex, low, high);
//		}
//	};
//	
//	public static final NamedAcex2Analyzer PARTITION_FWD = new NamedAcex2Analyzer("PartitionFwd") {
//		@Override
//		public int analyzeAbstractCounterexample(AbstractCounterexample2<?> acex, int low, int high) {
//			return AcexAnalysisAlgorithms.partitionSearchFwd(acex, low, high);
//		}
//	};
	
	private static Map<String,NamedAcexAnalyzer> createMap(NamedAcexAnalyzer... analyzers) {
		Map<String,NamedAcexAnalyzer> analyzerMap = new HashMap<>(analyzers.length * 3 / 2);
		for (NamedAcexAnalyzer a : analyzers) {
			analyzerMap.put(a.getName(), a);
		}
		return Collections.unmodifiableMap(analyzerMap);
	}
	
	@SafeVarargs
	private static Map<String,NamedAcexAnalyzer> createMap(Map<String,NamedAcexAnalyzer>... maps) {
		Map<String,NamedAcexAnalyzer> result = new HashMap<>();
		for (Map<String,NamedAcexAnalyzer> map : maps) {
			result.putAll(map);
		}
		return result;
	}
	
	public static final Map<String,NamedAcexAnalyzer> FWD_ANALYZERS = createMap(LINEAR_FWD, EXPONENTIAL_FWD, BINARY_SEARCH_FWD);
	public static final Map<String,NamedAcexAnalyzer> BWD_ANALYZERS = createMap(LINEAR_BWD, EXPONENTIAL_BWD, BINARY_SEARCH_BWD);
	public static final Map<String,NamedAcexAnalyzer> ALL_ANALYZERS = createMap(FWD_ANALYZERS, BWD_ANALYZERS);
	
	public static Collection<NamedAcexAnalyzer> getAnalyzers(Direction dir) {
		switch(dir) {
		case FORWARD:
			return getForwardAnalyzers();
		case  BACKWARD:
			return getBackwardAnalyzers();
		default:
			throw new IllegalArgumentException();
		}
	}
	
	public static Collection<NamedAcexAnalyzer> getForwardAnalyzers() {
		return FWD_ANALYZERS.values();
	}
	
	
	public static Collection<NamedAcexAnalyzer> getBackwardAnalyzers() {
		return BWD_ANALYZERS.values();
	}

	
	public static Collection<NamedAcexAnalyzer> getAllAnalyzers() {
		return ALL_ANALYZERS.values();
	}
	
	/*
	 * Constructor.
	 */
	private AcexAnalyzers() {
		throw new AssertionError("Class should not be instantiated");
	}

}
