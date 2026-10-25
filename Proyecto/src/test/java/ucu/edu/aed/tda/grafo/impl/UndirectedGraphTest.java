package ucu.edu.aed.tda.grafo.impl;

import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.model.edge.Edge;

import java.util.Arrays;
import java.util.Set;

public class UndirectedGraphTest extends TestCase {

    private UndirectedGraph<String, Integer> grafo(String... vertices) {
        UndirectedGraph<String, Integer> grafo = new UndirectedGraph<>();
        grafo.agregarVertices(Arrays.asList(vertices));
        return grafo;
    }

    private Comparable<String> criterio(String valor) {
        return x -> x.equals(valor) ? 0 : 1;
    }

    public void testGrafoVacio() {
        UndirectedGraph<String, Integer> grafo = grafo();
        assertEquals(0, grafo.cantidadDeVertices());
        assertEquals(0, grafo.cantidadDeAristas());
        assertTrue(grafo.esConexo());
        assertFalse(grafo.tieneCiclos());
        assertTrue(grafo.adyacencias(criterio("A")).isEmpty());
    }

    public void testUnSoloVertice() {
        UndirectedGraph<String, Integer> grafo = grafo("A");
        assertTrue(grafo.esConexo());
        assertFalse(grafo.tieneCiclos());
        assertTrue(grafo.adyacencias(criterio("A")).isEmpty());
    }

    public void testVerticesDuplicadosYNull() {
        UndirectedGraph<String, Integer> grafo = grafo("A", "B", "A");
        assertEquals(2, grafo.cantidadDeVertices());
        assertFalse(grafo.agregarVertice("A"));
        assertFalse(grafo.agregarVertice(null));
        assertEquals("B", grafo.buscarVertice(criterio("B")));
        assertNull(grafo.buscarVertice(criterio("X")));
    }

    public void testSimetria() {
        UndirectedGraph<String, Integer> grafo = grafo("A", "B");
        assertTrue(grafo.agregarArista("A", "B", 5));
        assertTrue(grafo.existeArista(criterio("A"), criterio("B")));
        assertTrue(grafo.existeArista(criterio("B"), criterio("A")));
        assertEquals(Integer.valueOf(5), grafo.obtenerArista("A", "B").dato());
        assertEquals(Integer.valueOf(5), grafo.obtenerArista("B", "A").dato());
        assertEquals(1, grafo.cantidadDeAristas());
    }

    public void testAdyacenciasTienenSourceEnElVerticeConsultado() {
        UndirectedGraph<String, Integer> grafo = grafo("A", "B", "C");
        grafo.agregarArista("A", "B", 1);
        grafo.agregarArista("C", "B", 2);
        for (Edge<String, Integer> e : grafo.adyacencias(criterio("B"))) {
            assertEquals("B", e.source());
        }
        assertEquals(Set.of("A", "C"), grafo.vecinos(criterio("B")));
    }

    public void testAristasDuplicadasBuclesYVerticesInexistentes() {
        UndirectedGraph<String, Integer> grafo = grafo("A", "B");
        assertTrue(grafo.agregarArista("A", "B", 1));
        assertFalse(grafo.agregarArista("A", "B", 2));
        assertFalse(grafo.agregarArista("B", "A", 3));
        assertFalse(grafo.agregarArista("A", "A", 1));
        assertFalse(grafo.agregarArista("A", "X", 1));
        assertEquals(1, grafo.cantidadDeAristas());
    }

    public void testEliminarAristaBorraAmbosLados() {
        UndirectedGraph<String, Integer> grafo = grafo("A", "B");
        grafo.agregarArista("A", "B", 1);
        assertTrue(grafo.eliminarArista(criterio("B"), criterio("A")));
        assertFalse(grafo.existeArista(criterio("A"), criterio("B")));
        assertFalse(grafo.existeArista(criterio("B"), criterio("A")));
        assertFalse(grafo.eliminarArista(criterio("A"), criterio("B")));
        assertEquals(0, grafo.cantidadDeAristas());
    }

    public void testRemoverVerticeBorraSusAristas() {
        UndirectedGraph<String, Integer> grafo = grafo("A", "B", "C");
        grafo.agregarArista("A", "B", 1);
        grafo.agregarArista("B", "C", 1);
        assertTrue(grafo.removerVertice(criterio("B")));
        assertFalse(grafo.removerVertice(criterio("B")));
        assertEquals(2, grafo.cantidadDeVertices());
        assertEquals(0, grafo.cantidadDeAristas());
        assertTrue(grafo.adyacencias(criterio("A")).isEmpty());
        assertTrue(grafo.adyacencias(criterio("C")).isEmpty());
    }

    public void testConectividad() {
        UndirectedGraph<String, Integer> grafo = grafo("A", "B", "C", "D");
        grafo.agregarArista("A", "B", 1);
        grafo.agregarArista("C", "D", 1);
        assertFalse(grafo.esConexo());
        grafo.agregarArista("B", "C", 1);
        assertTrue(grafo.esConexo());
        grafo.agregarVertice("E");
        assertFalse(grafo.esConexo());
    }

    public void testUnaAristaNoEsCiclo() {
        UndirectedGraph<String, Integer> grafo = grafo("A", "B");
        grafo.agregarArista("A", "B", 1);
        assertFalse(grafo.tieneCiclos());
    }

    public void testArbolNoTieneCiclos() {
        UndirectedGraph<String, Integer> grafo = grafo("A", "B", "C", "D");
        grafo.agregarArista("A", "B", 1);
        grafo.agregarArista("A", "C", 1);
        grafo.agregarArista("C", "D", 1);
        assertFalse(grafo.tieneCiclos());
    }

    public void testTrianguloTieneCiclo() {
        UndirectedGraph<String, Integer> grafo = grafo("A", "B", "C");
        grafo.agregarArista("A", "B", 1);
        grafo.agregarArista("B", "C", 1);
        grafo.agregarArista("C", "A", 1);
        assertTrue(grafo.tieneCiclos());
        grafo.eliminarArista(criterio("C"), criterio("A"));
        assertFalse(grafo.tieneCiclos());
    }

    public void testCicloEnComponenteNoConexa() {
        UndirectedGraph<String, Integer> grafo = grafo("A", "B", "X", "Y", "Z");
        grafo.agregarArista("A", "B", 1);
        grafo.agregarArista("X", "Y", 1);
        grafo.agregarArista("Y", "Z", 1);
        grafo.agregarArista("Z", "X", 1);
        assertTrue(grafo.tieneCiclos());
    }

    public void testVaciar() {
        UndirectedGraph<String, Integer> grafo = grafo("A", "B");
        grafo.agregarArista("A", "B", 1);
        grafo.vaciar();
        assertEquals(0, grafo.cantidadDeVertices());
        assertEquals(0, grafo.cantidadDeAristas());
    }
}