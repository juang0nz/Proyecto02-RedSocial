package ucu.edu.aed.tda.grafo.impl;

import ucu.edu.aed.tda.grafo.IDirectedGraphAlgorithms;
import ucu.edu.aed.tda.grafo.IDirectedIGraph;
import ucu.edu.aed.tda.grafo.model.IGraph;
import ucu.edu.aed.tda.grafo.model.edge.Edge;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import ucu.edu.aed.tda.grafo.model.result.IDijkstraResult;
import ucu.edu.aed.tda.grafo.model.result.IFloydWarshallResult;
import ucu.edu.aed.tda.grafo.model.result.Path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

// Implementa los caminos y recorridos del grafo dirigido.
// Acá están los algoritmos dirigidos pedidos en el segundo requisito del Desafío 1.
// V y E, en los costos, son la cantidad de vértices y aristas.
// Con DirectedGraph, pedir adyacencias busca el vértice en O(V), no en O(1).
public class DirectedGraphAlgorithms implements IDirectedGraphAlgorithms {

    // Enumera los caminos sin repetir vértices entre los dos extremos.
    // Para resolver "todos los caminos" usamos retroceso: exploramos una rama y luego probamos otra.
    // Si origen y destino coinciden, incluye el camino de ese único vértice con costo cero.
    // Si falta un extremo, devuelve una lista vacía. La cantidad de caminos
    // puede crecer factorialmente: no es una búsqueda de tiempo polinómico.
    @Override
    public <V, D extends WeightedEdge> List<Path<V>> obtenerTodosLosCaminos(Comparable<V> source, Comparable<V> target, IGraph<V, D> grafo) {
        List<Path<V>> caminos = new ArrayList<>();
        V origen = grafo.buscarVertice(source);
        V destino = grafo.buscarVertice(target);
        if (origen == null || destino == null) {
            return caminos;
        }
        // Ambos guardan la rama actual: uno permite consultar y el otro conserva el orden.
        Set<V> visitados = new HashSet<>();
        LinkedList<V> stack = new LinkedList<>();
        obtenerTodosLosCaminosAux(grafo, origen, destino, 0, visitados, stack, caminos);
        return caminos;
    }

    private <V, D extends WeightedEdge> void obtenerTodosLosCaminosAux(IGraph<V, D> grafo, V nodo, V destino, double costo,
                                                                       Set<V> visitados, LinkedList<V> stack, List<Path<V>> caminos) {
        if (visitados.contains(nodo)) {
            return;
        }
        if (nodo.equals(destino)) {
            // Copiamos la rama, porque stack va a cambiar al probar otros caminos.
            List<V> copia = new ArrayList<>(stack);
            copia.add(nodo);
            caminos.add(new Path<>(copia, costo));
            return;
        }
        visitados.add(nodo);
        stack.addLast(nodo);
        for (Edge<V, D> arista : grafo.adyacencias(grafo.construirComparable(nodo))) {
            obtenerTodosLosCaminosAux(grafo, arista.target(), destino, costo + arista.dato().getWeight(), visitados, stack, caminos);
        }
        // Deshacemos este paso para que otras ramas puedan usar el mismo vértice.
        visitados.remove(nodo);
        stack.removeLast();
    }

