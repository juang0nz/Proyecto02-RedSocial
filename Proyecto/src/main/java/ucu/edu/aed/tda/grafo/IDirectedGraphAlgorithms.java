package ucu.edu.aed.tda.grafo;


import ucu.edu.aed.tda.grafo.model.IGraph;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.tda.grafo.model.result.IDijkstraResult;
import ucu.edu.aed.tda.grafo.model.result.IFloydWarshallResult;
import ucu.edu.aed.tda.grafo.model.result.Path;

import java.util.List;
import java.util.function.Consumer;

// Reúne los recorridos y cálculos de caminos para grafos dirigidos.
// Declara los algoritmos del segundo requisito del Desafío 1;
// su implementación está en DirectedGraphAlgorithms, no en estas firmas.
public interface IDirectedGraphAlgorithms {

    // Calcula los costos mínimos desde source y permite reconstruir los caminos.
    // Los pesos tienen que ser finitos y no negativos.
    <V, D extends WeightedEdge> IDijkstraResult<V> dijkstra(Comparable<V> source, IDirectedIGraph<V, D> grafo);

    // Calcula los costos mínimos entre todos los pares de vértices usando los pesos.
    <V, D extends WeightedEdge> IFloydWarshallResult<V> floyd(IDirectedIGraph<V, D> grafo);

    // Calcula qué vértices se pueden alcanzar y con cuántos saltos, sin usar los pesos.
    <V, D extends WeightedEdge> IFloydWarshallResult<V> warshall(IDirectedIGraph<V, D> grafo);

    // Elige el vértice cuyo peor costo de llegada desde los demás sea el menor.
    // En un empate queda el primero. Si ninguno recibe caminos de todos, devuelve null.
    <V, D extends WeightedEdge> V obtenerCentroGrafo(IDirectedIGraph<V, D> grafo);

    // Busca el mayor costo mínimo para llegar al vértice desde cualquier origen.
    // Si alguno no llega, da infinito; si el vértice no existe, lanza una excepción.
    <V, D extends WeightedEdge> double obtenerExcentricidad(IDirectedIGraph<V, D> grafo, Comparable<V> vertexCriteria);

    // Devuelve los caminos de source a target sin repetir vértices, con sus costos.
    <V, D extends WeightedEdge> List<Path<V>> obtenerTodosLosCaminos(Comparable<V> source, Comparable<V> target, IGraph<V, D> grafo);

    // Recorre desde el origen en profundidad y aplica consumer a cada vértice visitado.
    <V, D> void recorridoEnProfundidad(IGraph<V, D> grafo, Comparable<V> sourceCriteria, Consumer<V> consumer);

    // Recorre desde el origen por niveles y aplica consumer a cada vértice visitado.
    <V, D> void recorridoEnAmplitud(IGraph<V, D> grafo, Comparable<V> sourceCriteria, Consumer<V> consumer);

    // Devuelve un orden donde cada origen va antes que sus destinos. Rechaza ciclos.
    <V, D> List<V> calcularClasificacionTopologica(IDirectedIGraph<V, D> grafo);

    // Inicia un recorrido desde cada vértice. Al ser recorridos separados,
    // un mismo vértice puede llegar al consumer más de una vez.
    default <V, D> void recorridoEnProfundidad(IGraph<V, D> grafo, Consumer<V> consumer) {
        for (V vertex : grafo.vertices()) {
            recorridoEnProfundidad(grafo, grafo.construirComparable(vertex), consumer);
        }
    }


    // También inicia una búsqueda nueva desde cada vértice, sin compartir visitados.
    default <V, D> void recorridoEnAmplitud(IGraph<V, D> grafo, Consumer<V> consumer) {
        for (V v : grafo.vertices()) {
            recorridoEnAmplitud(grafo, grafo.construirComparable(v), consumer);
        }
    }
}
