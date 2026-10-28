package ucu.edu.aed.redsocial.modelo;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Publicación de un usuario. No es un vértice del grafo de seguimiento: vive en una
 * colección aparte de RedSocial y referencia a su autor y a los usuarios que menciona.
 *
 * Es inmutable. Los comentarios y los compartidos se guardan en RedSocial, porque tienen
 * su propio autor y su propio momento. La identidad es el id.
 *
 * El orden es un contador global de la red (1, 2, 3, ...): sirve para ordenar el feed sin
 * depender del reloj de la máquina, así la demo y los tests dan siempre lo mismo.
 */
public final class Posteo {

    private final String id;
    private final Usuario autor;
    private final String texto;
    private final long orden;
    private final Set<Usuario> mencionados;

    public Posteo(String id, Usuario autor, String texto, long orden, Set<Usuario> mencionados) {
        this.id = Objects.requireNonNull(id);
        this.autor = Objects.requireNonNull(autor);
        this.texto = Objects.requireNonNull(texto);
        this.orden = orden;
        this.mencionados = Collections.unmodifiableSet(new LinkedHashSet<>(mencionados));
    }

    /** Copia del posteo sin la mención a ese usuario. Se usa al eliminar usuarios. */
    public Posteo sinMencionA(Usuario usuario) {
        Set<Usuario> restantes = new LinkedHashSet<>(mencionados);
        restantes.remove(usuario);
        return new Posteo(id, autor, texto, orden, restantes);
    }

    public String getId() {
        return id;
    }

    public Usuario getAutor() {
        return autor;
    }

    public String getTexto() {
        return texto;
    }

    public long getOrden() {
        return orden;
    }

    /** Usuarios arrobados en el texto del posteo, en orden de aparición. */
    public Set<Usuario> getMencionados() {
        return mencionados;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Posteo otro)) return false;
        return id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id + " de " + autor + ": \"" + texto + "\"";
    }
}
