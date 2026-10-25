package ucu.edu.aed.tda.grafo.impl;

import ucu.edu.aed.tda.grafo.model.result.IFloydWarshallResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class FloydWarshallResult<V> implements IFloydWarshallResult<V> {

    private final List<V> vertices;
    private final Map<V, Integer> indices = new HashMap<>();
    private final double[][] costos;
    private final int[][] siguientes;

    FloydWarshallResult(List<V> vertices, double[][] costos, int[][] siguientes) {
        this.vertices = List.copyOf(vertices);
        this.costos = costos;
        this.siguientes = siguientes;
        for (int i = 0; i < vertices.size(); i++) {
            indices.put(vertices.get(i), i);
        }
    }

    private int indice(V vertice) {
        Integer indice = indices.get(vertice);
        if (indice == null) {
            throw new IllegalArgumentException("El vertice no pertenece al resultado: " + vertice);
        }
        return indice;
    }

    @Override
    public List<V> getPath(V source, V target) {
        int origen = indice(source);
        int destino = indice(target);
        if (siguientes[origen][destino] == -1) {
            return List.of();
        }
        List<V> camino = new ArrayList<>();
        camino.add(vertices.get(origen));
        while (origen != destino) {
            origen = siguientes[origen][destino];
            camino.add(vertices.get(origen));
        }
        return List.copyOf(camino);
    }

    @Override
    public double getCost(V source, V target) {
        return costos[indice(source)][indice(target)];
    }

    @Override
    public boolean connected(V source, V target) {
        return siguientes[indice(source)][indice(target)] != -1;
    }
}
