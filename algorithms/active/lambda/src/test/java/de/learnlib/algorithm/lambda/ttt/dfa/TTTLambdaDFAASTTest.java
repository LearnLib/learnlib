/* Copyright (C) 2013-2026 TU Dortmund University
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
package de.learnlib.algorithm.lambda.ttt.dfa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.learnlib.algorithm.lambda.ttt.dt.DTLeaf;
import de.learnlib.algorithm.lambda.ttt.pt.PTNode;
import de.learnlib.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.testsupport.AbstractLearnerASTDFATest;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.word.Word;
import org.testng.Assert;

public class TTTLambdaDFAASTTest extends AbstractLearnerASTDFATest<TTTLambdaDFA<Character>> {

    @Override
    protected TTTLambdaDFA<Character> getLearner(DFAMembershipOracle<Character> oracle, Alphabet<Character> alphabet) {
        return new TTTLambdaDFA<>(alphabet, oracle);
    }

    @Override
    protected Collection<Word<Character>> getTrueRepresentatives() {
        final List<DTLeaf<Character, Boolean>> leaves = learner.dtree().leaves();
        final List<Word<Character>> result = new ArrayList<>(leaves.size());

        for (DTLeaf<Character, Boolean> leaf : leaves) {
            List<PTNode<Character, Boolean>> shortPrefixes = leaf.getShortPrefixes();
            Assert.assertEquals(shortPrefixes.size(), 1);
            result.add(shortPrefixes.get(0).word());
        }

        return result;
    }
}
