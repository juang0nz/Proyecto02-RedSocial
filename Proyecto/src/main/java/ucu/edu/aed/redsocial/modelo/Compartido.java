package ucu.edu.aed.redsocial.modelo;

import java.util.Objects;

/**
 * Registro de que un usuario compartió un posteo. Es una clase propia (y no solo un
 * conjunto de usuarios dentro del posteo) porque el feed necesita saber cuándo se
 * compartió: un compartido reciente sube el posteo en el feed de los seguidores.
 */
public final class Compartido {

    private final String posteoId;
    private final Usuario usuario;
    private final long orden;

    public Compartido(String posteoId, Usuario usuario, long orden) {
        this.posteoId = Objects.requireNonNull(posteoId);
        this.usuario = Objects.requireNonNull(usuario);
        this.orden = orden;
    }

    public String getPosteoId() {
        return posteoId;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public long getOrden() {
        return orden;
    }

    @Override
    public String toString() {
        return usuario + " compartió " + posteoId;
    }
}
