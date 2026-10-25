package ucu.edu.aed.tda.grafo.model.result;

import java.util.List;
import java.util.Map;

/**
 * Modela el resultado de ejecutar Dijkstra.
 * Ofrece el método obtener costo  y obtener un camino al vértice pasado
 */
public interface IDijkstraResult<V> {
    /**
     * Devuelve el costo minimo o infinito positivo si el destino es inaccesible.
     * Lanza IllegalArgumentException para vertices ajenos al resultado.
     */
    double getCost(V otherVertex);

    /**
     * Retorna el camino con origen y destino, o una lista vacia si no hay ruta.
     * Lanza IllegalArgumentException para vertices ajenos al resultado.
     */
    List<V> getPath(V otherVertex);

    /**
     * Vector de predecesores como mapa inmodificable destino -> predecesor.
     * El origen y los vertices inaccesibles no tienen entrada.
     */
    Map<V, V> getPredecessors();
}
