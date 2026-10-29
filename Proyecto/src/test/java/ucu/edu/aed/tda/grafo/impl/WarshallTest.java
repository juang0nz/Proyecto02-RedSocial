package ucu.edu.aed.tda.grafo.impl;

import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.tda.grafo.model.result.IFloydWarshallResult;
import java.util.List;

// Verifica la accesibilidad pedida para Warshall, junto con los saltos y rutas que devuelve esta versión.
public class WarshallTest extends TestCase {
    private final DirectedGraphAlgorithms algorithms = new DirectedGraphAlgorithms();

    private DirectedGraph<String, WeightedEdge> graph(String... vertices) {
        DirectedGraph<String, WeightedEdge> graph = new DirectedGraph<>();
        graph.agregarVertices(List.of(vertices));
        return graph;
    }

    // El vacío no admite consultas de vértices; un vértice solo sí se alcanza a sí mismo.
    public void testVacioYUnitario() {
        IFloydWarshallResult<String> empty = algorithms.warshall(graph());
        try {
            empty.connected("A", "A");
            fail("Vertice inexistente");
        } catch (IllegalArgumentException expected) {
            // No existen vertices validos.
        }
        IFloydWarshallResult<String> one = algorithms.warshall(graph("A"));
        assertTrue(one.connected("A", "A"));
        assertEquals(0.0, one.getCost("A", "A"), 0.0);
        assertEquals(List.of("A"), one.getPath("A", "A"));
    }

    // Usa pesos distintos, incluso negativo, para comprobar que Warshall cuenta saltos y no pesos.
    // También verifica el sentido de las aristas y un destino aislado.
    public void testCadenaYAristasDirigidas() {
        DirectedGraph<String, WeightedEdge> graph = graph("A", "B", "C", "D");
        graph.agregarArista("A", "B", new WeightedEdge(100));
        graph.agregarArista("B", "C", new WeightedEdge(-5));
        IFloydWarshallResult<String> result = algorithms.warshall(graph);
        assertTrue(result.connected("A", "C"));
        assertEquals(2.0, result.getCost("A", "C"), 0.0);
        assertEquals(List.of("A", "B", "C"), result.getPath("A", "C"));
        assertFalse(result.connected("C", "A"));
        assertFalse(result.connected("A", "D"));
        assertEquals(Double.POSITIVE_INFINITY, result.getCost("A", "D"), 0.0);
        assertEquals(List.of(), result.getPath("A", "D"));
    }

    // Un ciclo no debe alterar el costo cero hacia uno mismo ni conectar al vértice aislado.
    public void testCiclosYComponentes() {
        DirectedGraph<String, WeightedEdge> graph = graph("A", "B", "C", "D");
        graph.agregarArista("A", "B", new WeightedEdge(1));
        graph.agregarArista("B", "A", new WeightedEdge(1));
        graph.agregarArista("B", "C", new WeightedEdge(1));
        IFloydWarshallResult<String> result = algorithms.warshall(graph);
        assertTrue(result.connected("A", "C"));
        assertTrue(result.connected("B", "A"));
        assertFalse(result.connected("C", "B"));
        assertFalse(result.connected("D", "A"));
        assertEquals(0.0, result.getCost("A", "A"), 0.0);
    }

    // Aunque esté en esta clase, esta prueba llama a centro y excentricidad, que usan Floyd.
    public void testCentroYExcentricidadDeEntrada() {
        DirectedGraph<String, WeightedEdge> graph = graph("A", "B", "C");
        graph.agregarArista("A", "B", new WeightedEdge(2));
        graph.agregarArista("B", "C", new WeightedEdge(3));
        assertEquals("C", algorithms.obtenerCentroGrafo(graph));
        assertEquals(5.0, algorithms.obtenerExcentricidad(graph, graph.construirComparable("C")), 0.0);
        assertEquals(Double.POSITIVE_INFINITY,
                algorithms.obtenerExcentricidad(graph, graph.construirComparable("A")), 0.0);
    }
}
