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

import java.awt.Color;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.commons.util.comparison.CmpUtil;

@ParametersAreNonnullByDefault
public abstract class AbstractBlueFringePTAState<SP, TP, S extends AbstractBlueFringePTAState<SP, TP, S>>
        extends AbstractBasePTAState<SP, TP, S> implements Comparable<S> {

    protected boolean isCopy;
    @Nonnull
    protected Color color = Color.WHITE;
    @Nullable
    protected S parent;
    protected int parentInput = -1;
    @Nullable
    protected int[] access;

    @Nonnull
    public Color getColor() {
        return color;
    }

    @Override
    public S copy() {
        S copy = super.copy();
        copy.isCopy = true;
        return copy;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setSuccessor(int index, S successor, int alphabetSize) {
        super.setSuccessor(index, successor, alphabetSize);
        successor.parent = (S) this;
        successor.parentInput = index;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected S createSuccessor(int index) {
        S state = super.createSuccessor(index);
        state.parent = (S) this;
        state.parentInput = index;
        return state;
    }

    @Nonnull
    public PTATransition<S> makeBlue() {
        if (!isWhite()) {
            throw new IllegalStateException();
        }
        assert parent != null;
        if (!parent.isRed()) {
            throw new IllegalStateException();
        }

        this.color = Color.BLUE;

        return new PTATransition<>(parent, parentInput);
    }

    public boolean isWhite() {
        return color == Color.WHITE;
    }

    public boolean isRed() {
        return color == Color.RED;
    }

    public void makeRed(int id) {
        this.color = Color.RED;
        this.id = id;
        buildAccess();
    }

    private void buildAccess() {
        if (access != null) {
            throw new IllegalStateException();
        }
        if (parent == null) {
            this.access = new int[0];
            return;
        }
        int[] parentAccess = parent.access;
        if (parentAccess == null) {
            throw new IllegalStateException();
        }
        int paLen = parentAccess.length;
        this.access = new int[paLen + 1];
        System.arraycopy(parentAccess, 0, this.access, 0, paLen);
        this.access[paLen] = parentInput;
    }

    public boolean isBlue() {
        return color == Color.BLUE;
    }

    @Override
    public int compareTo(S other) {
        if (this == other) {
            return 0;
        }
        if (access == null) {
            if (other.access == null) {
                return 0;
            }
            return 1;
        }
        if (other.access == null) {
            return -1;
        }

        return CmpUtil.canonicalCompare(access, other.access);
    }

    public int lexCompareTo(S other) {
        if (this == other) {
            return 0;
        }
        if (access == null) {
            if (other.access == null) {
                return 0;
            }
            return 1;
        }
        if (other.access == null) {
            return -1;
        }

        return CmpUtil.lexCompare(access, other.access);
    }

    public void setForeignSuccessor(int index, S successor, int alphabetSize) {
        super.setSuccessor(index, successor, alphabetSize);
    }

}
