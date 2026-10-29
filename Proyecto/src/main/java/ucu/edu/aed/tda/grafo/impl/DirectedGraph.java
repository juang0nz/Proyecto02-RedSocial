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

// Guarda un grafo dirigido con una lista de aristas salientes por vértice.
// Con esta clase resolvemos las operaciones de IGraph e IDirectedIGraph del Desafío 1.
// En los costos, V es la cantidad de vértices y E la de aristas.
public class DirectedGraph<V, D> implements IDirectedIGraph<V, D> {

    // LinkedHashMap mantiene el orden en que se agregaron los vértices.
    private final Map<V, List<Edge<V, D>>> adyacencias = new LinkedHashMap<>();

    // Devuelve false si el vértice es nulo o ya estaba. Costo esperado O(1).
    @Override
    public boolean agregarVertice(V vertex) {
        if (vertex == null || adyacencias.containsKey(vertex)) {
            return false;
        }
        adyacencias.put(vertex, new ArrayList<>());
        return true;
    }

    // Busca por criterio, no por la clave del mapa. Por eso cuesta O(V).
    // Devuelve null si ningún vértice coincide.
    @Override
    public V buscarVertice(Comparable<V> criterio) {
        for (V v : adyacencias.keySet()) {
            if (criterio.compareTo(v) == 0) {
                return v;
            }
        }
        return null;
    }

    // Agrega source -> target si ambos existen y esa conexión no estaba.
    // A diferencia del grafo no dirigido, acá se permite una arista del vértice a sí mismo.
    // Revisar duplicados cuesta O(grado de salida del origen).
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

    // Busca ambos extremos y devuelve si quitó la arista. Costo O(V + grado de salida).
    @Override
    public boolean eliminarArista(Comparable<V> source, Comparable<V> target) {
        V origen = buscarVertice(source);
        V destino = buscarVertice(target);
        if (origen == null || destino == null) {
            return false;
        }
        return adyacencias.get(origen).removeIf(e -> e.target().equals(destino));
    }

    // Quita el vértice y también las aristas que llegan a él. Costo O(V + E).
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

    // Es una vista de las claves: no se puede modificar desde afuera,
    // pero refleja los cambios que se hagan en el grafo.
    @Override
    public Set<V> vertices() {
        return Collections.unmodifiableSet(adyacencias.keySet());
    }

    // Reúne las listas en un conjunto nuevo de solo lectura. Costo O(V + E).
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

    // Busca el origen y revisa sus salientes; devuelve null si no encuentra la arista.
    // Costo O(V + grado de salida del origen).
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

    // Busca el vértice en O(V) y devuelve sus salientes sin permitir cambios en la lista.
    // Si no existe, devuelve una lista vacía.
    @Override
    public List<Edge<V, D>> adyacencias(Comparable<V> verticeCriteria) {
        V vertice = buscarVertice(verticeCriteria);
        if (vertice == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(adyacencias.get(vertice));
    }

    // Los sucesores son los destinos de las aristas salientes.
    // Esta consulta de la consigna devuelve vecinos directos, no todos los alcanzables.
    @Override
    public Set<V> successors(Comparable<V> criteria) {
        Set<V> sucesores = new LinkedHashSet<>();
        for (Edge<V, D> e : adyacencias(criteria)) {
            sucesores.add(e.target());
        }
        return sucesores;
    }

    // Como solo guardamos salientes, hay que revisar todo para encontrar las entrantes.
    // Así resolvemos la consulta de predecesores sin guardar una segunda lista por vértice.
    // Devuelve los orígenes que apuntan al vértice. Costo O(V + E).
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

    // Comprueba conexión débil: ignora el sentido de las aristas.
    // Esta es la interpretación de conexidad implementada; no comprueba conexión fuerte.
    // Devuelve true si alcanza a todos, y también si el grafo está vacío.
    // Cuesta O(V * (V + E)), porque busca predecesores recorriendo todo en cada paso.
    @Override
    public boolean esConexo() {
        if (adyacencias.isEmpty()) {
            return true;
        }
        V inicio = adyacencias.keySet().iterator().next();
        // La cola recorre por niveles y visitados evita volver a encolar un vértice.
        Set<V> visitados = new HashSet<>();
        Deque<V> pendientes = new ArrayDeque<>();
        pendientes.add(inicio);
        visitados.add(inicio);
        while (!pendientes.isEmpty()) {
            V actual = pendientes.poll();
            Comparable<V> criterio = construirComparable(actual);
            // Juntamos los que salen y los que entran para ignorar la dirección.
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

    // Busca ciclos dirigidos con DFS, también en componentes separadas. O(V + E).
    // El grafo vacío y un vértice sin lazo dan false; un lazo sí cuenta como ciclo.
    @Override
    public boolean tieneCiclos() {
        Set<V> visitados = new HashSet<>();
        // No es lo mismo haber visitado un nodo que seguir dentro de su rama.
        Set<V> enCamino = new HashSet<>();
        for (V vertice : adyacencias.keySet()) {
            if (tieneCiclosAux(vertice, visitados, enCamino)) {
                return true;
            }
        }
        return false;
    }

    // Devuelve true si desde este nodo se vuelve a uno de la rama actual.
    private boolean tieneCiclosAux(V nodo, Set<V> visitados, Set<V> enCamino) {
        if (enCamino.contains(nodo)) {
            // Volver a un nodo que todavía no terminamos de explorar cierra un ciclo.
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
        // Al volver de la recursión, el nodo ya no pertenece al camino activo.
        enCamino.remove(nodo);
        return false;
    }
}