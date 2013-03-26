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
package de.learnlib.oracles;

import de.learnlib.api.MembershipOracle;
import de.learnlib.components.LLComponent;
import de.learnlib.components.LLComponentFactory;
import de.learnlib.components.LLComponentParameter;
import net.automatalib.automata.concepts.SODetOutputAutomaton;

/**
 * factory for simulator oracles.
 * 
 * @author falkhowar
 */
@LLComponent(
        name="SimulatorOracle",
        description="Oracle that uses an automaton to answer queries",
        type=MembershipOracle.class)
public class SimulatorOracleFactory<I,O> implements LLComponentFactory<MembershipOracle<I,O>> {

    private SODetOutputAutomaton<?,I,?,O> target = null;
    
    @LLComponentParameter(
            name="target",
            description="automaton to be used to answer queries",
            required=true)
    public void setTarget(SODetOutputAutomaton<?,I,?,O> target) {
        this.target = target;
    }
    
    @Override
    public MembershipOracle<I, O> instantiate() {
        if (this.target == null) {
            throw  new IllegalStateException("target cannot be null");
        }
        return new SimulatorOracle<>(this.target);
    }
    
}
