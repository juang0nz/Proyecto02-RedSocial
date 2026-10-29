package ucu.edu.aed.redsocial.modelo;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

// Guarda una publicación, su autor y sus menciones. No es un vértice del grafo.
// Los comentarios y compartidos se guardan aparte en RedSocial.
// orden indica cuándo se publicó dentro de la secuencia de eventos de la red.
public final class Posteo {

    private final String id;
    private final Usuario autor;
    private final String texto;
    private final long orden;
    private final Set<Usuario> mencionados;

    // Guarda los datos y copia las menciones para que no se puedan cambiar desde afuera.
    public Posteo(String id, Usuario autor, String texto, long orden, Set<Usuario> mencionados) {
        this.id = Objects.requireNonNull(id);
        this.autor = Objects.requireNonNull(autor);
        this.texto = Objects.requireNonNull(texto);
        this.orden = orden;
        this.mencionados = Collections.unmodifiableSet(new LinkedHashSet<>(mencionados));
    }

    // Devuelve una copia sin la mención al usuario eliminado; no cambia el texto original.
    // Cuesta O(M), siendo M la cantidad de mencionados.
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

    // Devuelve las menciones guardadas, conservando el orden y sin permitir cambios.
    public Set<Usuario> getMencionados() {
        return mencionados;
    }

    // La identidad del posteo depende solo de su id.
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
