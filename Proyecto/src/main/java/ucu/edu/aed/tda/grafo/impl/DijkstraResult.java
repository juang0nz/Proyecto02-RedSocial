package ucu.edu.aed.tda.grafo.impl;

import ucu.edu.aed.tda.grafo.model.result.IDijkstraResult;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Guarda los costos y predecesores calculados desde un único origen.
public final class DijkstraResult<V> implements IDijkstraResult<V> {

    private final V origen;
    private final Map<V, Double> costos;
    private final Map<V, V> predecesores;

    // Copia los mapas para que cambios externos no alteren el resultado.
    DijkstraResult(V origen, Map<V, Double> costos, Map<V, V> predecesores) {
        this.origen = origen;
        this.costos = Collections.unmodifiableMap(new LinkedHashMap<>(costos));
        this.predecesores = Collections.unmodifiableMap(new LinkedHashMap<>(predecesores));
    }

    // Devuelve el costo en O(1) esperado, o infinito si el vértice es inalcanzable.
    // Un vértice que no forma parte del resultado es un error.
    @Override
    public double getCost(V otherVertex) {
        Double costo = costos.get(otherVertex);
        if (costo == null) {
            throw new IllegalArgumentException("El vertice no pertenece al resultado: " + otherVertex);
        }
        return costo;
    }

    // Valida el destino y devuelve el camino desde el origen, o una lista vacía.
    @Override
    public List<V> getPath(V otherVertex) {
        getCost(otherVertex);
        return reconstruirCamino(origen, otherVertex, predecesores);
    }

    @Override
    public Map<V, V> getPredecessors() {
        return predecesores;
    }

    // Retrocede desde el destino siguiendo los predecesores.
    // Devuelve una lista vacía si no llega al origen y rechaza cadenas con ciclos.
    // Cuesta O(L) en tiempo y espacio, siendo L la cantidad de vértices de la cadena.
    public static <V> List<V> reconstruirCamino(V origen, V destino, Map<V, V> predecesores) {
        if (origen == null || destino == null || predecesores == null) {
            throw new IllegalArgumentException("Origen, destino y predecesores son obligatorios");
        }
        LinkedList<V> camino = new LinkedList<>();
        Set<V> visitados = new HashSet<>();
        V actual = destino;
        while (actual != null) {
            if (!visitados.add(actual)) {
                throw new IllegalArgumentException("El vector de predecesores contiene un ciclo");
            }
            // Agregar al principio deja el camino en orden de origen a destino.
            camino.addFirst(actual);
            if (actual.equals(origen)) {
                return List.copyOf(camino);
            }
            actual = predecesores.get(actual);
        }
        return List.of();
    }
}
