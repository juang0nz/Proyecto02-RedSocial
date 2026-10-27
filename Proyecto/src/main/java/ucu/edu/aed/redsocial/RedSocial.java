package ucu.edu.aed.redsocial;

import ucu.edu.aed.redsocial.modelo.Comentario;
import ucu.edu.aed.redsocial.modelo.Compartido;
import ucu.edu.aed.redsocial.modelo.EntradaFeed;
import ucu.edu.aed.redsocial.modelo.Posteo;
import ucu.edu.aed.redsocial.modelo.Usuario;
import ucu.edu.aed.tda.grafo.IDirectedIGraph;
import ucu.edu.aed.tda.grafo.impl.DirectedGraph;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Núcleo de la red social: usuarios, relaciones de seguimiento y publicaciones.
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
 * Publicaciones:
 * - Posteos, comentarios y compartidos no son vértices: viven en colecciones aparte
 *   y referencian usuarios. Así los algoritmos sobre el grafo de seguimiento no los ven.
 * - Las menciones salen del texto: cada @id de un usuario existente (que no sea el autor)
 *   es una mención. Un @ de alguien que no existe se deja como texto.
 * - Solo puede comentar o compartir un posteo su autor o alguien a quien le llegó.
 * - El alcance y el feed siguen las mismas reglas (ver Difusion.alcance), así que un
 *   usuario tiene un posteo en su feed si y solo si está en el alcance del posteo.
 *
 * Errores:
 * Igual que la biblioteca, toda entrada inválida lanza IllegalArgumentException
 * (id mal formado, nombre vacío, id repetido, usuario inexistente o auto-seguimiento);
 * el mensaje indica cuál fue. Seguir o dejar de seguir de forma redundante no es un error:
 * devuelve false, igual que la API del grafo.
 *
 * Complejidad:
 * V = usuarios, E = seguimientos, Δ = cantidad de seguidos del usuario consultado,
 * P = posteos, C = comentarios, S = compartidos (A = P + C + S, toda la actividad).
 * Los costos incluyen el buscarVertice lineal de DirectedGraph.
 */
public class RedSocial {

    /** Peso de cada arista de seguimiento. */
    public static final double PESO_SEGUIMIENTO = 1.0;

    /** Una @ que no esté pegada a otra palabra (así "ana@mail.com" no es una mención). */
    private static final Pattern MENCION = Pattern.compile("(?<![A-Za-z0-9._])@([A-Za-z0-9._]+)");

    private final IDirectedIGraph<Usuario, WeightedEdge> seguimientos = new DirectedGraph<>();
    private final IDirectedIGraph<Usuario, WeightedEdge> vistaSeguimientos = new GrafoSoloLectura<>(seguimientos);
    private final Map<String, Usuario> usuariosPorId = new LinkedHashMap<>();

    private final Map<String, Posteo> posteos = new LinkedHashMap<>();
    private final Map<String, List<Comentario>> comentariosPorPosteo = new HashMap<>();
    private final Map<String, List<Compartido>> compartidosPorPosteo = new HashMap<>();
    /** Contador global de eventos (publicar, comentar, compartir): da el orden del feed. */
    private long reloj = 0;
    private int ultimoPosteo = 0;
    private int ultimoComentario = 0;

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
     * Elimina un usuario con todos sus seguimientos (los que hace y los que recibe; el grafo
     * borra las aristas entrantes al remover el vértice) y toda su actividad: sus posteos
     * (con sus comentarios y compartidos), sus comentarios y compartidos en posteos ajenos y
     * las menciones que le hicieron. Así no quedan referencias a un usuario inexistente y,
     * si el id se vuelve a registrar, no hereda nada. O(V + E + A).
     * Lanza IllegalArgumentException si el usuario no existe.
     */
    public void eliminarUsuario(String id) {
        Usuario usuario = obtenerUsuario(id);
        seguimientos.removerVertice(seguimientos.construirComparable(usuario));
        usuariosPorId.remove(usuario.getId());
        eliminarActividadDe(usuario);
    }

