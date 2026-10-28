package ucu.edu.aed.tda.grafo.impl;

import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.tda.grafo.model.result.Path;
import java.util.List;

public class DirectedAllPathsTest extends TestCase {
    public void testCaminosSimplesConCiclo() {
        DirectedGraph<String, WeightedEdge> graph = new DirectedGraph<>();
        graph.agregarVertices(List.of("A", "B", "C"));
        graph.agregarArista("A", "B", new WeightedEdge(2));
        graph.agregarArista("B", "A", new WeightedEdge(1));
        graph.agregarArista("B", "C", new WeightedEdge(3));
        List<Path<String>> paths = new DirectedGraphAlgorithms().obtenerTodosLosCaminos(
                graph.construirComparable("A"), graph.construirComparable("C"), graph);
        assertEquals(1, paths.size());
        assertEquals(List.of("A", "B", "C"), paths.get(0).getPath());
        assertEquals(5.0, paths.get(0).getCost(), 0.0);
    }
}
