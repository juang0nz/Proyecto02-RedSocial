package ucu.edu.aed.tda.grafo.model.result;

import java.util.List;
import java.util.Map;

// Permite consultar los caminos mínimos desde el origen usado en Dijkstra.
public interface IDijkstraResult<V> {
    // Devuelve el costo al destino, o infinito si no se puede llegar.
    // Si el vértice no estaba en el resultado, lanza una excepción.
    double getCost(V otherVertex);

    // Devuelve la ruta incluyendo origen y destino, o lista vacía si no hay camino.
    // Un destino ajeno al resultado es un error.
    List<V> getPath(V otherVertex);

    // Para cada destino guarda el vértice anterior del camino, sin permitir cambios.
    // El origen y los inalcanzables no tienen predecesor.
    Map<V, V> getPredecessors();
}
