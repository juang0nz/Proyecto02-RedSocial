package ucu.edu.aed.redsocial;

import ucu.edu.aed.tda.grafo.IDirectedIGraph;
import ucu.edu.aed.tda.grafo.model.edge.Edge;

import java.util.List;
import java.util.Objects;
import java.util.Set;

// Deja consultar el grafo de seguimiento, pero rechaza las operaciones que lo modifican.
// Así los cambios pasan por RedSocial y sus validaciones.
// No hace una copia: las consultas usan el grafo original y tienen el mismo costo.
public final class GrafoSoloLectura<V, D> implements IDirectedIGraph<V, D> {

    private final IDirectedIGraph<V, D> grafo;

    // Guarda la referencia al grafo; por eso la vista refleja sus cambios posteriores.
    public GrafoSoloLectura(IDirectedIGraph<V, D> grafo) {
        this.grafo = Objects.requireNonNull(grafo);
    }

    private static UnsupportedOperationException soloLectura() {
        return new UnsupportedOperationException(
                "El grafo de seguimiento es de solo lectura; modificalo a través de RedSocial");
    }

    // Estas operaciones fallan en vez de modificar el grafo.

    @Override
    public boolean agregarVertice(V vertex) {
        throw soloLectura();
    }

    @Override
    public boolean agregarArista(V source, V target, D dato) {
        throw soloLectura();
    }

    @Override
    public boolean eliminarArista(Comparable<V> source, Comparable<V> target) {
        throw soloLectura();
    }

    @Override
    public boolean removerVertice(Comparable<V> criteria) {
        throw soloLectura();
    }

    @Override
    public void vaciar() {
        throw soloLectura();
    }

    // Para leer, usamos los métodos del grafo original sin cambiar sus resultados.

    @Override
    public V buscarVertice(Comparable<V> criterio) {
        return grafo.buscarVertice(criterio);
    }

    @Override
    public Set<V> vertices() {
        return grafo.vertices();
    }

    @Override
    public Set<Edge<V, D>> aristas() {
        return grafo.aristas();
    }

    @Override
    public boolean existeVertice(Comparable<V> criterio) {
        return grafo.existeVertice(criterio);
    }

    @Override
    public boolean existeArista(Comparable<V> sourceCriteria, Comparable<V> targetCriteria) {
        return grafo.existeArista(sourceCriteria, targetCriteria);
    }

    @Override
    public Edge<V, D> obtenerArista(Comparable<V> sourceCriteria, Comparable<V> targetCriteria) {
        return grafo.obtenerArista(sourceCriteria, targetCriteria);
    }

    @Override
    public List<Edge<V, D>> adyacencias(Comparable<V> verticeCriteria) {
        return grafo.adyacencias(verticeCriteria);
    }

    @Override
    public Set<V> successors(Comparable<V> criteria) {
        return grafo.successors(criteria);
    }

    @Override
    public Set<V> predecessors(Comparable<V> criteria) {
        return grafo.predecessors(criteria);
    }

    @Override
    public int gradoDeSalida(Comparable<V> criteria) {
        return grafo.gradoDeSalida(criteria);
    }

    @Override
    public int gradoDeEntrada(Comparable<V> criteria) {
        return grafo.gradoDeEntrada(criteria);
    }

    @Override
    public int cantidadDeVertices() {
        return grafo.cantidadDeVertices();
    }

    @Override
    public int cantidadDeAristas() {
        return grafo.cantidadDeAristas();
    }

    @Override
    public boolean esConexo() {
        return grafo.esConexo();
    }

    @Override
    public boolean tieneCiclos() {
        return grafo.tieneCiclos();
    }

    @Override
    public Comparable<V> construirComparable(V vertice) {
        return grafo.construirComparable(vertice);
    }
}
