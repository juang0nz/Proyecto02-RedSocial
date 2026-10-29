package ucu.edu.aed.tda.grafo.model.edge;

import java.util.Objects;

// Arista que va de source a target; la conexión inversa es otra arista.
public class DirectedEdge<V, D> implements Edge<V, D> {
    private final V source, target;
    private final D dato;

    public DirectedEdge(V source, V target, D dato) {
        this.source = Objects.requireNonNull(source);
        this.target = Objects.requireNonNull(target);
        this.dato = dato;
    }

    public V source() {
        return source;
    }

    public V target() {
        return target;
    }

    public D dato() {
        return dato;
    }

    public boolean directed() {
        return true;
    }

    // Compara los extremos en orden. El dato guardado no forma parte de la identidad.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DirectedEdge<?, ?> e)) return false;
        return Objects.equals(source, e.source)
                && Objects.equals(target, e.target);
    }

    @Override
    public int hashCode() {
        int h1 = Objects.hashCode(source);
        int h2 = Objects.hashCode(target);
        // Combina los extremos respetando su orden, igual que equals.
        return 31 * (31 + h1) + h2;
    }

    @Override
    public String toString() {
        return source + " -" + dato + "-> " + target;
    }
}