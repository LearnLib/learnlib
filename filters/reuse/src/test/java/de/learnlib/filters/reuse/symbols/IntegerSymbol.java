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
package de.learnlib.filters.reuse.symbols;

import de.learnlib.drivers.api.SULException;
import de.learnlib.filters.reuse.api.InjectableSystemStateRef;
import de.learnlib.filters.reuse.ssrs.IntegerSystemStateRef;

public class IntegerSymbol
		implements
		InjectableSystemStateRef<IntegerSystemStateRef<IntegerSymbol, String>, IntegerSymbol, String> {
	private final int VAL;
	private IntegerSystemStateRef<IntegerSymbol, String> ssr;

	public static int UPPER_BOUND = 5;
	
	public IntegerSymbol(int value) {
		this.VAL = value;
	}

	@Override
	public String execute() throws SULException {
		int value = ssr.getSystemState();

		if ((value+VAL) <= UPPER_BOUND) {
			value += VAL;
			ssr.setSystemState(value);
			return "ACK";
		} else {
			return "NAK";
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + VAL;
		return result;
	}

	@Override
	public String toString() {
		return "[" + VAL + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof IntegerSymbol)) {
			return false;
		}

		IntegerSymbol that = (IntegerSymbol) obj;

		return VAL == that.VAL;
	}

	@Override
	public void inject(IntegerSystemStateRef<IntegerSymbol, String> ssr) {
		this.ssr = ssr;
	}

	@Override
	public IntegerSystemStateRef<IntegerSymbol, String> retrieve() {
		return ssr;
	}
}
