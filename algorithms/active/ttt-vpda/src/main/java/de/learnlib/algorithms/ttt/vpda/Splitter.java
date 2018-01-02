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
package de.learnlib.algorithms.ttt.vpda;

import de.learnlib.algorithms.discriminationtree.hypothesis.vpda.ContextPair;
import de.learnlib.algorithms.discriminationtree.hypothesis.vpda.DTNode;
import de.learnlib.algorithms.discriminationtree.hypothesis.vpda.HypLoc;
import net.automatalib.words.Word;

/**
 * Data structure for representing a splitter.
 * <p>
 * A splitter is represented by an input symbol, and a DT node that separates the successors (wrt. the input symbol) of
 * the original states. From this, a discriminator can be obtained by prepending the input symbol to the discriminator
 * that labels the separating successor.
 * <p>
 * <b>Note:</b> as the discriminator finalization is applied to the root of a block and affects all nodes, there is no
 * need to store references to the source states from which this splitter was obtained.
 *
 * @param <I>
 *         input symbol type
 *
 * @author Malte Isberner
 */
public final class Splitter<I> {

    public final I symbol;
    public final HypLoc<I> location;
    public final I otherSymbol;
    public final SplitType type;
    public final DTNode<I> succSeparator;

    public Splitter(I symbol, DTNode<I> succSeparator) {
        this.symbol = symbol;
        this.location = null;
        this.otherSymbol = null;
        this.type = SplitType.INTERNAL;
        this.succSeparator = succSeparator;
    }

    public Splitter(I symbol, HypLoc<I> location, I otherSymbol, boolean call, DTNode<I> succSeparator) {
        this.symbol = symbol;
        this.location = location;
        this.otherSymbol = otherSymbol;
        this.type = call ? SplitType.CALL : SplitType.RETURN;
        this.succSeparator = succSeparator;
    }

    public ContextPair<I> getDiscriminator() {
        return succSeparator.getDiscriminator();
    }

    public ContextPair<I> getNewDiscriminator() {
        Word<I> prefix = succSeparator.getDiscriminator().getPrefix();
        Word<I> suffix = succSeparator.getDiscriminator().getSuffix();

        switch (type) {
            case INTERNAL:
                return new ContextPair<>(prefix, suffix.prepend(symbol));
            case RETURN:
                return new ContextPair<>(prefix.concat(location.getAccessSequence()).append(otherSymbol),
                                         suffix.prepend(symbol));
            case CALL:
                return new ContextPair<>(prefix,
                                         location.getAccessSequence()
                                                 .prepend(symbol)
                                                 .append(otherSymbol)
                                                 .concat(suffix));
            default:
                throw new IllegalStateException("Unhandled type " + type);
        }
    }

    public int getNewDiscriminatorLength() {
        if (type == SplitType.INTERNAL) {
            return succSeparator.getDiscriminator().getLength() + 1;
        }
        return succSeparator.getDiscriminator().getLength() + location.getAccessSequence().length() + 2;
    }

    public enum SplitType {
        INTERNAL,
        CALL,
        RETURN
    }
}
