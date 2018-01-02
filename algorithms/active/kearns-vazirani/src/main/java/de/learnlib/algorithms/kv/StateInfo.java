/* Copyright (C) 2013-2018 TU Dortmund
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
package de.learnlib.algorithms.kv;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.learnlib.datastructure.discriminationtree.model.AbstractWordBasedDTNode;
import net.automatalib.words.Word;

/**
 * The information associated with a state: it's access sequence (or access string), and the list of incoming
 * transitions.
 *
 * @param <I>
 *         input symbol type
 * @param <D>
 *         data type
 *
 * @author Malte Isberner
 */
public final class StateInfo<I, D> implements Serializable {

    public static final short INTEGER_WORD_WIDTH = 32;

    public final int id;
    public final Word<I> accessSequence;
    public AbstractWordBasedDTNode<I, D, StateInfo<I, D>> dtNode;
    //private TLongList incoming;
    private List<Long> incoming; // TODO: replace with primitive specialization

    public StateInfo(int id, Word<I> accessSequence) {
        this.accessSequence = accessSequence.trimmed();
        this.id = id;
    }

    public void addIncoming(int sourceState, int transIdx) {
        long encodedTrans = ((long) sourceState << INTEGER_WORD_WIDTH) | transIdx;
        if (incoming == null) {
            //incoming = new TLongArrayList();
            incoming = new ArrayList<>(); // TODO: replace with primitive specialization
        }
        incoming.add(encodedTrans);
    }

    //public TLongList fetchIncoming() {
    public List<Long> fetchIncoming() { // TODO: replace with primitive specialization
        if (incoming == null || incoming.isEmpty()) {
            //return EMPTY_LONG_LIST;
            return Collections.emptyList(); // TODO: replace with primitive specialization
        }
        //TLongList result = incoming;
        List<Long> result = incoming;
        this.incoming = null;
        return result;
    }
}