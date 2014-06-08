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

/**
 * A filter is an oracle that can be used as the middle 
 * element in a chain of oracles.
 * 
 * @param <I> input symbol class
 * @param <D> output domain type
 * 
 * @author falkhowar
 */
public interface Filter<I, D> extends MembershipOracle<I, D> {

    /**
     * sets oracle for processing membership queries.
     * 
     * @param next 
     */
    void setNext(MembershipOracle<I, D> next);

}
