package ucu.edu.aed.redsocial;

import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.impl.DirectedGraph;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;
import java.util.List;
import java.util.Set;

public class ConsultasAdicionalesTest extends TestCase {
    private DirectedGraph<String, WeightedEdge> grafo(String... usuarios) {
        DirectedGraph<String, WeightedEdge> g = new DirectedGraph<>();
        g.agregarVertices(List.of(usuarios));
        return g;
    }

    private void seguir(DirectedGraph<String, WeightedEdge> g, String a, String b) {
        g.agregarArista(a, b, new WeightedEdge(1));
    }

    public void testAlcanzablesHastaKYCiclos() {
        DirectedGraph<String, WeightedEdge> g = grafo("A", "B", "C", "D", "X");
        seguir(g, "A", "B"); seguir(g, "B", "C"); seguir(g, "C", "A");
        seguir(g, "C", "D");
        assertEquals(Set.of(), ConsultasAdicionales.alcanzablesHastaK(g, "A", 0));
        assertEquals(Set.of("B"), ConsultasAdicionales.alcanzablesHastaK(g, "A", 1));
        assertEquals(Set.of("B", "C"), ConsultasAdicionales.alcanzablesHastaK(g, "A", 2));
        assertEquals(Set.of("B", "C", "D"), ConsultasAdicionales.alcanzablesHastaK(g, "A", 9));
        assertEquals(Set.of(), ConsultasAdicionales.alcanzablesHastaK(g, "X", 3));
        try {
            ConsultasAdicionales.alcanzablesHastaK(g, "A", -1);
            fail("k negativo");
        } catch (IllegalArgumentException expected) { }
    }

    public void testSeguidoresComunes() {
        DirectedGraph<String, WeightedEdge> g = grafo("A", "B", "C", "D", "X");
        seguir(g, "C", "A"); seguir(g, "C", "B");
        seguir(g, "D", "A"); seguir(g, "D", "B");
        seguir(g, "X", "A");
        assertEquals(Set.of("C", "D"), ConsultasAdicionales.seguidoresComunes(g, "A", "B"));
        assertEquals(Set.of(), ConsultasAdicionales.seguidoresComunes(g, "B", "X"));
    }

    public void testCaminoEvitandoCuenta() {
        DirectedGraph<String, WeightedEdge> g = grafo("A", "B", "C", "D", "X");
        seguir(g, "A", "B"); seguir(g, "B", "D");
        seguir(g, "A", "C"); seguir(g, "C", "D");
        assertEquals(List.of("A", "C", "D"), ConsultasAdicionales.caminoEvitando(g, "A", "D", "B"));
        assertEquals(List.of(), ConsultasAdicionales.caminoEvitando(g, "A", "D", "A"));
        assertEquals(List.of(), ConsultasAdicionales.caminoEvitando(g, "X", "D", "B"));
        assertEquals(List.of("A"), ConsultasAdicionales.caminoEvitando(g, "A", "A", "B"));
    }

    public void testGruposDeSeguimientoMutuo() {
        DirectedGraph<String, WeightedEdge> g = grafo("A", "B", "C", "D", "X");
        seguir(g, "A", "B"); seguir(g, "B", "A");
        seguir(g, "B", "C"); seguir(g, "C", "B");
        seguir(g, "C", "D");
        assertEquals(List.of(Set.of("A", "B", "C"), Set.of("D"), Set.of("X")),
                ConsultasAdicionales.gruposDeSeguimientoMutuo(g));
        assertEquals(List.of(), ConsultasAdicionales.gruposDeSeguimientoMutuo(grafo()));
    }

    public void testPerdidaPorEliminarSeguimientoSinModificarGrafo() {
        DirectedGraph<String, WeightedEdge> g = grafo("A", "B", "C", "D", "X");
        seguir(g, "A", "B"); seguir(g, "B", "C"); seguir(g, "C", "D");
        assertEquals(Set.of("C", "D"), ConsultasAdicionales.perdidaPorEliminarSeguimiento(g, "A", "B", "C"));
        assertEquals(Set.of(), ConsultasAdicionales.perdidaPorEliminarSeguimiento(g, "X", "B", "C"));
        assertEquals(Set.of("B", "C", "D"), ConsultasAdicionales.perdidaPorEliminarSeguimiento(g, "A", "A", "B"));
        assertEquals(Set.of("B"), ConsultasAdicionales.alcanzablesHastaK(g, "A", 1));
        try {
            ConsultasAdicionales.perdidaPorEliminarSeguimiento(g, "A", "X", "D");
            fail("Arista inexistente");
        } catch (IllegalArgumentException expected) { }
    }

    public void testUsuariosInexistentes() {
        DirectedGraph<String, WeightedEdge> g = grafo("A", "B");
        try {
            ConsultasAdicionales.seguidoresComunes(g, "A", "Z");
            fail("Usuario inexistente");
        } catch (IllegalArgumentException expected) { }
        try {
            ConsultasAdicionales.caminoEvitando(g, "A", "B", "Z");
            fail("Usuario inexistente");
        } catch (IllegalArgumentException expected) { }
    }
}
