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

// Guarda un grafo sin dirección, con una lista de vecinos para cada vértice.
// Implementa IUndirectedGraph y las operaciones comunes que solicita el primer desafío.
// En los costos, V es la cantidad de vértices y E la de conexiones.
public class UndirectedGraph<V, D> implements IUndirectedGraph<V, D> {

    // El mapa conserva el orden de inserción de los vértices.
    private final Map<V, List<Edge<V, D>>> adyacencias = new LinkedHashMap<>();

    // Agrega el vértice si no es nulo ni está repetido. Costo esperado O(1).
    @Override
    public boolean agregarVertice(V vertex) {
        if (vertex == null || adyacencias.containsKey(vertex)) {
            return false;
        }
        adyacencias.put(vertex, new ArrayList<>());
        return true;
    }

    // Compara con cada vértice y devuelve null si no hay coincidencia. O(V).
    @Override
    public V buscarVertice(Comparable<V> criterio) {
        for (V v : adyacencias.keySet()) {
            if (criterio.compareTo(v) == 0) {
                return v;
            }
        }
        return null;
    }

    // Conecta dos vértices existentes. No permite lazos ni conexiones repetidas.
    // Revisar si ya existe cuesta O(grado del origen).
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
        // Guardamos ambos sentidos para poder recorrer la conexión desde cualquier extremo.
        adyacencias.get(source).add(new UndirectedEdge<>(source, target, dato));
        adyacencias.get(target).add(new UndirectedEdge<>(target, source, dato));
        return true;
    }

    // Borra la conexión de las dos listas y devuelve si existía.
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

    // Antes de quitar el vértice, borra las referencias que tienen sus vecinos.
    // Devuelve false si no existe. En el peor caso cuesta O(V + E).
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

    // Vista de solo lectura que refleja las altas y bajas del grafo.
    @Override
    public Set<V> vertices() {
        return Collections.unmodifiableSet(adyacencias.keySet());
    }

    // Reúne las aristas en O(V + E). El conjunto usa equals de UndirectedEdge
    // para no contar dos veces la misma conexión guardada en sentidos opuestos.
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

    // Busca entre los vecinos del origen; devuelve null si no hay conexión.
    // Costo O(V + grado del origen), por la búsqueda inicial del vértice.
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

    // Busca en O(V) y devuelve la lista sin permitir modificarla desde afuera.
    @Override
    public List<Edge<V, D>> adyacencias(Comparable<V> verticeCriteria) {
        V vertice = buscarVertice(verticeCriteria);
        if (vertice == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(adyacencias.get(vertice));
    }

    // Extrae los destinos de las aristas del vértice en un conjunto nuevo.
    public Set<V> vecinos(Comparable<V> criteria) {
        Set<V> vecinos = new LinkedHashSet<>();
        for (Edge<V, D> e : adyacencias(criteria)) {
            vecinos.add(e.target());
        }
        return vecinos;
    }

    // Hace BFS desde un vértice y comprueba si llegó a todos. O(V + E).
    // El grafo vacío también se considera conexo.
    // Un vértice solo da true; un aislado junto a otros vértices impide llegar a todos.
    @Override
    public boolean esConexo() {
        if (adyacencias.isEmpty()) {
            return true;
        }
        V inicio = adyacencias.keySet().iterator().next();
        Set<V> visitados = new HashSet<>();
        // La cola guarda los pendientes y el conjunto evita repetir visitas.
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

    // Revisa con DFS todas las componentes hasta encontrar un ciclo. O(V + E).
    // Para el caso sin ciclos, una sola conexión de ida y vuelta no debe contarse dos veces.
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

    // Recibe también el padre para distinguir un ciclo de la vuelta por la misma arista.
    private boolean tieneCiclosAux(V actual, V padre, Set<V> visitados) {
        visitados.add(actual);
        for (Edge<V, D> e : adyacencias.get(actual)) {
            V vecino = e.target();
            if (!visitados.contains(vecino)) {
                if (tieneCiclosAux(vecino, actual, visitados)) {
                    return true;
                }
            } else if (!vecino.equals(padre)) {
                // Un vecino ya visitado que no es el padre indica otro camino hasta él.
                return true;
            }
        }
        return false;
    }
}