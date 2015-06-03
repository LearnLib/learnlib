/* Copyright (C) 2015 TU Dortmund
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
 * http://www.gnu.de/documents/lgpl.en.html.
 */
package de.learnlib.passive.commons.pta;

import java.text.MessageFormat;

/**
 * Exception to signal that two (state or transition) properties in a {@link BasePTA PTA} cannot
 * be merged since they are incompatible.
 * <p>
 * 
 * @author Malte Isberner
 *
 */
public class PropertyConflictException extends IllegalArgumentException {
	private static final long serialVersionUID = 1L;

	public PropertyConflictException(Object oldProperty, Object newProperty) {
		super(MessageFormat.format("Cannot merge incompatible properties {0} and {1}",
				oldProperty, newProperty));
	}

}
