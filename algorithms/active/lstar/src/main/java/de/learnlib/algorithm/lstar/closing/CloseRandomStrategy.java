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
package de.learnlib.algorithm.lstar.closing;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.learnlib.datastructure.observationtable.ObservationTable;
import de.learnlib.datastructure.observationtable.Row;
import de.learnlib.oracle.MembershipOracle;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CloseRandomStrategy implements ClosingStrategy<@Nullable Object, @Nullable Object> {

    private final Random random;

    public CloseRandomStrategy() {
        this(new Random());
    }

    public CloseRandomStrategy(Random random) {
        this.random = random;
    }

    @Override
    public <RI, RD> List<Row<RI>> selectClosingRows(List<List<Row<RI>>> unclosedClasses,
                                                    ObservationTable<RI, RD> table,
                                                    MembershipOracle<RI, RD> oracle) {
        List<Row<RI>> result = new ArrayList<>(unclosedClasses.size());

        for (List<Row<RI>> clazz : unclosedClasses) {
            int card = clazz.size();
            result.add(clazz.get(random.nextInt(card)));
        }

        return result;
    }

    @Override
    public String toString() {
        return "CloseRandom";
    }

}
