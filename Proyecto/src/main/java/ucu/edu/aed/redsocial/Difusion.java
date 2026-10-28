package ucu.edu.aed.redsocial;

import ucu.edu.aed.tda.grafo.IDirectedIGraph;
import ucu.edu.aed.tda.grafo.model.edge.Edge;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Algoritmo de difusión de contenido sobre un grafo dirigido donde A -> B significa
 * "A sigue a B". Es genérico (no conoce Usuario ni Posteo), igual que SocialGraphQueries,
 * para que el algoritmo quede separado del dominio y se pueda probar con Strings.
 *
 * El contenido viaja al revés que las aristas: lo que publica B le llega a quienes siguen
 * a B, o sea a sus predecesores.
 */
public final class Difusion {

    private Difusion() {
    }

    /**
     * Seguidores de cada vértice (predecesores), calculados de una sola pasada por las aristas.
     * O(V + E) en total, en lugar de O(V + E) por cada vértice que costaría llamar a
     * predecessors() una vez por emisor. Cada lista respeta el orden de aristas() del grafo.
     */
    public static <V, D> Map<V, List<V>> seguidoresDeCadaVertice(IDirectedIGraph<V, D> grafo) {
        Map<V, List<V>> seguidores = new LinkedHashMap<>();
        for (V v : grafo.vertices()) {
            seguidores.put(v, new ArrayList<>());
        }
        for (Edge<V, D> arista : grafo.aristas()) {
            seguidores.get(arista.target()).add(arista.source());
        }
        return seguidores;
    }

    /**
     * Calcula a quiénes llega un contenido publicado por origen.
     *
     * Reglas:
     * - Los receptores directos (los mencionados) lo reciben aunque no sigan a nadie.
     * - Un emisor hace llegar el contenido a todos sus seguidores. El origen es emisor.
     * - Un reemisor (alguien que lo compartió) se vuelve emisor solo si lo recibió; así,
     *   un mencionado que comparte lo hace llegar a sus propios seguidores.
     * - Cada vértice se cuenta una sola vez (conjunto de alcanzados), por eso los ciclos de
     *   seguimiento no producen difusión infinita.
     * - El origen no forma parte de su propio alcance.
     *
     * Es un BFS sobre predecesores que solo expande desde los emisores. Devuelve los
     * alcanzados en orden de descubrimiento: primero los receptores directos y después
     * por niveles de compartidos.
     *
     * Complejidad: O(V + E) para armar los seguidores, más O(R) por los receptores directos;
     * el BFS visita cada vértice y cada arista a lo sumo una vez. Espacio O(V + E).
     *
     * Lanza IllegalArgumentException si origen o algún receptor directo no está en el grafo.
     */
    public static <V, D> Set<V> alcance(IDirectedIGraph<V, D> grafo, V origen,
                                        Collection<V> receptoresDirectos, Set<V> reemisores) {
        Set<V> vertices = grafo.vertices();
        if (origen == null || !vertices.contains(origen)) {
            throw new IllegalArgumentException("El origen no pertenece al grafo: " + origen);
        }
        for (V receptor : receptoresDirectos) {
            if (!vertices.contains(receptor)) {
                throw new IllegalArgumentException("El receptor no pertenece al grafo: " + receptor);
            }
        }

        Map<V, List<V>> seguidores = seguidoresDeCadaVertice(grafo);
        Set<V> alcanzados = new LinkedHashSet<>();
        Deque<V> emisores = new ArrayDeque<>();
        emisores.add(origen);

        for (V receptor : receptoresDirectos) {
            recibir(receptor, origen, reemisores, alcanzados, emisores);
        }
        while (!emisores.isEmpty()) {
            V emisor = emisores.poll();
            for (V seguidor : seguidores.get(emisor)) {
                recibir(seguidor, origen, reemisores, alcanzados, emisores);
            }
        }
        return Collections.unmodifiableSet(alcanzados);
    }

    private static <V> void recibir(V vertice, V origen, Set<V> reemisores,
                                    Set<V> alcanzados, Deque<V> emisores) {
        if (vertice.equals(origen) || !alcanzados.add(vertice)) {
            return;
        }
        if (reemisores.contains(vertice)) {
            emisores.add(vertice);
        }
    }
}
