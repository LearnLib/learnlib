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
package de.learnlib.algorithm.observationpack.vpa.hypothesis;

import de.learnlib.datastructure.list.IntrusiveList;
import de.learnlib.datastructure.list.IntrusiveListEntry;

/**
 * A list of transitions.
 *
 * @param <I>
 *         input symbol type
 */
public class TransList<I> extends IntrusiveList<AbstractHypTrans<I>> {

    public AbstractHypTrans<I> chooseMinimal() {
        IntrusiveListEntry<AbstractHypTrans<I>> curr = getNext();
        assert curr != null;

        IntrusiveListEntry<AbstractHypTrans<I>> shortest = curr;
        int shortestLen = shortest.getElement().getAccessSequence().length();

        curr = curr.getNext();
        while (curr != null) {
            int transLen = curr.getElement().getAccessSequence().length();
            if (transLen < shortestLen) {
                shortestLen = transLen;
                shortest = curr;
            }
            curr = curr.getNext();
        }

        return shortest.getElement();
    }

}
