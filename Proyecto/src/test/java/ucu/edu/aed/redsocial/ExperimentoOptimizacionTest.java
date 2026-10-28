package ucu.edu.aed.redsocial;

import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.impl.DirectedGraph;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import java.util.*;

/**
 * Desafio 3: comparar obtencion de seguidores para TODOS los vertices.
 * Original: invocar predecessors(v) para cada usuario (reexplora aristas).
 * Optimizada: Difusion.seguidoresDeCadaVertice, una sola pasada por aristas.
 * El test verifica equivalencia; el benchmark opcional imprime tiempos.
 */
public class ExperimentoOptimizacionTest extends TestCase {
    private DirectedGraph<Integer, WeightedEdge> crear(int n) {
        DirectedGraph<Integer, WeightedEdge> g = new DirectedGraph<>();
        for (int i = 0; i < n; i++) g.agregarVertice(i);
        for (int i = 0; i < n; i++) {
            for (int salto = 1; salto <= 4; salto++) {
                int destino = (i + salto * 7) % n;
                if (i != destino) g.agregarArista(i, destino, new WeightedEdge(1));
            }
        }
        return g;
    }

    private Map<Integer, Set<Integer>> original(DirectedGraph<Integer, WeightedEdge> g) {
        Map<Integer, Set<Integer>> resultado = new LinkedHashMap<>();
        for (Integer v : g.vertices()) {
            resultado.put(v, g.predecessors(g.construirComparable(v)));
        }
        return resultado;
    }

    private Map<Integer, Set<Integer>> optimizada(DirectedGraph<Integer, WeightedEdge> g) {
        Map<Integer, Set<Integer>> resultado = new LinkedHashMap<>();
        for (Map.Entry<Integer, List<Integer>> e : Difusion.seguidoresDeCadaVertice(g).entrySet()) {
            resultado.put(e.getKey(), new LinkedHashSet<>(e.getValue()));
        }
        return resultado;
    }

    public void testEquivalenciaEnGrafosDeDistintosTamanos() {
        for (int n : new int[]{0, 1, 10, 50, 150}) {
            DirectedGraph<Integer, WeightedEdge> g = crear(n);
            assertEquals("Resultados distintos para V=" + n, original(g), optimizada(g));
        }
    }

    /** Ejecutar con -Dexperimento=true para obtener mediciones, sin hacer fallar CI por tiempos. */
    public void testMedicionesOpcionales() {
        if (!Boolean.getBoolean("experimento")) return;
        for (int n : new int[]{100, 300, 600}) {
            DirectedGraph<Integer, WeightedEdge> g = crear(n);
            original(g); optimizada(g); // calentamiento basico
            int repeticiones = 5;
            long originalNs = 0, optimizadaNs = 0;
            for (int r = 0; r < repeticiones; r++) {
                long inicio = System.nanoTime();
                Map<Integer, Set<Integer>> a = original(g);
                originalNs += System.nanoTime() - inicio;
                inicio = System.nanoTime();
                Map<Integer, Set<Integer>> b = optimizada(g);
                optimizadaNs += System.nanoTime() - inicio;
                assertEquals(a, b);
            }
            System.out.printf(Locale.ROOT,
                    "EXPERIMENTO V=%d E=%d original_ms=%.3f optimizada_ms=%.3f mejora_x=%.2f%n",
                    n, g.cantidadDeAristas(), originalNs / (repeticiones * 1e6),
                    optimizadaNs / (repeticiones * 1e6),
                    optimizadaNs == 0 ? 0.0 : (double) originalNs / optimizadaNs);
        }
    }
}
