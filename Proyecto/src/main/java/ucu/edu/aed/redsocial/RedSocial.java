package ucu.edu.aed.redsocial;

import ucu.edu.aed.redsocial.modelo.Usuario;
import ucu.edu.aed.tda.grafo.IDirectedIGraph;
import ucu.edu.aed.tda.grafo.impl.DirectedGraph;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Núcleo de la red social: usuarios y relaciones de seguimiento.
 *
 * Modelo:
 * - Cada usuario es un vértice de un DirectedGraph<Usuario, WeightedEdge>.
 * - Una arista A -> B significa "A sigue a B". Por eso los seguidos de A son
 *   sus sucesores y los seguidores de B son sus predecesores.
 * - Todas las aristas pesan PESO_SEGUIMIENTO (1): los grados de separación cuentan
 *   saltos. El dato es un WeightedEdge porque Dijkstra, Floyd y Warshall lo exigen.
 * - Un seguimiento mutuo son dos aristas, A -> B y B -> A.
 * - No se permite seguirse a sí mismo (el grafo lo aceptaría, por eso se valida acá) ni
 *   seguimientos repetidos (los rechaza el grafo).
 *
 * Las adyacencias viven solo en el grafo. Además se mantiene un índice id -> Usuario
 * para resolver ids en O(1) y validar existencia sin pasar por buscarVertice, que es O(V).
 * El índice no duplica relaciones: solo los vértices.
 *
 * Errores:
 * Igual que la biblioteca, toda entrada inválida lanza IllegalArgumentException
 * (id mal formado, nombre vacío, id repetido, usuario inexistente o auto-seguimiento);
 * el mensaje indica cuál fue. Seguir o dejar de seguir de forma redundante no es un error:
 * devuelve false, igual que la API del grafo.
 *
 * Complejidad:
 * V = usuarios, E = seguimientos, Δ = cantidad de seguidos del usuario consultado.
 * Los costos incluyen el buscarVertice lineal de DirectedGraph.
 */
public class RedSocial {

    /** Peso de cada arista de seguimiento. */
    public static final double PESO_SEGUIMIENTO = 1.0;

    private final IDirectedIGraph<Usuario, WeightedEdge> seguimientos = new DirectedGraph<>();
    private final IDirectedIGraph<Usuario, WeightedEdge> vistaSeguimientos = new GrafoSoloLectura<>(seguimientos);
    private final Map<String, Usuario> usuariosPorId = new LinkedHashMap<>();

    // ------------------------------------------------------------------ usuarios

    /**
     * Registra un usuario nuevo y lo devuelve con el id normalizado. O(1) promedio.
     * Lanza IllegalArgumentException si el id o el nombre son inválidos, o si el id ya existe.
     */
    public Usuario registrarUsuario(String id, String nombre) {
        Usuario usuario = new Usuario(id, nombre);
        if (usuariosPorId.containsKey(usuario.getId())) {
            throw new IllegalArgumentException("Ya existe un usuario con id '" + usuario.getId() + "'");
        }
        seguimientos.agregarVertice(usuario);
        usuariosPorId.put(usuario.getId(), usuario);
        return usuario;
    }

    /**
     * Elimina un usuario y todos sus seguimientos, tanto los que hace como los que recibe
     * (el grafo borra las aristas entrantes al remover el vértice). O(V + E).
     * Lanza IllegalArgumentException si el usuario no existe.
     */
    public void eliminarUsuario(String id) {
        Usuario usuario = obtenerUsuario(id);
        seguimientos.removerVertice(seguimientos.construirComparable(usuario));
        usuariosPorId.remove(usuario.getId());
    }

    /**
     * Devuelve el usuario con ese id. O(1).
     * Lanza IllegalArgumentException si el usuario no existe o el id tiene formato inválido.
     */
    public Usuario obtenerUsuario(String id) {
        Usuario usuario = usuariosPorId.get(Usuario.normalizarId(id));
        if (usuario == null) {
            throw new IllegalArgumentException("No existe el usuario '" + id + "'");
        }
        return usuario;
    }