    // Devuelve costos mínimos y predecesores desde source.
    // Dijkstra resuelve los caminos mínimos desde un origen que pide la consigna.
    // Si no existe el origen, incluso en un grafo vacío, lanza una excepción.
    // Un origen aislado queda con costo cero y los demás con infinito.
    // Esta versión elige el mínimo recorriendo el mapa, sin cola de prioridad.
    // Con DirectedGraph cuesta O(V² + E) y usa O(V) para los mapas y visitados.
    @Override
    public <V, D extends WeightedEdge> IDijkstraResult<V> dijkstra(Comparable<V> source, IDirectedIGraph<V, D> grafo) {
        V origen = grafo.buscarVertice(source);
        if (origen == null) {
            throw new IllegalArgumentException("El vertice de origen no pertenece al grafo");
        }
        // Con pesos negativos ya no sería seguro dar por definitivo el mínimo elegido.
        for (Edge<V, D> arista : grafo.aristas()) {
            if (arista.dato() == null || !Double.isFinite(arista.dato().getWeight())
                    || arista.dato().getWeight() < 0) {
                throw new IllegalArgumentException("Dijkstra requiere pesos finitos y no negativos");
            }
        }
        // Infinito significa que todavía no conocemos un camino hasta ese vértice.
        Map<V, Double> costos = new LinkedHashMap<>();
        Map<V, V> predecesores = new LinkedHashMap<>();
        Set<V> visitados = new HashSet<>();
        for (V vertice : grafo.vertices()) {
            costos.put(vertice, Double.POSITIVE_INFINITY);
        }
        costos.put(origen, 0.0);
        while (visitados.size() < costos.size()) {
            V actual = null;
            double minimo = Double.POSITIVE_INFINITY;
            // Buscamos el pendiente más cercano al origen.
            for (Map.Entry<V, Double> entrada : costos.entrySet()) {
                if (!visitados.contains(entrada.getKey()) && entrada.getValue() < minimo) {
                    actual = entrada.getKey();
                    minimo = entrada.getValue();
                }
            }
            if (actual == null) {
                // Los que quedan son inalcanzables y conservan costo infinito.
                break;
            }
            visitados.add(actual);
            for (Edge<V, D> arista : grafo.adyacencias(grafo.construirComparable(actual))) {
                if (visitados.contains(arista.target())) {
                    continue;
                }
                double nuevoCosto = minimo + arista.dato().getWeight();
                if (!Double.isFinite(nuevoCosto)) {
                    throw new ArithmeticException("El costo de un camino excede el rango de double");
                }
                if (nuevoCosto < costos.get(arista.target())) {
                    // Relajamos la arista: guardamos el costo mejor y por dónde llegamos.
                    // Si dos alternativas cuestan lo mismo, conservamos la que ya estaba.
                    costos.put(arista.target(), nuevoCosto);
                    predecesores.put(arista.target(), actual);
                }
            }
        }
        return new DijkstraResult<>(origen, costos, predecesores);
    }

    // Calcula caminos mínimos entre todos los pares. Tiempo O(V³), espacio O(V²).
    // Para cumplir el requisito de Floyd guardamos tanto los costos como los próximos pasos.
    // Un grafo vacío deja matrices vacías; un vértice aislado conserva su diagonal en cero.
    // Acepta pesos negativos, pero rechaza ciclos de costo negativo.
    @Override
    public <V, D extends WeightedEdge> IFloydWarshallResult<V> floyd(IDirectedIGraph<V, D> grafo) {
        List<V> vertices = new ArrayList<>(grafo.vertices());
        Map<V, Integer> indices = new HashMap<>();
        int cantidad = vertices.size();
        // Los índices permiten usar matrices aunque los vértices no sean números.
        double[][] costos = new double[cantidad][cantidad];
        int[][] siguientes = new int[cantidad][cantidad];
        for (int i = 0; i < cantidad; i++) {
            indices.put(vertices.get(i), i);
            Arrays.fill(costos[i], Double.POSITIVE_INFINITY);
            // -1 indica que no hay camino; la diagonal arranca con costo cero.
            Arrays.fill(siguientes[i], -1);
            costos[i][i] = 0;
            siguientes[i][i] = i;
        }
        for (Edge<V, D> arista : grafo.aristas()) {
            if (arista.dato() == null || !Double.isFinite(arista.dato().getWeight())) {
                throw new IllegalArgumentException("Floyd requiere pesos finitos en todas las aristas");
            }
            int origen = indices.get(arista.source());
            int destino = indices.get(arista.target());
            double peso = arista.dato().getWeight();
            if (peso < costos[origen][destino]) {
                costos[origen][destino] = peso;
                siguientes[origen][destino] = destino;
            }
        }
        // Probamos cada k como intermedio: comparamos ir directo con pasar por k.
        for (int k = 0; k < cantidad; k++) {
            for (int i = 0; i < cantidad; i++) {
                if (costos[i][k] == Double.POSITIVE_INFINITY) {
                    continue;
                }
                for (int j = 0; j < cantidad; j++) {
                    if (costos[k][j] == Double.POSITIVE_INFINITY) {
                        continue;
                    }
                    double nuevoCosto = costos[i][k] + costos[k][j];
                    if (!Double.isFinite(nuevoCosto)) {
                        throw new ArithmeticException("El costo de un camino excede el rango de double");
                    }
                    if (nuevoCosto < costos[i][j]) {
                        // En un empate no cambiamos la ruta: alcanza con guardar un camino mínimo.
                        costos[i][j] = nuevoCosto;
                        // Para llegar a j por k, el primer paso es el mismo que hacia k.
                        siguientes[i][j] = siguientes[i][k];
                    }
                }
            }
        }
        for (int i = 0; i < cantidad; i++) {
            if (costos[i][i] < 0) {
                // Volver al mismo vértice con costo negativo permitiría bajar el costo sin límite.
                throw new IllegalArgumentException("El grafo tiene un ciclo de costo negativo");
            }
        }
        return new FloydWarshallResult<>(vertices, costos, siguientes);
    }

