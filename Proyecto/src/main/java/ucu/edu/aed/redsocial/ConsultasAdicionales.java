package ucu.edu.aed.redsocial;

import ucu.edu.aed.tda.grafo.IDirectedIGraph;
import ucu.edu.aed.tda.grafo.model.edge.Edge;
import java.util.*;

// Resuelve cinco consultas de seguimiento sin modificar el grafo.
// A -> B significa que A sigue a B. V son los vértices y E las aristas.
// Cada consulta arma sus propios índices en O(V + E) para recorrerlos después.
public final class ConsultasAdicionales {
    private ConsultasAdicionales() {}

    // Guarda a quién sigue cada usuario y quiénes lo siguen, para consultar sin búsquedas lineales.
    private static final class Indice<V> {
        final Map<V, Set<V>> salientes = new LinkedHashMap<>();
        final Map<V, Set<V>> entrantes = new LinkedHashMap<>();
        // Recorre una vez los vértices y las aristas para llenar los dos sentidos.
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
        // Rechaza un usuario que no forma parte del índice.
        void exigir(V v) {
            if (!salientes.containsKey(v)) {
                throw new IllegalArgumentException("Usuario inexistente: " + v);
            }
        }
        // BFS con límite de saltos. Puede saltarse un usuario o una arista sin borrarlos.
        // Incluye el inicio, salvo que esté bloqueado. Costo O(V + E).
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
                // El nivel límite se incluye, pero ya no se expande.
                if (distancia >= limite) continue;
                for (V vecino : salientes.get(actual)) {
                    if (Objects.equals(vecino, bloqueado)
                            || (Objects.equals(actual, aristaOrigen) && Objects.equals(vecino, aristaDestino))) {
                        continue;
                    }
                    // Marcar al encolar evita repetir usuarios y conserva la distancia mínima.
                    if (visitados.add(vecino)) {
                        distancias.put(vecino, distancia + 1);
                        cola.add(vecino);
                    }
                }
            }
            return visitados;
        }
    }

    // Devuelve a quiénes llega el origen siguiendo hasta k conexiones, sin incluirse.
    // Con k = 0 queda vacío; un k negativo es un error. Tiempo y espacio O(V + E).
    public static <V, D> Set<V> alcanzablesHastaK(IDirectedIGraph<V, D> grafo, V origen, int k) {
        Indice<V> i = new Indice<>(grafo);
        i.exigir(origen);
        if (k < 0) throw new IllegalArgumentException("k debe ser no negativo");
        Set<V> resultado = i.alcanzables(origen, null, null, null, k);
        resultado.remove(origen);
        return Collections.unmodifiableSet(resultado);
    }

    // Devuelve los usuarios que siguen a ambos. Tiempo y espacio O(V + E).
    public static <V, D> Set<V> seguidoresComunes(IDirectedIGraph<V, D> grafo, V a, V b) {
        Indice<V> i = new Indice<>(grafo);
        i.exigir(a);
        i.exigir(b);
        Set<V> resultado = new LinkedHashSet<>(i.entrantes.get(a));
        // Partimos de los seguidores de a y dejamos solo los que también siguen a b.
        resultado.retainAll(i.entrantes.get(b));
        return Collections.unmodifiableSet(resultado);
    }

    // Devuelve un camino mínimo en saltos que no pase por el usuario bloqueado.
    // Si no hay camino, devuelve lista vacía. Tiempo y espacio O(V + E).
    public static <V, D> List<V> caminoEvitando(IDirectedIGraph<V, D> grafo, V origen, V destino, V bloqueado) {
        Indice<V> i = new Indice<>(grafo);
        i.exigir(origen);
        i.exigir(destino);
        i.exigir(bloqueado);
        if (Objects.equals(origen, bloqueado) || Objects.equals(destino, bloqueado)) {
            return List.of();
        }
        // padre guarda desde dónde se descubrió cada usuario para reconstruir la ruta.
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
        // Retrocedemos desde el destino y agregamos al principio para dejar el orden correcto.
        while (actual != null) {
            camino.addFirst(actual);
            actual = padre.get(actual);
        }
        return Collections.unmodifiableList(camino);
    }

    // Forma grupos conectados usando solamente seguimientos de ida y vuelta.
    // No exige que todos se sigan entre sí. Los aislados forman grupos de uno.
    // Tiempo y espacio O(V + E).
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
                    // Solo cruzamos la conexión si también existe en sentido contrario.
                    if (i.salientes.get(siguiente).contains(actual) && vistos.add(siguiente)) {
                        cola.add(siguiente);
                    }
                }
            }
            grupos.add(Collections.unmodifiableSet(grupo));
        }
        return Collections.unmodifiableList(grupos);
    }

    // Compara el alcance del origen con y sin la arista a -> b.
    // Devuelve los usuarios que se perderían, sin borrar nada. Tiempo y espacio O(V + E).
    public static <V, D> Set<V> perdidaPorEliminarSeguimiento(
            IDirectedIGraph<V, D> grafo, V origen, V a, V b) {
        Indice<V> i = new Indice<>(grafo);
        i.exigir(origen);
        i.exigir(a);
        i.exigir(b);
        if (!i.salientes.get(a).contains(b)) {
            throw new IllegalArgumentException("La relacion a eliminar no existe");
        }
        // Hacemos dos BFS: el segundo ignora únicamente la arista indicada.
        Set<V> antes = i.alcanzables(origen, null, null, null, Integer.MAX_VALUE);
        Set<V> despues = i.alcanzables(origen, null, a, b, Integer.MAX_VALUE);
        antes.removeAll(despues);
        antes.remove(origen);
        return Collections.unmodifiableSet(antes);
    }
}
