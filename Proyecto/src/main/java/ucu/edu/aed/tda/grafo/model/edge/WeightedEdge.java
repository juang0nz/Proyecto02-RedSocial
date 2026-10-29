package ucu.edu.aed.tda.grafo.model.edge;

// Guarda el peso que se usa como dato de una arista en los cálculos de caminos.
// La validación del peso queda a cargo del algoritmo que lo usa.
public class WeightedEdge {
    private final double weight;


    public WeightedEdge(double weight) {
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return String.valueOf(weight);
    }
}
