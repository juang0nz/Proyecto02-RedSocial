package ucu.edu.aed.tda.grafo.impl;

import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.model.edge.Edge;

import java.util.Arrays;
import java.util.Set;

public class DirectedGraphTest extends TestCase {

    private DirectedGraph<String, Integer> grafo(String... vertices) {
        DirectedGraph<String, Integer> grafo = new DirectedGraph<>();
        grafo.agregarVertices(Arrays.asList(vertices));
        return grafo;
    }

    private Comparable<String> criterio(String valor) {
        return x -> x.equals(valor) ? 0 : 1;
    }

    public void testAgregarVerticesYDuplicados() {
        DirectedGraph<String, Integer> grafo = grafo("A", "B", "A");
        assertEquals(2, grafo.cantidadDeVertices());
        assertFalse(grafo.agregarVertice("A"));
        assertFalse(grafo.agregarVertice(null));
        assertTrue(grafo.agregarVertice("C"));
        assertEquals(3, grafo.cantidadDeVertices());
        assertEquals("B", grafo.buscarVertice(criterio("B")));
        assertNull(grafo.buscarVertice(criterio("X")));
        assertTrue(grafo.existeVertice(criterio("C")));
        assertFalse(grafo.existeVertice(criterio("X")));
    }

    public void testAristasDirigidasYDuplicados() {
        DirectedGraph<String, Integer> grafo = grafo("A", "B");
        assertFalse(grafo.agregarArista("A", "X", 1));
        assertFalse(grafo.agregarArista("X", "B", 1));
        assertTrue(grafo.agregarArista("A", "B", 7));
        assertFalse(grafo.agregarArista("A", "B", 9));
        assertTrue(grafo.agregarArista("B", "A", 3));
        assertEquals(2, grafo.cantidadDeAristas());
        assertEquals(Integer.valueOf(7), grafo.obtenerArista("A", "B").dato());
        assertEquals(Integer.valueOf(3), grafo.obtenerArista("B", "A").dato());
        assertTrue(grafo.existeArista(criterio("A"), criterio("B")));
        assertNull(grafo.obtenerArista("A", "X"));
        assertFalse(grafo.existeArista(criterio("X"), criterio("A")));
    }

    public void testEliminarAristaSoloEnUnaDireccion() {
        DirectedGraph<String, Integer> grafo = grafo("A", "B");
        grafo.agregarArista("A", "B", 1);
        grafo.agregarArista("B", "A", 2);
        assertTrue(grafo.eliminarArista(criterio("A"), criterio("B")));
        assertFalse(grafo.eliminarArista(criterio("A"), criterio("B")));
        assertFalse(grafo.eliminarArista(criterio("A"), criterio("X")));
        assertNull(grafo.obtenerArista("A", "B"));
        assertNotNull(grafo.obtenerArista("B", "A"));
        assertEquals(1, grafo.cantidadDeAristas());
    }

    public void testRemoverVerticeEliminaEntrantesSalientesYAutoarista() {
        DirectedGraph<String, Integer> grafo = grafo("A", "B", "C", "D");
        grafo.agregarArista("A", "B", 1);
        grafo.agregarArista("B", "C", 1);
        grafo.agregarArista("C", "B", 1);
        grafo.agregarArista("B", "B", 1);
        grafo.agregarArista("C", "D", 1);
        assertTrue(grafo.removerVertice(criterio("B")));
        assertFalse(grafo.removerVertice(criterio("B")));
        assertEquals(3, grafo.cantidadDeVertices());
        assertEquals(1, grafo.cantidadDeAristas());
        assertNull(grafo.obtenerArista("A", "B"));
        assertEquals(Set.of("D"), grafo.successors(criterio("C")));
        assertEquals(Set.of("C"), grafo.predecessors(criterio("D")));
    }

