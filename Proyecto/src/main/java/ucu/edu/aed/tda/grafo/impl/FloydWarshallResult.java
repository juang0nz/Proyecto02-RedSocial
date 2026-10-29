package ucu.edu.aed.tda.grafo.impl;

import ucu.edu.aed.tda.grafo.model.result.IFloydWarshallResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Permite consultar las matrices de costos y caminos que dejan Floyd y Warshall.
final class FloydWarshallResult<V> implements IFloydWarshallResult<V> {

    private final List<V> vertices;
    private final Map<V, Integer> indices = new HashMap<>();
    private final double[][] costos;
    private final int[][] siguientes;

    // Guarda las matrices recibidas y asocia cada vértice con su fila y columna.
    FloydWarshallResult(List<V> vertices, double[][] costos, int[][] siguientes) {
        this.vertices = List.copyOf(vertices);
        this.costos = costos;
        this.siguientes = siguientes;
        for (int i = 0; i < vertices.size(); i++) {
            indices.put(vertices.get(i), i);
        }
    }

    // Traduce el vértice a su posición y rechaza los que no estaban en el cálculo.
    private int indice(V vertice) {
        Integer indice = indices.get(vertice);
        if (indice == null) {
            throw new IllegalArgumentException("El vertice no pertenece al resultado: " + vertice);
        }
        return indice;
    }

    // Sigue los próximos saltos hasta el destino. Si no hay ruta, devuelve lista vacía.
    // Para un camino de L vértices, cuesta O(L) en tiempo y espacio.
    @Override
    public List<V> getPath(V source, V target) {
        int origen = indice(source);
        int destino = indice(target);
        if (siguientes[origen][destino] == -1) {
            return List.of();
        }
        List<V> camino = new ArrayList<>();
        camino.add(vertices.get(origen));
        // Cada celda dice a qué vértice ir primero para acercarse al destino.
        while (origen != destino) {
            origen = siguientes[origen][destino];
            camino.add(vertices.get(origen));
        }
        return List.copyOf(camino);
    }

    // Consulta la matriz en O(1) esperado; infinito significa que no hay camino.
    @Override
    public double getCost(V source, V target) {
        return costos[indice(source)][indice(target)];
    }

    // Un próximo salto distinto de -1 indica que existe un camino.
    @Override
    public boolean connected(V source, V target) {
        return siguientes[indice(source)][indice(target)] != -1;
    }
}
