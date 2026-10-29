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

// Maneja los usuarios, seguimientos y publicaciones de la red social.
// Cada usuario es un vértice y A -> B significa que A sigue a B.
// Los posteos, comentarios y compartidos se guardan aparte, no son vértices.
// El mapa por id permite encontrar usuarios sin recorrer todo el grafo.
// En los costos: V son los usuarios, E los seguimientos y P los posteos.
// A cuenta la actividad recorrida, incluyendo sus menciones, y F las entradas del feed.
// Los accesos a mapas se consideran O(1) esperado, aparte del costo de procesar los textos.
public class RedSocial {

    // Todos los seguimientos pesan uno, así los costos de los caminos cuentan saltos.
    public static final double PESO_SEGUIMIENTO = 1.0;

    // Evita tomar el @ de un correo como una mención.
    private static final Pattern MENCION = Pattern.compile("(?<![A-Za-z0-9._])@([A-Za-z0-9._]+)");

    private final IDirectedIGraph<Usuario, WeightedEdge> seguimientos = new DirectedGraph<>();
    private final IDirectedIGraph<Usuario, WeightedEdge> vistaSeguimientos = new GrafoSoloLectura<>(seguimientos);
    private final Map<String, Usuario> usuariosPorId = new LinkedHashMap<>();

    private final Map<String, Posteo> posteos = new LinkedHashMap<>();
    private final Map<String, List<Comentario>> comentariosPorPosteo = new HashMap<>();
    private final Map<String, List<Compartido>> compartidosPorPosteo = new HashMap<>();
    // Ordena los eventos sin depender de la hora de la computadora.
    private long reloj = 0;
    private int ultimoPosteo = 0;
    private int ultimoComentario = 0;

    // Usuarios

    // Valida los datos y devuelve el usuario nuevo con su id normalizado.
    // Lo agrega al grafo y al índice por id; si ya existe, lanza una excepción.
    // Los accesos a las dos estructuras cuestan O(1) esperado.
    public Usuario registrarUsuario(String id, String nombre) {
        Usuario usuario = new Usuario(id, nombre);
        if (usuariosPorId.containsKey(usuario.getId())) {
            throw new IllegalArgumentException("Ya existe un usuario con id '" + usuario.getId() + "'");
        }
        seguimientos.agregarVertice(usuario);
        usuariosPorId.put(usuario.getId(), usuario);
        return usuario;
    }

    // Quita al usuario, sus seguimientos y su actividad, incluidas las menciones que recibió.
    // Así, si se registra otra vez con el mismo id, no hereda la actividad anterior.
    // Si no existe, falla. Costo O(V + E + A).
    public void eliminarUsuario(String id) {
        Usuario usuario = obtenerUsuario(id);
        seguimientos.removerVertice(seguimientos.construirComparable(usuario));
        usuariosPorId.remove(usuario.getId());
        eliminarActividadDe(usuario);
    }

    // Borra primero los posteos propios y después limpia la actividad en posteos ajenos.
    private void eliminarActividadDe(Usuario usuario) {
        List<String> posteosPropios = new ArrayList<>();
        for (Posteo posteo : posteos.values()) {
            if (posteo.getAutor().equals(usuario)) {
                posteosPropios.add(posteo.getId());
            }
        }
        // Juntamos los ids antes de borrar para no modificar el mapa mientras lo recorremos.
        for (String posteoId : posteosPropios) {
            posteos.remove(posteoId);
            comentariosPorPosteo.remove(posteoId);
            compartidosPorPosteo.remove(posteoId);
        }
        for (Map.Entry<String, Posteo> entrada : posteos.entrySet()) {
            String posteoId = entrada.getKey();
            if (entrada.getValue().getMencionados().contains(usuario)) {
                // Los modelos son inmutables: los reemplazamos por copias sin esa mención.
                entrada.setValue(entrada.getValue().sinMencionA(usuario));
            }
            compartidosPorPosteo.get(posteoId).removeIf(c -> c.getUsuario().equals(usuario));
            List<Comentario> comentarios = comentariosPorPosteo.get(posteoId);
            comentarios.removeIf(c -> c.getAutor().equals(usuario));
            comentarios.replaceAll(c -> c.getMencionados().contains(usuario) ? c.sinMencionA(usuario) : c);
        }
    }

