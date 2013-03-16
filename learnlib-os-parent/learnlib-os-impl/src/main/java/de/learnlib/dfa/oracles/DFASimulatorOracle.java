/* Copyright (C) 2013 TU Dortmund
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
   <http://www.gnu.de/documents/lgpl.en.html>.  */

package de.learnlib.dfa.oracles;

import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.ls5.automata.fsa.DFA;
import java.util.List;

/**
 *
 * @author Maik Merten <maikmerten@googlemail.com>
 */
public class DFASimulatorOracle<I> implements MembershipOracle<I, Boolean> {
    
    DFA<?, I> dfa;
    
    public DFASimulatorOracle(DFA<?, I> dfa) {
        this.dfa = dfa;
    }

    @Override
    public void processQueries(List<Query<I, Boolean>> queries) {
        for(Query<I, Boolean> q : queries) {
            q.setOutput(dfa.accepts(q.getInput()));
        }
    }
    
}
