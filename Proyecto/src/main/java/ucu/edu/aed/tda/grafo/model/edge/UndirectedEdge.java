package ucu.edu.aed.tda.grafo.model.edge;

import java.util.Objects;

// Arista sin dirección: conectar A con B es lo mismo que conectar B con A.
public class UndirectedEdge<V, D> implements Edge<V, D> {
    private final V a, b;
    private final D dato;

    public UndirectedEdge(V u, V v, D dato) {
        this.a = Objects.requireNonNull(u);
        this.b = Objects.requireNonNull(v);
        this.dato = dato;
    }
    public V source() { return a; }    // Estos nombres no le dan dirección a la arista.
    public V target() { return b; }
    public D dato()   { return dato; }
    public boolean directed() { return false; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UndirectedEdge<?, ?> e)) return false;
        // Acepta los extremos en cualquiera de los dos órdenes; el dato no se compara.
        return (Objects.equals(a, e.a) && Objects.equals(b, e.b))
            || (Objects.equals(a, e.b) && Objects.equals(b, e.a));
    }

    @Override public int hashCode() {
        // El hash también tiene que ser el mismo si se intercambian los extremos.
        int h1 = Objects.hashCode(a);
        int h2 = Objects.hashCode(b);
        // Tanto la suma como XOR dan lo mismo al invertir los operandos.
        return (h1 ^ h2) + (h1 + h2);
    }

    @Override public String toString() {
        return "{" + a + " -" + dato + "- " + b + "}";
    }
}