    // Usa la misma idea de intermedios, pero cada arista vale un salto.
    // Esta versión de Warshall resuelve la accesibilidad pedida y además permite consultar saltos.
    // El grafo vacío no tiene pares válidos; un vértice se considera alcanzable desde sí mismo.
    // Devuelve alcance y caminos mínimos en saltos, no en peso. O(V³) y espacio O(V²).
    @Override
    public <V, D extends WeightedEdge> IFloydWarshallResult<V> warshall(IDirectedIGraph<V, D> grafo) {
        List<V> vertices = new ArrayList<>(grafo.vertices());
        int cantidad = vertices.size();
        Map<V, Integer> indices = new HashMap<>();
        // Infinito es inalcanzable y siguientes guarda cómo reconstruir el camino.
        double[][] saltos = new double[cantidad][cantidad];
        int[][] siguientes = new int[cantidad][cantidad];
        for (int i = 0; i < cantidad; i++) {
            indices.put(vertices.get(i), i);
            Arrays.fill(saltos[i], Double.POSITIVE_INFINITY);
            Arrays.fill(siguientes[i], -1);
            saltos[i][i] = 0;
            siguientes[i][i] = i;
        }
        for (Edge<V, D> arista : grafo.aristas()) {
            int origen = indices.get(arista.source());
            int destino = indices.get(arista.target());
            // El peso original no se consulta: cualquier conexión directa cuenta como uno.
            if (saltos[origen][destino] > 1) {
                saltos[origen][destino] = 1;
                siguientes[origen][destino] = destino;
            }
        }
        for (int k = 0; k < cantidad; k++) {
            for (int i = 0; i < cantidad; i++) {
                if (siguientes[i][k] == -1) {
                    continue;
                }
                for (int j = 0; j < cantidad; j++) {
                    if (siguientes[k][j] == -1) {
                        continue;
                    }
                    double nuevoCosto = saltos[i][k] + saltos[k][j];
                    // Solo combinamos tramos existentes; k puede abrir una ruta que antes no había.
                    if (nuevoCosto < saltos[i][j]) {
                        saltos[i][j] = nuevoCosto;
                        siguientes[i][j] = siguientes[i][k];
                    }
                }
            }
        }
        return new FloydWarshallResult<>(vertices, saltos, siguientes);
    }

