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
package de.learnlib.algorithm.ostia;

import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

import net.automatalib.common.smartcollection.IntSeq;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.PolyNull;

class IntQueue {

    int value;
    @Nullable IntQueue next;

    @Override
    public String toString() {
        final StringJoiner sj = new StringJoiner(", ", "[", "]");

        IntQueue iter = this;
        while (iter != null) {
            sj.add(Integer.toString(iter.value));
            iter = iter.next;
        }
        return sj.toString();
    }

    static @Nullable IntQueue asQueue(IntSeq str) {
        IntQueue q = null;
        for (int i = str.size() - 1; i >= 0; i--) {
            IntQueue next = new IntQueue();
            next.value = str.get(i);
            next.next = q;
            q = next;
        }
        assert !IntQueue.hasCycle(q);
        return q;
    }

    static boolean eq(@Nullable IntQueue a, @Nullable IntQueue b) {
        IntQueue aIter = a;
        IntQueue bIter = b;
        while (aIter != null && bIter != null) {
            if (aIter.value != bIter.value) {
                return false;
            }
            aIter = aIter.next;
            bIter = bIter.next;
        }
        return aIter == null && bIter == null;
    }

    static boolean hasCycle(@Nullable IntQueue q) {
        final Set<IntQueue> elements = new HashSet<>();
        IntQueue iter = q;
        while (iter != null) {
            if (!elements.add(iter)) {
                return true;
            }
            iter = iter.next;
        }
        return false;
    }

    static @PolyNull IntQueue copyAndConcat(@Nullable IntQueue q, @PolyNull IntQueue tail) {
        assert !hasCycle(q) && !hasCycle(tail);
        if (q == null) {
            return tail;
        }
        final IntQueue root = new IntQueue();
        root.value = q.value;
        IntQueue curr = root;
        IntQueue iter = q.next;
        while (iter != null) {
            curr.next = new IntQueue();
            curr = curr.next;
            curr.value = iter.value;
            iter = iter.next;
        }
        curr.next = tail;
        assert !hasCycle(root);
        return root;
    }
}
