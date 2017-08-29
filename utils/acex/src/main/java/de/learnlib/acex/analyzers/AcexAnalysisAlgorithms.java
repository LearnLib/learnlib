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

import de.learnlib.acex.AbstractCounterexample;

public class AcexAnalysisAlgorithms {

	/**
	 * Scan linearly through the counterexample in ascending order.
	 * 
	 * @param acex the abstract counterexample
	 * @param low the lower bound of the search range
	 * @param high the upper bound of the search range
	 * @return an index <code>i</code> such that
	 * <code>acex.testEffect(i) != acex.testEffect(i+1)</code>
	 */
	public static <E> int linearSearchFwd(AbstractCounterexample<E> acex, int low, int high) {
		assert !acex.testEffects(low, high);
		
		E effPrev = acex.effect(low);
		for (int i = low + 1; i <= high; i++) {
			E eff = acex.effect(i);
			if (!acex.checkEffects(effPrev, eff)) {
				return i - 1;
			}
			effPrev = eff;
		}
		throw new IllegalArgumentException();
	}
	
	/**
	 * Scan linearly through the counterexample in descending order.
	 * 
	 * @param acex the abstract counterexample
	 * @param low the lower bound of the search range
	 * @param high the upper bound of the search range
	 * @return an index <code>i</code> such that
	 * <code>acex.testEffect(i) != acex.testEffect(i+1)</code>
	 */
	public static <E> int linearSearchBwd(AbstractCounterexample<E> acex, int low, int high) {
		assert !acex.testEffects(low, high);
		
		E effPrev = acex.effect(high);
		for (int i = high - 1; i >= low; i--) {
			E eff = acex.effect(i);
			if (!acex.checkEffects(eff, effPrev)) {
				return i;
			}
			effPrev = eff;
		}
		throw new IllegalArgumentException();
	}
	
	/**
	 * Search for a suffix index using an exponential search.
	 * 
	 * @param acex the abstract counterexample
	 * @param low the lower bound of the search range
	 * @param high the upper bound of the search range
	 * @return an index <code>i</code> such that
	 * <code>acex.testEffect(i) != acex.testEffect(i+1)</code>
	 */
	public static <E> int exponentialSearchBwd(AbstractCounterexample<E> acex, int low, int high) {
		assert !acex.testEffects(low, high);
		
		int ofs = 1;
		E effHigh = acex.effect(high);
		
		while(high - ofs > low) {
			int next = high - ofs;
			E eff = acex.effect(next);
			if(!acex.checkEffects(eff, effHigh)) {
				low = next;
				break;
			}
			high = next;
			ofs *= 2;
		}
		
		return binarySearchRight(acex, low, high);
	}
	
	public static <E> int exponentialSearchFwd(AbstractCounterexample<E> acex, int low, int high) {
		assert !acex.testEffects(low, high);
		
		int ofs = 1;
		E effLow = acex.effect(low);
		while(low + ofs < high) {
			int next = low + ofs;
			E eff = acex.effect(next);
			if(!acex.checkEffects(effLow, eff)) {
				high = next;
				break;
			}
			low = next;
			ofs *= 2;
		}
		
		return binarySearchLeft(acex, low, high);
	}
	
	/**
	 * Search for a suffix index using a binary search.
	 * 
	 * @param acex the abstract counterexample
	 * @param low the lower bound of the search range
	 * @param high the upper bound of the search range
	 * @return an index <code>i</code> such that
	 * <code>acex.testEffect(i) != acex.testEffect(i+1)</code>
	 */
	public static <E> int binarySearchRight(AbstractCounterexample<E> acex, int low, int high) {
		E effLow = acex.effect(low);
		E effHigh = acex.effect(high);
		
		assert !acex.checkEffects(effLow, effHigh) : "compatible effects at " + low + ", " + high + ": " + effLow + ", " + effHigh;
		
		while(high - low > 1) {
			int mid = low + (high - low)/2;
			E effMid = acex.effect(mid);
			if(!acex.checkEffects(effMid, effHigh)) {
				low = mid;
				effLow = effMid;
			}
			else {
				high = mid;
				effHigh = effMid;
			}
		}
		
		return low;
	}
	
	public static <E> int binarySearchLeft(AbstractCounterexample<E> acex, int low, int high) {
		E effLow = acex.effect(low);
		E effHigh = acex.effect(high);
		
		assert !acex.checkEffects(effLow, effHigh) : "compatible effects at " + low + ", " + high + ": " + effLow + ", " + effHigh;
		
		while(high - low > 1) {
			int mid = low + (high - low)/2;
			E effMid = acex.effect(mid);
			if(!acex.checkEffects(effLow, effMid)) {
				high = mid;
				effHigh = effMid;
			}
			else {
				low = mid;
				effLow = effMid;
			}
		}
		
		return low;
	}
//	
//	/**
//	 *  Search for a suffix index using a partition search
//	 * 
//	 * @param acex the abstract counterexample
//	 * @param low the lower bound of the search range
//	 * @param high the upper bound of the search range
//	 * @return an index <code>i</code> such that
//	 * <code>acex.testEffect(i) != acex.testEffect(i+1)</code>
//	 */
//	public static int partitionSearchBwd(AbstractCounterexample acex, int low, int high) {
//		assert acex.test(low) == 0 && acex.test(high) == 1;
//		
//		int span = high - low + 1;
//		double logSpan = Math.log(span)/Math.log(2);
//		
//		int step = (int)(span/logSpan);
//		
//		while(high - step > low) {
//			if(acex.test(high - step) == 0) {
//				low = high - step;
//				break;
//			}
//			high -= step;
//		}
//		
//		return binarySearch(acex, low, high);
//	}
//	
//	public static int partitionSearchFwd(AbstractCounterexample acex, int low, int high) {
//		assert acex.test(low) == 0 && acex.test(high) == 1;
//		
//		int span = high - low + 1;
//		double logSpan = Math.log(span)/Math.log(2);
//		
//		int step = (int)(span/logSpan);
//		
//		while(low + step < high) {
//			if(acex.test(low + step) == 1) {
//				high = low + step;
//				break;
//			}
//			low += step;
//		}
//		
//		return binarySearch(acex, low, high);
//	}
}
