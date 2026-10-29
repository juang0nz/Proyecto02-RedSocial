package ucu.edu.aed.tda.grafo;


import ucu.edu.aed.tda.grafo.model.edge.Edge;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;

import java.util.Collection;
import java.util.function.Consumer;

// Reúne algoritmos para recorrer y conectar grafos no dirigidos.
// Prim, Kruskal y bea corresponden al tercer requisito del Desafío 1.
// Los puntos de articulación se implementan aparte en UndirectedGraphAlgorithms.
public interface IUndirectedGraphAlgorithm {
    // Conecta usando las aristas más baratas sin formar ciclos.
    // Si el grafo está separado, devuelve un bosque en lugar de un solo árbol.
    <V, D extends WeightedEdge> IUndirectedGraph<V, D> kruskal(IUndirectedGraph<V, D> graph);

    // Construye el árbol o bosque mínimo empezando por el vértice indicado.
    <V, D extends WeightedEdge> IUndirectedGraph<V, D> prim(IUndirectedGraph<V, D> graph, Comparable<V> source);

    // Usa el primer vértice como origen. Si el grafo está vacío, devuelve null.
    default <V, D extends WeightedEdge> IUndirectedGraph<V, D> prim(IUndirectedGraph<V, D> graph) {

        for (V vertex : graph.vertices()) {
            return prim(graph, graph.construirComparable(vertex));
        }

        return null;
    }


    // Busca la arista de menor peso que une un vértice de U con uno de V.
    // Devuelve null si no hay ninguna.
    <V, D extends WeightedEdge> Edge<V, D> searchMinEdge(IUndirectedGraph<V, D> graph, Collection<V> U, Collection<V> V);

    // Recorre todas las componentes por niveles y aplica consumer a cada vértice.
    <V, D> void bea(IUndirectedGraph<V, D> graph, Consumer<V> consumer);
}
