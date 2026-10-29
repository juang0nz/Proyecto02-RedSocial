package ucu.edu.aed.tda.grafo.model.result;

import java.util.List;

// Permite consultar caminos entre todos los pares después de ejecutar Floyd o Warshall.
public interface IFloydWarshallResult<V> {


    // Devuelve la ruta completa o una lista vacía si no hay camino.
    // Del vértice a sí mismo devuelve solo ese vértice; ambos extremos deben existir.
    List<V> getPath(V source, V target);

    // Devuelve el costo mínimo: pesos en Floyd, cantidad de saltos en Warshall.
    // Da cero para el mismo vértice e infinito si no llega; rechaza vértices inexistentes.
    double getCost(V source, V target);

    // Indica si se puede llegar de source a target, incluso si son el mismo vértice.
    // Si algún extremo no existe en el resultado, lanza una excepción.
    boolean connected(V source, V target);
}
