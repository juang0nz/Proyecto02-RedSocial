package ucu.edu.aed.tda.grafo.impl;

import ucu.edu.aed.tda.grafo.IDirectedIGraph;
import ucu.edu.aed.tda.grafo.model.edge.Edge;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Consultas sobre relaciones dirigidas: A -> B significa que A sigue a B.
 * No depende del modelo de usuario ni de los pesos de las aristas.
 */
public final class SocialGraphQueries {
    private SocialGraphQueries() {
    }

    /**
     * Cantidad minima de seguimientos entre origen y destino.
     * Retorna -1 cuando no existe una ruta dirigida. O(V + E).
     */
    public static <V, D> int gradosDeSeparacion(IDirectedIGraph<V, D> graph,
                                                  Comparable<V> source, Comparable<V> target) {
        V origen = graph.buscarVertice(source);
        V destino = graph.buscarVertice(target);
        if (origen == null || destino == null) {
            throw new IllegalArgumentException("Los usuarios deben pertenecer al grafo");
        }
        if (origen.equals(destino)) {
            return 0;
        }
        Deque<V> queue = new ArrayDeque<>();
        Map<V, Integer> distances = new HashMap<>();
        queue.addLast(origen);
        distances.put(origen, 0);
        while (!queue.isEmpty()) {
            V current = queue.removeFirst();
            int nextDistance = distances.get(current) + 1;
            for (Edge<V, D> edge : graph.adyacencias(graph.construirComparable(current))) {
                V neighbor = edge.target();
                if (!distances.containsKey(neighbor)) {
                    if (neighbor.equals(destino)) {
                        return nextDistance;
                    }
                    distances.put(neighbor, nextDistance);
                    queue.addLast(neighbor);
                }
            }
        }
        return -1;
    }

    /**
     * Sugiere seguidos de seguidos no seguidos directamente.
     * Ordena por cantidad de conexiones compartidas descendente;
     * los empates siguen el orden de insercion de vertices del grafo.
     * O(V + E) en la exploracion local, mas O(S log S) al ordenar sugerencias.
     */
    public static <V, D> List<V> sugerencias(IDirectedIGraph<V, D> graph,
                                               Comparable<V> userCriteria) {
        V usuario = graph.buscarVertice(userCriteria);
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no pertenece al grafo");
        }
        Set<V> seguidos = graph.successors(graph.construirComparable(usuario));
        Map<V, Integer> comunes = new LinkedHashMap<>();
        for (V seguido : seguidos) {
            for (V candidato : graph.successors(graph.construirComparable(seguido))) {
                if (!candidato.equals(usuario) && !seguidos.contains(candidato)) {
                    comunes.merge(candidato, 1, Integer::sum);
                }
            }
        }
        Map<V, Integer> ordenVertices = new HashMap<>();
        int index = 0;
        for (V vertex : graph.vertices()) {
            ordenVertices.put(vertex, index++);
        }
        List<V> result = new ArrayList<>(comunes.keySet());
        result.sort(Comparator.<V>comparingInt(v -> comunes.get(v)).reversed()
                .thenComparingInt(ordenVertices::get));
        return List.copyOf(result);
    }
}
