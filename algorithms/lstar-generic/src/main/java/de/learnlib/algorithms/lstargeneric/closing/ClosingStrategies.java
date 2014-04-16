/* Copyright (C) 2013-2014 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 * 
 * LearnLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 * 
 * LearnLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with LearnLib; if not, see
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package de.learnlib.algorithms.lstargeneric.closing;

import java.util.ArrayList;
import java.util.List;

import net.automatalib.commons.util.comparison.CmpUtil;
import net.automatalib.words.Alphabet;
import de.learnlib.algorithms.lstargeneric.table.ObservationTable;
import de.learnlib.algorithms.lstargeneric.table.Row;
import de.learnlib.api.MembershipOracle;


/**
 * Collection of predefined observation table closing strategies.
 * 
 * @see ClosingStrategy
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 */
public class ClosingStrategies {
	
	/**
	 * Closing strategy that randomly selects one representative row to close from each equivalence
	 * class.
	 */
	public static final ClosingStrategy<Object,Object> CLOSE_RANDOM
		= new CloseRandomStrategy();
	
	/**
	 * Closing strategy that selects the first row from each equivalence class as representative.
	 */
	public static final ClosingStrategy<Object,Object> CLOSE_FIRST
		= new ClosingStrategy<Object,Object>() {
			@Override
			public <RI, RO> List<Row<RI>> selectClosingRows(
					List<List<Row<RI>>> unclosedClasses,
					ObservationTable<RI, RO> table,
					MembershipOracle<RI, RO> oracle) {
				List<Row<RI>> result = new ArrayList<Row<RI>>(unclosedClasses.size());
				for(List<Row<RI>> clazz : unclosedClasses)
					result.add(clazz.get(0));
				return result;
			}
			@Override
			public String toString() {
				return "CloseFirst";
			}
	};
	
	/**
	 * Closing strategy that selects the shortest row of each equivalence class (more precisely:
	 * a row which's prefix has minimal length in the respective class) as representative. 
	 */
	public static final ClosingStrategy<Object,Object> CLOSE_SHORTEST
		= new ClosingStrategy<Object,Object>() {
			@Override
			public <RI, RO> List<Row<RI>> selectClosingRows(
					List<List<Row<RI>>> unclosedClasses,
					ObservationTable<RI, RO> table,
					MembershipOracle<RI, RO> oracle) {
				
				List<Row<RI>> result = new ArrayList<Row<RI>>();
				for(List<Row<RI>> clazz : unclosedClasses) {
					Row<RI> shortest = null;
					int shortestLen = Integer.MAX_VALUE;
					for(Row<RI> row : clazz) {
						int prefixLen = row.getPrefix().length();
						if(shortest == null || prefixLen < shortestLen) {
							shortest = row;
							shortestLen = prefixLen;
						}
					}
					result.add(shortest);
				}
				return result;
			}
			@Override
			public String toString() {
				return "CloseShortest";
			}
	};
	
	/**
	 * Closing strategy that selects the lexicographically minimal row (wrt. its prefix)
	 * of each equivalence class as representative.
	 */
	public static final ClosingStrategy<Object,Object> CLOSE_LEX_MIN
		= new ClosingStrategy<Object,Object>() {
			@Override
			public <RI, RO> List<Row<RI>> selectClosingRows(
					List<List<Row<RI>>> unclosedClasses,
					ObservationTable<RI, RO> table,
					MembershipOracle<RI, RO> oracle) {
				List<Row<RI>> result = new ArrayList<Row<RI>>(unclosedClasses.size());
				Alphabet<RI> alphabet = table.getInputAlphabet();
				for(List<Row<RI>> clazz : unclosedClasses) {
					Row<RI> lexMin = null;
					for(Row<RI> row : clazz) {
						if(lexMin == null)
							lexMin = row;
						else if(CmpUtil.lexCompare(row.getPrefix(), lexMin.getPrefix(), alphabet) < 0)
							lexMin = row;
					}
					result.add(lexMin);
				}
				return result;
			}
			@Override
			public String toString() {
				return "CloseLexMin";
			}
	};

	@SuppressWarnings("unchecked")
	public static ClosingStrategy<Object,Object>[] values() {
		return new ClosingStrategy[]{
				CLOSE_RANDOM,
				CLOSE_FIRST,
				CLOSE_SHORTEST,
				CLOSE_LEX_MIN
			};
	}

}
