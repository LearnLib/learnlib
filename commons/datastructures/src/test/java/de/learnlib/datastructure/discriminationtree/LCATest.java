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
package de.learnlib.datastructure.discriminationtree;

import de.learnlib.datastructure.discriminationtree.model.AbstractWordBasedDTNode;
import de.learnlib.datastructure.discriminationtree.model.LCAInfo;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LCATest {

    @Test
    public void testLCA() {
        final LCAInfo<Boolean, AbstractWordBasedDTNode<Integer, Boolean, Character>> lcaInfo =
                DummyDT.DT.lcaInfo(DummyDT.LEAF_2, DummyDT.LEAF_3);

        Assert.assertEquals(lcaInfo.leastCommonAncestor, DummyDT.INNER_2);
        Assert.assertFalse(lcaInfo.subtree1Label);
        Assert.assertTrue(lcaInfo.subtree2Label);

        final LCAInfo<Boolean, AbstractWordBasedDTNode<Integer, Boolean, Character>> lcaInfo2 =
                DummyDT.DT.lcaInfo(DummyDT.LEAF_3, DummyDT.LEAF_1);

        Assert.assertEquals(lcaInfo2.leastCommonAncestor, DummyDT.DT.getRoot());
        Assert.assertTrue(lcaInfo2.subtree1Label);
        Assert.assertFalse(lcaInfo2.subtree2Label);

        final LCAInfo<Boolean, AbstractWordBasedDTNode<Integer, Boolean, Character>> lcaInfo3 =
                DummyDT.DT.lcaInfo(DummyDT.INNER_2, DummyDT.INNER_2);

        Assert.assertEquals(lcaInfo3.leastCommonAncestor, DummyDT.INNER_2);
        Assert.assertNull(lcaInfo3.subtree1Label);
        Assert.assertNull(lcaInfo3.subtree2Label);
    }
}
