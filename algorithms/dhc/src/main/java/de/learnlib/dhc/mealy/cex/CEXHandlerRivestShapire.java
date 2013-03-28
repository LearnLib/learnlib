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

package de.learnlib.dhc.mealy.cex;

import de.learnlib.api.MembershipOracle;
import de.learnlib.dhc.mealy.MealyDHC;
import de.learnlib.oracles.DefaultQuery;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.automatalib.automata.transout.impl.FastMealy;
import net.automatalib.automata.transout.impl.FastMealyState;
import net.automatalib.words.Word;

/**
 *
 * @author Maik Merten <maikmerten@googlemail.com>
 */
public class CEXHandlerRivestShapire<I, O> {

	private static final Logger log = Logger.getLogger( CEXHandlerRivestShapire.class.getName() );
	
	private MembershipOracle<I, Word<O>> oracle;
	private MealyDHC dhc;
	
	public CEXHandlerRivestShapire(MealyDHC<I, O> dhc, MembershipOracle<I, Word<O>> oracle) {
		this.dhc = dhc;
		this.oracle = oracle;
	}
	
	public void createSuffixes(DefaultQuery<I, Word<O>> ceQuery, Collection<Word<I>> suffixes) {
		Word<I> cex = ceQuery.getInput();
		FastMealy<I,O> hypo = (FastMealy<I,O>) dhc.getHypothesisModel();
		
		List<DefaultQuery<I, Word<O>>> queries = new ArrayList<>();
		DefaultQuery<I, Word<O>> query = new DefaultQuery<>(cex);
		queries.add(query);
		oracle.processQueries(queries);
		
		Word<O> originaloutput = query.getOutput();
		
		int suffixlength = 0;
		// TODO: do this as a binary search
		for(int prefixlength = 0; prefixlength < cex.size(); ++prefixlength) {
			suffixlength = cex.size() - prefixlength;
			
			Word<I> prefix = cex.prefix(prefixlength);
			Word<I> suffix = cex.suffix(suffixlength);
			
			Word<O> origsuff = originaloutput.suffix(suffixlength);
			
			FastMealyState<O> hypoState = hypo.getSuccessor(hypo.getInitialState(), prefix);
			Word<O> access = dhc.getAccessSequence(hypoState);
			
			query = new DefaultQuery(access, suffix);
			queries.clear();
			queries.add(query);
			oracle.processQueries(queries);
			
			Word<O> newsuff = query.getOutput();
			
			log.log(Level.FINE, "{0} vs. {1}", new Object[]{origsuff, newsuff});
			
			if(!origsuff.equals(newsuff)) {
				break;
			}
		}
		
		log.log(Level.FINE, "\n");
		
		suffixes.add(cex.suffix(suffixlength));
	}
	
}
