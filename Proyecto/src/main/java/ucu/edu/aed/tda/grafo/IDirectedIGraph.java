package ucu.edu.aed.tda.grafo;


import ucu.edu.aed.tda.grafo.model.IGraph;

import java.util.Set;

// Grafo dirigido: que A apunte a B no significa que B apunte a A.
// Agrega las consultas de sucesores y predecesores que pide el Desafío 1.
public interface IDirectedIGraph<V, D> extends IGraph<V, D> {

    // Devuelve los vecinos a los que apunta el vértice.
    Set<V> successors(Comparable<V> criteria);

    // Devuelve los vecinos que apuntan al vértice.
    Set<V> predecessors(Comparable<V> criteria);

    // Cuenta a cuántos vecinos apunta el vértice.
    default int gradoDeSalida(Comparable<V> criteria) {
        return successors(criteria).size();
    }

    // Cuenta cuántos vecinos apuntan al vértice.
    default int gradoDeEntrada(Comparable<V> criteria) {
        return predecessors(criteria).size();
    }

}
