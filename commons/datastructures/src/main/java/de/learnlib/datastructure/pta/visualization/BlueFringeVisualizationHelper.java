/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of LearnLib <https://learnlib.de>.
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
package de.learnlib.datastructure.pta.visualization;

import java.util.Map;

import de.learnlib.datastructure.pta.AbstractBlueFringePTA;
import de.learnlib.datastructure.pta.AbstractBlueFringePTAState;
import de.learnlib.datastructure.pta.PTATransition;

public class BlueFringeVisualizationHelper<S extends AbstractBlueFringePTAState<S, SP, TP>, SP, TP>
        extends PTAVisualizationHelper<S, Integer, PTATransition<S>, SP, TP, AbstractBlueFringePTA<S, SP, TP>> {

    public BlueFringeVisualizationHelper(AbstractBlueFringePTA<S, SP, TP> automaton) {
        super(automaton);
    }

    @Override
    public boolean getNodeProperties(S node, Map<String, String> properties) {

        // don't put the color directly, because WHITE should render as black
        switch (node.getColor()) {
            case RED:
            case BLUE:
                properties.put(NodeAttrs.COLOR, node.getColor().toString());
                break;
            default:
                // to nothing
        }

        return super.getNodeProperties(node, properties);
    }
}