    // Calcula Floyd una sola vez y elige la menor excentricidad de entrada.
    // Para el centro pedido en la consigna miramos cuánto cuesta llegar al candidato, no salir de él.
    // Vacío da null; con un único vértice, ese vértice es el centro.
    // No reemplaza en empates; si todas dan infinito, devuelve null. Costo O(V³).
    @Override
    public <V, D extends WeightedEdge> V obtenerCentroGrafo(IDirectedIGraph<V, D> grafo) {
        IFloydWarshallResult<V> resultado = floyd(grafo);
        V centro = null;
        double menorExcentricidad = Double.POSITIVE_INFINITY;
        for (V vertice : grafo.vertices()) {
            double excentricidad = excentricidadDeEntrada(grafo, resultado, vertice);
            if (excentricidad < menorExcentricidad) {
                centro = vertice;
                menorExcentricidad = excentricidad;
            }
        }
        return centro;
    }

    // Busca el vértice y calcula su excentricidad usando Floyd. Costo O(V³).
    // La excentricidad usada es la de entrada: el mayor costo mínimo para llegar a él.
    // Si algún origen no lo alcanza, el máximo queda en infinito.
    @Override
    public <V, D extends WeightedEdge> double obtenerExcentricidad(IDirectedIGraph<V, D> grafo, Comparable<V> vertexCriteria) {
        V vertice = grafo.buscarVertice(vertexCriteria);
        if (vertice == null) {
            throw new IllegalArgumentException("El vertice no pertenece al grafo");
        }
        return excentricidadDeEntrada(grafo, floyd(grafo), vertice);
    }

    private <V, D extends WeightedEdge> double excentricidadDeEntrada(
            IDirectedIGraph<V, D> grafo, IFloydWarshallResult<V> resultado, V destino) {
        // Mira los costos de llegada al destino. Arranca en cero, así que nunca da negativo.
        double maximo = 0;
        for (V origen : grafo.vertices()) {
            maximo = Math.max(maximo, resultado.getCost(origen, destino));
        }
        return maximo;
    }

    // DFS: usa una pila para avanzar por una rama antes de pasar a la siguiente.
    // Este es el recorrido en profundidad solicitado; visitados corta las repeticiones por ciclos.
    // Desde un aislado solo visita ese vértice; un origen inexistente se rechaza.
    // Aplica consumer una vez por vértice alcanzado, sin devolver una colección.
    // Con DirectedGraph cuesta O(V² + E) en el peor caso por las búsquedas de adyacencias.
    @Override
    public <V, D> void recorridoEnProfundidad(IGraph<V, D> grafo, Comparable<V> sourceCriteria, Consumer<V> consumer) {
        V origen = grafo.buscarVertice(sourceCriteria);
        if (origen == null) {
            throw new IllegalArgumentException("El vertice de origen no pertenece al grafo");
        }
        java.util.Objects.requireNonNull(consumer, "consumer");
        Set<V> visitados = new HashSet<>();
        Deque<V> pendientes = new ArrayDeque<>();
        pendientes.push(origen);
        while (!pendientes.isEmpty()) {
            V actual = pendientes.pop();
            if (!visitados.add(actual)) {
                continue;
            }
            consumer.accept(actual);
            List<Edge<V, D>> adyacentes = new ArrayList<>(grafo.adyacencias(grafo.construirComparable(actual)));
            // Apilamos al revés para visitar primero al primer vecino de la lista.
            for (int i = adyacentes.size() - 1; i >= 0; i--) {
                V vecino = adyacentes.get(i).target();
                if (!visitados.contains(vecino)) {
                    pendientes.push(vecino);
                }
            }
        }
    }

