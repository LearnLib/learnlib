/* Copyright (C) 2013-2018 TU Dortmund
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
package de.learnlib.filter.reuse.tree;

import java.util.Map;

import net.automatalib.visualization.DefaultVisualizationHelper;
import net.automatalib.visualization.VisualizationHelper;

/**
 * {@link VisualizationHelper} implementation for the {@link ReuseTree} that renders nodes as white circles (if no
 * system state is available on the node) or black diamonds (otherwise). Edges are labeled with input / output
 * behavior.
 *
 * @param <S>
 *         system state class
 * @param <I>
 *         input symbol class
 * @param <O>
 *         output symbol class
 *
 * @author Oliver Bauer
 */
class ReuseTreeDotHelper<S, I, O> extends DefaultVisualizationHelper<ReuseNode<S, I, O>, ReuseEdge<S, I, O>> {

    @Override
    public boolean getNodeProperties(ReuseNode<S, I, O> node, Map<String, String> properties) {
        super.getNodeProperties(node, properties);
        if (node.hasSystemStates()) {
            properties.put(NodeAttrs.COLOR, "black");
            properties.put(NodeAttrs.STYLE, "filled");
            properties.put(NodeAttrs.SHAPE, "diamond");
            properties.put("fontcolor", "white");
        }
        // properties.put(NodeAttrs.LABEL, String.valueOf(node.getId()));
        properties.put(NodeAttrs.LABEL, "");
        return true;
    }

    @Override
    public boolean getEdgeProperties(ReuseNode<S, I, O> src,
                                     ReuseEdge<S, I, O> edge,
                                     ReuseNode<S, I, O> tgt,
                                     Map<String, String> properties) {
        super.getEdgeProperties(src, edge, tgt, properties);

        final StringBuilder labelBuilder = new StringBuilder();
        labelBuilder.append(String.valueOf(edge.getInput())).append(" / ");
        O output = edge.getOutput();
        if (output != null) {
            labelBuilder.append(String.valueOf(output));
        }
        properties.put(EdgeAttrs.LABEL, labelBuilder.toString());
        return true;
    }
}
