package ucu.edu.aed.redsocial.modelo;

import java.util.Objects;

// Registra quién compartió un posteo y en qué orden ocurrió.
// Ese orden permite subir el posteo en el feed cuando alguien lo comparte.
public final class Compartido {

    private final String posteoId;
    private final Usuario usuario;
    private final long orden;

    // Relaciona el id del posteo con quien lo compartió y el orden del evento.
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
