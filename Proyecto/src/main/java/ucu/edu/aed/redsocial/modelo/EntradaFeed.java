package ucu.edu.aed.redsocial.modelo;

import java.util.Objects;

/**
 * Una línea del feed: qué posteo es, por qué le llegó al usuario y a través de quién.
 *
 * Si un posteo le llega por varios motivos, el feed muestra una sola entrada con el
 * motivo más reciente (por ejemplo, un compartido posterior a la publicación).
 */
public final class EntradaFeed {

    public enum Motivo {
        /** Lo publicó alguien que el usuario sigue. via = el autor. */
        PUBLICACION,
        /** Lo compartió alguien que el usuario sigue. via = quien lo compartió. */
        COMPARTIDO,
        /** Arrobaron al usuario en el posteo o en un comentario. via = quien lo arrobó. */
        MENCION
    }

    private final Posteo posteo;
    private final Motivo motivo;
    private final Usuario via;
    private final long orden;

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

    /** Momento del evento que puso el posteo en el feed (publicación, compartido o mención). */
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