    public void testSucesoresPredecesoresYGrados() {
        DirectedGraph<String, Integer> grafo = grafo("A", "B", "C", "D");
        grafo.agregarArista("A", "B", 1);
        grafo.agregarArista("A", "C", 1);
        grafo.agregarArista("D", "B", 1);
        assertEquals(Set.of("B", "C"), grafo.successors(criterio("A")));
        assertEquals(Set.of("A", "D"), grafo.predecessors(criterio("B")));
        assertEquals(2, grafo.gradoDeSalida(criterio("A")));
        assertEquals(2, grafo.gradoDeEntrada(criterio("B")));
        assertEquals(0, grafo.gradoDeEntrada(criterio("A")));
        assertTrue(grafo.successors(criterio("X")).isEmpty());
        assertTrue(grafo.predecessors(criterio("X")).isEmpty());
        Set<String> copia = grafo.successors(criterio("A"));
        copia.clear();
        assertEquals(2, grafo.gradoDeSalida(criterio("A")));
    }

    public void testColeccionesInmodificables() {
        DirectedGraph<String, Integer> grafo = grafo("A", "B");
        grafo.agregarArista("A", "B", 1);
        try {
            grafo.vertices().clear();
            fail("La vista de vertices debe ser inmodificable");
        } catch (UnsupportedOperationException expected) {
            assertEquals(2, grafo.cantidadDeVertices());
        }
        try {
            grafo.aristas().clear();
            fail("La coleccion de aristas debe ser inmodificable");
        } catch (UnsupportedOperationException expected) {
            assertEquals(1, grafo.cantidadDeAristas());
        }
        try {
            grafo.adyacencias(criterio("A")).clear();
            fail("La lista de adyacencias debe ser inmodificable");
        } catch (UnsupportedOperationException expected) {
            assertEquals(1, grafo.cantidadDeAristas());
        }
    }

    public void testVaciarRestableceConteosYConsultas() {
        DirectedGraph<String, Integer> grafo = grafo("A", "B");
        grafo.agregarArista("A", "B", 1);
        grafo.vaciar();
        assertEquals(0, grafo.cantidadDeVertices());
        assertEquals(0, grafo.cantidadDeAristas());
        assertTrue(grafo.vertices().isEmpty());
        assertTrue(grafo.aristas().isEmpty());
        assertNull(grafo.buscarVertice(criterio("A")));
        assertTrue(grafo.adyacencias(criterio("A")).isEmpty());
        assertTrue(grafo.esConexo());
        assertFalse(grafo.tieneCiclos());
    }

    public void testConectividadDebil() {
        DirectedGraph<String, Integer> cadena = grafo("A", "B", "C");
        cadena.agregarArista("A", "B", 1);
        cadena.agregarArista("C", "B", 1);
        assertTrue(cadena.esConexo()); // Conexo ignorando el sentido de las aristas.
        cadena.agregarVertice("D");
        assertFalse(cadena.esConexo());
        DirectedGraph<String, Integer> unitario = grafo("A");
        assertTrue(unitario.esConexo());
        assertTrue(grafo().esConexo());
    }

    public void testCiclosDirigidosYComponentes() {
        DirectedGraph<String, Integer> grafo = grafo("A", "B", "C", "D", "E");
        grafo.agregarArista("A", "B", 1);
        grafo.agregarArista("A", "C", 1);
        grafo.agregarArista("B", "D", 1);
        grafo.agregarArista("C", "D", 1);
        assertFalse(grafo.tieneCiclos()); // DAG con caminos convergentes.
        grafo.agregarArista("D", "B", 1);
        assertTrue(grafo.tieneCiclos());
        assertTrue(grafo.eliminarArista(criterio("D"), criterio("B")));
        assertFalse(grafo.tieneCiclos());
        grafo.agregarArista("E", "E", 1);
        assertTrue(grafo.tieneCiclos());
    }

    public void testComparablePersonalizadoYObjetosIguales() {
        DirectedGraph<String, Integer> grafo = new DirectedGraph<>();
        String registrado = new String("usuario");
        String equivalente = new String("usuario");
        assertTrue(grafo.agregarVertice(registrado));
        assertFalse(grafo.agregarVertice(equivalente));
        assertTrue(grafo.agregarVertice("destino"));
        assertTrue(grafo.agregarArista(equivalente, "destino", 1));
        assertEquals(registrado, grafo.buscarVertice(x -> x.startsWith("usu") ? 0 : 1));
        assertTrue(grafo.existeArista(criterio("usuario"), criterio("destino")));
        assertEquals(1, grafo.cantidadDeAristas());
    }
}
