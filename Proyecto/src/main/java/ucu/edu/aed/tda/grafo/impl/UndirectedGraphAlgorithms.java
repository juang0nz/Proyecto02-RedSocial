package ucu.edu.aed.tda.grafo.impl;

import ucu.edu.aed.tda.grafo.IDirectedIGraph;
import ucu.edu.aed.tda.grafo.IUndirectedGraph;
import ucu.edu.aed.tda.grafo.IUndirectedGraphAlgorithm;
import ucu.edu.aed.tda.grafo.model.edge.Edge;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class UndirectedGraphAlgorithms implements IUndirectedGraphAlgorithm {

    @Override
    public <V, D extends WeightedEdge> IUndirectedGraph<V, D> prim(IUndirectedGraph<V, D> graph, Comparable<V> source) {
        UndirectedGraph<V, D> arbol = new UndirectedGraph<>();
        V inicio = graph.buscarVertice(source);
        if (inicio == null) {
            return arbol;
        }
        arbol.agregarVertices(graph.vertices());

        Set<V> visitados = new LinkedHashSet<>();
        Set<V> pendientes = new LinkedHashSet<>(graph.vertices());
        visitados.add(inicio);
        pendientes.remove(inicio);

        while (!pendientes.isEmpty()) {
            Edge<V, D> minima = searchMinEdge(graph, visitados, pendientes);
            V nuevo;
            if (minima == null) {
                nuevo = pendientes.iterator().next();
            } else {
                arbol.agregarArista(minima.source(), minima.target(), minima.dato());
                nuevo = minima.target();
            }
            visitados.add(nuevo);
            pendientes.remove(nuevo);
        }
        return arbol;
    }

    @Override
    public <V, D extends WeightedEdge> Edge<V, D> searchMinEdge(IUndirectedGraph<V, D> graph, Collection<V> conjuntoU, Collection<V> conjuntoV) {
        Edge<V, D> minima = null;
        for (V u : conjuntoU) {
            for (Edge<V, D> e : graph.adyacencias(graph.construirComparable(u))) {
                if (conjuntoV.contains(e.target())
                        && (minima == null || e.dato().getWeight() < minima.dato().getWeight())) {
                    minima = e;
                }
            }
        }
        return minima;
    }

    @Override
    public <V, D extends WeightedEdge> IUndirectedGraph<V, D> kruskal(IUndirectedGraph<V, D> graph) {
        UndirectedGraph<V, D> arbol = new UndirectedGraph<>();
        arbol.agregarVertices(graph.vertices());

        List<Edge<V, D>> aristas = new ArrayList<>(graph.aristas());
        aristas.sort(Comparator.comparingDouble(e -> e.dato().getWeight()));

        Map<V, V> padre = new HashMap<>();
        for (V v : graph.vertices()) {
            padre.put(v, v);
        }

        int necesarias = graph.cantidadDeVertices() - 1;
        int agregadas = 0;
        for (Edge<V, D> e : aristas) {
            if (agregadas == necesarias) {
                break;
            }
            V raizA = buscarRaiz(padre, e.source());
            V raizB = buscarRaiz(padre, e.target());
            if (!raizA.equals(raizB)) {
                padre.put(raizA, raizB);
                arbol.agregarArista(e.source(), e.target(), e.dato());
                agregadas++;
            }
        }
        return arbol;
    }

    private <V> V buscarRaiz(Map<V, V> padre, V v) {
        V raiz = v;
        while (!padre.get(raiz).equals(raiz)) {
            raiz = padre.get(raiz);
        }
        while (!v.equals(raiz)) {
            V siguiente = padre.get(v);
            padre.put(v, raiz);
            v = siguiente;
        }
        return raiz;
    }

    @Override
    public <V, D> void bea(IUndirectedGraph<V, D> graph, Consumer<V> consumer) {
        Set<V> visitados = new HashSet<>();
        for (V inicio : graph.vertices()) {
            if (visitados.contains(inicio)) {
                continue;
            }
            Deque<V> cola = new ArrayDeque<>();
            visitados.add(inicio);
            cola.add(inicio);
            while (!cola.isEmpty()) {
                V actual = cola.poll();
                consumer.accept(actual);
                for (Edge<V, D> e : graph.adyacencias(graph.construirComparable(actual))) {
                    if (visitados.add(e.target())) {
                        cola.add(e.target());
                    }
                }
            }
        }
    }

    public <V, D> Set<V> puntosDeArticulacion(IUndirectedGraph<V, D> graph) {
        Set<V> articulaciones = new LinkedHashSet<>();
        Map<V, Integer> descubrimiento = new HashMap<>();
        Map<V, Integer> low = new HashMap<>();
        int[] contador = {0};
        for (V raiz : graph.vertices()) {
            if (!descubrimiento.containsKey(raiz)) {
                articulacionesAux(graph, raiz, null, descubrimiento, low, contador, articulaciones);
            }
        }
        return articulaciones;
    }

    private <V, D> void articulacionesAux(IUndirectedGraph<V, D> graph, V u, V padre,
                                          Map<V, Integer> descubrimiento, Map<V, Integer> low,
                                          int[] contador, Set<V> articulaciones) {
        contador[0]++;
        descubrimiento.put(u, contador[0]);
        low.put(u, contador[0]);
        int hijos = 0;

        for (Edge<V, D> e : graph.adyacencias(graph.construirComparable(u))) {
            V v = e.target();
            if (!descubrimiento.containsKey(v)) {
                hijos++;
                articulacionesAux(graph, v, u, descubrimiento, low, contador, articulaciones);
                low.put(u, Math.min(low.get(u), low.get(v)));
                if (padre != null && low.get(v) >= descubrimiento.get(u)) {
                    articulaciones.add(u);
                }
            } else if (!v.equals(padre)) {
                low.put(u, Math.min(low.get(u), descubrimiento.get(v)));
            }
        }

        if (padre == null && hijos >= 2) {
            articulaciones.add(u);
        }
    }

    public <V, D> UndirectedGraph<V, D> proyeccionNoDirigida(IDirectedIGraph<V, D> dirigido) {
        UndirectedGraph<V, D> proyeccion = new UndirectedGraph<>();
        proyeccion.agregarVertices(dirigido.vertices());
        for (Edge<V, D> e : dirigido.aristas()) {
            proyeccion.agregarArista(e.source(), e.target(), e.dato());
        }
        return proyeccion;
    }
}