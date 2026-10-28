package ucu.edu.aed.redsocial.modelo;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Comentario sobre un posteo. Inmutable; referencia al posteo por su id.
 *
 * Las menciones de un comentario cuentan para el posteo: el usuario arrobado recibe el
 * posteo comentado aunque no siga a nadie que lo haya publicado o compartido.
 */
public final class Comentario {

    private final String id;
    private final String posteoId;
    private final Usuario autor;
    private final String texto;
    private final long orden;
    private final Set<Usuario> mencionados;

    public Comentario(String id, String posteoId, Usuario autor, String texto, long orden,
                      Set<Usuario> mencionados) {
        this.id = Objects.requireNonNull(id);
        this.posteoId = Objects.requireNonNull(posteoId);
        this.autor = Objects.requireNonNull(autor);
        this.texto = Objects.requireNonNull(texto);
        this.orden = orden;
        this.mencionados = Collections.unmodifiableSet(new LinkedHashSet<>(mencionados));
    }

    /** Copia del comentario sin la mención a ese usuario. Se usa al eliminar usuarios. */
    public Comentario sinMencionA(Usuario usuario) {
        Set<Usuario> restantes = new LinkedHashSet<>(mencionados);
        restantes.remove(usuario);
        return new Comentario(id, posteoId, autor, texto, orden, restantes);
    }

    public String getId() {
        return id;
    }

    public String getPosteoId() {
        return posteoId;
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

    /** Usuarios arrobados en el comentario, en orden de aparición. */
    public Set<Usuario> getMencionados() {
        return mencionados;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Comentario otro)) return false;
        return id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id + " de " + autor + " en " + posteoId + ": \"" + texto + "\"";
    }
}
