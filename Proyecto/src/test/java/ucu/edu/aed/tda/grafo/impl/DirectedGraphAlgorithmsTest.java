package ucu.edu.aed.tda.grafo.impl;

import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.tda.grafo.model.result.IFloydWarshallResult;

import java.util.List;

// Comprueba Floyd, centro y excentricidad del Desafío 1 con resultados esperados concretos.
public class DirectedGraphAlgorithmsTest extends TestCase {

    private final DirectedGraphAlgorithms algoritmos = new DirectedGraphAlgorithms();

    private DirectedGraph<String, WeightedEdge> grafo(String... vertices) {
        DirectedGraph<String, WeightedEdge> grafo = new DirectedGraph<>();
        grafo.agregarVertices(List.of(vertices));
        return grafo;
    }

    // Antes de probar los algoritmos, verifica que la estructura conserve las conexiones ingresadas.
    public void testInsercionDirigidaSinDuplicados() {
        DirectedGraph<String, WeightedEdge> grafo = grafo("A", "B", "C");
        assertFalse(grafo.agregarVertice("A"));
        assertFalse(grafo.agregarVertice(null));
        assertFalse(grafo.agregarArista("A", "X", new WeightedEdge(1)));
        assertTrue(grafo.agregarArista("A", "B", new WeightedEdge(5)));
        assertFalse(grafo.agregarArista("A", "B", new WeightedEdge(2)));
        assertEquals(1, grafo.cantidadDeAristas());
        assertEquals(5.0, grafo.obtenerArista("A", "B").dato().getWeight(), 0);
        assertNull(grafo.obtenerArista("B", "A"));
        assertEquals(List.of("A", "B", "C"), List.copyOf(grafo.vertices()));
        try {
            grafo.vertices().clear();
            fail("Los vertices deben ser inmodificables");
        } catch (UnsupportedOperationException expected) {
            assertEquals(3, grafo.cantidadDeVertices());
        }
        try {
            grafo.adyacencias(grafo.construirComparable("A")).clear();
            fail("Las adyacencias deben ser inmodificables");
        } catch (UnsupportedOperationException expected) {
            assertEquals(1, grafo.cantidadDeAristas());
        }
    }

    // La ruta por varios intermedios cuesta menos que la directa, incluso con un tramo de peso cero.
    // El vértice E permite comprobar también aislamiento y el camino de longitud cero.
    public void testFloydReconstruyeCaminoConVariosIntermedios() {
        DirectedGraph<String, WeightedEdge> grafo = grafo("A", "B", "C", "D", "E");
        grafo.agregarArista("A", "D", new WeightedEdge(20));
        grafo.agregarArista("A", "B", new WeightedEdge(4));
        grafo.agregarArista("B", "C", new WeightedEdge(0));
        grafo.agregarArista("C", "D", new WeightedEdge(3));
        IFloydWarshallResult<String> resultado = algoritmos.floyd(grafo);
        assertEquals(7.0, resultado.getCost("A", "D"), 0);
        assertEquals(List.of("A", "B", "C", "D"), resultado.getPath("A", "D"));
        assertTrue(resultado.connected("A", "D"));
        assertFalse(resultado.connected("D", "A"));
        assertEquals(Double.POSITIVE_INFINITY, resultado.getCost("A", "E"), 0);
        assertEquals(List.of(), resultado.getPath("A", "E"));
        assertTrue(resultado.connected("E", "E"));
        assertEquals(0.0, resultado.getCost("E", "E"), 0);
        assertEquals(List.of("E"), resultado.getPath("E", "E"));
    }

    // El resultado de Floyd debe seguir consultándose después de vaciar el grafo original.
    public void testResultadoNoCambiaAlModificarGrafo() {
        DirectedGraph<String, WeightedEdge> grafo = grafo("A", "B");
        grafo.agregarArista("A", "B", new WeightedEdge(5));
        IFloydWarshallResult<String> resultado = algoritmos.floyd(grafo);
        grafo.vaciar();
        assertEquals(5.0, resultado.getCost("A", "B"), 0);
        assertEquals(List.of("A", "B"), resultado.getPath("A", "B"));
    }

