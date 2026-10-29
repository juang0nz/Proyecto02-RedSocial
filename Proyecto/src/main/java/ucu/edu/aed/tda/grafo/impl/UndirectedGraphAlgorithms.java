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
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.Consumer;

// Implementa árboles mínimos, recorridos y puntos de articulación.
// Resuelve los requisitos de algoritmos no dirigidos y articulaciones del Desafío 1.
// En los costos, V es la cantidad de vértices y E la de conexiones.
public class UndirectedGraphAlgorithms implements IUndirectedGraphAlgorithm {

    // Prim va sumando la conexión más barata hacia un vértice nuevo.
    // Para este requisito usamos una cola de prioridad en vez de comparar todas las candidatas cada vez.
    // Un vértice solo se conserva sin aristas; los aislados también quedan en el bosque.
    // Si hay pesos iguales, puede elegir cualquiera de las candidatas de ese peso.
    // Devuelve un bosque si hay varias componentes y un grafo vacío si no existe source.
    // La cola cuesta O(E log E). Con UndirectedGraph, construir el resultado
    // también puede costar O(V * E) por las revisiones de aristas repetidas.
    @Override
    public <V, D extends WeightedEdge> IUndirectedGraph<V, D> prim(IUndirectedGraph<V, D> graph, Comparable<V> source) {
        UndirectedGraph<V, D> arbol = new UndirectedGraph<>();
        V inicio = graph.buscarVertice(source);
        if (inicio == null) {
            return arbol;
        }
        arbol.agregarVertices(graph.vertices());

        // Armamos las listas una vez para no buscar adyacencias por criterio a cada paso.
        Map<V, List<Edge<V, D>>> vecinos = new HashMap<>();
        for (V v : graph.vertices()) {
            vecinos.put(v, new ArrayList<>());
        }
        for (Edge<V, D> e : graph.aristas()) {
            vecinos.get(e.source()).add(e);
            vecinos.get(e.target()).add(e);
        }

        Set<V> visitados = new HashSet<>();
        // La cola deja primero la arista de menor peso disponible.
        PriorityQueue<Edge<V, D>> cola = new PriorityQueue<>(
                Comparator.comparingDouble(e -> e.dato().getWeight()));

        List<V> ordenDeArranque = new ArrayList<>();
        ordenDeArranque.add(inicio);
        ordenDeArranque.addAll(graph.vertices());

        // Después del origen, probamos los demás para cubrir componentes desconectadas.
        for (V raiz : ordenDeArranque) {
            if (visitados.contains(raiz)) {
                continue;
            }
            visitar(raiz, vecinos, visitados, cola);
            while (!cola.isEmpty()) {
                Edge<V, D> minima = cola.poll();
                V nuevo;
                V desde;
                if (!visitados.contains(minima.target())) {
                    nuevo = minima.target();
                    desde = minima.source();
                } else if (!visitados.contains(minima.source())) {
                    nuevo = minima.source();
                    desde = minima.target();
                } else {
                    // Si ambos extremos ya están dentro, agregarla formaría un ciclo.
                    continue;
                }
                arbol.agregarArista(desde, nuevo, minima.dato());
                visitar(nuevo, vecinos, visitados, cola);
            }
        }
        return arbol;
    }

    // Marca el vértice y agrega a la cola las conexiones hacia vértices todavía nuevos.
    private <V, D> void visitar(V v, Map<V, List<Edge<V, D>>> vecinos, Set<V> visitados,
                                PriorityQueue<Edge<V, D>> cola) {
        visitados.add(v);
        for (Edge<V, D> e : vecinos.get(v)) {
            V otro = e.source().equals(v) ? e.target() : e.source();
            if (!visitados.contains(otro)) {
                cola.add(e);
            }
        }
    }

    // Devuelve la arista más barata entre los dos conjuntos, o null si no hay ninguna.
    // El costo depende también de contains: una lista busca linealmente, un HashSet no.
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

