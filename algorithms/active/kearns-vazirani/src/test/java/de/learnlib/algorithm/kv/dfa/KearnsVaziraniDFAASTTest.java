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
package de.learnlib.algorithm.kv.dfa;

import java.util.Collection;

import de.learnlib.algorithm.kv.StateInfo;
import de.learnlib.datastructure.discriminationtree.BinaryDTree;
import de.learnlib.datastructure.discriminationtree.iterators.DiscriminationTreeIterators;
import de.learnlib.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.testsupport.AbstractLearnerASTDFATest;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.common.util.collection.IteratorUtil;
import net.automatalib.word.Word;

public class KearnsVaziraniDFAASTTest extends AbstractLearnerASTDFATest<KearnsVaziraniDFA<Character>> {

    @Override
    protected KearnsVaziraniDFA<Character> getLearner(DFAMembershipOracle<Character> oracle,
                                                      Alphabet<Character> alphabet) {
        return new KearnsVaziraniDFABuilder<Character>().withAlphabet(alphabet).withOracle(oracle).create();
    }

    @Override
    protected Collection<Word<Character>> getTrueRepresentatives() {
        final BinaryDTree<Character, StateInfo<Character, Boolean>> dt = learner.getDiscriminationTree();
        return IteratorUtil.list(IteratorUtil.map(DiscriminationTreeIterators.leafIterator(dt.getRoot()),
                                                  si -> si.getData().accessSequence));
    }
}
