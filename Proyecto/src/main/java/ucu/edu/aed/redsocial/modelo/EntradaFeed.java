package ucu.edu.aed.redsocial.modelo;

import java.util.Objects;

// Representa un posteo dentro del feed, con el motivo por el que apareció.
// RedSocial elige la entrada más reciente si un posteo llega por varios motivos.
public final class EntradaFeed {

    // Distingue las tres formas en que un posteo puede aparecer en el feed.
    public enum Motivo {
        // Lo publicó alguien que sigue el usuario; via es el autor.
        PUBLICACION,
        // Lo compartió alguien que sigue el usuario; via es quien compartió.
        COMPARTIDO,
        // Lo mencionaron en el posteo o en un comentario; via es quien lo mencionó.
        MENCION
    }

    private final Posteo posteo;
    private final Motivo motivo;
    private final Usuario via;
    private final long orden;

    // Guarda el posteo y el evento que explica su aparición en el feed.
    public EntradaFeed(Posteo posteo, Motivo motivo, Usuario via, long orden) {
        this.posteo = Objects.requireNonNull(posteo);
        this.motivo = Objects.requireNonNull(motivo);
        this.via = Objects.requireNonNull(via);
        this.orden = orden;
    }

    public Posteo getPosteo() {
        return posteo;
    }

    public Motivo getMotivo() {
        return motivo;
    }

    public Usuario getVia() {
        return via;
    }

    // Es el orden del evento que lo hizo aparecer, no necesariamente el de publicación.
    public long getOrden() {
        return orden;
    }

    @Override
    public String toString() {
        String como;
        switch (motivo) {
            case PUBLICACION:
                como = "publicado por ";
                break;
            case COMPARTIDO:
                como = "compartido por ";
                break;
            default:
                como = "mencion de ";
        }
        return posteo.getId() + " (" + como + via + ")";
    }
}
