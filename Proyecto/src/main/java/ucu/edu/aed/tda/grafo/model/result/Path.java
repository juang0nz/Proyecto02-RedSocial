package ucu.edu.aed.tda.grafo.model.result;

import java.util.List;

// Guarda la secuencia de vértices de un camino y su costo total.
public class Path<V> {
    private final List<V> path;
    private final double cost;

    // Guarda la lista recibida sin copiarla; si esa lista cambia, el camino también.
    public Path(List<V> path, double cost) {
        this.path = path;
        this.cost = cost;
    }

    public List<V> getPath() {
        return path;
    }

    public double getCost() {
        return cost;
    }
}
