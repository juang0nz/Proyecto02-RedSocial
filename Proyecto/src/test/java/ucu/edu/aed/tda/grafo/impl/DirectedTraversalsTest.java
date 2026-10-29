package ucu.edu.aed.tda.grafo.impl;

import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

// Cubre DFS, BFS y clasificación topológica pedidos entre los algoritmos dirigidos.
public class DirectedTraversalsTest extends TestCase {
    private final DirectedGraphAlgorithms algorithms = new DirectedGraphAlgorithms();

    private DirectedGraph<String, WeightedEdge> graph(String... vertices) {
        DirectedGraph<String, WeightedEdge> graph = new DirectedGraph<>();
        graph.agregarVertices(List.of(vertices));
        return graph;
    }

    // Compara el orden de pila y cola, comprueba que el ciclo no repita visitas
    // y deja un aislado fuera de la parte alcanzable desde A.
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

    // Tanto DFS como BFS deben rechazar el origen inexistente, no fingir un recorrido vacío.
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

    // Sin origen y sin vértices, no hay nada para pasar al consumer.
    public void testRecorridosSinOrigenEnGrafoVacio() {
        DirectedGraph<String, WeightedEdge> graph = graph();
        List<String> dfs = new ArrayList<>();
        List<String> bfs = new ArrayList<>();
        algorithms.recorridoEnProfundidad(graph, dfs::add);
        algorithms.recorridoEnAmplitud(graph, bfs::add);
        assertTrue(dfs.isEmpty());
        assertTrue(bfs.isEmpty());
    }

    // Si se pide un origen en el grafo vacío, ambos recorridos deben rechazarlo.
    public void testRecorridosConOrigenEnGrafoVacio() {
        DirectedGraph<String, WeightedEdge> graph = graph();
        List<String> visitados = new ArrayList<>();
        try {
            algorithms.recorridoEnProfundidad(graph, graph.construirComparable("A"), visitados::add);
            fail("DFS debe rechazar un origen en el grafo vacio");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("origen"));
        }
        try {
            algorithms.recorridoEnAmplitud(graph, graph.construirComparable("A"), visitados::add);
            fail("BFS debe rechazar un origen en el grafo vacio");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("origen"));
        }
        assertTrue(visitados.isEmpty());
    }

    // Las dos versiones de cada recorrido deben visitar una sola vez al único vértice.
    public void testRecorridosEnGrafoUnitario() {
        DirectedGraph<String, WeightedEdge> graph = graph("A");
        List<String> dfs = new ArrayList<>();
        List<String> bfs = new ArrayList<>();
        algorithms.recorridoEnProfundidad(graph, graph.construirComparable("A"), dfs::add);
        algorithms.recorridoEnAmplitud(graph, graph.construirComparable("A"), bfs::add);
        assertEquals(List.of("A"), dfs);
        assertEquals(List.of("A"), bfs);

        dfs.clear();
        bfs.clear();
        algorithms.recorridoEnProfundidad(graph, dfs::add);
        algorithms.recorridoEnAmplitud(graph, bfs::add);
        assertEquals(List.of("A"), dfs);
        assertEquals(List.of("A"), bfs);
    }

    // Un origen aislado se visita, pero no permite entrar en la otra componente.
    public void testRecorridosDesdeVerticeAislado() {
        DirectedGraph<String, WeightedEdge> graph = graph("A", "B", "SOLO");
        graph.agregarArista("A", "B", new WeightedEdge(1));
        List<String> dfs = new ArrayList<>();
        List<String> bfs = new ArrayList<>();
        algorithms.recorridoEnProfundidad(graph, graph.construirComparable("SOLO"), dfs::add);
        algorithms.recorridoEnAmplitud(graph, graph.construirComparable("SOLO"), bfs::add);
        assertEquals(List.of("SOLO"), dfs);
        assertEquals(List.of("SOLO"), bfs);
    }

    // No exige un orden único: comprueba que cada origen aparezca antes de su destino.
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

    // El ciclo impide ordenar todas las dependencias, así que se espera una excepción.
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

    // El caso vacío solicitado en la consigna debe producir un orden vacío.
    public void testOrdenTopologicoVacio() {
        assertEquals(List.of(), algorithms.calcularClasificacionTopologica(graph()));
    }

    // Sin dependencias, el único vértice ya forma un orden válido.
    public void testOrdenTopologicoUnitario() {
        assertEquals(List.of("A"), algorithms.calcularClasificacionTopologica(graph("A")));
    }

    // Incluye las dos componentes y el aislado, sin exigir un orden único entre ellas.
    public void testOrdenTopologicoDesconectadoConAislado() {
        DirectedGraph<String, WeightedEdge> graph = graph("A", "B", "X", "Y", "SOLO");
        graph.agregarArista("A", "B", new WeightedEdge(1));
        graph.agregarArista("X", "Y", new WeightedEdge(1));
        List<String> order = algorithms.calcularClasificacionTopologica(graph);
        assertEquals(5, order.size());
        assertEquals(graph.vertices(), new HashSet<>(order));
        assertTrue(order.indexOf("A") < order.indexOf("B"));
        assertTrue(order.indexOf("X") < order.indexOf("Y"));
    }
}
