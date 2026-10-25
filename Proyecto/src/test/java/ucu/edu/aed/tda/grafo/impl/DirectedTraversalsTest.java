package ucu.edu.aed.tda.grafo.impl;

import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import java.util.ArrayList;
import java.util.List;

public class DirectedTraversalsTest extends TestCase {
    private final DirectedGraphAlgorithms algorithms = new DirectedGraphAlgorithms();

    private DirectedGraph<String, WeightedEdge> graph(String... vertices) {
        DirectedGraph<String, WeightedEdge> graph = new DirectedGraph<>();
        graph.agregarVertices(List.of(vertices));
        return graph;
    }

    public void testRecorridosConBifurcacionesYCiclo() {
        DirectedGraph<String, WeightedEdge> graph = graph("A", "B", "C", "D", "E");
        graph.agregarArista("A", "B", new WeightedEdge(1));
        graph.agregarArista("A", "C", new WeightedEdge(1));
        graph.agregarArista("B", "D", new WeightedEdge(1));
        graph.agregarArista("C", "D", new WeightedEdge(1));
        graph.agregarArista("D", "A", new WeightedEdge(1));
        List<String> dfs = new ArrayList<>();
        List<String> bfs = new ArrayList<>();
        algorithms.recorridoEnProfundidad(graph, graph.construirComparable("A"), dfs::add);
        algorithms.recorridoEnAmplitud(graph, graph.construirComparable("A"), bfs::add);
        assertEquals(List.of("A", "B", "D", "C"), dfs);
        assertEquals(List.of("A", "B", "C", "D"), bfs);
        assertFalse(dfs.contains("E"));
    }

    public void testOrigenDesconocido() {
        DirectedGraph<String, WeightedEdge> graph = graph("A");
        try {
            algorithms.recorridoEnAmplitud(graph, graph.construirComparable("X"), v -> {});
            fail("Origen desconocido");
        } catch (IllegalArgumentException expected) {
            // Contrato explicito.
        }
        try {
            algorithms.recorridoEnProfundidad(graph, graph.construirComparable("X"), v -> {});
            fail("Origen desconocido");
        } catch (IllegalArgumentException expected) {
            // Contrato explicito.
        }
    }

    public void testOrdenTopologicoRespetaDependencias() {
        DirectedGraph<String, WeightedEdge> graph = graph("A", "B", "C", "D");
        graph.agregarArista("A", "C", new WeightedEdge(1));
        graph.agregarArista("B", "C", new WeightedEdge(1));
        graph.agregarArista("C", "D", new WeightedEdge(1));
        List<String> order = algorithms.calcularClasificacionTopologica(graph);
        assertEquals(4, order.size());
        assertTrue(order.indexOf("A") < order.indexOf("C"));
        assertTrue(order.indexOf("B") < order.indexOf("C"));
        assertTrue(order.indexOf("C") < order.indexOf("D"));
    }

    public void testOrdenTopologicoRechazaCiclos() {
        DirectedGraph<String, WeightedEdge> graph = graph("A", "B");
        graph.agregarArista("A", "B", new WeightedEdge(1));
        graph.agregarArista("B", "A", new WeightedEdge(1));
        try {
            algorithms.calcularClasificacionTopologica(graph);
            fail("Los ciclos impiden el orden topologico");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("ciclos"));
        }
    }

    public void testOrdenTopologicoVacio() {
        assertEquals(List.of(), algorithms.calcularClasificacionTopologica(graph()));
    }
}
