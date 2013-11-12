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
package de.learnlib.oracles;

import net.automatalib.words.Word;
import de.learnlib.api.Query;

/**
 * A query is a container for tests a learning algorithms performs, containing
 * the actual test and the corresponding result.
 *
 * @param <I> input symbol class.
 * @param <O> output class. 
 * 
 * @author Maik Merten <maikmerten@googlemail.com>
 */
public class DefaultQuery<I, O> extends AbstractQuery<I,O> {
    
    private O output;
    
    public DefaultQuery(Word<I> prefix, Word<I> suffix) {
        super(prefix, suffix);
    }
    
    public DefaultQuery(Word<I> prefix, Word<I> suffix, O output) {
    	this(prefix, suffix);
    	this.output = output;
    }
    
    public DefaultQuery(Word<I> input) {
    	super(input);
    }
    
    public DefaultQuery(Word<I> input, O output) {
    	super(input);
    	this.output = output;
    }
    
    public DefaultQuery(Query<I,?> query) {
    	super(query);
    }

    public O getOutput() {
        return output;
    }

    /*
     * (non-Javadoc)
     * @see de.learnlib.api.Query#setOutput(java.lang.Object)
     */
    @Override
    public void answer(O output) {
        this.output = output;
    }
}
