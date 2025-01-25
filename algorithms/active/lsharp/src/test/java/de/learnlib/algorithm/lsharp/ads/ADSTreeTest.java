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
package de.learnlib.algorithm.lsharp.ads;

import java.util.Arrays;
import java.util.List;

import de.learnlib.algorithm.lsharp.NormalObservationTree;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ADSTreeTest {

    private final Alphabet<Integer> alpha = Alphabets.integers(0, 12);

    // @formatter:off
    private final Integer[][] inputSeqs= {{0}, {1}, {2}, {3}, {4}, {5}, {6}, {7}, {8}, {9}, {10}, {11}, {12},
    {2, 3, 8, 8, 8, 1, 12, 7, 4, 7, 5, 0, 2, 7, 6, 6, 6, 8, 3, 12, 11, 8, 8, 7, 3, 2}};
    private final Integer[][] outputSeqs = {{0}, {0}, {0}, {0}, {0}, {1}, {0}, {1}, {0}, {2}, {1}, {2}, {1},
    {0, 3, 3, 3, 3, 3, 1, 1, 3, 1, 1, 3, 3, 1, 3, 4, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3}};
    // @formatter:on

    @Test
    public void adsRegression() {
        NormalObservationTree<Integer, Integer> tree = new NormalObservationTree<>(alpha);
        for (int i = 0; i < inputSeqs.length; i++) {
            tree.insertObservation(null, Word.fromSymbols(inputSeqs[i]), Word.fromSymbols(outputSeqs[i]));
        }

        List<Integer> block = Arrays.asList(0, 3);
        ADSTree<Integer, Integer, Integer> x = new ADSTree<>(tree, block, Integer.MAX_VALUE);

        Assert.assertEquals(x.getScore(), 2);
    }
}