    // Costo, camino y accesibilidad deben rechazar vértices que no estén en el resultado.
    public void testConsultasConVerticeDesconocidoFallanExplicitamente() {
        IFloydWarshallResult<String> resultado = algoritmos.floyd(grafo("A"));
        try {
            resultado.getCost("A", "X");
            fail("Debe rechazar vertices desconocidos");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("X"));
        }
        try {
            resultado.getPath("X", "A");
            fail("Debe rechazar vertices desconocidos");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("X"));
        }
        try {
            resultado.connected("A", "X");
            fail("Debe rechazar vertices desconocidos");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("X"));
        }
    }

    // Floyd, a diferencia de Dijkstra, permite este peso negativo porque no hay ciclo negativo.
    public void testPesosNegativosSinCicloNegativo() {
        DirectedGraph<String, WeightedEdge> grafo = grafo("A", "B", "C");
        grafo.agregarArista("A", "B", new WeightedEdge(4));
        grafo.agregarArista("B", "C", new WeightedEdge(-2));
        grafo.agregarArista("A", "C", new WeightedEdge(5));
        IFloydWarshallResult<String> resultado = algoritmos.floyd(grafo);
        assertEquals(2.0, resultado.getCost("A", "C"), 0);
        assertEquals(List.of("A", "B", "C"), resultado.getPath("A", "C"));
    }

    // Con un ciclo negativo no hay costo mínimo finito, así que se espera una excepción.
    public void testRechazaCicloNegativo() {
        DirectedGraph<String, WeightedEdge> grafo = grafo("A", "B");
        grafo.agregarArista("A", "B", new WeightedEdge(1));
        grafo.agregarArista("B", "A", new WeightedEdge(-2));
        try {
            algoritmos.floyd(grafo);
            fail("No existe costo minimo con ciclos negativos");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("negativo"));
        }
    }

    // NaN e infinito no se aceptan como pesos de entrada en Floyd.
    public void testRechazaPesosInvalidos() {
        for (double peso : new double[]{Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            DirectedGraph<String, WeightedEdge> grafo = grafo("A", "B");
            grafo.agregarArista("A", "B", new WeightedEdge(peso));
            try {
                algoritmos.floyd(grafo);
                fail("Floyd requiere pesos finitos");
            } catch (IllegalArgumentException expected) {
                assertTrue(expected.getMessage().contains("finitos"));
            }
        }
    }

    // Verifica que el centro se calcule por costos de llegada: C recibe caminos desde todos.
    public void testCentroUsaExcentricidadDeEntrada() {
        DirectedGraph<String, WeightedEdge> grafo = grafo("A", "B", "C");
        grafo.agregarArista("A", "B", new WeightedEdge(2));
        grafo.agregarArista("B", "C", new WeightedEdge(3));
        assertEquals("C", algoritmos.obtenerCentroGrafo(grafo));
        assertEquals(5.0, algoritmos.obtenerExcentricidad(grafo, grafo.construirComparable("C")), 0);
        assertEquals(Double.POSITIVE_INFINITY,
                algoritmos.obtenerExcentricidad(grafo, grafo.construirComparable("A")), 0);
    }

    // Reúne casos especiales de la consigna: vacío, desconectado, unitario y empate de centros.
    // También comprueba que la excentricidad rechace un vértice inexistente.
    public void testCentroVacioDesconectadoUnitarioYEmpatado() {
        assertNull(algoritmos.obtenerCentroGrafo(grafo()));
        assertNull(algoritmos.obtenerCentroGrafo(grafo("A", "B")));
        assertEquals("A", algoritmos.obtenerCentroGrafo(grafo("A")));
        DirectedGraph<String, WeightedEdge> grafo = grafo("A", "B");
        grafo.agregarArista("A", "B", new WeightedEdge(1));
        grafo.agregarArista("B", "A", new WeightedEdge(1));
        assertEquals("A", algoritmos.obtenerCentroGrafo(grafo));
        try {
            algoritmos.obtenerExcentricidad(grafo, grafo.construirComparable("X"));
            fail("Debe rechazar vertices desconocidos");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("vertice"));
        }
    }
}
