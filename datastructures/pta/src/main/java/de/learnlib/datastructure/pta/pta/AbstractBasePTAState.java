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
package de.learnlib.datastructure.pta.pta;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import net.automatalib.commons.util.array.RichArray;

public abstract class AbstractBasePTAState<SP, TP, S extends AbstractBasePTAState<SP, TP, S>> implements Cloneable {

    protected SP property;
    protected RichArray<TP> transProperties;
    protected RichArray<S> successors;
    protected int id = -1;

    public SP getStateProperty() {
        return property;
    }

    public TP getTransProperty(int index) {
        if (transProperties == null) {
            return null;
        }
        return transProperties.get(index);
    }

    public S copy() {
        return copy((transProperties != null) ? transProperties.clone() : null);
    }

    public S copy(RichArray<TP> newTPs) {
        try {
            @SuppressWarnings("unchecked")
            S copy = (S) clone();
            copy.transProperties = newTPs;
            if (successors != null) {
                copy.successors = successors.clone();
            }
            return copy;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError(ex);
        }
    }

    public S getSuccessor(int index) {
        if (successors == null) {
            return null;
        }
        return successors.get(index);
    }

    public void setSuccessor(int index, S succ, int alphabetSize) {
        if (successors == null) {
            successors = new RichArray<>(alphabetSize);
        }
        successors.update(index, succ);
    }

    public S getOrCreateSuccessor(int index, int alphabetSize) {
        if (successors == null) {
            successors = new RichArray<>(alphabetSize);
        }
        S succ = successors.get(index);
        if (succ == null) {
            succ = createSuccessor(index);
            successors.update(index, succ);
        }
        return succ;
    }

    protected S createSuccessor(int index) {
        return createState();
    }

    protected abstract S createState();

    public void forEachSucc(Consumer<? super S> cons) {
        if (successors != null) {
            for (S succ : successors) {
                if (succ != null) {
                    cons.accept(succ);
                }
            }
        }
    }

    public void mergeTransitionProperty(int index, int alphabetSize, TP newTP) {
        if (!tryMergeTransitionProperty(index, alphabetSize, newTP)) {
            throw new IllegalArgumentException();
        }
    }

    public boolean tryMergeTransitionProperty(int index, int alphabetSize, TP newTP) {
        if (transProperties != null) {
            TP oldTp = transProperties.get(index);
            if (oldTp != null) {
                return Objects.equals(oldTp, newTP);
            }
        } else {
            transProperties = new RichArray<>(alphabetSize);
        }

        transProperties.update(index, newTP);
        return true;
    }

    public void mergeStateProperty(SP newSP) {
        if (!tryMergeStateProperty(newSP)) {
            throw new IllegalStateException();
        }
    }

    public boolean tryMergeStateProperty(SP newSP) {
        if (property != null) {
            return Objects.equals(property, newSP);
        }
        this.property = newSP;
        return true;
    }

    public Stream<S> successors() {
        if (successors == null) {
            return Stream.empty();
        }
        return successors.stream().filter(Objects::nonNull);
    }
}
