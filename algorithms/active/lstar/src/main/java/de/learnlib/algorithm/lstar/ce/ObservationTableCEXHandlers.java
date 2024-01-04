/* Copyright (C) 2013-2024 TU Dortmund University
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
package de.learnlib.algorithm.lstar.ce;

import java.util.Collections;
import java.util.List;

import de.learnlib.counterexample.GlobalSuffixFinder;
import de.learnlib.counterexample.GlobalSuffixFinders;
import de.learnlib.counterexample.LocalSuffixFinder;
import de.learnlib.counterexample.LocalSuffixFinders;
import de.learnlib.datastructure.observationtable.MutableObservationTable;
import de.learnlib.datastructure.observationtable.Row;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.query.Query;
import net.automatalib.automaton.concept.SuffixOutput;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ObservationTableCEXHandlers {

    public static final ObservationTableCEXHandler<@Nullable Object, @Nullable Object> CLASSIC_LSTAR =
            new ObservationTableCEXHandler<@Nullable Object, @Nullable Object>() {

                @Override
                public <RI, RD> List<List<Row<RI>>> handleCounterexample(DefaultQuery<RI, RD> ceQuery,
                                                                         MutableObservationTable<RI, RD> table,
                                                                         SuffixOutput<RI, RD> hypOutput,
                                                                         MembershipOracle<RI, RD> oracle) {
                    return handleClassicLStar(ceQuery, table, oracle);
                }

                @Override
                public boolean needsConsistencyCheck() {
                    return true;
                }

                @Override
                public String toString() {
                    return "ClassicLStar";
                }

            };

    public static final ObservationTableCEXHandler<@Nullable Object, @Nullable Object> SUFFIX1BY1 =
            new ObservationTableCEXHandler<@Nullable Object, @Nullable Object>() {

                @Override
                public <RI, RD> List<List<Row<RI>>> handleCounterexample(DefaultQuery<RI, RD> ceQuery,
                                                                         MutableObservationTable<RI, RD> table,
                                                                         SuffixOutput<RI, RD> hypOutput,
                                                                         MembershipOracle<RI, RD> oracle) {
                    return handleSuffix1by1(ceQuery, table, oracle);
                }

                @Override
                public boolean needsConsistencyCheck() {
                    return false;
                }

                @Override
                public String toString() {
                    return "Suffix1by1";
                }
            };

    public static final ObservationTableCEXHandler<@Nullable Object, @Nullable Object> MALER_PNUELI =
            fromGlobalSuffixFinder(GlobalSuffixFinders.MALER_PNUELI);

    public static final ObservationTableCEXHandler<@Nullable Object, @Nullable Object> SHAHBAZ =
            fromGlobalSuffixFinder(GlobalSuffixFinders.SHAHBAZ);

    public static final ObservationTableCEXHandler<@Nullable Object, @Nullable Object> FIND_LINEAR =
            fromLocalSuffixFinder(LocalSuffixFinders.FIND_LINEAR, false);

    public static final ObservationTableCEXHandler<@Nullable Object, @Nullable Object> FIND_LINEAR_ALLSUFFIXES =
            fromLocalSuffixFinder(LocalSuffixFinders.FIND_LINEAR, true);

    public static final ObservationTableCEXHandler<@Nullable Object, @Nullable Object> FIND_LINEAR_REVERSE =
            fromLocalSuffixFinder(LocalSuffixFinders.FIND_LINEAR_REVERSE, false);

    public static final ObservationTableCEXHandler<@Nullable Object, @Nullable Object> FIND_LINEAR_REVERSE_ALLSUFFIXES =
            fromLocalSuffixFinder(LocalSuffixFinders.FIND_LINEAR_REVERSE, true);

    public static final ObservationTableCEXHandler<@Nullable Object, @Nullable Object> RIVEST_SCHAPIRE =
            fromLocalSuffixFinder(LocalSuffixFinders.RIVEST_SCHAPIRE, false);

    public static final ObservationTableCEXHandler<@Nullable Object, @Nullable Object> RIVEST_SCHAPIRE_ALLSUFFIXES =
            fromLocalSuffixFinder(LocalSuffixFinders.RIVEST_SCHAPIRE, true);

    private ObservationTableCEXHandlers() {
        // prevent instantiation
    }

    public static <I, D> ObservationTableCEXHandler<I, D> fromGlobalSuffixFinder(GlobalSuffixFinder<I, D> globalFinder) {
        return new ObservationTableCEXHandler<I, D>() {

            @Override
            public <RI extends I, RD extends D> List<List<Row<RI>>> handleCounterexample(DefaultQuery<RI, RD> ceQuery,
                                                                                         MutableObservationTable<RI, RD> table,
                                                                                         SuffixOutput<RI, RD> hypOutput,
                                                                                         MembershipOracle<RI, RD> oracle) {
                List<Word<RI>> suffixes = globalFinder.findSuffixes(ceQuery, table, hypOutput, oracle);
                return handleGlobalSuffixes(table, suffixes, oracle);
            }

            @Override
            public boolean needsConsistencyCheck() {
                return false;
            }

            @Override
            public String toString() {
                return globalFinder.toString();
            }
        };
    }

    public static <I, D> List<List<Row<I>>> handleGlobalSuffixes(MutableObservationTable<I, D> table,
                                                                 List<? extends Word<I>> suffixes,
                                                                 MembershipOracle<I, D> oracle) {
        return table.addSuffixes(suffixes, oracle);
    }

    public static <I, D> ObservationTableCEXHandler<I, D> fromLocalSuffixFinder(LocalSuffixFinder<I, D> localFinder) {
        return fromLocalSuffixFinder(localFinder, false);
    }

    public static <I, D> ObservationTableCEXHandler<I, D> fromLocalSuffixFinder(LocalSuffixFinder<I, D> localFinder,
                                                                                boolean allSuffixes) {
        return new ObservationTableCEXHandler<I, D>() {

            @Override
            public <RI extends I, RD extends D> List<List<Row<RI>>> handleCounterexample(DefaultQuery<RI, RD> ceQuery,
                                                                                         MutableObservationTable<RI, RD> table,
                                                                                         SuffixOutput<RI, RD> hypOutput,
                                                                                         MembershipOracle<RI, RD> oracle) {
                int suffixIdx = localFinder.findSuffixIndex(ceQuery, table, hypOutput, oracle);
                return handleLocalSuffix(ceQuery, table, suffixIdx, allSuffixes, oracle);
            }

            @Override
            public boolean needsConsistencyCheck() {
                return false;
            }

            @Override
            public String toString() {
                return localFinder.toString();
            }
        };
    }

    public static <I, D> List<List<Row<I>>> handleLocalSuffix(Query<I, D> ceQuery,
                                                              MutableObservationTable<I, D> table,
                                                              int suffixIndex,
                                                              MembershipOracle<I, D> oracle) {
        return handleLocalSuffix(ceQuery, table, suffixIndex, false, oracle);
    }

    public static <I, D> List<List<Row<I>>> handleLocalSuffix(Query<I, D> ceQuery,
                                                              MutableObservationTable<I, D> table,
                                                              int suffixIndex,
                                                              boolean allSuffixes,
                                                              MembershipOracle<I, D> oracle) {
        List<Word<I>> suffixes = GlobalSuffixFinders.suffixesForLocalOutput(ceQuery, suffixIndex, allSuffixes);
        return handleGlobalSuffixes(table, suffixes, oracle);
    }

    public static <I, D> List<List<Row<I>>> handleClassicLStar(DefaultQuery<I, D> ceQuery,
                                                               MutableObservationTable<I, D> table,
                                                               MembershipOracle<I, D> oracle) {

        List<Word<I>> prefixes = ceQuery.getInput().prefixes(false);

        return table.addShortPrefixes(prefixes, oracle);
    }

    public static <I, D> List<List<Row<I>>> handleSuffix1by1(DefaultQuery<I, D> ceQuery,
                                                             MutableObservationTable<I, D> table,
                                                             MembershipOracle<I, D> oracle) {
        List<List<Row<I>>> unclosed = Collections.emptyList();

        Word<I> ceWord = ceQuery.getInput();
        int ceLen = ceWord.length();

        for (int i = 1; i <= ceLen; i++) {
            Word<I> suffix = ceWord.suffix(i);
            unclosed = table.addSuffix(suffix, oracle);
            if (!unclosed.isEmpty()) {
                break;
            }
        }

        return unclosed;
    }

    @SuppressWarnings("unchecked")
    public static ObservationTableCEXHandler<@Nullable Object, @Nullable Object>[] values() {
        return new ObservationTableCEXHandler[] {CLASSIC_LSTAR,
                                                 SUFFIX1BY1,
                                                 MALER_PNUELI,
                                                 SHAHBAZ,
                                                 FIND_LINEAR,
                                                 FIND_LINEAR_ALLSUFFIXES,
                                                 FIND_LINEAR_REVERSE,
                                                 FIND_LINEAR_REVERSE_ALLSUFFIXES,
                                                 RIVEST_SCHAPIRE,
                                                 RIVEST_SCHAPIRE_ALLSUFFIXES};
    }
}
