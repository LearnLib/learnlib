/* Copyright (C) 2013-2023 TU Dortmund
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
package de.learnlib.datastructure.discriminationtree;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;
import de.learnlib.datastructure.discriminationtree.iterators.DiscriminationTreeIterators;
import de.learnlib.datastructure.discriminationtree.model.AbstractWordBasedDTNode;
import org.testng.Assert;
import org.testng.annotations.Test;

public class IteratorsTest {

    @Test
    public void testNodeIterator() {
        final Set<AbstractWordBasedDTNode<Integer, Boolean, Character>> nodes =
                Sets.newHashSet(DiscriminationTreeIterators.nodeIterator(DummyDT.DT.getRoot()));

        Assert.assertEquals(nodes,
                            new HashSet<>(Arrays.asList(DummyDT.INNER_1,
                                                        DummyDT.INNER_2,
                                                        DummyDT.LEAF_1,
                                                        DummyDT.LEAF_2,
                                                        DummyDT.LEAF_3)));
    }

    @Test
    public void testLeafIterator() {
        final Set<AbstractWordBasedDTNode<Integer, Boolean, Character>> nodes =
                Sets.newHashSet(DiscriminationTreeIterators.leafIterator(DummyDT.DT.getRoot()));

        Assert.assertEquals(nodes, new HashSet<>(Arrays.asList(DummyDT.LEAF_1, DummyDT.LEAF_2, DummyDT.LEAF_3)));
    }

    @Test
    public void testInnerNodeIterator() {
        final Set<AbstractWordBasedDTNode<Integer, Boolean, Character>> nodes =
                Sets.newHashSet(DiscriminationTreeIterators.innerNodeIterator(DummyDT.DT.getRoot()));

        Assert.assertEquals(nodes, new HashSet<>(Arrays.asList(DummyDT.INNER_1, DummyDT.INNER_2)));
    }

    @Test
    public void testTransformingLeafIterator() {
        final Set<String> nodes =
                Sets.newHashSet(DiscriminationTreeIterators.transformingLeafIterator(DummyDT.DT.getRoot(),
                                                                                     n -> String.valueOf(n.getData())));

        Assert.assertEquals(nodes, Sets.newHashSet("a", "b", "c"));
    }
}
