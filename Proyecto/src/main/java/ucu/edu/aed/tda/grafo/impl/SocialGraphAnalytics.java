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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SocialGraphAnalytics {

    private SocialGraphAnalytics() {
    }

    public static <V, D> Map<V, Integer> cantidadDeSeguidores(IDirectedIGraph<V, D> graph) {
        Map<V, Integer> seguidores = new LinkedHashMap<>();
        for (V v : graph.vertices()) {
            seguidores.put(v, 0);
        }
        for (Edge<V, D> e : graph.aristas()) {
            seguidores.merge(e.target(), 1, Integer::sum);
        }
        return seguidores;
    }

    public static <V, D> List<V> rankingPorSeguidores(IDirectedIGraph<V, D> graph) {
        return ordenarDescendente(graph, cantidadDeSeguidores(graph));
    }

    public static <V, D> int alcance(IDirectedIGraph<V, D> graph, Comparable<V> criterio) {
        V usuario = graph.buscarVertice(criterio);
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no pertenece al grafo");
        }
        return alcanceDesde(usuario, listaDeSeguidores(graph));
    }

    public static <V, D> Map<V, Integer> alcanceDeTodos(IDirectedIGraph<V, D> graph) {
        Map<V, List<V>> seguidoresDe = listaDeSeguidores(graph);
        Map<V, Integer> alcances = new LinkedHashMap<>();
        for (V v : graph.vertices()) {
            alcances.put(v, alcanceDesde(v, seguidoresDe));
        }
        return alcances;
    }

    public static <V, D> List<V> rankingPorAlcance(IDirectedIGraph<V, D> graph) {
        return ordenarDescendente(graph, alcanceDeTodos(graph));
    }

    public static <V, D> List<Set<V>> comunidades(IDirectedIGraph<V, D> graph) {
        Map<V, List<V>> seguidos = listaDeSeguidos(graph);
        Map<V, List<V>> seguidoresDe = listaDeSeguidores(graph);

        Set<V> visitados = new HashSet<>();
        Deque<V> ordenDeFinalizacion = new ArrayDeque<>();
        for (V v : graph.vertices()) {
            if (!visitados.contains(v)) {
                dfsOrden(v, seguidos, visitados, ordenDeFinalizacion);
            }
        }

        visitados.clear();
        List<Set<V>> comunidades = new ArrayList<>();
        while (!ordenDeFinalizacion.isEmpty()) {
            V v = ordenDeFinalizacion.pop();
            if (!visitados.contains(v)) {
                Set<V> comunidad = new LinkedHashSet<>();
                dfsComunidad(v, seguidoresDe, visitados, comunidad);
                comunidades.add(comunidad);
            }
        }
        comunidades.sort(Comparator.comparingInt((Set<V> c) -> c.size()).reversed());
        return comunidades;
    }

    public static <V, D> Set<V> usuariosPuente(IDirectedIGraph<V, D> graph) {
        UndirectedGraphAlgorithms algoritmos = new UndirectedGraphAlgorithms();
        return algoritmos.puntosDeArticulacion(algoritmos.proyeccionNoDirigida(graph));
    }

    private static <V> void dfsOrden(V v, Map<V, List<V>> seguidos, Set<V> visitados, Deque<V> orden) {
        visitados.add(v);
        for (V w : seguidos.get(v)) {
            if (!visitados.contains(w)) {
                dfsOrden(w, seguidos, visitados, orden);
            }
        }
        orden.push(v);
    }

    private static <V> void dfsComunidad(V v, Map<V, List<V>> seguidoresDe, Set<V> visitados, Set<V> comunidad) {
        visitados.add(v);
        comunidad.add(v);
        for (V w : seguidoresDe.get(v)) {
            if (!visitados.contains(w)) {
                dfsComunidad(w, seguidoresDe, visitados, comunidad);
            }
        }
    }

    private static <V> int alcanceDesde(V usuario, Map<V, List<V>> seguidoresDe) {
        Set<V> alcanzados = new HashSet<>();
        Deque<V> cola = new ArrayDeque<>();
        alcanzados.add(usuario);
        cola.add(usuario);
        while (!cola.isEmpty()) {
            V actual = cola.poll();
            for (V seguidor : seguidoresDe.get(actual)) {
                if (alcanzados.add(seguidor)) {
                    cola.add(seguidor);
                }
            }
        }
        return alcanzados.size() - 1;
    }

    private static <V, D> Map<V, List<V>> listaDeSeguidos(IDirectedIGraph<V, D> graph) {
        Map<V, List<V>> seguidos = new HashMap<>();
        for (V v : graph.vertices()) {
            seguidos.put(v, new ArrayList<>());
        }
        for (Edge<V, D> e : graph.aristas()) {
            seguidos.get(e.source()).add(e.target());
        }
        return seguidos;
    }

    private static <V, D> Map<V, List<V>> listaDeSeguidores(IDirectedIGraph<V, D> graph) {
        Map<V, List<V>> seguidores = new HashMap<>();
        for (V v : graph.vertices()) {
            seguidores.put(v, new ArrayList<>());
        }
        for (Edge<V, D> e : graph.aristas()) {
            seguidores.get(e.target()).add(e.source());
        }
        return seguidores;
    }

    private static <V, D> List<V> ordenarDescendente(IDirectedIGraph<V, D> graph, Map<V, Integer> valores) {
        Map<V, Integer> ordenInsercion = new HashMap<>();
        int i = 0;
        for (V v : graph.vertices()) {
            ordenInsercion.put(v, i++);
        }
        List<V> ranking = new ArrayList<>(graph.vertices());
        ranking.sort(Comparator.<V>comparingInt(valores::get).reversed()
                .thenComparingInt(ordenInsercion::get));
        return ranking;
    }
}