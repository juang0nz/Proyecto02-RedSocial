package ucu.edu.aed.tda.grafo.impl;

import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import java.util.List;

public class SocialGraphQueriesTest extends TestCase {
    private DirectedGraph<String, WeightedEdge> graph(String... users) {
        DirectedGraph<String, WeightedEdge> graph = new DirectedGraph<>();
        graph.agregarVertices(List.of(users));
        return graph;
    }

    public void testSeparacionDirigidaYAislados() {
        DirectedGraph<String, WeightedEdge> graph = graph("A", "B", "C", "D");
        graph.agregarArista("A", "B", new WeightedEdge(1));
        graph.agregarArista("B", "C", new WeightedEdge(1));
        assertEquals(2, SocialGraphQueries.gradosDeSeparacion(graph,
                graph.construirComparable("A"), graph.construirComparable("C")));
        assertEquals(-1, SocialGraphQueries.gradosDeSeparacion(graph,
                graph.construirComparable("C"), graph.construirComparable("A")));
        assertEquals(0, SocialGraphQueries.gradosDeSeparacion(graph,
                graph.construirComparable("D"), graph.construirComparable("D")));
        assertEquals(-1, SocialGraphQueries.gradosDeSeparacion(graph,
                graph.construirComparable("A"), graph.construirComparable("D")));
    }

    public void testSugerenciasExcluyenPropioYSeguidosYOrdenanPorComunes() {
        DirectedGraph<String, WeightedEdge> graph = graph("A", "B", "C", "D", "E", "F");
        graph.agregarArista("A", "B", new WeightedEdge(1));
        graph.agregarArista("A", "C", new WeightedEdge(1));
        graph.agregarArista("B", "D", new WeightedEdge(1));
        graph.agregarArista("C", "D", new WeightedEdge(1));
        graph.agregarArista("C", "E", new WeightedEdge(1));
        graph.agregarArista("B", "A", new WeightedEdge(1));
        graph.agregarArista("B", "C", new WeightedEdge(1));
        assertEquals(List.of("D", "E"), SocialGraphQueries.sugerencias(
                graph, graph.construirComparable("A")));
        assertEquals(List.of(), SocialGraphQueries.sugerencias(
                graph, graph.construirComparable("F")));
    }

    public void testUsuariosInexistentes() {
        DirectedGraph<String, WeightedEdge> graph = graph("A");
        try {
            SocialGraphQueries.gradosDeSeparacion(graph,
                    graph.construirComparable("A"), graph.construirComparable("X"));
            fail("Usuario inexistente");
        } catch (IllegalArgumentException expected) {
            // Se valida la existencia.
        }
        try {
            SocialGraphQueries.sugerencias(graph, graph.construirComparable("X"));
            fail("Usuario inexistente");
        } catch (IllegalArgumentException expected) {
            // Se valida la existencia.
        }
    }
}
