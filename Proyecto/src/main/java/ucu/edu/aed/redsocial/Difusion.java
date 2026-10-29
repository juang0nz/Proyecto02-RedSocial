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

// Calcula cómo se reparte un contenido entre seguidores, mencionados y quienes comparten.
// A -> B significa que A sigue a B, así que el contenido viaja al revés de las aristas.
// No depende de Usuario ni de Posteo: trabaja directamente con los vértices del grafo.
public final class Difusion {

    private Difusion() {
    }

    // Devuelve una lista de seguidores por vértice, invirtiendo las conexiones.
    // Cuesta O(V + E) en tiempo y espacio; evita llamar a predecessors para cada emisor.
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

    // Recibe el autor, los receptores directos y quienes compartieron.
    // Devuelve a quiénes llegó, sin incluir al autor y sin repetir usuarios.
    // Un mencionado recibe aunque no siga al autor; solo reenvía si además compartió.
    // El origen y todos los receptores directos tienen que existir en el grafo.
    // Tiempo O(V + E + R), con R receptores directos, y espacio O(V + E).
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
        // La cola no guarda a todos los que reciben, solo a quienes pueden reenviar.
        Deque<V> emisores = new ArrayDeque<>();
        emisores.add(origen);

        for (V receptor : receptoresDirectos) {
            recibir(receptor, origen, reemisores, alcanzados, emisores);
        }
        // BFS por cadenas de compartidos, usando las listas de seguidores ya preparadas.
        while (!emisores.isEmpty()) {
            V emisor = emisores.poll();
            for (V seguidor : seguidores.get(emisor)) {
                recibir(seguidor, origen, reemisores, alcanzados, emisores);
            }
        }
        return Collections.unmodifiableSet(alcanzados);
    }

    // Registra una recepción nueva y, si compartió, lo deja pendiente para reenviar.
    private static <V> void recibir(V vertice, V origen, Set<V> reemisores,
                                    Set<V> alcanzados, Deque<V> emisores) {
        // Este control también corta los ciclos de seguimiento.
        if (vertice.equals(origen) || !alcanzados.add(vertice)) {
            return;
        }
        if (reemisores.contains(vertice)) {
            emisores.add(vertice);
        }
    }
}
