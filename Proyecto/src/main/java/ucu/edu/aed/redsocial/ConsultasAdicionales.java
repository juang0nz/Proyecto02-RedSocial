package ucu.edu.aed.redsocial;

import ucu.edu.aed.tda.grafo.IDirectedIGraph;
import ucu.edu.aed.tda.grafo.model.edge.Edge;
import java.util.*;

/**
 * Cinco consultas estructurales adicionales sobre el grafo de seguimiento.
 * Convencion: A -> B significa que A sigue a B. No modifica el grafo.
 * V = vertices, E = aristas. Cada consulta construye adyacencias O(V+E).
 */
public final class ConsultasAdicionales {
    private ConsultasAdicionales() {}

    private static final class Indice<V> {
        final Map<V, Set<V>> salientes = new LinkedHashMap<>();
        final Map<V, Set<V>> entrantes = new LinkedHashMap<>();
        Indice(IDirectedIGraph<V, ?> grafo) {
            Objects.requireNonNull(grafo, "grafo");
            for (V v : grafo.vertices()) {
                salientes.put(v, new LinkedHashSet<>());
                entrantes.put(v, new LinkedHashSet<>());
            }
            for (Edge<V, ?> e : grafo.aristas()) {
                salientes.get(e.source()).add(e.target());
                entrantes.get(e.target()).add(e.source());
            }
        }
        void exigir(V v) {
            if (!salientes.containsKey(v)) {
                throw new IllegalArgumentException("Usuario inexistente: " + v);
            }
        }
        Set<V> alcanzables(V inicio, V bloqueado, V aristaOrigen, V aristaDestino, int limite) {
            Set<V> visitados = new LinkedHashSet<>();
            if (Objects.equals(inicio, bloqueado)) return visitados;
            Deque<V> cola = new ArrayDeque<>();
            Map<V, Integer> distancias = new HashMap<>();
            cola.add(inicio);
            distancias.put(inicio, 0);
            visitados.add(inicio);
            while (!cola.isEmpty()) {
                V actual = cola.remove();
                int distancia = distancias.get(actual);
                if (distancia >= limite) continue;
                for (V vecino : salientes.get(actual)) {
                    if (Objects.equals(vecino, bloqueado)
                            || (Objects.equals(actual, aristaOrigen) && Objects.equals(vecino, aristaDestino))) {
                        continue;
                    }
                    if (visitados.add(vecino)) {
                        distancias.put(vecino, distancia + 1);
                        cola.add(vecino);
                    }
                }
            }
            return visitados;
        }
    }

    /** 1. Cuentas distintas del origen alcanzables siguiendo hasta k relaciones.
     * O(V+E) tiempo y espacio. Con k=0 devuelve conjunto vacio.
     */
    public static <V, D> Set<V> alcanzablesHastaK(IDirectedIGraph<V, D> grafo, V origen, int k) {
        Indice<V> i = new Indice<>(grafo);
        i.exigir(origen);
        if (k < 0) throw new IllegalArgumentException("k debe ser no negativo");
        Set<V> resultado = i.alcanzables(origen, null, null, null, k);
        resultado.remove(origen);
        return Collections.unmodifiableSet(resultado);
    }

    /** 2. Cuentas que siguen a ambos usuarios. O(V+E) tiempo y espacio. */
    public static <V, D> Set<V> seguidoresComunes(IDirectedIGraph<V, D> grafo, V a, V b) {
        Indice<V> i = new Indice<>(grafo);
        i.exigir(a);
        i.exigir(b);
        Set<V> resultado = new LinkedHashSet<>(i.entrantes.get(a));
        resultado.retainAll(i.entrantes.get(b));
        return Collections.unmodifiableSet(resultado);
    }

    /** 3. Camino dirigido minimo en saltos sin atravesar la cuenta bloqueada.
     * Devuelve lista vacia cuando no existe. O(V+E) tiempo y espacio.
     */
    public static <V, D> List<V> caminoEvitando(IDirectedIGraph<V, D> grafo, V origen, V destino, V bloqueado) {
        Indice<V> i = new Indice<>(grafo);
        i.exigir(origen);
        i.exigir(destino);
        i.exigir(bloqueado);
        if (Objects.equals(origen, bloqueado) || Objects.equals(destino, bloqueado)) {
            return List.of();
        }
        Map<V, V> padre = new HashMap<>();
        Set<V> vistos = new HashSet<>();
        Deque<V> cola = new ArrayDeque<>();
        vistos.add(origen);
        cola.add(origen);
        while (!cola.isEmpty() && !vistos.contains(destino)) {
            V actual = cola.remove();
            for (V siguiente : i.salientes.get(actual)) {
                if (!Objects.equals(siguiente, bloqueado) && vistos.add(siguiente)) {
                    padre.put(siguiente, actual);
                    cola.add(siguiente);
                }
            }
        }
        if (!vistos.contains(destino)) return List.of();
        LinkedList<V> camino = new LinkedList<>();
        V actual = destino;
        while (actual != null) {
            camino.addFirst(actual);
            actual = padre.get(actual);
        }
        return Collections.unmodifiableList(camino);
    }

    /** 4. Componentes de la subred formada SOLO por seguimientos mutuos.
     * Incluye usuarios aislados como grupos unitarios. O(V+E) tiempo y espacio.
     */
    public static <V, D> List<Set<V>> gruposDeSeguimientoMutuo(IDirectedIGraph<V, D> grafo) {
        Indice<V> i = new Indice<>(grafo);
        Set<V> vistos = new HashSet<>();
        List<Set<V>> grupos = new ArrayList<>();
        for (V origen : i.salientes.keySet()) {
            if (!vistos.add(origen)) continue;
            Set<V> grupo = new LinkedHashSet<>();
            Deque<V> cola = new ArrayDeque<>();
            cola.add(origen);
            while (!cola.isEmpty()) {
                V actual = cola.remove();
                grupo.add(actual);
                for (V siguiente : i.salientes.get(actual)) {
                    if (i.salientes.get(siguiente).contains(actual) && vistos.add(siguiente)) {
                        cola.add(siguiente);
                    }
                }
            }
            grupos.add(Collections.unmodifiableSet(grupo));
        }
        return Collections.unmodifiableList(grupos);
    }

    /** 5. Cuentas que origen deja de poder alcanzar al quitar una arista A -> B.
     * No altera el grafo. O(V+E) tiempo y espacio.
     */
    public static <V, D> Set<V> perdidaPorEliminarSeguimiento(
            IDirectedIGraph<V, D> grafo, V origen, V a, V b) {
        Indice<V> i = new Indice<>(grafo);
        i.exigir(origen);
        i.exigir(a);
        i.exigir(b);
        if (!i.salientes.get(a).contains(b)) {
            throw new IllegalArgumentException("La relacion a eliminar no existe");
        }
        Set<V> antes = i.alcanzables(origen, null, null, null, Integer.MAX_VALUE);
        Set<V> despues = i.alcanzables(origen, null, a, b, Integer.MAX_VALUE);
        antes.removeAll(despues);
        antes.remove(origen);
        return Collections.unmodifiableSet(antes);
    }
}
