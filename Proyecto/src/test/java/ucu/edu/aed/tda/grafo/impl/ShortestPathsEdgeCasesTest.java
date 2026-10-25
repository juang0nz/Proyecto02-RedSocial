package ucu.edu.aed.tda.grafo.impl;

import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.tda.grafo.model.result.IDijkstraResult;
import ucu.edu.aed.tda.grafo.model.result.IFloydWarshallResult;

import java.util.List;

/** Casos complementarios de rutas minimas para grafos dirigidos. */
public class ShortestPathsEdgeCasesTest extends TestCase {

    private final DirectedGraphAlgorithms algorithms = new DirectedGraphAlgorithms();

    public void testGrafoVacioYUnSoloVertice() {
        DirectedGraph<String, WeightedEdge> empty = new DirectedGraph<>();
        IFloydWarshallResult<String> emptyResult = algorithms.floyd(empty);
        try {
            emptyResult.getCost("A", "A");
            fail("Un vertice inexistente debe rechazarse");
        } catch (IllegalArgumentException expected) {
            // Resultado vacio: no existen consultas de vertices validas.
        }

        DirectedGraph<String, WeightedEdge> single = new DirectedGraph<>();
        single.agregarVertice("A");
        IDijkstraResult<String> dijkstra = algorithms.dijkstra(single.construirComparable("A"), single);
        IFloydWarshallResult<String> floyd = algorithms.floyd(single);
        assertEquals(0.0, dijkstra.getCost("A"), 0.0);
        assertEquals(List.of("A"), dijkstra.getPath("A"));
        assertEquals(0.0, floyd.getCost("A", "A"), 0.0);
        assertEquals(List.of("A"), floyd.getPath("A", "A"));
    }

    public void testEmpatesYAristasDePesoCero() {
        DirectedGraph<String, WeightedEdge> graph = new DirectedGraph<>();
        graph.agregarVertices(List.of("A", "B", "C", "D"));
        graph.agregarArista("A", "B", new WeightedEdge(1));
        graph.agregarArista("A", "C", new WeightedEdge(1));
        graph.agregarArista("B", "D", new WeightedEdge(0));
        graph.agregarArista("C", "D", new WeightedEdge(0));

        IDijkstraResult<String> dijkstra = algorithms.dijkstra(graph.construirComparable("A"), graph);
        IFloydWarshallResult<String> floyd = algorithms.floyd(graph);
        assertEquals(1.0, dijkstra.getCost("D"), 0.0);
        assertEquals(1.0, floyd.getCost("A", "D"), 0.0);
        assertEquals("A", dijkstra.getPath("D").get(0));
        assertEquals("D", dijkstra.getPath("D").get(dijkstra.getPath("D").size() - 1));
        assertEquals(3, dijkstra.getPath("D").size());
        assertEquals("A", floyd.getPath("A", "D").get(0));
        assertEquals("D", floyd.getPath("A", "D").get(floyd.getPath("A", "D").size() - 1));
    }

    public void testSentidoDeAristasYDestinosInalcanzables() {
        DirectedGraph<String, WeightedEdge> graph = new DirectedGraph<>();
        graph.agregarVertices(List.of("A", "B", "C"));
        graph.agregarArista("A", "B", new WeightedEdge(2));

        IDijkstraResult<String> fromB = algorithms.dijkstra(graph.construirComparable("B"), graph);
        IFloydWarshallResult<String> floyd = algorithms.floyd(graph);
        assertEquals(Double.POSITIVE_INFINITY, fromB.getCost("A"), 0.0);
        assertEquals(List.of(), fromB.getPath("A"));
        assertEquals(Double.POSITIVE_INFINITY, floyd.getCost("B", "A"), 0.0);
        assertEquals(List.of(), floyd.getPath("B", "A"));
        assertFalse(floyd.connected("A", "C"));
        assertEquals(2.0, floyd.getCost("A", "B"), 0.0);
    }
}
