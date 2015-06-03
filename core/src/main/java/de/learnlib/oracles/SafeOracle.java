/* Copyright (C) 2013 TU Dortmund
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

package de.learnlib.oracles;

import java.util.Collection;
import java.util.Collections;

import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;

/**
 *
 * @author Maik Merten 
 * 
 * @deprecated since 2015-05-10. This class has no real (and reasonable) use case.
 * All it does is to check whether the collection of queries passed to
 * {@link #processQueries(Collection)} is being modified. However, this is highly unlikely,
 * and wrapping the passed queries using {@link Collections#unmodifiableCollection(Collection)}
 * is a way better approach for intercepting these cases than wrapping the oracle.
 */
@Deprecated
public class SafeOracle<I,D> implements MembershipOracle<I,D> {
    
    private MembershipOracle<I,D> nextOracle;
    
    public SafeOracle(MembershipOracle<I,D> nextOracle) {
        this.nextOracle = nextOracle;
    }
    

    @Override
    public void processQueries(Collection<? extends Query<I, D>> queries) {
        // let the next oracle in chain process the queries
        nextOracle.processQueries(queries);
        
        // now, let's see if everything is okay
        for(Query<I,D> query : queries)
            checkQuery(query);
    }
    
    protected void checkQuery(Query<I,D> query) {
    	
    	// somebody punched holes into our query batch
        if(query == null)
            throw new RuntimeException("Query batch is incomplete, contains null query.");
        
        // is there actual output?
        // FIXME: Removed this because of Query interface change, but since we do not require outputs
        //        to be alphabet symbols, I believe prohibiting null outputs is not a valid requirement?
        //        -mi
        //if(query.getOutput() == null)
        //    throw new RuntimeException("Query batch is not answered, contains null answer for Query (" + query.getPrefix() + ", " + query.getSuffix() + ")");
    }
    
}
