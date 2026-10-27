package ucu.edu.aed.redsocial;

import junit.framework.TestCase;
import ucu.edu.aed.tda.grafo.impl.DirectedGraph;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Pruebas del algoritmo de difusión sobre un grafo de Strings, sin el modelo social.
 * Recordar: A -> B significa "A sigue a B", así que el contenido de B le llega a A.
 */
public class DifusionTest extends TestCase {

    private DirectedGraph<String, WeightedEdge> grafo(String... vertices) {
        DirectedGraph<String, WeightedEdge> grafo = new DirectedGraph<>();
        grafo.agregarVertices(List.of(vertices));
        return grafo;
    }

    private void sigue(DirectedGraph<String, WeightedEdge> grafo, String seguidor, String seguido) {
        grafo.agregarArista(seguidor, seguido, new WeightedEdge(1));
    }

    private static List<String> lista(Set<String> conjunto) {
        return List.copyOf(conjunto);
    }

    public void testSeguidoresDeCadaVerticeSonLosPredecesores() {
        DirectedGraph<String, WeightedEdge> g = grafo("A", "B", "C");
        sigue(g, "A", "B");
        sigue(g, "C", "B");
        sigue(g, "B", "C");

        Map<String, List<String>> seguidores = Difusion.seguidoresDeCadaVertice(g);

        assertEquals(List.of("A", "C"), seguidores.get("B"));
        assertEquals(List.of("B"), seguidores.get("C"));
        assertEquals(List.of(), seguidores.get("A"));
    }

    public void testSinCompartidosSoloLlegaALosSeguidoresDirectos() {
        DirectedGraph<String, WeightedEdge> g = grafo("A", "B", "C");
        sigue(g, "B", "A");
        sigue(g, "C", "B");   // C sigue a B, pero B no comparte

        assertEquals(List.of("B"), lista(Difusion.alcance(g, "A", Set.of(), Set.of())));
    }

    public void testCadenaDeCompartidos() {
        DirectedGraph<String, WeightedEdge> g = grafo("A", "B", "C", "D");
        sigue(g, "B", "A");
        sigue(g, "C", "B");
        sigue(g, "D", "C");

        assertEquals(List.of("B", "C"), lista(Difusion.alcance(g, "A", Set.of(), Set.of("B"))));
        assertEquals(List.of("B", "C", "D"), lista(Difusion.alcance(g, "A", Set.of(), Set.of("B", "C"))));
    }

    public void testUnReemisorQueNoLoRecibioNoDifunde() {
        DirectedGraph<String, WeightedEdge> g = grafo("A", "B", "X", "Y");
        sigue(g, "B", "A");
        sigue(g, "Y", "X");   // X no sigue a A ni fue mencionado

        assertEquals(List.of("B"), lista(Difusion.alcance(g, "A", Set.of(), Set.of("X"))));
    }

    public void testMencionadoQueCompartePropagaASusSeguidores() {
        DirectedGraph<String, WeightedEdge> g = grafo("A", "M", "S1", "S2");
        sigue(g, "S1", "M");
        sigue(g, "S2", "M");   // M no sigue a A

        assertEquals(List.of("M"), lista(Difusion.alcance(g, "A", List.of("M"), Set.of())));
        assertEquals(List.of("M", "S1", "S2"), lista(Difusion.alcance(g, "A", List.of("M"), Set.of("M"))));
    }

    public void testCicloDeSeguimientoNoRepiteNiSeVuelveInfinito() {
        DirectedGraph<String, WeightedEdge> g = grafo("A", "B", "C");
        sigue(g, "B", "A");
        sigue(g, "C", "B");
        sigue(g, "A", "C");   // A -> C -> B -> A es un ciclo
        sigue(g, "B", "C");

        Set<String> alcance = Difusion.alcance(g, "A", Set.of(), Set.of("B", "C"));

        assertEquals(List.of("B", "C"), lista(alcance));
        assertFalse("el autor no está en su alcance", alcance.contains("A"));
    }

    public void testSeguidoresMutuosCuentanUnaVez() {
        DirectedGraph<String, WeightedEdge> g = grafo("A", "B");
        sigue(g, "A", "B");
        sigue(g, "B", "A");

        assertEquals(List.of("B"), lista(Difusion.alcance(g, "A", List.of("B"), Set.of("B"))));
    }

    public void testVerticeAisladoTieneAlcanceVacio() {
        DirectedGraph<String, WeightedEdge> g = grafo("A", "B");
        assertTrue(Difusion.alcance(g, "A", Set.of(), Set.of()).isEmpty());
    }

    public void testMencionarAlAutorNoLoAgrega() {
        DirectedGraph<String, WeightedEdge> g = grafo("A");
        assertTrue(Difusion.alcance(g, "A", List.of("A"), Set.of("A")).isEmpty());
    }

    public void testOrigenOReceptorInexistenteFalla() {
        DirectedGraph<String, WeightedEdge> g = grafo("A");
        try {
            Difusion.alcance(g, "X", Set.of(), Set.of());
            fail("Origen inexistente");
        } catch (IllegalArgumentException esperado) {
            // Se valida la existencia.
        }
        try {
            Difusion.alcance(g, "A", List.of("X"), Set.of());
            fail("Receptor inexistente");
        } catch (IllegalArgumentException esperado) {
            // Se valida la existencia.
        }
    }
}
