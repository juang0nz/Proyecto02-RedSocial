package ucu.edu.aed.tda.grafo.impl;

import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.IUndirectedGraph;
import ucu.edu.aed.tda.grafo.model.edge.Edge;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

// Pruebas de Prim, Kruskal y BFS no dirigido, correspondientes al tercer requisito del desafío.
public class UndirectedGraphAlgorithmsTest extends TestCase {

    private final UndirectedGraphAlgorithms algoritmos = new UndirectedGraphAlgorithms();

    private UndirectedGraph<String, WeightedEdge> grafo(String... vertices) {
        UndirectedGraph<String, WeightedEdge> grafo = new UndirectedGraph<>();
        grafo.agregarVertices(Arrays.asList(vertices));
        return grafo;
    }

    private void arista(UndirectedGraph<String, WeightedEdge> grafo, String a, String b, double peso) {
        grafo.agregarArista(a, b, new WeightedEdge(peso));
    }

    private Comparable<String> criterio(String valor) {
        return x -> x.equals(valor) ? 0 : 1;
    }

    private double costo(IUndirectedGraph<String, WeightedEdge> grafo) {
        double total = 0;
        for (Edge<String, WeightedEdge> e : grafo.aristas()) {
            total += e.dato().getWeight();
        }
        return total;
    }

    private UndirectedGraph<String, WeightedEdge> grafoClasico() {
        UndirectedGraph<String, WeightedEdge> g = grafo("A", "B", "C", "D", "E", "F", "G", "H", "I");
        arista(g, "A", "B", 4);
        arista(g, "A", "H", 8);
        arista(g, "B", "C", 8);
        arista(g, "B", "H", 11);
        arista(g, "C", "D", 7);
        arista(g, "C", "F", 4);
        arista(g, "C", "I", 2);
        arista(g, "D", "E", 9);
        arista(g, "D", "F", 14);
        arista(g, "E", "F", 10);
        arista(g, "F", "G", 2);
        arista(g, "G", "H", 1);
        arista(g, "G", "I", 6);
        arista(g, "H", "I", 7);
        return g;
    }

    // No alcanza con obtener costo 37: también se exige conexión, V - 1 aristas y ningún ciclo.
    public void testKruskalGrafoClasicoCosto37() {
        IUndirectedGraph<String, WeightedEdge> arbol = algoritmos.kruskal(grafoClasico());
        assertEquals(9, arbol.cantidadDeVertices());
        assertEquals(8, arbol.cantidadDeAristas());
        assertEquals(37.0, costo(arbol), 0.0001);
        assertTrue(arbol.esConexo());
        assertFalse(arbol.tieneCiclos());
    }

    // Comprueba las mismas propiedades del árbol mínimo, esta vez construido con Prim.
    public void testPrimGrafoClasicoCosto37() {
        IUndirectedGraph<String, WeightedEdge> arbol = algoritmos.prim(grafoClasico(), criterio("A"));
        assertEquals(9, arbol.cantidadDeVertices());
        assertEquals(8, arbol.cantidadDeAristas());
        assertEquals(37.0, costo(arbol), 0.0001);
        assertTrue(arbol.esConexo());
        assertFalse(arbol.tieneCiclos());
    }

    // Cambiar el origen puede cambiar las aristas elegidas, pero no el costo mínimo esperado.
    public void testPrimDaElMismoCostoDesdeCualquierOrigen() {
        UndirectedGraph<String, WeightedEdge> g = grafoClasico();
        for (String origen : g.vertices()) {
            assertEquals(37.0, costo(algoritmos.prim(g, criterio(origen))), 0.0001);
        }
    }

    // Verifica la versión que elige el primer vértice como origen automáticamente.
    public void testPrimSinOrigen() {
        assertEquals(37.0, costo(algoritmos.prim(grafoClasico())), 0.0001);
    }

    // Ambos algoritmos construyen otro grafo; acá se comprueba que no cambie el número de aristas original.
    public void testNoModificaElGrafoOriginal() {
        UndirectedGraph<String, WeightedEdge> g = grafoClasico();
        algoritmos.kruskal(g);
        algoritmos.prim(g, criterio("A"));
        assertEquals(14, g.cantidadDeAristas());
    }

    // En vacío, Kruskal devuelve un grafo vacío y Prim sin origen devuelve null.
    public void testGrafoVacio() {
        UndirectedGraph<String, WeightedEdge> g = grafo();
        assertEquals(0, algoritmos.kruskal(g).cantidadDeVertices());
        assertNull(algoritmos.prim(g));
    }

    // El caso unitario debe conservar el vértice sin intentar agregar aristas.
    public void testUnSoloVertice() {
        UndirectedGraph<String, WeightedEdge> g = grafo("A");
        IUndirectedGraph<String, WeightedEdge> k = algoritmos.kruskal(g);
        IUndirectedGraph<String, WeightedEdge> p = algoritmos.prim(g, criterio("A"));
        assertEquals(1, k.cantidadDeVertices());
        assertEquals(0, k.cantidadDeAristas());
        assertEquals(1, p.cantidadDeVertices());
        assertEquals(0, p.cantidadDeAristas());
    }

    // La versión de Prim con origen devuelve un grafo vacío si ese origen no existe.
    public void testPrimOrigenInexistente() {
        IUndirectedGraph<String, WeightedEdge> arbol = algoritmos.prim(grafoClasico(), criterio("X"));
        assertEquals(0, arbol.cantidadDeVertices());
    }

