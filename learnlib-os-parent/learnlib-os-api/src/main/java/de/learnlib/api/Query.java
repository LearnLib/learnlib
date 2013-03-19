/* Copyright (C) 2013 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 * 
 * LearnLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 * 
 * LearnLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with LearnLib; if not, see
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package de.learnlib.api;

import net.automatalib.words.Word;
import net.automatalib.words.util.Words;

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
    private final Word<I> prefix;
    
    /**
     * suffix portion of test
     */
    private final Word<I> suffix;
    
    private O output;
    
    public Query(Word<I> prefix, Word<I> suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }
    
    public Query(Word<I> input) {
    	this(Words.<I>epsilon(), input);
    }
    
    public Query(Query<I,?> query) {
    	this(query.getPrefix(), query.getSuffix());
    }

    public O getOutput() {
        return output;
    }

    public void setOutput(O output) {
        this.output = output;
    }
    
    public Word<I> getPrefix() {
    	return prefix;
    }
    
    public Word<I> getSuffix() {
    	return suffix;
    }
    
    /** 
     * @return prefix.suffix
     */
    public Word<I> getInput() {
        return Words.concat(this.prefix, this.suffix);
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
        final Query<?, ?> other = (Query<?, ?>) obj;
        if (this.prefix != other.prefix && (this.prefix == null || !this.prefix.equals(other.prefix))) {
            return false;
        }
        if (this.suffix != other.suffix && (this.suffix == null || !this.suffix.equals(other.suffix))) {
            return false;
        }
        return true;
    }    
}