    // Kruskal ordena todas las aristas y une componentes sin cerrar ciclos.
    // Es la otra forma de construir el árbol mínimo solicitada en la consigna.
    // Con pesos iguales no exigimos un árbol único, sino un costo mínimo y ausencia de ciclos.
    // En vacío devuelve un grafo vacío y con un único vértice lo conserva sin aristas.
    // Devuelve un árbol mínimo, o un bosque si no se puede conectar todo.
    // Ordenar cuesta O(E log E); una cota conservadora total es O(V + E log E + V * E),
    // incluyendo las búsquedas de raíces y la construcción con UndirectedGraph.
    @Override
    public <V, D extends WeightedEdge> IUndirectedGraph<V, D> kruskal(IUndirectedGraph<V, D> graph) {
        UndirectedGraph<V, D> arbol = new UndirectedGraph<>();
        arbol.agregarVertices(graph.vertices());

        List<Edge<V, D>> aristas = new ArrayList<>(graph.aristas());
        aristas.sort(Comparator.comparingDouble(e -> e.dato().getWeight()));

        // Cada vértice arranca en su propia componente, siendo su propio padre.
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
                // Raíces distintas significa que la arista une grupos separados.
                padre.put(raizA, raizB);
                arbol.agregarArista(e.source(), e.target(), e.dato());
                agregadas++;
            }
        }
        return arbol;
    }

    // Busca el representante del grupo y acorta el camino para futuras consultas.
    // Hay compresión de caminos, pero no unión por rango.
    private <V> V buscarRaiz(Map<V, V> padre, V v) {
        V raiz = v;
        while (!padre.get(raiz).equals(raiz)) {
            raiz = padre.get(raiz);
        }
        // Los nodos recorridos pasan a apuntar directamente a la raíz.
        while (!v.equals(raiz)) {
            V siguiente = padre.get(v);
            padre.put(v, raiz);
            v = siguiente;
        }
        return raiz;
    }

    // BFS de todas las componentes: aplica consumer una vez por vértice.
    // Para el recorrido no dirigido pedido, la cola respeta los niveles y visitados evita volver atrás.
    // El bucle externo permite visitar también los aislados; en vacío no llama al consumer.
    // Con UndirectedGraph cuesta O(V² + E), porque adyacencias busca en O(V).
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

    // Devuelve los vértices cuya eliminación separaría más el grafo.
    // Este método cubre el requisito de puntos de articulación, no el de aristas puente.
    // Revisa cada componente; el vacío y los vértices aislados no aportan articulaciones.
    // Usa DFS con tiempos de descubrimiento. Con UndirectedGraph cuesta O(V² + E).
    public <V, D> Set<V> puntosDeArticulacion(IUndirectedGraph<V, D> graph) {
        Set<V> articulaciones = new LinkedHashSet<>();
        Map<V, Integer> descubrimiento = new HashMap<>();
        // low guarda el descubrimiento más antiguo al que puede volver cada subárbol.
        Map<V, Integer> low = new HashMap<>();
        // El arreglo permite compartir el contador entre las llamadas recursivas.
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
                // El padre hereda la posibilidad de volver atrás que encontró el hijo.
                low.put(u, Math.min(low.get(u), low.get(v)));
                if (padre != null && low.get(v) >= descubrimiento.get(u)) {
                    // Este hijo no puede llegar por otro lado a un antecesor de u.
                    // Si quitamos u, su subárbol queda separado.
                    articulaciones.add(u);
                }
            } else if (!v.equals(padre)) {
                // Es una conexión de vuelta; no contamos la misma arista hacia el padre.
                low.put(u, Math.min(low.get(u), descubrimiento.get(v)));
            }
        }

        // La raíz es un caso aparte: necesita al menos dos hijos en el árbol DFS.
        if (padre == null && hijos >= 2) {
            articulaciones.add(u);
        }
    }

    // Copia los vértices y conexiones ignorando su dirección.
    // Si existían A -> B y B -> A, queda una conexión con el dato de la primera agregada.
    public <V, D> UndirectedGraph<V, D> proyeccionNoDirigida(IDirectedIGraph<V, D> dirigido) {
        UndirectedGraph<V, D> proyeccion = new UndirectedGraph<>();
        proyeccion.agregarVertices(dirigido.vertices());
        for (Edge<V, D> e : dirigido.aristas()) {
            proyeccion.agregarArista(e.source(), e.target(), e.dato());
        }
        return proyeccion;
    }
}