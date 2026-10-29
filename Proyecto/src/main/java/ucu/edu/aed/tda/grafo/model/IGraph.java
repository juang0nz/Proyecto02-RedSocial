package ucu.edu.aed.tda.grafo.model;


import ucu.edu.aed.tda.grafo.model.edge.Edge;

import java.util.Collection;
import java.util.List;
import java.util.Set;

// Define las operaciones comunes a los grafos.
// Es la base del primer requisito del Desafío 1: vértices, aristas, adyacencias,
// conexidad y ciclos. DirectedGraph y UndirectedGraph ponen en práctica estas operaciones.
// V es el tipo de los vértices y D es el dato que guarda cada arista.
public interface IGraph<V, D> {
    // Agrega el vértice y devuelve true si lo pudo agregar.
    boolean agregarVertice(V vertex);

    // Intenta agregar cada vértice de la colección.
    default void agregarVertices(Collection<V> vertices) {
        vertices.forEach(this::agregarVertice);
    }


    // Busca el vértice usando el criterio recibido.
    V buscarVertice(Comparable<V> criterio);

    // Intenta conectar source con target guardando dato en la arista.
    // Devuelve true si la agregó.
    boolean agregarArista(V source, V target, D dato);

    // Elimina la arista entre los vértices indicados por los criterios.
    boolean eliminarArista(Comparable<V> source, Comparable<V> target);

    // Elimina el vértice buscado y devuelve true si lo pudo quitar.
    boolean removerVertice(Comparable<V> criteria);

    // Devuelve los vértices del grafo.
    Set<V> vertices();

    // Devuelve las aristas del grafo.
    Set<Edge<V, D>> aristas();

    // Revisa los vértices hasta encontrar uno que coincida con el criterio.
    // La coincidencia se reconoce cuando compareTo devuelve 0.
    default boolean existeVertice(Comparable<V> criterio) {
        return vertices().stream().anyMatch(x -> criterio.compareTo(x) == 0);
    }

    // Indica si existe la conexión. En un grafo dirigido importa el sentido.
    boolean existeArista(Comparable<V> sourceCriteria, Comparable<V> targetCriteria);

    // Busca una arista usando los criterios de origen y destino.
    Edge<V, D> obtenerArista(Comparable<V> sourceCriteria, Comparable<V> targetCriteria);

    // Permite buscar la arista pasando los vértices directamente.
    // Los convierte en criterios que comparan con equals y usa la otra versión.
    default Edge<V, D> obtenerArista(V sourceCriteria, V targetCriteria) {
        return obtenerArista(construirComparable(sourceCriteria), construirComparable(targetCriteria));
    }

    // Devuelve las aristas que salen del vértice buscado.
    // En un grafo no dirigido, source() representa al vértice consultado.
    List<Edge<V, D>> adyacencias(Comparable<V> verticeCriteria);

    // Cuenta los vértices del grafo.
    default int cantidadDeVertices() {
        return vertices().size();
    }

    // Cuenta las aristas del grafo.
    default int cantidadDeAristas() {
        return aristas().size();
    }

    // Indica si el grafo es conexo; el criterio depende del tipo de grafo.
    boolean esConexo();

    // Quita todos los vértices y aristas.
    void vaciar();


    // Indica si hay algún ciclo en el grafo.
    boolean tieneCiclos();


    // Arma un criterio para buscar este vértice usando equals.
    // Devuelve 0 si coincide y 1 si no: sirve para buscar, no para ordenar.
    default Comparable<V> construirComparable(V vertice) {
        return x -> x.equals(vertice) ? 0 : 1;
    }
}
