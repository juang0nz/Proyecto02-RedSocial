package ucu.edu.aed.tda.grafo.impl;

import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.tda.grafo.model.result.IDijkstraResult;
import ucu.edu.aed.tda.grafo.model.result.IFloydWarshallResult;

import java.util.List;
import java.util.Map;

public class DijkstraTest extends TestCase {

    private final DirectedGraphAlgorithms algoritmos = new DirectedGraphAlgorithms();

    private DirectedGraph<String, WeightedEdge> crearGrafo() {
        DirectedGraph<String, WeightedEdge> grafo = new DirectedGraph<>();
        List<String> vertices = List.of("A", "B", "C", "D", "E");
        grafo.agregarVertices(vertices);
        int[][] matriz = {
                {-1, 1, 2, 7, -1},
                {7, -1, 1, 2, -1},
                {-1, -1, -1, 3, -1},
                {6, -1, 4, -1, 4},
                {-1, 2, -1, 8, -1}
        };
        for (int i = 0; i < vertices.size(); i++) {
            for (int j = 0; j < vertices.size(); j++) {
                if (matriz[i][j] >= 0) {
                    grafo.agregarArista(vertices.get(i), vertices.get(j), new WeightedEdge(matriz[i][j]));
                }
            }
        }
        return grafo;
    }

    public void testEjercicio7DesdeB() {
        DirectedGraph<String, WeightedEdge> grafo = crearGrafo();
        assertEquals(5, grafo.cantidadDeVertices());
        assertEquals(12, grafo.cantidadDeAristas());
        IDijkstraResult<String> resultado = algoritmos.dijkstra(grafo.construirComparable("B"), grafo);
        List<String> vertices = List.of("A", "B", "C", "D", "E");
        double[] costos = {7, 0, 1, 2, 6};
        for (int i = 0; i < vertices.size(); i++) {
            assertEquals(costos[i], resultado.getCost(vertices.get(i)), 0);
        }
        assertEquals(Map.of("A", "B", "C", "B", "D", "B", "E", "D"), resultado.getPredecessors());
        assertEquals(List.of("B", "A"), resultado.getPath("A"));
        assertEquals(List.of("B"), resultado.getPath("B"));
        assertEquals(List.of("B", "C"), resultado.getPath("C"));
        assertEquals(List.of("B", "D"), resultado.getPath("D"));
        assertEquals(List.of("B", "D", "E"), resultado.getPath("E"));
        assertEquals(List.of("B", "D", "E"),
                DijkstraResult.reconstruirCamino("B", "E", resultado.getPredecessors()));
    }

    public void testTodosLosOrigenesCoincidenConFloyd() {
        DirectedGraph<String, WeightedEdge> grafo = crearGrafo();
        IFloydWarshallResult<String> floyd = algoritmos.floyd(grafo);
        for (String origen : grafo.vertices()) {
            IDijkstraResult<String> resultado = algoritmos.dijkstra(grafo.construirComparable(origen), grafo);
            for (String destino : grafo.vertices()) {
                assertEquals(floyd.getCost(origen, destino), resultado.getCost(destino), 0);
                List<String> camino = resultado.getPath(destino);
                double costo = 0;
                for (int i = 1; i < camino.size(); i++) {
                    costo += grafo.obtenerArista(camino.get(i - 1), camino.get(i)).dato().getWeight();
                }
                assertEquals(resultado.getCost(destino), costo, 0);
            }
        }
    }

    public void testPesosCeroDesconexionYEjemploInmutable() {
        DirectedGraph<String, WeightedEdge> grafo = new DirectedGraph<>();
        grafo.agregarVertices(List.of("A", "B", "C", "D"));
        grafo.agregarArista("A", "B", new WeightedEdge(0));
        grafo.agregarArista("B", "A", new WeightedEdge(0));
        grafo.agregarArista("B", "C", new WeightedEdge(2));
        IDijkstraResult<String> resultado = algoritmos.dijkstra(grafo.construirComparable("A"), grafo);
        grafo.vaciar();
        assertEquals(List.of("A", "B", "C"), resultado.getPath("C"));
        assertEquals(2.0, resultado.getCost("C"), 0);
        assertEquals(Double.POSITIVE_INFINITY, resultado.getCost("D"), 0);
        assertEquals(List.of(), resultado.getPath("D"));
        try {
            resultado.getPredecessors().clear();
            fail("El vector debe ser inmodificable");
        } catch (UnsupportedOperationException expected) {
            assertEquals(2, resultado.getPredecessors().size());
        }
        try {
            resultado.getPath("X");
            fail("Debe rechazar vertices desconocidos");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("X"));
        }
    }

    public void testRechazaPesosInvalidosInclusoEnComponenteInaccesible() {
        for (double peso : new double[]{-1, Double.NaN, Double.POSITIVE_INFINITY}) {
            DirectedGraph<String, WeightedEdge> grafo = new DirectedGraph<>();
            grafo.agregarVertices(List.of("A", "B", "C"));
            grafo.agregarArista("B", "C", new WeightedEdge(peso));
            try {
                algoritmos.dijkstra(grafo.construirComparable("A"), grafo);
                fail("Debe validar todos los pesos");
            } catch (IllegalArgumentException expected) {
                assertTrue(expected.getMessage().contains("no negativos"));
            }
        }
    }

    public void testOrigenInexistenteYVectorCiclico() {
        DirectedGraph<String, WeightedEdge> grafo = crearGrafo();
        try {
            algoritmos.dijkstra(grafo.construirComparable("X"), grafo);
            fail("Debe rechazar origen inexistente");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("origen"));
        }
        try {
            DijkstraResult.reconstruirCamino("A", "C", Map.of("C", "B", "B", "C"));
            fail("Debe rechazar un vector ciclico");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("ciclo"));
        }
        assertEquals(List.of(), DijkstraResult.reconstruirCamino("A", "C", Map.of()));
        assertEquals(List.of("A"), DijkstraResult.reconstruirCamino("A", "A", Map.of()));
    }
}