    // Con pesos iguales puede haber varios árboles válidos; se verifican costo y ausencia de ciclos.
    public void testPesosIguales() {
        UndirectedGraph<String, WeightedEdge> g = grafo("A", "B", "C", "D");
        arista(g, "A", "B", 1);
        arista(g, "B", "C", 1);
        arista(g, "C", "D", 1);
        arista(g, "D", "A", 1);
        arista(g, "A", "C", 1);
        IUndirectedGraph<String, WeightedEdge> k = algoritmos.kruskal(g);
        IUndirectedGraph<String, WeightedEdge> p = algoritmos.prim(g, criterio("A"));
        assertEquals(3, k.cantidadDeAristas());
        assertEquals(3, p.cantidadDeAristas());
        assertEquals(3.0, costo(k), 0.0001);
        assertEquals(3.0, costo(p), 0.0001);
        assertFalse(k.tieneCiclos());
        assertFalse(p.tieneCiclos());
    }

    // Para el caso desconectado se esperan dos árboles y un aislado conservado en el resultado.
    public void testGrafoDesconectadoDevuelveBosque() {
        UndirectedGraph<String, WeightedEdge> g = grafo("A", "B", "C", "X", "Y", "Z", "SOLO");
        arista(g, "A", "B", 1);
        arista(g, "B", "C", 2);
        arista(g, "C", "A", 3);
        arista(g, "X", "Y", 5);
        arista(g, "Y", "Z", 4);
        arista(g, "Z", "X", 6);
        IUndirectedGraph<String, WeightedEdge> k = algoritmos.kruskal(g);
        IUndirectedGraph<String, WeightedEdge> p = algoritmos.prim(g, criterio("A"));
        assertEquals(7, k.cantidadDeVertices());
        assertEquals(7, p.cantidadDeVertices());
        assertEquals(4, k.cantidadDeAristas());
        assertEquals(4, p.cantidadDeAristas());
        assertEquals(12.0, costo(k), 0.0001);
        assertEquals(12.0, costo(p), 0.0001);
        assertFalse(k.esConexo());
        assertFalse(p.tieneCiclos());
    }

    // Compara los costos de ambos algoritmos cuando existen muchas conexiones posibles.
    public void testGrafoDensoPrimYKruskalCoinciden() {
        String[] nombres = {"A", "B", "C", "D", "E", "F"};
        UndirectedGraph<String, WeightedEdge> g = grafo(nombres);
        for (int i = 0; i < nombres.length; i++) {
            for (int j = i + 1; j < nombres.length; j++) {
                arista(g, nombres[i], nombres[j], (i * 7 + j * 3) % 10 + 1);
            }
        }
        assertEquals(15, g.cantidadDeAristas());
        IUndirectedGraph<String, WeightedEdge> k = algoritmos.kruskal(g);
        IUndirectedGraph<String, WeightedEdge> p = algoritmos.prim(g, criterio("C"));
        assertEquals(5, k.cantidadDeAristas());
        assertEquals(5, p.cantidadDeAristas());
        assertEquals(costo(k), costo(p), 0.0001);
    }

    // Verifica la arista mínima entre conjuntos y null cuando no hay conexión entre ellos.
    public void testSearchMinEdge() {
        UndirectedGraph<String, WeightedEdge> g = grafoClasico();
        Edge<String, WeightedEdge> e = algoritmos.searchMinEdge(g, Set.of("A"), Set.of("B", "H"));
        assertEquals("A", e.source());
        assertEquals("B", e.target());
        assertEquals(4.0, e.dato().getWeight(), 0.0001);
        assertNull(algoritmos.searchMinEdge(g, Set.of("A"), Set.of("E")));
    }

    // El orden esperado muestra que BFS termina un nivel antes de visitar el siguiente.
    public void testBeaRecorreEnAmplitud() {
        UndirectedGraph<String, WeightedEdge> g = grafo("A", "B", "C", "D", "E");
        arista(g, "A", "B", 1);
        arista(g, "A", "C", 1);
        arista(g, "B", "D", 1);
        arista(g, "C", "E", 1);
        List<String> orden = new ArrayList<>();
        algoritmos.bea(g, orden::add);
        assertEquals(Arrays.asList("A", "B", "C", "D", "E"), orden);
    }

    // El recorrido no debe terminar al agotar la primera componente.
    public void testBeaVisitaTodasLasComponentes() {
        UndirectedGraph<String, WeightedEdge> g = grafo("A", "B", "X", "Y");
        arista(g, "A", "B", 1);
        arista(g, "X", "Y", 1);
        List<String> orden = new ArrayList<>();
        algoritmos.bea(g, orden::add);
        assertEquals(Arrays.asList("A", "B", "X", "Y"), orden);
    }

    // En vacío, BFS termina sin llamar al consumer.
    public void testBeaGrafoVacio() {
        List<String> orden = new ArrayList<>();
        algoritmos.bea(grafo(), orden::add);
        assertTrue(orden.isEmpty());
    }

    // El caso unitario debe visitar al vértice aunque no tenga conexiones.
    public void testBeaUnSoloVertice() {
        List<String> orden = new ArrayList<>();
        algoritmos.bea(grafo("A"), orden::add);
        assertEquals(List.of("A"), orden);
    }

    // El ciclo no repite visitas y los aislados también se incluyen al recorrer todo el grafo.
    public void testBeaConCicloYAislados() {
        UndirectedGraph<String, WeightedEdge> g = grafo("SOLO", "A", "B", "C", "OTRO");
        arista(g, "A", "B", 1);
        arista(g, "B", "C", 1);
        arista(g, "C", "A", 1);
        List<String> orden = new ArrayList<>();
        algoritmos.bea(g, orden::add);
        assertEquals(List.of("SOLO", "A", "B", "C", "OTRO"), orden);
        assertEquals(5, g.cantidadDeVertices());
        assertEquals(3, g.cantidadDeAristas());
    }
}