    /** O(1). Un id con formato inválido simplemente no existe. */
    public boolean existeUsuario(String id) {
        try {
            return usuariosPorId.containsKey(Usuario.normalizarId(id));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /** Usuarios en orden de registro (vista inmodificable). O(1). */
    public Collection<Usuario> usuarios() {
        return Collections.unmodifiableCollection(usuariosPorId.values());
    }

    public int cantidadDeUsuarios() {
        return usuariosPorId.size();
    }

    // ------------------------------------------------------------------ seguimiento

    /**
     * seguidor empieza a seguir a seguido: agrega la arista seguidor -> seguido.
     * O(Δ) (el grafo revisa los seguidos del seguidor para no duplicar).
     * Retorna true si se agregó y false si ya lo seguía.
     * Lanza IllegalArgumentException si alguno no existe o si seguidor y seguido son el mismo usuario.
     */
    public boolean seguir(String seguidorId, String seguidoId) {
        Usuario seguidor = obtenerUsuario(seguidorId);
        Usuario seguido = obtenerUsuario(seguidoId);
        if (seguidor.equals(seguido)) {
            throw new IllegalArgumentException("Un usuario no puede seguirse a sí mismo: " + seguidor);
        }
        return seguimientos.agregarArista(seguidor, seguido, new WeightedEdge(PESO_SEGUIMIENTO));
    }

    /**
     * Quita la arista seguidor -> seguido. La arista inversa, si existe, no se toca. O(V + Δ).
     * Retorna true si lo seguía y se quitó, false si no lo seguía.
     * Lanza IllegalArgumentException si alguno no existe.
     */
    public boolean dejarDeSeguir(String seguidorId, String seguidoId) {
        Usuario seguidor = obtenerUsuario(seguidorId);
        Usuario seguido = obtenerUsuario(seguidoId);
        return seguimientos.eliminarArista(seguimientos.construirComparable(seguidor),
                seguimientos.construirComparable(seguido));
    }

    /** ¿Existe la arista seguidor -> seguido? O(V + Δ). */
    public boolean sigueA(String seguidorId, String seguidoId) {
        Usuario seguidor = obtenerUsuario(seguidorId);
        Usuario seguido = obtenerUsuario(seguidoId);
        return seguimientos.existeArista(seguimientos.construirComparable(seguidor),
                seguimientos.construirComparable(seguido));
    }

    /** ¿Se siguen mutuamente? Comprueba A -> B y B -> A. O(V + Δ). */
    public boolean sonMutuos(String idA, String idB) {
        return sigueA(idA, idB) && sigueA(idB, idA);
    }

    /**
     * A quiénes sigue el usuario: sus sucesores, en el orden en que empezó a seguirlos.
     * O(V + Δ).
     */
    public Set<Usuario> seguidos(String id) {
        Usuario usuario = obtenerUsuario(id);
        return Collections.unmodifiableSet(seguimientos.successors(seguimientos.construirComparable(usuario)));
    }

    /**
     * Quiénes siguen al usuario: sus predecesores, en orden de registro. O(V + E), porque
     * DirectedGraph solo guarda aristas salientes y tiene que revisarlas todas.
     */
    public Set<Usuario> seguidores(String id) {
        Usuario usuario = obtenerUsuario(id);
        return Collections.unmodifiableSet(seguimientos.predecessors(seguimientos.construirComparable(usuario)));
    }

    /**
     * Usuarios con los que el seguimiento es recíproco: seguidos ∩ seguidores, en el orden
     * de los seguidos. O(V + E): se calcula una vez cada conjunto y se intersecta en O(Δ).
     */
    public Set<Usuario> seguidoresMutuos(String id) {
        Set<Usuario> mutuos = new LinkedHashSet<>(seguidos(id));
        mutuos.retainAll(seguidores(id));
        return Collections.unmodifiableSet(mutuos);
    }

    public int cantidadDeSeguidores(String id) {
        return seguidores(id).size();
    }

    public int cantidadDeSeguidos(String id) {
        return seguidos(id).size();
    }

    public int cantidadDeSeguimientos() {
        return seguimientos.cantidadDeAristas();
    }

    /**
     * Grafo de seguimiento para pasarle a los algoritmos de la biblioteca. Es una vista de
     * solo lectura: las modificaciones tiran UnsupportedOperationException.
     */
    public IDirectedIGraph<Usuario, WeightedEdge> grafoDeSeguimiento() {
        return vistaSeguimientos;
    }
}
