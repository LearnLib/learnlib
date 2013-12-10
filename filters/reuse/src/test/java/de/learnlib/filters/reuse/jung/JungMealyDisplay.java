package de.learnlib.filters.reuse.jung;

import java.awt.Color;
import java.awt.Paint;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.words.Alphabet;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.DAGLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout2;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout2;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.PluggableGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;

/**
 * TODO JavaDoc.
 * 
 * @author Oliver Bauer <oliver.bauer@tu-dortmund.de>
 *
 * @param <S>
 * @param <I>
 * @param <T>
 * @param <O>
 */
class JungMealyDisplay<S, I, T, O> {
	public static class Edge<I, O> {
		public final I i;
		public final O o;

		public Edge(I i, O o) {
			this.i = i;
			this.o = o;
		}
	}

	/**
	 * Wrapper around the state from the mealy automaton.
	 * The number is to display q${number}.
	 * @param <S>
	 */
	public static class Node<S> {
		public final S s;
		public final int number;

		public Node(S s, int number) {
			this.s = s;
			this.number = number;
		}
	}

	public static <S, I, T, O> void render(
			MealyMachine<S, I, T, O> mealyMachine, Alphabet<I> alphabet) {
		DirectedGraph<Node<S>, Edge<I, O>> graph = new DirectedSparseMultigraph<Node<S>, Edge<I, O>>();

		MutableMapping<S, Node<S>> mapping = mealyMachine
				.createStaticStateMapping();
		int count = 0;
		for (S state : mealyMachine.getStates()) {
			mapping.put(state, new Node<S>(state, count++));
		}

		// now construct the concrete graph
		for (S state : mealyMachine.getStates()) {
			Node<S> sourceNode = mapping.get(state);

			for (I input : alphabet) {
				O output = mealyMachine.getOutput(state, input);
				S successor = mealyMachine.getSuccessor(state, input);

				graph.addEdge(new Edge<>(input, output), sourceNode,
						mapping.get(successor));
			}
		}

		// TODO use enum?
		Layout<Node<S>, Edge<I, O>> layout = null;
//		layout = new BalloonLayout<>(graph); // needs "Forest<V,E>"
		layout = new CircleLayout<>(graph);
		layout = new DAGLayout<>(graph);
		layout = new FRLayout<>(graph);
		layout = new FRLayout2<>(graph);
		layout = new ISOMLayout<>(graph);
//		layout = new RadialTreeLayout<>(graph); // needs "Forest<V,E>"
		layout = new SpringLayout<>(graph);
		layout = new SpringLayout2<>(graph);
		layout = new KKLayout<>(graph);
//		layout = new SugiyamaLayout<>(graph);
		
		VisualizationViewer<Node<S>, Edge<I, O>> vv = new VisualizationViewer<Node<S>, Edge<I, O>>(
				layout);

		vv.getRenderContext().setVertexFillPaintTransformer(
				new Transformer<Node<S>, Paint>() {
					@Override
					public Paint transform(Node<S> node) {
						return Color.white;
					}
				});

		vv.getRenderContext().setVertexLabelTransformer(
				new Transformer<Node<S>, String>() {
					@Override
					public String transform(Node<S> arg0) {
						return "q" + arg0.number;
					}
				});

		vv.getRenderContext().setEdgeLabelTransformer(
				new Transformer<Edge<I, O>, String>() {
					@Override
					public String transform(Edge<I, O> arg0) {
						return arg0.i + "/" + arg0.o;
					}
				});

		PluggableGraphMouse pgm = new PluggableGraphMouse();
		pgm.add(new PickingGraphMousePlugin<>());
		pgm.add(new TranslatingGraphMousePlugin(MouseEvent.BUTTON3_MASK));
		pgm.add(new ScalingGraphMousePlugin(new CrossoverScalingControl(), 0,
				1 / 1.1f, 1.1f));
		vv.setGraphMouse(pgm);

		final JFrame frame = new JFrame();
		frame.setTitle("Resulting mealy machine");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(vv);
		frame.pack();
		frame.setVisible(true);
	}
}