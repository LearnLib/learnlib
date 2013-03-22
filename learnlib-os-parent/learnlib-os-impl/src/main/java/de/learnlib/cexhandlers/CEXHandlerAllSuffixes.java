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

package de.learnlib.cexhandlers;

import de.learnlib.api.CEXHandlerSuffixes;
import de.learnlib.api.Query;
import java.util.Collection;
import net.automatalib.words.Word;

/**
 * Simple strategy that return all suffixes of a counterexample
 * 
 * @author Maik Merten <maikmerten@googlemail.com>
 */
public class CEXHandlerAllSuffixes<I, O> implements CEXHandlerSuffixes<I, O>{

    @Override
    public void createSuffixes(Query<I, O> ceQuery, Collection<Word<I>> suffixes) {
        Word<I> counterexample = ceQuery.getInput();
        
        for(int i = 1; i <= counterexample.size(); ++i) {
            suffixes.add(counterexample.suffix(i));
        }
        
    }
    
}
