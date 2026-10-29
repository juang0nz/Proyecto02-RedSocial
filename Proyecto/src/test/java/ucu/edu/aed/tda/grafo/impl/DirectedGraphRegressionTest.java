package ucu.edu.aed.tda.grafo.impl;

import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.model.edge.Edge;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.tda.grafo.model.result.IFloydWarshallResult;
import ucu.edu.aed.tda.grafo.model.result.Path;

import java.util.HashSet;
import java.util.List;

// Comprueba los requisitos de Floyd, todos los caminos, centro y excentricidad con un grafo de ciudades.
// Los resultados esperados ayudan a detectar cambios que alteren el funcionamiento de la biblioteca.
public class DirectedGraphRegressionTest extends TestCase {

    private static final List<String> VERTICES = List.of("Asuncion", "Buenos_Aires", "Curitiba", "Montevideo",
            "Porto_Alegre", "Rio_de_Janeiro", "San_Pablo", "Santos");

    private final DirectedGraphAlgorithms algoritmos = new DirectedGraphAlgorithms();

    private DirectedGraph<String, WeightedEdge> crearGrafo() {
        DirectedGraph<String, WeightedEdge> grafo = new DirectedGraph<>();
        grafo.agregarVertices(VERTICES);
        int[][] tabla = {
                {0, 1600, 800, 0, 700, 0, 0, 0},
                {1600, 0, 0, 200, 1250, 0, 0, 0},
                {0, 0, 0, 0, 0, 1500, 0, 0},
                {0, 200, 0, 0, 1000, 0, 980, 1200},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 1500, 0, 0, 0, 1800, 1900},
                {1200, 0, 600, 2000, 980, 1800, 0, 220},
                {0, 0, 0, 0, 1200, 0, 220, 0}
        };
        for (int i = 0; i < VERTICES.size(); i++) {
            for (int j = 0; j < VERTICES.size(); j++) {
                if (tabla[i][j] != 0) {
                    assertTrue(grafo.agregarArista(VERTICES.get(i), VERTICES.get(j), new WeightedEdge(tabla[i][j])));
                }
            }
        }
        assertEquals(VERTICES, List.copyOf(grafo.vertices()));
        assertEquals(8, grafo.cantidadDeVertices());
        assertEquals(22, grafo.cantidadDeAristas());
        return grafo;
    }

    // Compara cada costo con una matriz esperada, no solamente con otro algoritmo.
    public void testMatrizFloydParaLos64Pares() {
        IFloydWarshallResult<String> resultado = algoritmos.floyd(crearGrafo());
        double inf = Double.POSITIVE_INFINITY;
        double[][] esperados = {
                {0, 1600, 800, 1800, 700, 2300, 2780, 3000},
                {1600, 0, 1780, 200, 1200, 2980, 1180, 1400},
                {4500, 5500, 0, 5300, 4280, 1500, 3300, 3400},
                {1800, 200, 1580, 0, 1000, 2780, 980, 1200},
                {inf, inf, inf, inf, 0, inf, inf, inf},
                {3000, 4000, 1500, 3800, 2780, 0, 1800, 1900},
                {1200, 2200, 600, 2000, 980, 1800, 0, 220},
                {1420, 2420, 820, 2220, 1200, 2020, 220, 0}
        };
        for (int i = 0; i < VERTICES.size(); i++) {
            for (int j = 0; j < VERTICES.size(); j++) {
                assertEquals(VERTICES.get(i) + " -> " + VERTICES.get(j),
                        esperados[i][j], resultado.getCost(VERTICES.get(i), VERTICES.get(j)), 0);
            }
        }
    }

    // Contrasta Floyd con el mínimo de todos los caminos simples para cada par.
    // Además verifica que cada ruta use aristas reales y no repita vértices.
    public void testFloydCoincideConEnumeracionDeCaminosParaLos64Pares() {
        DirectedGraph<String, WeightedEdge> grafo = crearGrafo();
        IFloydWarshallResult<String> resultado = algoritmos.floyd(grafo);
        for (String origen : VERTICES) {
            for (String destino : VERTICES) {
                List<Path<String>> caminos = algoritmos.obtenerTodosLosCaminos(
                        grafo.construirComparable(origen), grafo.construirComparable(destino), grafo);
                double minimo = caminos.stream().mapToDouble(Path::getCost).min().orElse(Double.POSITIVE_INFINITY);
                assertEquals(origen + " -> " + destino, minimo, resultado.getCost(origen, destino), 0);
                assertEquals(!caminos.isEmpty(), resultado.connected(origen, destino));
                for (Path<String> camino : caminos) {
                    assertEquals(camino.getPath().size(), new HashSet<>(camino.getPath()).size());
                    verificarCamino(grafo, origen, destino, camino.getPath(), camino.getCost());
                }
                List<String> itinerario = resultado.getPath(origen, destino);
                if (caminos.isEmpty()) {
                    assertTrue(itinerario.isEmpty());
                } else {
                    verificarCamino(grafo, origen, destino, itinerario, minimo);
                }
            }
        }
    }

    // Recorre la ruta devuelta y suma sus pesos para comprobar el costo informado.
    private void verificarCamino(DirectedGraph<String, WeightedEdge> grafo, String origen, String destino,
                                 List<String> itinerario, double costoEsperado) {
        assertFalse(itinerario.isEmpty());
        assertEquals(origen, itinerario.get(0));
        assertEquals(destino, itinerario.get(itinerario.size() - 1));
        double costo = 0;
        for (int i = 1; i < itinerario.size(); i++) {
            Edge<String, WeightedEdge> tramo = grafo.obtenerArista(itinerario.get(i - 1), itinerario.get(i));
            assertNotNull(tramo);
            costo += tramo.dato().getWeight();
        }
        assertEquals(costoEsperado, costo, 0);
    }

    // Conserva dos rutas concretas con sus costos y vértices intermedios esperados.
    public void testRutasMinimasHistoricas() {
        IFloydWarshallResult<String> resultado = algoritmos.floyd(crearGrafo());
        assertEquals(1580.0, resultado.getCost("Montevideo", "Curitiba"), 0);
        assertEquals(List.of("Montevideo", "San_Pablo", "Curitiba"),
                resultado.getPath("Montevideo", "Curitiba"));
        assertEquals(2780.0, resultado.getCost("Montevideo", "Rio_de_Janeiro"), 0);
        assertEquals(List.of("Montevideo", "San_Pablo", "Rio_de_Janeiro"),
                resultado.getPath("Montevideo", "Rio_de_Janeiro"));
    }

    // Porto Alegre recibe caminos desde todos, aunque no tenga salidas.
    // Por eso puede ser centro cuando se usa excentricidad de entrada.
    public void testCentroYExcentricidadDeEntrada() {
        DirectedGraph<String, WeightedEdge> grafo = crearGrafo();
        assertEquals("Porto_Alegre", algoritmos.obtenerCentroGrafo(grafo));
        assertEquals(4280.0, algoritmos.obtenerExcentricidad(grafo,
                grafo.construirComparable("Porto_Alegre")), 0);
        for (String vertice : VERTICES) {
            if (!vertice.equals("Porto_Alegre")) {
                assertEquals(Double.POSITIVE_INFINITY, algoritmos.obtenerExcentricidad(grafo,
                        grafo.construirComparable(vertice)), 0);
            }
        }
    }

    // Desde un sumidero no se llega a los demás, pero sí a sí mismo con costo cero.
    // Se comprueba tanto Floyd como la enumeración de caminos.
    public void testVerticeSinSalidasYCaminoDeLongitudCero() {
        DirectedGraph<String, WeightedEdge> grafo = crearGrafo();
        IFloydWarshallResult<String> resultado = algoritmos.floyd(grafo);
        for (String destino : VERTICES) {
            if (!destino.equals("Porto_Alegre")) {
                assertFalse(resultado.connected("Porto_Alegre", destino));
                assertEquals(Double.POSITIVE_INFINITY, resultado.getCost("Porto_Alegre", destino), 0);
                assertEquals(List.of(), resultado.getPath("Porto_Alegre", destino));
                assertTrue(algoritmos.obtenerTodosLosCaminos(grafo.construirComparable("Porto_Alegre"),
                        grafo.construirComparable(destino), grafo).isEmpty());
                assertTrue(resultado.connected(destino, "Porto_Alegre"));
            }
        }
        assertTrue(resultado.connected("Porto_Alegre", "Porto_Alegre"));
        assertEquals(0.0, resultado.getCost("Porto_Alegre", "Porto_Alegre"), 0);
        assertEquals(List.of("Porto_Alegre"), resultado.getPath("Porto_Alegre", "Porto_Alegre"));
        List<Path<String>> caminos = algoritmos.obtenerTodosLosCaminos(grafo.construirComparable("Porto_Alegre"),
                grafo.construirComparable("Porto_Alegre"), grafo);
        assertEquals(1, caminos.size());
        assertEquals(List.of("Porto_Alegre"), caminos.get(0).getPath());
        assertEquals(0.0, caminos.get(0).getCost(), 0);
    }
}
