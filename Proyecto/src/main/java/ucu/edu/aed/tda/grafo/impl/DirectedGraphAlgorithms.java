package ucu.edu.aed.tda.grafo.impl;

import ucu.edu.aed.tda.grafo.IDirectedGraphAlgorithms;
import ucu.edu.aed.tda.grafo.IDirectedIGraph;
import ucu.edu.aed.tda.grafo.model.IGraph;
import ucu.edu.aed.tda.grafo.model.edge.Edge;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.tda.grafo.model.result.IDijkstraResult;
import ucu.edu.aed.tda.grafo.model.result.IFloydWarshallResult;
import ucu.edu.aed.tda.grafo.model.result.Path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class DirectedGraphAlgorithms implements IDirectedGraphAlgorithms {

    @Override
    public <V, D extends WeightedEdge> List<Path<V>> obtenerTodosLosCaminos(Comparable<V> source, Comparable<V> target, IGraph<V, D> grafo) {
        List<Path<V>> caminos = new ArrayList<>();
        V origen = grafo.buscarVertice(source);
        V destino = grafo.buscarVertice(target);
        if (origen == null || destino == null) {
            return caminos;
        }
        Set<V> visitados = new HashSet<>();
        LinkedList<V> stack = new LinkedList<>();
        obtenerTodosLosCaminosAux(grafo, origen, destino, 0, visitados, stack, caminos);
        return caminos;
    }

    private <V, D extends WeightedEdge> void obtenerTodosLosCaminosAux(IGraph<V, D> grafo, V nodo, V destino, double costo,
                                                                       Set<V> visitados, LinkedList<V> stack, List<Path<V>> caminos) {
        if (visitados.contains(nodo)) {
            return;
        }
        if (nodo.equals(destino)) {
            List<V> copia = new ArrayList<>(stack);
            copia.add(nodo);
            caminos.add(new Path<>(copia, costo));
            return;
        }
        visitados.add(nodo);
        stack.addLast(nodo);
        for (Edge<V, D> arista : grafo.adyacencias(grafo.construirComparable(nodo))) {
            obtenerTodosLosCaminosAux(grafo, arista.target(), destino, costo + arista.dato().getWeight(), visitados, stack, caminos);
        }
        visitados.remove(nodo);
        stack.removeLast();
    }

    @Override
    public <V, D extends WeightedEdge> IDijkstraResult<V> dijkstra(Comparable<V> source, IDirectedIGraph<V, D> grafo) {
        V origen = grafo.buscarVertice(source);
        if (origen == null) {
            throw new IllegalArgumentException("El vertice de origen no pertenece al grafo");
        }
        for (Edge<V, D> arista : grafo.aristas()) {
            if (arista.dato() == null || !Double.isFinite(arista.dato().getWeight())
                    || arista.dato().getWeight() < 0) {
                throw new IllegalArgumentException("Dijkstra requiere pesos finitos y no negativos");
            }
        }
        Map<V, Double> costos = new LinkedHashMap<>();
        Map<V, V> predecesores = new LinkedHashMap<>();
        Set<V> visitados = new HashSet<>();
        for (V vertice : grafo.vertices()) {
            costos.put(vertice, Double.POSITIVE_INFINITY);
        }
        costos.put(origen, 0.0);
        while (visitados.size() < costos.size()) {
            V actual = null;
            double minimo = Double.POSITIVE_INFINITY;
            for (Map.Entry<V, Double> entrada : costos.entrySet()) {
                if (!visitados.contains(entrada.getKey()) && entrada.getValue() < minimo) {
                    actual = entrada.getKey();
                    minimo = entrada.getValue();
                }
            }
            if (actual == null) {
                break;
            }
            visitados.add(actual);
            for (Edge<V, D> arista : grafo.adyacencias(grafo.construirComparable(actual))) {
                if (visitados.contains(arista.target())) {
                    continue;
                }
                double nuevoCosto = minimo + arista.dato().getWeight();
                if (!Double.isFinite(nuevoCosto)) {
                    throw new ArithmeticException("El costo de un camino excede el rango de double");
                }
                if (nuevoCosto < costos.get(arista.target())) {
                    costos.put(arista.target(), nuevoCosto);
                    predecesores.put(arista.target(), actual);
                }
            }
        }
        return new DijkstraResult<>(origen, costos, predecesores);
    }

    @Override
    public <V, D extends WeightedEdge> IFloydWarshallResult<V> floyd(IDirectedIGraph<V, D> grafo) {
        List<V> vertices = new ArrayList<>(grafo.vertices());
        Map<V, Integer> indices = new HashMap<>();
        int cantidad = vertices.size();
        double[][] costos = new double[cantidad][cantidad];
        int[][] siguientes = new int[cantidad][cantidad];
        for (int i = 0; i < cantidad; i++) {
            indices.put(vertices.get(i), i);
            Arrays.fill(costos[i], Double.POSITIVE_INFINITY);
            Arrays.fill(siguientes[i], -1);
            costos[i][i] = 0;
            siguientes[i][i] = i;
        }
        for (Edge<V, D> arista : grafo.aristas()) {
            if (arista.dato() == null || !Double.isFinite(arista.dato().getWeight())) {
                throw new IllegalArgumentException("Floyd requiere pesos finitos en todas las aristas");
            }
            int origen = indices.get(arista.source());
            int destino = indices.get(arista.target());
            double peso = arista.dato().getWeight();
            if (peso < costos[origen][destino]) {
                costos[origen][destino] = peso;
                siguientes[origen][destino] = destino;
            }
        }
        for (int k = 0; k < cantidad; k++) {
            for (int i = 0; i < cantidad; i++) {
                if (costos[i][k] == Double.POSITIVE_INFINITY) {
                    continue;
                }
                for (int j = 0; j < cantidad; j++) {
                    if (costos[k][j] == Double.POSITIVE_INFINITY) {
                        continue;
                    }
                    double nuevoCosto = costos[i][k] + costos[k][j];
                    if (!Double.isFinite(nuevoCosto)) {
                        throw new ArithmeticException("El costo de un camino excede el rango de double");
                    }
                    if (nuevoCosto < costos[i][j]) {
                        costos[i][j] = nuevoCosto;
                        siguientes[i][j] = siguientes[i][k];
                    }
                }
            }
        }
        for (int i = 0; i < cantidad; i++) {
            if (costos[i][i] < 0) {
                throw new IllegalArgumentException("El grafo tiene un ciclo de costo negativo");
            }
        }
        return new FloydWarshallResult<>(vertices, costos, siguientes);
    }

    @Override
    public <V, D extends WeightedEdge> IFloydWarshallResult<V> warshall(IDirectedIGraph<V, D> grafo) {
        List<V> vertices = new ArrayList<>(grafo.vertices());
        int cantidad = vertices.size();
        Map<V, Integer> indices = new HashMap<>();
        double[][] saltos = new double[cantidad][cantidad];
        int[][] siguientes = new int[cantidad][cantidad];
        for (int i = 0; i < cantidad; i++) {
            indices.put(vertices.get(i), i);
            Arrays.fill(saltos[i], Double.POSITIVE_INFINITY);
            Arrays.fill(siguientes[i], -1);
            saltos[i][i] = 0;
            siguientes[i][i] = i;
        }
        for (Edge<V, D> arista : grafo.aristas()) {
            int origen = indices.get(arista.source());
            int destino = indices.get(arista.target());
            if (saltos[origen][destino] > 1) {
                saltos[origen][destino] = 1;
                siguientes[origen][destino] = destino;
            }
        }
        for (int k = 0; k < cantidad; k++) {
            for (int i = 0; i < cantidad; i++) {
                if (siguientes[i][k] == -1) {
                    continue;
                }
                for (int j = 0; j < cantidad; j++) {
                    if (siguientes[k][j] == -1) {
                        continue;
                    }
                    double nuevoCosto = saltos[i][k] + saltos[k][j];
                    if (nuevoCosto < saltos[i][j]) {
                        saltos[i][j] = nuevoCosto;
                        siguientes[i][j] = siguientes[i][k];
                    }
                }
            }
        }
        return new FloydWarshallResult<>(vertices, saltos, siguientes);
    }

    @Override
    public <V, D extends WeightedEdge> V obtenerCentroGrafo(IDirectedIGraph<V, D> grafo) {
        IFloydWarshallResult<V> resultado = floyd(grafo);
        V centro = null;
        double menorExcentricidad = Double.POSITIVE_INFINITY;
        for (V vertice : grafo.vertices()) {
            double excentricidad = excentricidadDeEntrada(grafo, resultado, vertice);
            if (excentricidad < menorExcentricidad) {
                centro = vertice;
                menorExcentricidad = excentricidad;
            }
        }
        return centro;
    }

    @Override
    public <V, D extends WeightedEdge> double obtenerExcentricidad(IDirectedIGraph<V, D> grafo, Comparable<V> vertexCriteria) {
        V vertice = grafo.buscarVertice(vertexCriteria);
        if (vertice == null) {
            throw new IllegalArgumentException("El vertice no pertenece al grafo");
        }
        return excentricidadDeEntrada(grafo, floyd(grafo), vertice);
    }

    private <V, D extends WeightedEdge> double excentricidadDeEntrada(
            IDirectedIGraph<V, D> grafo, IFloydWarshallResult<V> resultado, V destino) {
        double maximo = 0;
        for (V origen : grafo.vertices()) {
            maximo = Math.max(maximo, resultado.getCost(origen, destino));
        }
        return maximo;
    }

    /**
     * DFS iterativo desde el origen, respetando el orden de adyacencias.
     * Complejidad O(V + E) en la componente alcanzable.
     */
    @Override
    public <V, D> void recorridoEnProfundidad(IGraph<V, D> grafo, Comparable<V> sourceCriteria, Consumer<V> consumer) {
        V origen = grafo.buscarVertice(sourceCriteria);
        if (origen == null) {
            throw new IllegalArgumentException("El vertice de origen no pertenece al grafo");
        }
        java.util.Objects.requireNonNull(consumer, "consumer");
        Set<V> visitados = new HashSet<>();
        Deque<V> pendientes = new ArrayDeque<>();
        pendientes.push(origen);
        while (!pendientes.isEmpty()) {
            V actual = pendientes.pop();
            if (!visitados.add(actual)) {
                continue;
            }
            consumer.accept(actual);
            List<Edge<V, D>> adyacentes = new ArrayList<>(grafo.adyacencias(grafo.construirComparable(actual)));
            for (int i = adyacentes.size() - 1; i >= 0; i--) {
                V vecino = adyacentes.get(i).target();
                if (!visitados.contains(vecino)) {
                    pendientes.push(vecino);
                }
            }
        }
    }

    /**
     * BFS desde el origen, respetando el orden de adyacencias.
     * Complejidad O(V + E) en la componente alcanzable.
     */
    @Override
    public <V, D> void recorridoEnAmplitud(IGraph<V, D> grafo, Comparable<V> sourceCriteria, Consumer<V> consumer) {
        V origen = grafo.buscarVertice(sourceCriteria);
        if (origen == null) {
            throw new IllegalArgumentException("El vertice de origen no pertenece al grafo");
        }
        java.util.Objects.requireNonNull(consumer, "consumer");
        Set<V> visitados = new HashSet<>();
        Deque<V> pendientes = new ArrayDeque<>();
        visitados.add(origen);
        pendientes.addLast(origen);
        while (!pendientes.isEmpty()) {
            V actual = pendientes.removeFirst();
            consumer.accept(actual);
            for (Edge<V, D> arista : grafo.adyacencias(grafo.construirComparable(actual))) {
                if (visitados.add(arista.target())) {
                    pendientes.addLast(arista.target());
                }
            }
        }
    }

    /**
     * Orden topologico de Kahn. Rechaza grafos dirigidos con ciclos.
     * Complejidad O(V + E).
     */
    @Override
    public <V, D> List<V> calcularClasificacionTopologica(IDirectedIGraph<V, D> grafo) {
        Map<V, Integer> entradas = new LinkedHashMap<>();
        for (V vertice : grafo.vertices()) {
            entradas.put(vertice, 0);
        }
        for (Edge<V, D> arista : grafo.aristas()) {
            entradas.put(arista.target(), entradas.get(arista.target()) + 1);
        }
        Deque<V> pendientes = new ArrayDeque<>();
        for (Map.Entry<V, Integer> entrada : entradas.entrySet()) {
            if (entrada.getValue() == 0) {
                pendientes.addLast(entrada.getKey());
            }
        }
        List<V> orden = new ArrayList<>();
        while (!pendientes.isEmpty()) {
            V actual = pendientes.removeFirst();
            orden.add(actual);
            for (Edge<V, D> arista : grafo.adyacencias(grafo.construirComparable(actual))) {
                V destino = arista.target();
                int restante = entradas.get(destino) - 1;
                entradas.put(destino, restante);
                if (restante == 0) {
                    pendientes.addLast(destino);
                }
            }
        }
        if (orden.size() != entradas.size()) {
            throw new IllegalArgumentException("No existe orden topologico: el grafo contiene ciclos");
        }
        return List.copyOf(orden);
    }

    public <V, D extends WeightedEdge> List<Path<V>> caminoCritico(IDirectedIGraph<V, D> grafo) {
        if (grafo.tieneCiclos()) {
            throw new IllegalArgumentException("El grafo tiene ciclos, no se puede calcular el camino critico");
        }
        List<Path<V>> caminos = new ArrayList<>();
        for (V inicio : grafo.vertices()) {
            if (grafo.gradoDeEntrada(grafo.construirComparable(inicio)) == 0) {
                for (V fin : grafo.vertices()) {
                    if (grafo.gradoDeSalida(grafo.construirComparable(fin)) == 0) {
                        caminos.addAll(obtenerTodosLosCaminos(grafo.construirComparable(inicio), grafo.construirComparable(fin), grafo));
                    }
                }
            }
        }
        caminos.sort((a, b) -> Double.compare(b.getCost(), a.getCost()));
        return caminos;
    }
}