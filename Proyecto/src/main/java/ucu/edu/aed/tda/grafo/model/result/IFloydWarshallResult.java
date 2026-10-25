package ucu.edu.aed.tda.grafo.model.result;

import java.util.List;

/**
 * Interfaz destinada para modelar el resultado de ejecutar floyd y warshall
 */
public interface IFloydWarshallResult<V> {


    /**
     * Retorna el camino incluyendo origen y destino, o una lista vacia si no
     * existe. Desde un vertice a si mismo retorna una lista con ese vertice.
     * Lanza IllegalArgumentException si algun vertice no pertenece al resultado.
     */
    List<V> getPath(V source, V target);

    /**
     * Retorna el costo minimo, cero para el mismo vertice o infinito positivo
     * si no hay camino. Lanza IllegalArgumentException para vertices inexistentes.
     */
    double getCost(V source, V target);

    /**
     * Retorna true si hay un camino dirigido (incluido el camino de longitud cero).
     * Lanza IllegalArgumentException para vertices inexistentes.
     */
    boolean connected(V source, V target);
}
