package ucu.edu.aed.tda.grafo.impl;

import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.tda.grafo.model.result.Path;
import java.util.List;
import java.util.Set;

// Prueba la enumeración de caminos entre dos vértices solicitada en el Desafío 1.
public class DirectedAllPathsTest extends TestCase {
    private final DirectedGraphAlgorithms algorithms = new DirectedGraphAlgorithms();

    private DirectedGraph<String, WeightedEdge> graph(String... vertices) {
        DirectedGraph<String, WeightedEdge> graph = new DirectedGraph<>();
        graph.agregarVertices(List.of(vertices));
        return graph;
    }

    // El ciclo A -> B -> A no debe generar rutas infinitas ni repetir vértices en el camino.
    // Se comprueban tanto la secuencia como la suma de sus pesos.
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

    // Sin vértices no se puede formar un camino entre los extremos solicitados.
    public void testGrafoVacio() {
        DirectedGraph<String, WeightedEdge> graph = graph();
        assertTrue(algorithms.obtenerTodosLosCaminos(
                graph.construirComparable("A"), graph.construirComparable("B"), graph).isEmpty());
    }

    // La enumeración devuelve vacío si falta el origen, aunque el destino sí exista.
    public void testOrigenInexistente() {
        DirectedGraph<String, WeightedEdge> graph = graph("B");
        assertTrue(algorithms.obtenerTodosLosCaminos(
                graph.construirComparable("X"), graph.construirComparable("B"), graph).isEmpty());
    }

    // Tener un origen válido no alcanza si el destino no pertenece al grafo.
    public void testDestinoInexistente() {
        DirectedGraph<String, WeightedEdge> graph = graph("A");
        assertTrue(algorithms.obtenerTodosLosCaminos(
                graph.construirComparable("A"), graph.construirComparable("X"), graph).isEmpty());
    }

    // También se comprueba que ambos extremos falten en un grafo que no está vacío.
    public void testAmbosExtremosInexistentes() {
        DirectedGraph<String, WeightedEdge> graph = graph("A");
        assertTrue(algorithms.obtenerTodosLosCaminos(
                graph.construirComparable("X"), graph.construirComparable("Y"), graph).isEmpty());
    }

    // De un vértice a sí mismo existe el camino sin aristas, con costo cero.
    public void testGrafoUnitario() {
        DirectedGraph<String, WeightedEdge> graph = graph("A");
        List<Path<String>> paths = algorithms.obtenerTodosLosCaminos(
                graph.construirComparable("A"), graph.construirComparable("A"), graph);
        assertEquals(1, paths.size());
        assertEquals(List.of("A"), paths.get(0).getPath());
        assertEquals(0.0, paths.get(0).getCost(), 0.0);
    }

    // Ambos extremos existen, pero ni el aislado ni la dirección inversa ofrecen un camino.
    public void testAisladoYSentidoInverso() {
        DirectedGraph<String, WeightedEdge> graph = graph("A", "B", "SOLO");
        graph.agregarArista("A", "B", new WeightedEdge(1));
        assertTrue(algorithms.obtenerTodosLosCaminos(
                graph.construirComparable("A"), graph.construirComparable("SOLO"), graph).isEmpty());
        assertTrue(algorithms.obtenerTodosLosCaminos(
                graph.construirComparable("SOLO"), graph.construirComparable("A"), graph).isEmpty());
        assertTrue(algorithms.obtenerTodosLosCaminos(
                graph.construirComparable("B"), graph.construirComparable("A"), graph).isEmpty());
    }

    // A diferencia de los algoritmos que guardan un mínimo, acá deben aparecer las dos rutas empatadas.
    public void testCaminosAlternativosDelMismoCosto() {
        DirectedGraph<String, WeightedEdge> graph = graph("A", "B", "C", "D");
        graph.agregarArista("A", "B", new WeightedEdge(1));
        graph.agregarArista("A", "C", new WeightedEdge(1));
        graph.agregarArista("B", "D", new WeightedEdge(1));
        graph.agregarArista("C", "D", new WeightedEdge(1));
        List<Path<String>> paths = algorithms.obtenerTodosLosCaminos(
                graph.construirComparable("A"), graph.construirComparable("D"), graph);
        assertEquals(2, paths.size());
        assertEquals(Set.of(List.of("A", "B", "D"), List.of("A", "C", "D")),
                Set.of(paths.get(0).getPath(), paths.get(1).getPath()));
        for (Path<String> path : paths) {
            assertEquals(2.0, path.getCost(), 0.0);
        }
        assertEquals(4, graph.cantidadDeVertices());
        assertEquals(4, graph.cantidadDeAristas());
    }
}
