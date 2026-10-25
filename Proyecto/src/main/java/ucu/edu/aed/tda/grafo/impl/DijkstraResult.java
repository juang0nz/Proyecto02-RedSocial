package ucu.edu.aed.tda.grafo.impl;

import ucu.edu.aed.tda.grafo.model.result.IDijkstraResult;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class DijkstraResult<V> implements IDijkstraResult<V> {

    private final V origen;
    private final Map<V, Double> costos;
    private final Map<V, V> predecesores;

    DijkstraResult(V origen, Map<V, Double> costos, Map<V, V> predecesores) {
        this.origen = origen;
        this.costos = Collections.unmodifiableMap(new LinkedHashMap<>(costos));
        this.predecesores = Collections.unmodifiableMap(new LinkedHashMap<>(predecesores));
    }

    @Override
    public double getCost(V otherVertex) {
        Double costo = costos.get(otherVertex);
        if (costo == null) {
            throw new IllegalArgumentException("El vertice no pertenece al resultado: " + otherVertex);
        }
        return costo;
    }

    @Override
    public List<V> getPath(V otherVertex) {
        getCost(otherVertex);
        return reconstruirCamino(origen, otherVertex, predecesores);
    }

    @Override
    public Map<V, V> getPredecessors() {
        return predecesores;
    }

    /**
     * Sigue los predecesores desde el destino hasta el origen e invierte el orden.
     * Devuelve una lista vacia si no hay camino y rechaza ciclos en el vector.
     */
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
            camino.addFirst(actual);
            if (actual.equals(origen)) {
                return List.copyOf(camino);
            }
            actual = predecesores.get(actual);
        }
        return List.of();
    }
}
