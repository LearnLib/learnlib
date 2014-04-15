/* Copyright (C) 2014 TU Dortmund
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
package de.learnlib.mapper;

import de.learnlib.api.SULException;
import de.learnlib.mapper.api.Mapper;

/**
 * Abstract base for a {@link Mapper}.
 * <p>
 * This class comes with the following default behavior:
 * <ul>
 * <li>both {@link #pre()} and {@link #post()} do nothing.</li>
 * <li>if a {@link SULException} occurs, it is treated like an ordinary {@link RuntimeException}
 * and hence passed to {@link #mapUnwrappedException(RuntimeException)}.</li>
 * <li>unwrapped exceptions are simply rethrown.</li>
 * </ul>
 *  
 * @author Malte Isberner
 *
 * @param <AI> abstract input symbol type
 * @param <AO> abstract output symbol type
 * @param <CI> concrete input symbol type
 * @param <CO> concrete output symbol type
 */
public abstract class AbstractMapper<AI, AO, CI, CO> implements Mapper<AI, AO, CI, CO> {

	@Override
	public void pre() {
	}

	@Override
	public void post() {
	}


	@Override
	public MappedException<? extends AO> mapWrappedException(
			SULException exception) throws SULException {
		return mapUnwrappedException(exception);
	}

	@Override
	public MappedException<? extends AO> mapUnwrappedException(
			RuntimeException exception) throws SULException, RuntimeException {
		throw exception;
	}

}
