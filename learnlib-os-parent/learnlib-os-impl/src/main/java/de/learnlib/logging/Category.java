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

package de.learnlib.logging;

/**
 * Categories for filter.
 * 
 * @author falkhowar
 */
public enum Category {
    SYSTEM,         // not LearnLib related
    PHASE,          // new phase (learning, ce-handling, etc.)
    QUERY,          // membership query
    COUNTEREXAMPLE, // counterexample
    STATISTIC,      // statistic information
    PROFILING,      // profiling information
    DATASTRUCTURE,  // ds maintained by algorithm
    MODEL,          // inferred hypothesis
    CONFIG,         // learning setup
    EVENT;          // splitting of a table cell or similar
}
