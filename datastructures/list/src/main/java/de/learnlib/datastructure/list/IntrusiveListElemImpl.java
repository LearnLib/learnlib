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
package de.learnlib.datastructure.list;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An element in an {@link IntrusiveList}.
 *
 * @param <T>
 *         element type
 *
 * @author Malte Isberner
 */
public class IntrusiveListElemImpl<T> implements IntrusiveListElem<T> {

    protected @Nullable T next;

    @Override
    public @Nullable T getNextElement() {
        return next;
    }

    @Override
    public void setNextElement(@Nullable T next) {
        this.next = next;
    }
}
