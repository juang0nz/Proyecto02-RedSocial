package ucu.edu.aed.tda.grafo.impl;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Set;

// Comprueba el requisito de articulaciones con formas de grafo donde se conoce qué vértices separan.
public class PuntosDeArticulacionTest extends TestCase {

    private final UndirectedGraphAlgorithms algoritmos = new UndirectedGraphAlgorithms();

    private UndirectedGraph<String, Integer> grafo(String... vertices) {
        UndirectedGraph<String, Integer> grafo = new UndirectedGraph<>();
        grafo.agregarVertices(Arrays.asList(vertices));
        return grafo;
    }

    private void aristas(UndirectedGraph<String, Integer> grafo, String... pares) {
        for (int i = 0; i < pares.length; i += 2) {
            grafo.agregarArista(pares[i], pares[i + 1], 1);
        }
    }

    private Comparable<String> criterio(String valor) {
        return x -> x.equals(valor) ? 0 : 1;
    }

    // Sin vértices no puede haber puntos de articulación.
    public void testGrafoVacio() {
        assertTrue(algoritmos.puntosDeArticulacion(grafo()).isEmpty());
    }

    // Quitar el único vértice no aumenta la cantidad de componentes.
    public void testVerticeUnico() {
        assertTrue(algoritmos.puntosDeArticulacion(grafo("A")).isEmpty());
    }

    // Una sola conexión tampoco tiene vértices cuya eliminación aumente las componentes.
    public void testDosVertices() {
        UndirectedGraph<String, Integer> g = grafo("A", "B");
        aristas(g, "A", "B");
        assertTrue(algoritmos.puntosDeArticulacion(g).isEmpty());
    }

    // En una cadena, los vértices internos separan el grafo; los extremos no.
    public void testCamino() {
        UndirectedGraph<String, Integer> g = grafo("A", "B", "C", "D");
        aristas(g, "A", "B", "B", "C", "C", "D");
        assertEquals(Set.of("B", "C"), algoritmos.puntosDeArticulacion(g));
    }

    // Cambia el inicio del DFS para comprobar también el caso especial de la raíz con varios hijos.
    public void testCaminoEmpezandoDesdeElMedio() {
        UndirectedGraph<String, Integer> g = grafo("B", "A", "C", "D");
        aristas(g, "A", "B", "B", "C", "C", "D");
        assertEquals(Set.of("B", "C"), algoritmos.puntosDeArticulacion(g));
    }

    // En un ciclo queda un camino alternativo al quitar cualquiera de sus vértices.
    public void testCicloNoTieneArticulaciones() {
        UndirectedGraph<String, Integer> g = grafo("A", "B", "C", "D", "E");
        aristas(g, "A", "B", "B", "C", "C", "D", "D", "E", "E", "A");
        assertTrue(algoritmos.puntosDeArticulacion(g).isEmpty());
    }

    // Si se quita el centro de la estrella, sus hojas quedan separadas.
    public void testEstrellaElCentroEsArticulacion() {
        UndirectedGraph<String, Integer> g = grafo("CENTRO", "A", "B", "C", "D");
        aristas(g, "CENTRO", "A", "CENTRO", "B", "CENTRO", "C", "CENTRO", "D");
        assertEquals(Set.of("CENTRO"), algoritmos.puntosDeArticulacion(g));
    }

    // Tener muchos vecinos no alcanza: acá siguen conectados por otro camino.
    public void testMuchosVecinosNoImplicaArticulacion() {
        UndirectedGraph<String, Integer> g = grafo("FAMOSO", "A", "B", "C", "D");
        aristas(g, "FAMOSO", "A", "FAMOSO", "B", "FAMOSO", "C", "FAMOSO", "D",
                "A", "B", "B", "C", "C", "D");
        assertTrue(algoritmos.puntosDeArticulacion(g).isEmpty());
    }

    // El único vértice compartido por los dos ciclos debe detectarse como articulación.
    public void testDosTriangulosUnidosPorUnVertice() {
        UndirectedGraph<String, Integer> g = grafo("A", "B", "X", "C", "D");
        aristas(g, "A", "B", "B", "X", "X", "A", "X", "C", "C", "D", "D", "X");
        assertEquals(Set.of("X"), algoritmos.puntosDeArticulacion(g));
    }

    // Comprueba los vértices de ambos extremos de la conexión que une los dos grupos.
    public void testDosComunidadesUnidasPorUnPuente() {
        UndirectedGraph<String, Integer> g = grafo("A", "B", "C", "X", "Y", "Z");
        aristas(g, "A", "B", "B", "C", "C", "A", "C", "X", "X", "Y", "Y", "Z", "Z", "X");
        assertEquals(Set.of("C", "X"), algoritmos.puntosDeArticulacion(g));
    }

    // Revisa una cadena, un ciclo y un aislado dentro del mismo grafo desconectado.
    public void testComponentesDesconectadas() {
        UndirectedGraph<String, Integer> g = grafo("A", "B", "C", "X", "Y", "Z", "SOLO");
        aristas(g, "A", "B", "B", "C", "X", "Y", "Y", "Z", "Z", "X");
        assertEquals(Set.of("B"), algoritmos.puntosDeArticulacion(g));
    }

    // Comprueba directamente que quitar una articulación conocida rompe la conexidad.
    public void testEliminarLaArticulacionDivideElGrafo() {
        UndirectedGraph<String, Integer> g = grafo("A", "B", "C", "X", "Y", "Z");
        aristas(g, "A", "B", "B", "C", "C", "A", "C", "X", "X", "Y", "Y", "Z", "Z", "X");
        assertTrue(g.esConexo());
        g.removerVertice(criterio("C"));
        assertFalse(g.esConexo());
    }

    // Como contraste, quitar este otro vértice deja el grafo conectado.
    public void testEliminarUnVerticeNoArticulacionNoDivide() {
        UndirectedGraph<String, Integer> g = grafo("A", "B", "C", "X", "Y", "Z");
        aristas(g, "A", "B", "B", "C", "C", "A", "C", "X", "X", "Y", "Y", "Z", "Z", "X");
        g.removerVertice(criterio("A"));
        assertTrue(g.esConexo());
    }

    // Ignora el sentido de las aristas y evita duplicar seguimientos mutuos antes de buscar articulaciones.
    public void testProyeccionNoDirigida() {
        DirectedGraph<String, Integer> dirigido = new DirectedGraph<>();
        dirigido.agregarVertices(Arrays.asList("A", "B", "C"));
        dirigido.agregarArista("A", "B", 1);
        dirigido.agregarArista("B", "A", 1);
        dirigido.agregarArista("C", "B", 1);
        UndirectedGraph<String, Integer> proyeccion = algoritmos.proyeccionNoDirigida(dirigido);
        assertEquals(3, proyeccion.cantidadDeVertices());
        assertEquals(2, proyeccion.cantidadDeAristas());
        assertTrue(proyeccion.existeArista(criterio("B"), criterio("C")));
        assertTrue(proyeccion.existeArista(criterio("A"), criterio("B")));
        assertEquals(Set.of("B"), algoritmos.puntosDeArticulacion(proyeccion));
    }
}