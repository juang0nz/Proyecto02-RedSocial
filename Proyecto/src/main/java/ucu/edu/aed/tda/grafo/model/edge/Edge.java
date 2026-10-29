package ucu.edu.aed.tda.grafo.model.edge;

// Una conexión entre dos vértices, con un dato asociado y una posible dirección.
public interface Edge<V, D> {
    // En un grafo no dirigido, origen y destino solo indican desde qué extremo se la mira.
    V source();

    V target();

    D dato();

    // Indica si el orden de los extremos importa.
    boolean directed();
}