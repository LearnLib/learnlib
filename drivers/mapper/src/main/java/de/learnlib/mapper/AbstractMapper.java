/* Copyright (C) 2014 TU Dortmund
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
