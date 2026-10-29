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

// Resuelve distancias y sugerencias sin depender de la clase Usuario ni de los pesos.
// Cada arista A -> B indica que A sigue a B.
public final class SocialGraphQueries {
    private SocialGraphQueries() {
    }

    // Busca la menor cantidad de saltos desde source hasta target, respetando la dirección.
    // Devuelve -1 si no hay camino. Con DirectedGraph cuesta O(V² + E) en el peor caso,
    // porque cada consulta de adyacencias vuelve a buscar el vértice en O(V).
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
        // El mapa guarda la distancia y también sirve para saber si ya lo descubrimos.
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
                        // BFS explora por niveles, así que la primera llegada usa menos saltos.
                        return nextDistance;
                    }
                    distances.put(neighbor, nextDistance);
                    queue.addLast(neighbor);
                }
            }
        }
        return -1;
    }

    // Sugiere a quienes siguen los seguidos del usuario, sin incluirlo ni repetir sus seguidos.
    // Primero van los que aparecen a través de más seguidos; los empates usan el registro.
    // Con DirectedGraph, el peor caso es O(V² + E + S log S), con S sugerencias.
    public static <V, D> List<V> sugerencias(IDirectedIGraph<V, D> graph,
                                               Comparable<V> userCriteria) {
        V usuario = graph.buscarVertice(userCriteria);
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no pertenece al grafo");
        }
        Set<V> seguidos = graph.successors(graph.construirComparable(usuario));
        // Cada candidato suma una vez por cada seguido que apunta a él.
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
