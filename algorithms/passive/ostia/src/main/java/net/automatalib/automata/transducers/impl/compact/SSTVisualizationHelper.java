package net.automatalib.automata.transducers.impl.compact;

import java.util.Map;

import net.automatalib.automata.graphs.TransitionEdge;
import net.automatalib.automata.visualization.AutomatonVisualizationHelper;
import net.automatalib.words.Word;

public class SSTVisualizationHelper<S, I, T, O>
        extends AutomatonVisualizationHelper<S, I, T, SubsequentialTransducer<S, I, T, O>> {

    public SSTVisualizationHelper(SubsequentialTransducer<S, I, T, O> automaton) {
        super(automaton);
    }

    @Override
    public boolean getEdgeProperties(S src, TransitionEdge<I, T> edge, S tgt, Map<String, String> properties) {
        if (!super.getEdgeProperties(src, edge, tgt, properties)) {
            return false;
        }

        final StringBuilder labelBuilder = new StringBuilder();
        labelBuilder.append(edge.getInput()).append(" / ");
        Word<O> output = automaton.getTransitionProperty(edge.getTransition());
        if (output != null) {
            labelBuilder.append(output);
        }
        properties.put(EdgeAttrs.LABEL, labelBuilder.toString());
        return true;
    }

    @Override
    public boolean getNodeProperties(S node, Map<String, String> properties) {
        if (!super.getNodeProperties(node, properties)) {
            return false;
        }

        properties.put(NodeAttrs.LABEL, automaton.getStateProperty(node).toString());
        return true;
    }
}
