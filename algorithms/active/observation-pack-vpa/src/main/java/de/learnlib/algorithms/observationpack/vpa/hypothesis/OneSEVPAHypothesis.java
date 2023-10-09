/* Copyright (C) 2013-2023 TU Dortmund
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
package de.learnlib.algorithms.observationpack.vpa.hypothesis;

import java.util.ArrayList;
import java.util.List;

import net.automatalib.automata.vpa.AbstractSEVPA;
import net.automatalib.automata.vpa.OneSEVPA;
import net.automatalib.automata.vpa.StackContents;
import net.automatalib.automata.vpa.State;
import net.automatalib.words.VPAlphabet;

/**
 * @param <I>
 *         input symbol type
 */
public class OneSEVPAHypothesis<I> extends AbstractSEVPA<HypLoc<I>, I> implements OneSEVPA<HypLoc<I>, I> {

    private final List<HypLoc<I>> locations = new ArrayList<>();

    private HypLoc<I> initLoc;

    public OneSEVPAHypothesis(VPAlphabet<I> alphabet) {
        super(alphabet);
    }

    public AbstractHypTrans<I> getInternalTransition(State<HypLoc<I>> state, I sym) {
        switch (alphabet.getSymbolType(sym)) {
            case INTERNAL:
                return state.getLocation().getInternalTransition(alphabet.getInternalSymbolIndex(sym));
            case RETURN:
                StackContents stackContents = state.getStackContents();
                assert stackContents != null;
                return state.getLocation()
                            .getReturnTransition(alphabet.getReturnSymbolIndex(sym), stackContents.peek());
            default:
                return null;
        }
    }

    public AbstractHypTrans<I> getInternalTransition(HypLoc<I> loc, I intSym) {
        return loc.getInternalTransition(alphabet.getInternalSymbolIndex(intSym));
    }

    public AbstractHypTrans<I> getReturnTransition(HypLoc<I> loc, I retSym, int stackSym) {
        return loc.getReturnTransition(alphabet.getReturnSymbolIndex(retSym), stackSym);
    }

    public AbstractHypTrans<I> getReturnTransition(HypLoc<I> loc, I retSym, HypLoc<I> stackLoc, I callSym) {
        int stackSym = encodeStackSym(stackLoc, callSym);
        return loc.getReturnTransition(alphabet.getReturnSymbolIndex(retSym), stackSym);
    }

    public HypLoc<I> createLocation(boolean accepting, AbstractHypTrans<I> treeIncoming) {
        HypLoc<I> loc = new HypLoc<>(alphabet, locations.size(), accepting, treeIncoming);
        locations.add(loc);
        return loc;
    }

    public HypLoc<I> initialize() {
        HypLoc<I> loc = createLocation(false, null);
        this.initLoc = loc;

        return loc;
    }

    @Override
    public HypLoc<I> getInternalSuccessor(HypLoc<I> loc, I intSym) {
        return loc.getInternalTransition(alphabet.getInternalSymbolIndex(intSym)).getTargetLocation();
    }

    @Override
    public HypLoc<I> getLocation(int id) {
        return locations.get(id);
    }

    @Override
    public int getLocationId(HypLoc<I> loc) {
        return loc.getIndex();
    }

    @Override
    public List<HypLoc<I>> getLocations() {
        return locations;
    }

    @Override
    public HypLoc<I> getReturnSuccessor(HypLoc<I> loc, I retSym, int stackSym) {
        return loc.getReturnTransition(alphabet.getReturnSymbolIndex(retSym), stackSym).getTargetLocation();
    }

    @Override
    public boolean isAcceptingLocation(HypLoc<I> loc) {
        return loc.isAccepting();
    }

    @Override
    public HypLoc<I> getInitialLocation() {
        return initLoc;
    }

    @Override
    public int size() {
        return locations.size();
    }

}
