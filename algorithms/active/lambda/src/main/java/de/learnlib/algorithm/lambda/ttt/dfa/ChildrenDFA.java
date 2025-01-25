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
package de.learnlib.algorithm.lambda.ttt.dfa;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import de.learnlib.algorithm.lambda.ttt.dt.AbstractDTNode;
import de.learnlib.algorithm.lambda.ttt.dt.Children;
import de.learnlib.algorithm.lambda.ttt.dt.DTInnerNode;
import de.learnlib.algorithm.lambda.ttt.dt.DTLeaf;
import org.checkerframework.checker.nullness.qual.Nullable;

class ChildrenDFA<I> implements Children<I, Boolean> {

    private @Nullable AbstractDTNode<I, Boolean> trueChild;
    private @Nullable AbstractDTNode<I, Boolean> falseChild;

    @Override
    public @Nullable AbstractDTNode<I, Boolean> child(Boolean out) {
        return out ? trueChild : falseChild;
    }

    @Override
    public Boolean key(AbstractDTNode<I, Boolean> child) {
        if (child == trueChild) {
            return true;
        } else if (child == falseChild) {
            return false;
        } else {
            throw new AssertionError("this should not be possible");
        }
    }

    @Override
    public void addChild(Boolean out, AbstractDTNode<I, Boolean> child) {
        assert child(out) == null;
        if (out) {
            trueChild = child;
        } else {
            falseChild = child;
        }
    }

    @Override
    public void replace(DTLeaf<I, Boolean> oldNode, DTInnerNode<I, Boolean> newNode) {
        if (oldNode == trueChild) {
            trueChild = newNode;
        } else if (oldNode == falseChild) {
            falseChild = newNode;
        } else {
            throw new AssertionError("this should not be possible");
        }
    }

    @Override
    public Collection<AbstractDTNode<I, Boolean>> all() {
        if (trueChild != null && falseChild != null) {
            return Arrays.asList(trueChild, falseChild);
        } else if (trueChild != null) {
            return Collections.singletonList(trueChild);
        } else if (falseChild != null) {
            return Collections.singletonList(falseChild);
        } else {
            return Collections.emptyList();
        }
    }

}