    // Normaliza el id y busca en el mapa en O(1) esperado.
    // Devuelve el usuario o lanza una excepción si no existe.
    public Usuario obtenerUsuario(String id) {
        Usuario usuario = usuariosPorId.get(Usuario.normalizarId(id));
        if (usuario == null) {
            throw new IllegalArgumentException("No existe el usuario '" + id + "'");
        }
        return usuario;
    }

    // Comprueba el id en el mapa; si tiene formato inválido, devuelve false.
    public boolean existeUsuario(String id) {
        try {
            return usuariosPorId.containsKey(Usuario.normalizarId(id));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // Devuelve una vista de los usuarios en orden de registro, sin permitir cambios.
    public Collection<Usuario> usuarios() {
        return Collections.unmodifiableCollection(usuariosPorId.values());
    }

    public int cantidadDeUsuarios() {
        return usuariosPorId.size();
    }

    // Seguimientos

    // Valida ambos usuarios y agrega seguidor -> seguido; no permite seguirse a uno mismo.
    // Devuelve false si ya lo seguía. Cuesta O(cantidad de seguidos del seguidor).
    public boolean seguir(String seguidorId, String seguidoId) {
        Usuario seguidor = obtenerUsuario(seguidorId);
        Usuario seguido = obtenerUsuario(seguidoId);
        if (seguidor.equals(seguido)) {
            throw new IllegalArgumentException("Un usuario no puede seguirse a sí mismo: " + seguidor);
        }
        return seguimientos.agregarArista(seguidor, seguido, new WeightedEdge(PESO_SEGUIMIENTO));
    }

    // Quita solo seguidor -> seguido, sin tocar el seguimiento en sentido contrario.
    // Devuelve si lo pudo quitar. Costo O(V + cantidad de seguidos del seguidor).
    public boolean dejarDeSeguir(String seguidorId, String seguidoId) {
        Usuario seguidor = obtenerUsuario(seguidorId);
        Usuario seguido = obtenerUsuario(seguidoId);
        return seguimientos.eliminarArista(seguimientos.construirComparable(seguidor),
                seguimientos.construirComparable(seguido));
    }

    // Comprueba el seguimiento en ese sentido. O(V + cantidad de seguidos del seguidor).
    public boolean sigueA(String seguidorId, String seguidoId) {
        Usuario seguidor = obtenerUsuario(seguidorId);
        Usuario seguido = obtenerUsuario(seguidoId);
        return seguimientos.existeArista(seguimientos.construirComparable(seguidor),
                seguimientos.construirComparable(seguido));
    }

    // Para que sean mutuos tienen que existir las dos aristas, A -> B y B -> A.
    public boolean sonMutuos(String idA, String idB) {
        return sigueA(idA, idB) && sigueA(idB, idA);
    }

    // Devuelve a quiénes sigue, en el orden en que empezó a seguirlos.
    // Costo O(V + cantidad de seguidos), por la búsqueda lineal dentro del grafo.
    public Set<Usuario> seguidos(String id) {
        Usuario usuario = obtenerUsuario(id);
        return Collections.unmodifiableSet(seguimientos.successors(seguimientos.construirComparable(usuario)));
    }

    // Devuelve quiénes lo siguen, en orden de registro.
    // Cuesta O(V + E): el grafo revisa todas las salientes para encontrar las entrantes.
    public Set<Usuario> seguidores(String id) {
        Usuario usuario = obtenerUsuario(id);
        return Collections.unmodifiableSet(seguimientos.predecessors(seguimientos.construirComparable(usuario)));
    }

    // Deja solo los usuarios que están tanto en seguidos como en seguidores.
    // Conserva el orden de los seguidos. Costo O(V + E).
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

    // Contar aristas arma antes el conjunto completo, así que cuesta O(V + E).
    public int cantidadDeSeguimientos() {
        return seguimientos.cantidadDeAristas();
    }

    // Publicaciones

    // Crea y devuelve el posteo, con id propio, orden y menciones tomadas del texto.
    // También prepara sus listas de comentarios y compartidos.
    // Rechaza autores inexistentes y textos vacíos.
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

    // Crea y devuelve un comentario si el usuario es el autor del posteo o lo recibió.
    // Las menciones del comentario también hacen llegar el posteo a esos usuarios.
    // Verificar el alcance cuesta O(V + E + A), además de procesar el texto nuevo.
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

    // Registra un compartido para que el posteo pueda llegar a los seguidores del usuario.
    // El autor no puede compartirlo y los demás tienen que haberlo recibido.
    // Devuelve false si ya lo compartió. Costo O(V + E + A).
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

    // Devuelve quienes reciben el posteo por seguimiento, mención o cadenas de compartidos.
    // No incluye al autor ni repite usuarios. Se recalcula con la red actual. O(V + E + A).
    public Set<Usuario> alcance(String posteoId) {
        return alcanceDe(obtenerPosteo(posteoId));
    }

    // Prepara los reemisores y las menciones para que Difusion haga el recorrido.
    private Set<Usuario> alcanceDe(Posteo posteo) {
        Set<Usuario> compartieron = new LinkedHashSet<>();
        for (Compartido compartido : compartidosPorPosteo.get(posteo.getId())) {
            compartieron.add(compartido.getUsuario());
        }
        return Difusion.alcance(seguimientos, posteo.getAutor(), mencionadosDe(posteo), compartieron);
    }

    // El autor puede comentar directamente; para los demás se comprueba la recepción.
    private boolean puedeInteractuar(Usuario usuario, Posteo posteo) {
        return usuario.equals(posteo.getAutor()) || alcanceDe(posteo).contains(usuario);
    }

    // Arma el feed revisando publicaciones, menciones y compartidos de quienes sigue.
    // No muestra posteos propios y deja una sola entrada por posteo: la del motivo más reciente.
    // Devuelve las entradas de más nuevas a más viejas.
    // Costo O(V + cantidad de seguidos + A + F log F).
    public List<EntradaFeed> feed(String usuarioId) {
        Usuario usuario = obtenerUsuario(usuarioId);
        Set<Usuario> seguidos = seguidos(usuarioId);
        // Usar el id del posteo como clave evita duplicarlo por distintos motivos.
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

    // Reemplaza la entrada solo si la nueva es más reciente; en empate queda la primera.
    private static void considerar(Map<String, EntradaFeed> masReciente, EntradaFeed candidata) {
        String posteoId = candidata.getPosteo().getId();
        EntradaFeed actual = masReciente.get(posteoId);
        if (actual == null || candidata.getOrden() > actual.getOrden()) {
            masReciente.put(posteoId, candidata);
        }
    }

    // Busca el posteo por id en O(1) esperado y falla si no existe.
    public Posteo obtenerPosteo(String posteoId) {
        Posteo posteo = posteoId == null ? null : posteos.get(posteoId);
        if (posteo == null) {
            throw new IllegalArgumentException("No existe el posteo '" + posteoId + "'");
        }
        return posteo;
    }

    // Revisa todos los posteos y devuelve los del usuario en orden de publicación. O(P).
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

    // Vista de los posteos en orden de publicación, sin permitir cambios desde afuera.
    public Collection<Posteo> posteos() {
        return Collections.unmodifiableCollection(posteos.values());
    }

    public int cantidadDePosteos() {
        return posteos.size();
    }

    // Valida el posteo y devuelve una vista de sus comentarios en orden. O(1) esperado.
    public List<Comentario> comentarios(String posteoId) {
        return Collections.unmodifiableList(comentariosPorPosteo.get(obtenerPosteo(posteoId).getId()));
    }

    // Devuelve quiénes compartieron, en orden. Recorre los compartidos de este posteo.
    public List<Usuario> compartidoPor(String posteoId) {
        List<Usuario> usuarios = new ArrayList<>();
        for (Compartido compartido : compartidosPorPosteo.get(obtenerPosteo(posteoId).getId())) {
            usuarios.add(compartido.getUsuario());
        }
        return Collections.unmodifiableList(usuarios);
    }

    // Junta las menciones del posteo y sus comentarios, sin repetir usuarios.
    // Recorre todos sus comentarios, incluso los que no tienen menciones.
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

    // Rechaza texto vacío y devuelve el contenido sin espacios en los extremos.
    private static String validarTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("El texto no puede ser vacío");
        }
        return texto.trim();
    }

    // Extrae los @id del texto y devuelve los usuarios existentes, sin repetir.
    // Ignora al autor y los puntos finales: "@ana." se busca como "ana".
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

    // Entrega el grafo a los algoritmos sin permitir que cambien los seguimientos.
    public IDirectedIGraph<Usuario, WeightedEdge> grafoDeSeguimiento() {
        return vistaSeguimientos;
    }
}
