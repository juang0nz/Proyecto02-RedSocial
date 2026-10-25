package ucu.edu.aed.tda.grafo.impl;

import ucu.edu.aed.tda.grafo.IDirectedIGraph;
import ucu.edu.aed.tda.grafo.model.edge.DirectedEdge;
import ucu.edu.aed.tda.grafo.model.edge.Edge;

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

public class DirectedGraph<V, D> implements IDirectedIGraph<V, D> {

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
        List<Edge<V, D>> salientes = adyacencias.get(source);
        for (Edge<V, D> e : salientes) {
            if (e.target().equals(target)) {
                return false;
            }
        }
        salientes.add(new DirectedEdge<>(source, target, dato));
        return true;
    }

    @Override
    public boolean eliminarArista(Comparable<V> source, Comparable<V> target) {
        V origen = buscarVertice(source);
        V destino = buscarVertice(target);
        if (origen == null || destino == null) {
            return false;
        }
        return adyacencias.get(origen).removeIf(e -> e.target().equals(destino));
    }

    @Override
    public boolean removerVertice(Comparable<V> criteria) {
        V vertice = buscarVertice(criteria);
        if (vertice == null) {
            return false;
        }
        adyacencias.remove(vertice);
        for (List<Edge<V, D>> salientes : adyacencias.values()) {
            salientes.removeIf(e -> e.target().equals(vertice));
        }
        return true;
    }

    @Override
    public Set<V> vertices() {
        return Collections.unmodifiableSet(adyacencias.keySet());
    }

    @Override
    public Set<Edge<V, D>> aristas() {
        Set<Edge<V, D>> todas = new LinkedHashSet<>();
        for (List<Edge<V, D>> salientes : adyacencias.values()) {
            todas.addAll(salientes);
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

    @Override
    public Set<V> successors(Comparable<V> criteria) {
        Set<V> sucesores = new LinkedHashSet<>();
        for (Edge<V, D> e : adyacencias(criteria)) {
            sucesores.add(e.target());
        }
        return sucesores;
    }

    @Override
    public Set<V> predecessors(Comparable<V> criteria) {
        V vertice = buscarVertice(criteria);
        Set<V> predecesores = new LinkedHashSet<>();
        if (vertice == null) {
            return predecesores;
        }
        for (Map.Entry<V, List<Edge<V, D>>> entrada : adyacencias.entrySet()) {
            for (Edge<V, D> e : entrada.getValue()) {
                if (e.target().equals(vertice)) {
                    predecesores.add(entrada.getKey());
                }
            }
        }
        return predecesores;
    }

    @Override
    public boolean esConexo() {
        if (adyacencias.isEmpty()) {
            return true;
        }
        V inicio = adyacencias.keySet().iterator().next();
        Set<V> visitados = new HashSet<>();
        Deque<V> pendientes = new ArrayDeque<>();
        pendientes.add(inicio);
        visitados.add(inicio);
        while (!pendientes.isEmpty()) {
            V actual = pendientes.poll();
            Comparable<V> criterio = construirComparable(actual);
            Set<V> vecinos = new HashSet<>(successors(criterio));
            vecinos.addAll(predecessors(criterio));
            for (V vecino : vecinos) {
                if (visitados.add(vecino)) {
                    pendientes.add(vecino);
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
        Set<V> enCamino = new HashSet<>();
        for (V vertice : adyacencias.keySet()) {
            if (tieneCiclosAux(vertice, visitados, enCamino)) {
                return true;
            }
        }
        return false;
    }

    private boolean tieneCiclosAux(V nodo, Set<V> visitados, Set<V> enCamino) {
        if (enCamino.contains(nodo)) {
            return true;
        }
        if (visitados.contains(nodo)) {
            return false;
        }
        visitados.add(nodo);
        enCamino.add(nodo);
        for (Edge<V, D> arista : adyacencias.get(nodo)) {
            if (tieneCiclosAux(arista.target(), visitados, enCamino)) {
                return true;
            }
        }
        enCamino.remove(nodo);
        return false;
    }
}