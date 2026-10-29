package ucu.edu.aed.tda.grafo;


import ucu.edu.aed.tda.grafo.model.IGraph;

// Grafo no dirigido: una conexión entre A y B se puede recorrer en ambos sentidos.
// Para este requisito del Desafío 1 alcanza con las operaciones heredadas de IGraph;
// la diferencia está en cómo UndirectedGraph guarda y recorre las conexiones.
public interface IUndirectedGraph<V, D> extends IGraph<V, D> {
}