    // BFS: usa una cola para visitar primero los vecinos más cercanos en saltos.
    // Resuelve la búsqueda en amplitud de la consigna sin usar los pesos de las aristas.
    // Solo visita la parte alcanzable desde el origen y rechaza un origen que no existe.
    // Aplica consumer a cada alcanzado. Con DirectedGraph cuesta O(V² + E).
    @Override
    public <V, D> void recorridoEnAmplitud(IGraph<V, D> grafo, Comparable<V> sourceCriteria, Consumer<V> consumer) {
        V origen = grafo.buscarVertice(sourceCriteria);
        if (origen == null) {
            throw new IllegalArgumentException("El vertice de origen no pertenece al grafo");
        }
        java.util.Objects.requireNonNull(consumer, "consumer");
        Set<V> visitados = new HashSet<>();
        Deque<V> pendientes = new ArrayDeque<>();
        visitados.add(origen);
        pendientes.addLast(origen);
        while (!pendientes.isEmpty()) {
            V actual = pendientes.removeFirst();
            consumer.accept(actual);
            for (Edge<V, D> arista : grafo.adyacencias(grafo.construirComparable(actual))) {
                // Lo marcamos al encolarlo, así no queda pendiente más de una vez.
                if (visitados.add(arista.target())) {
                    pendientes.addLast(arista.target());
                }
            }
        }
    }

    // Ordena con Kahn: va sacando vértices que no tienen entradas pendientes.
    // Para la clasificación topológica pedida, contamos cuántas dependencias quedan por vértice.
    // También incluye aislados; con grafo vacío devuelve una lista vacía.
    // Devuelve el orden o lanza una excepción si hay ciclos.
    // Con DirectedGraph cuesta O(V² + E), no O(V + E), por la búsqueda de cada lista.
    @Override
    public <V, D> List<V> calcularClasificacionTopologica(IDirectedIGraph<V, D> grafo) {
        Map<V, Integer> entradas = new LinkedHashMap<>();
        for (V vertice : grafo.vertices()) {
            entradas.put(vertice, 0);
        }
        for (Edge<V, D> arista : grafo.aristas()) {
            entradas.put(arista.target(), entradas.get(arista.target()) + 1);
        }
        Deque<V> pendientes = new ArrayDeque<>();
        for (Map.Entry<V, Integer> entrada : entradas.entrySet()) {
            if (entrada.getValue() == 0) {
                pendientes.addLast(entrada.getKey());
            }
        }
        // La cola empieza con los que no dependen de ningún otro vértice.
        List<V> orden = new ArrayList<>();
        while (!pendientes.isEmpty()) {
            V actual = pendientes.removeFirst();
            orden.add(actual);
            for (Edge<V, D> arista : grafo.adyacencias(grafo.construirComparable(actual))) {
                V destino = arista.target();
                // Al sacar actual, su arista deja de ser una entrada pendiente del destino.
                int restante = entradas.get(destino) - 1;
                entradas.put(destino, restante);
                if (restante == 0) {
                    pendientes.addLast(destino);
                }
            }
        }
        if (orden.size() != entradas.size()) {
            // Si quedaron vértices, se están bloqueando entre sí por un ciclo.
            throw new IllegalArgumentException("No existe orden topologico: el grafo contiene ciclos");
        }
        return List.copyOf(orden);
    }

    // En un grafo sin ciclos, enumera caminos de fuentes a sumideros y los ordena
    // de mayor a menor costo. Devuelve todos, no solamente el de costo máximo.
    // Puede generar una cantidad exponencial de caminos, más el costo de ordenarlos.
    public <V, D extends WeightedEdge> List<Path<V>> caminoCritico(IDirectedIGraph<V, D> grafo) {
        if (grafo.tieneCiclos()) {
            throw new IllegalArgumentException("El grafo tiene ciclos, no se puede calcular el camino critico");
        }
        List<Path<V>> caminos = new ArrayList<>();
        for (V inicio : grafo.vertices()) {
            // Las fuentes no reciben aristas; los sumideros no tienen salientes.
            if (grafo.gradoDeEntrada(grafo.construirComparable(inicio)) == 0) {
                for (V fin : grafo.vertices()) {
                    if (grafo.gradoDeSalida(grafo.construirComparable(fin)) == 0) {
                        caminos.addAll(obtenerTodosLosCaminos(grafo.construirComparable(inicio), grafo.construirComparable(fin), grafo));
                    }
                }
            }
        }
        caminos.sort((a, b) -> Double.compare(b.getCost(), a.getCost()));
        return caminos;
    }
}