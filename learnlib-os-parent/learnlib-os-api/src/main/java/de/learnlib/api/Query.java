/* Copyright (C) 2012 TU Dortmund
 This file is part of LearnLib 

 LearnLib is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License version 3.0 as published by the Free Software Foundation.

 LearnLib is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with LearnLib; if not, see
 <http://www.gnu.de/documents/lgpl.en.html>
 */
package de.learnlib.api;

import de.ls5.words.Word;
import static de.ls5.words.util.Words.concat;
import static de.ls5.words.util.Words.epsilon;

/**
 * A query is a container for tests a learning algorithms performs, containing
 * the actual test and the corresponding result.
 *
 * @param <I> input symbol class.
 * @param <O> output class. 
 * 
 * @author merten
 */
public class Query<I, O> {

    /**
     * prefix portion of test
     */
    public final Word<I> prefix;
    
    /**
     * suffix portion of test
     */
    public final Word<I> suffix;
    
    private O output;
    
    public Query(Word<I> prefix, Word<I> suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }
    
    public Query(Word<I> input) {
        this.prefix = epsilon();
        this.suffix = input;
    }

    public O getOutput() {
        return output;
    }

    public void setOutput(O output) {
        this.output = output;
    }
    
    /** 
     * @return prefix.suffix
     */
    public Word<I> getInput() {
        return concat(this.prefix, this.suffix);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + (this.prefix != null ? this.prefix.hashCode() : 0);
        hash = 89 * hash + (this.suffix != null ? this.suffix.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Query<I, O> other = (Query<I, O>) obj;
        if (this.prefix != other.prefix && (this.prefix == null || !this.prefix.equals(other.prefix))) {
            return false;
        }
        if (this.suffix != other.suffix && (this.suffix == null || !this.suffix.equals(other.suffix))) {
            return false;
        }
        return true;
    }    
}