    private void eliminarActividadDe(Usuario usuario) {
        List<String> posteosPropios = new ArrayList<>();
        for (Posteo posteo : posteos.values()) {
            if (posteo.getAutor().equals(usuario)) {
                posteosPropios.add(posteo.getId());
            }
        }
        for (String posteoId : posteosPropios) {
            posteos.remove(posteoId);
            comentariosPorPosteo.remove(posteoId);
            compartidosPorPosteo.remove(posteoId);
        }
        for (Map.Entry<String, Posteo> entrada : posteos.entrySet()) {
            String posteoId = entrada.getKey();
            if (entrada.getValue().getMencionados().contains(usuario)) {
                entrada.setValue(entrada.getValue().sinMencionA(usuario));
            }
            compartidosPorPosteo.get(posteoId).removeIf(c -> c.getUsuario().equals(usuario));
            List<Comentario> comentarios = comentariosPorPosteo.get(posteoId);
            comentarios.removeIf(c -> c.getAutor().equals(usuario));
            comentarios.replaceAll(c -> c.getMencionados().contains(usuario) ? c.sinMencionA(usuario) : c);
        }
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

    // ------------------------------------------------------------------ publicaciones

    /**
     * Publica un posteo. Las menciones (@id) se detectan en el texto y cada una se resuelve
     * en el índice de usuarios en O(1), así que cuesta O(largo del texto).
     * Lanza IllegalArgumentException si el autor no existe o el texto está vacío.
     */
    public Posteo publicar(String autorId, String texto) {
        Usuario autor = obtenerUsuario(autorId);
        String contenido = validarTexto(texto);
        Posteo posteo = new Posteo("p" + (++ultimoPosteo), autor, contenido, ++reloj,
                mencionesEn(contenido, autor));
        posteos.put(posteo.getId(), posteo);
        comentariosPorPosteo.put(posteo.getId(), new ArrayList<>());
        compartidosPorPosteo.put(posteo.getId(), new ArrayList<>());
        return posteo;
    }

    /**
     * Comenta un posteo. Los usuarios arrobados en el comentario pasan a recibir el posteo.
     * Solo puede comentar el autor o alguien a quien le llegó el posteo, porque hay que
     * verificar que esté en el alcance: O(V + E + A).
     * Lanza IllegalArgumentException si el posteo o el autor no existen, si el texto está
     * vacío o si el posteo no le llegó al autor del comentario.
     */
    public Comentario comentar(String posteoId, String autorId, String texto) {
        Posteo posteo = obtenerPosteo(posteoId);
        Usuario autor = obtenerUsuario(autorId);
        String contenido = validarTexto(texto);
        if (!puedeInteractuar(autor, posteo)) {
            throw new IllegalArgumentException(autor + " no puede comentar " + posteo.getId()
                    + ": el posteo no le llegó");
        }
        Comentario comentario = new Comentario("c" + (++ultimoComentario), posteo.getId(), autor,
                contenido, ++reloj, mencionesEn(contenido, autor));
        comentariosPorPosteo.get(posteo.getId()).add(comentario);
        return comentario;
    }

    /**
     * El usuario comparte el posteo: a partir de ahora les llega también a sus seguidores.
     * Solo puede compartir alguien a quien el posteo le llegó (seguidor de quien lo publicó o
     * compartió, o mencionado), así que se verifica el alcance: O(V + E + A).
     * Retorna true si lo compartió y false si ya lo había compartido.
     * Lanza IllegalArgumentException si el posteo o el usuario no existen, si es el autor o
     * si el posteo no le llegó.
     */
    public boolean compartir(String posteoId, String usuarioId) {
        Posteo posteo = obtenerPosteo(posteoId);
        Usuario usuario = obtenerUsuario(usuarioId);
        if (usuario.equals(posteo.getAutor())) {
            throw new IllegalArgumentException("El autor no puede compartir su propio posteo");
        }
        List<Compartido> compartidos = compartidosPorPosteo.get(posteo.getId());
        for (Compartido compartido : compartidos) {
            if (compartido.getUsuario().equals(usuario)) {
                return false;
            }
        }
        if (!puedeInteractuar(usuario, posteo)) {
            throw new IllegalArgumentException(usuario + " no puede compartir " + posteo.getId()
                    + ": el posteo no le llegó");
        }
        compartidos.add(new Compartido(posteo.getId(), usuario, ++reloj));
        return true;
    }

    /**
     * Usuarios a los que llega el posteo: seguidores del autor, mencionados (en el posteo o
     * en sus comentarios) y, por cada usuario alcanzado que lo compartió, sus seguidores.
     * No incluye al autor. Cada usuario aparece una vez. O(V + E + A); ver Difusion.alcance.
     * Lanza IllegalArgumentException si el posteo no existe.
     */
    public Set<Usuario> alcance(String posteoId) {
        return alcanceDe(obtenerPosteo(posteoId));
    }

    private Set<Usuario> alcanceDe(Posteo posteo) {
        Set<Usuario> compartieron = new LinkedHashSet<>();
        for (Compartido compartido : compartidosPorPosteo.get(posteo.getId())) {
            compartieron.add(compartido.getUsuario());
        }
        return Difusion.alcance(seguimientos, posteo.getAutor(), mencionadosDe(posteo), compartieron);
    }

    private boolean puedeInteractuar(Usuario usuario, Posteo posteo) {
        return usuario.equals(posteo.getAutor()) || alcanceDe(posteo).contains(usuario);
    }

    /**
     * Feed del usuario: posteos publicados o compartidos por alguien que sigue y posteos
     * donde lo mencionaron. No incluye sus propios posteos. Hay una entrada por posteo, con
     * el motivo más reciente, ordenadas de la más nueva a la más vieja.
     *
     * Recorre toda la actividad de la red: O(V + Δ + A + F log F), con F las entradas del feed.
     * Es candidata a optimizar (Desafío 3) con índices por usuario.
     * Lanza IllegalArgumentException si el usuario no existe.
     */
    public List<EntradaFeed> feed(String usuarioId) {
        Usuario usuario = obtenerUsuario(usuarioId);
        Set<Usuario> seguidos = seguidos(usuarioId);
        Map<String, EntradaFeed> masReciente = new LinkedHashMap<>();
        for (Posteo posteo : posteos.values()) {
            if (posteo.getAutor().equals(usuario)) {
                continue;
            }
            if (posteo.getMencionados().contains(usuario)) {
                considerar(masReciente, new EntradaFeed(posteo, EntradaFeed.Motivo.MENCION,
                        posteo.getAutor(), posteo.getOrden()));
            }
            if (seguidos.contains(posteo.getAutor())) {
                considerar(masReciente, new EntradaFeed(posteo, EntradaFeed.Motivo.PUBLICACION,
                        posteo.getAutor(), posteo.getOrden()));
            }
            for (Comentario comentario : comentariosPorPosteo.get(posteo.getId())) {
                if (comentario.getMencionados().contains(usuario)) {
                    considerar(masReciente, new EntradaFeed(posteo, EntradaFeed.Motivo.MENCION,
                            comentario.getAutor(), comentario.getOrden()));
                }
            }
            for (Compartido compartido : compartidosPorPosteo.get(posteo.getId())) {
                if (seguidos.contains(compartido.getUsuario())) {
                    considerar(masReciente, new EntradaFeed(posteo, EntradaFeed.Motivo.COMPARTIDO,
                            compartido.getUsuario(), compartido.getOrden()));
                }
            }
        }
        List<EntradaFeed> feed = new ArrayList<>(masReciente.values());
        feed.sort(Comparator.comparingLong(EntradaFeed::getOrden).reversed());
        return Collections.unmodifiableList(feed);
    }

    /** Se queda con la entrada más reciente de cada posteo; ante empate, con la primera. */
    private static void considerar(Map<String, EntradaFeed> masReciente, EntradaFeed candidata) {
        String posteoId = candidata.getPosteo().getId();
        EntradaFeed actual = masReciente.get(posteoId);
        if (actual == null || candidata.getOrden() > actual.getOrden()) {
            masReciente.put(posteoId, candidata);
        }
    }

    /** Lanza IllegalArgumentException si el posteo no existe. O(1). */
    public Posteo obtenerPosteo(String posteoId) {
        Posteo posteo = posteoId == null ? null : posteos.get(posteoId);
        if (posteo == null) {
            throw new IllegalArgumentException("No existe el posteo '" + posteoId + "'");
        }
        return posteo;
    }

    /** Posteos del usuario en orden de publicación. O(P). */
    public List<Posteo> posteosDe(String usuarioId) {
        Usuario usuario = obtenerUsuario(usuarioId);
        List<Posteo> propios = new ArrayList<>();
        for (Posteo posteo : posteos.values()) {
            if (posteo.getAutor().equals(usuario)) {
                propios.add(posteo);
            }
        }
        return Collections.unmodifiableList(propios);
    }

    /** Todos los posteos en orden de publicación. */
    public Collection<Posteo> posteos() {
        return Collections.unmodifiableCollection(posteos.values());
    }

    public int cantidadDePosteos() {
        return posteos.size();
    }

    /** Comentarios del posteo en orden. O(1). */
    public List<Comentario> comentarios(String posteoId) {
        return Collections.unmodifiableList(comentariosPorPosteo.get(obtenerPosteo(posteoId).getId()));
    }

    /** Quiénes compartieron el posteo, en el orden en que lo hicieron. O(S del posteo). */
    public List<Usuario> compartidoPor(String posteoId) {
        List<Usuario> usuarios = new ArrayList<>();
        for (Compartido compartido : compartidosPorPosteo.get(obtenerPosteo(posteoId).getId())) {
            usuarios.add(compartido.getUsuario());
        }
        return Collections.unmodifiableList(usuarios);
    }

    /** Mencionados en el posteo y en sus comentarios, sin repetir. O(menciones del posteo). */
    public Set<Usuario> mencionados(String posteoId) {
        return Collections.unmodifiableSet(mencionadosDe(obtenerPosteo(posteoId)));
    }

    private Set<Usuario> mencionadosDe(Posteo posteo) {
        Set<Usuario> mencionados = new LinkedHashSet<>(posteo.getMencionados());
        for (Comentario comentario : comentariosPorPosteo.get(posteo.getId())) {
            mencionados.addAll(comentario.getMencionados());
        }
        return mencionados;
    }

    private static String validarTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("El texto no puede ser vacío");
        }
        return texto.trim();
    }

    /**
     * Usuarios existentes arrobados en el texto, en orden de aparición y sin repetir.
     * Se ignoran el propio autor, los @ de usuarios que no existen y los puntos finales
     * ("@ana." menciona a ana).
     */
    private Set<Usuario> mencionesEn(String texto, Usuario autor) {
        Set<Usuario> mencionados = new LinkedHashSet<>();
        Matcher matcher = MENCION.matcher(texto);
        while (matcher.find()) {
            String id = matcher.group(1).replaceAll("\\.+$", "");
            if (existeUsuario(id)) {
                Usuario mencionado = obtenerUsuario(id);
                if (!mencionado.equals(autor)) {
                    mencionados.add(mencionado);
                }
            }
        }
        return mencionados;
    }

    /**
     * Grafo de seguimiento para pasarle a los algoritmos de la biblioteca. Es una vista de
     * solo lectura: las modificaciones tiran UnsupportedOperationException.
     */
    public IDirectedIGraph<Usuario, WeightedEdge> grafoDeSeguimiento() {
        return vistaSeguimientos;
    }
}
