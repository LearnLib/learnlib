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
package de.learnlib.algorithms.lstar.closing;

import java.util.ArrayList;
import java.util.List;

import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.datastructure.observationtable.ObservationTable;
import de.learnlib.datastructure.observationtable.Row;
import net.automatalib.commons.util.comparison.CmpUtil;
import net.automatalib.words.Alphabet;

/**
 * Collection of predefined observation table closing strategies.
 *
 * @author Malte Isberner
 * @see ClosingStrategy
 */
public final class ClosingStrategies {

    /**
     * Closing strategy that randomly selects one representative row to close from each equivalence class.
     */
    public static final ClosingStrategy<Object, Object> CLOSE_RANDOM = new CloseRandomStrategy();

    /**
     * Closing strategy that selects the first row from each equivalence class as representative.
     */
    public static final ClosingStrategy<Object, Object> CLOSE_FIRST = new ClosingStrategy<Object, Object>() {

        @Override
        public <RI, RD> List<Row<RI>> selectClosingRows(List<List<Row<RI>>> unclosedClasses,
                                                        ObservationTable<RI, RD> table,
                                                        MembershipOracle<RI, RD> oracle) {
            List<Row<RI>> result = new ArrayList<>(unclosedClasses.size());
            for (List<Row<RI>> clazz : unclosedClasses) {
                result.add(clazz.get(0));
            }
            return result;
        }

        @Override
        public String toString() {
            return "CloseFirst";
        }
    };

    /**
     * Closing strategy that selects the shortest row of each equivalence class (more precisely: a row which's prefix
     * has minimal length in the respective class) as representative.
     */
    public static final ClosingStrategy<Object, Object> CLOSE_SHORTEST = new ClosingStrategy<Object, Object>() {

        @Override
        public <RI, RD> List<Row<RI>> selectClosingRows(List<List<Row<RI>>> unclosedClasses,
                                                        ObservationTable<RI, RD> table,
                                                        MembershipOracle<RI, RD> oracle) {

            List<Row<RI>> result = new ArrayList<>();
            for (List<Row<RI>> clazz : unclosedClasses) {
                Row<RI> shortest = null;
                int shortestLen = Integer.MAX_VALUE;
                for (Row<RI> row : clazz) {
                    int prefixLen = row.getLabel().length();
                    if (shortest == null || prefixLen < shortestLen) {
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
     * Closing strategy that selects the lexicographically minimal row (wrt. its prefix) of each equivalence class as
     * representative.
     */
    public static final ClosingStrategy<Object, Object> CLOSE_LEX_MIN = new ClosingStrategy<Object, Object>() {

        @Override
        public <RI, RD> List<Row<RI>> selectClosingRows(List<List<Row<RI>>> unclosedClasses,
                                                        ObservationTable<RI, RD> table,
                                                        MembershipOracle<RI, RD> oracle) {
            List<Row<RI>> result = new ArrayList<>(unclosedClasses.size());
            Alphabet<RI> alphabet = table.getInputAlphabet();
            for (List<Row<RI>> clazz : unclosedClasses) {
                Row<RI> lexMin = null;
                for (Row<RI> row : clazz) {
                    if (lexMin == null) {
                        lexMin = row;
                    } else if (CmpUtil.lexCompare(row.getLabel(), lexMin.getLabel(), alphabet) < 0) {
                        lexMin = row;
                    }
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

    private ClosingStrategies() {
        // prevent instantiation
    }

    @SuppressWarnings("unchecked")
    public static ClosingStrategy<Object, Object>[] values() {
        return new ClosingStrategy[] {CLOSE_RANDOM, CLOSE_FIRST, CLOSE_SHORTEST, CLOSE_LEX_MIN};
    }

}
