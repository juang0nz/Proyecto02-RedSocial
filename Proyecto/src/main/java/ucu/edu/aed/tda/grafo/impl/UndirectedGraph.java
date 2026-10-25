package ucu.edu.aed.tda.grafo.impl;

import ucu.edu.aed.tda.grafo.IUndirectedGraph;
import ucu.edu.aed.tda.grafo.model.edge.Edge;
import ucu.edu.aed.tda.grafo.model.edge.UndirectedEdge;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UndirectedGraph<V, D> implements IUndirectedGraph<V, D> {

    private final Map<V, List<Edge<V, D>>> adyacencias = new LinkedHashMap<>();

    @Override
    public boolean agregarVertice(V vertex) {
        if (vertex == null || adyacencias.containsKey(vertex)) {
            return false;
        }
        adyacencias.put(vertex, new ArrayList<>());
        return true;
    }

    @Override
    public V buscarVertice(Comparable<V> criterio) {
        for (V v : adyacencias.keySet()) {
            if (criterio.compareTo(v) == 0) {
                return v;
            }
        }
        return null;
    }

    @Override
    public boolean agregarArista(V source, V target, D dato) {
        if (!adyacencias.containsKey(source) || !adyacencias.containsKey(target)) {
            return false;
        }
        if (source.equals(target)) {
            return false;
        }
        for (Edge<V, D> e : adyacencias.get(source)) {
            if (e.target().equals(target)) {
                return false;
            }
        }
        adyacencias.get(source).add(new UndirectedEdge<>(source, target, dato));
        adyacencias.get(target).add(new UndirectedEdge<>(target, source, dato));
        return true;
    }

    @Override
    public boolean eliminarArista(Comparable<V> source, Comparable<V> target) {
        V a = buscarVertice(source);
        V b = buscarVertice(target);
        if (a == null || b == null) {
            return false;
        }
        boolean borrada = adyacencias.get(a).removeIf(e -> e.target().equals(b));
        adyacencias.get(b).removeIf(e -> e.target().equals(a));
        return borrada;
    }

    @Override
    public boolean removerVertice(Comparable<V> criteria) {
        V vertice = buscarVertice(criteria);
        if (vertice == null) {
            return false;
        }
        for (Edge<V, D> e : adyacencias.get(vertice)) {
            adyacencias.get(e.target()).removeIf(x -> x.target().equals(vertice));
        }
        adyacencias.remove(vertice);
        return true;
    }

    @Override
    public Set<V> vertices() {
        return Collections.unmodifiableSet(adyacencias.keySet());
    }

    @Override
    public Set<Edge<V, D>> aristas() {
        Set<Edge<V, D>> todas = new LinkedHashSet<>();
        for (List<Edge<V, D>> lista : adyacencias.values()) {
            todas.addAll(lista);
        }
        return Collections.unmodifiableSet(todas);
    }

    @Override
    public boolean existeArista(Comparable<V> sourceCriteria, Comparable<V> targetCriteria) {
        return obtenerArista(sourceCriteria, targetCriteria) != null;
    }

    @Override
    public Edge<V, D> obtenerArista(Comparable<V> sourceCriteria, Comparable<V> targetCriteria) {
        V origen = buscarVertice(sourceCriteria);
        if (origen == null) {
            return null;
        }
        for (Edge<V, D> e : adyacencias.get(origen)) {
            if (targetCriteria.compareTo(e.target()) == 0) {
                return e;
            }
        }
        return null;
    }

    @Override
    public List<Edge<V, D>> adyacencias(Comparable<V> verticeCriteria) {
        V vertice = buscarVertice(verticeCriteria);
        if (vertice == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(adyacencias.get(vertice));
    }

    public Set<V> vecinos(Comparable<V> criteria) {
        Set<V> vecinos = new LinkedHashSet<>();
        for (Edge<V, D> e : adyacencias(criteria)) {
            vecinos.add(e.target());
        }
        return vecinos;
    }

    @Override
    public boolean esConexo() {
        if (adyacencias.isEmpty()) {
            return true;
        }
        V inicio = adyacencias.keySet().iterator().next();
        Set<V> visitados = new HashSet<>();
        Deque<V> pendientes = new ArrayDeque<>();
        visitados.add(inicio);
        pendientes.add(inicio);
        while (!pendientes.isEmpty()) {
            V actual = pendientes.poll();
            for (Edge<V, D> e : adyacencias.get(actual)) {
                if (visitados.add(e.target())) {
                    pendientes.add(e.target());
                }
            }
        }
        return visitados.size() == adyacencias.size();
    }

    @Override
    public void vaciar() {
        adyacencias.clear();
    }

    @Override
    public boolean tieneCiclos() {
        Set<V> visitados = new HashSet<>();
        for (V vertice : adyacencias.keySet()) {
            if (!visitados.contains(vertice) && tieneCiclosAux(vertice, null, visitados)) {
                return true;
            }
        }
        return false;
    }

    private boolean tieneCiclosAux(V actual, V padre, Set<V> visitados) {
        visitados.add(actual);
        for (Edge<V, D> e : adyacencias.get(actual)) {
            V vecino = e.target();
            if (!visitados.contains(vecino)) {
                if (tieneCiclosAux(vecino, actual, visitados)) {
                    return true;
                }
            } else if (!vecino.equals(padre)) {
                return true;
            }
        }
        return false;
    }
}