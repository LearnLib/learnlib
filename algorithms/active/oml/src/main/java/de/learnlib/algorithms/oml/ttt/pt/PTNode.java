/* Copyright (C) 2013-2022 TU Dortmund
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
package de.learnlib.algorithms.oml.ttt.pt;

import de.learnlib.algorithms.oml.ttt.dt.DTLeaf;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author fhowar
 */
public interface PTNode<I, D> {

    Word<I> word();

    PTNode<I, D> append(I a);

    void setState(DTLeaf<I, D> node);

    DTLeaf<I, D> state();

    @Nullable PTNode<I, D> succ(I a);

    void makeShortPrefix();
}
