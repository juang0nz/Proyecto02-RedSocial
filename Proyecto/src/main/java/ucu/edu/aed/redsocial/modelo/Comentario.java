package ucu.edu.aed.redsocial.modelo;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

// Guarda un comentario y el id del posteo al que pertenece.
// Sus menciones hacen llegar el posteo a esos usuarios, aunque no sigan al autor.
public final class Comentario {

    private final String id;
    private final String posteoId;
    private final Usuario autor;
    private final String texto;
    private final long orden;
    private final Set<Usuario> mencionados;

    // Copia las menciones para que el comentario no cambie si se modifica el conjunto recibido.
    public Comentario(String id, String posteoId, Usuario autor, String texto, long orden,
                      Set<Usuario> mencionados) {
        this.id = Objects.requireNonNull(id);
        this.posteoId = Objects.requireNonNull(posteoId);
        this.autor = Objects.requireNonNull(autor);
        this.texto = Objects.requireNonNull(texto);
        this.orden = orden;
        this.mencionados = Collections.unmodifiableSet(new LinkedHashSet<>(mencionados));
    }

    // Devuelve una copia sin mencionar al usuario eliminado, pero conserva el texto.
    // Cuesta O(M), siendo M la cantidad de mencionados.
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

    // Devuelve las menciones guardadas, en orden y sin permitir cambios.
    public Set<Usuario> getMencionados() {
        return mencionados;
    }

    // Dos comentarios con el mismo id se consideran el mismo comentario.
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
