package ucu.edu.aed.tda.grafo.impl;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SocialGraphAnalyticsTest extends TestCase {

    private DirectedGraph<String, Integer> red(String... usuarios) {
        DirectedGraph<String, Integer> red = new DirectedGraph<>();
        red.agregarVertices(Arrays.asList(usuarios));
        return red;
    }

    private void sigue(DirectedGraph<String, Integer> red, String... pares) {
        for (int i = 0; i < pares.length; i += 2) {
            red.agregarArista(pares[i], pares[i + 1], 1);
        }
    }

    private Comparable<String> criterio(String valor) {
        return x -> x.equals(valor) ? 0 : 1;
    }

    private DirectedGraph<String, Integer> redDosComunidades() {
        DirectedGraph<String, Integer> red = red("ana", "beto", "caro", "dani", "eze", "fede", "gabi");
        sigue(red,
                "ana", "beto", "beto", "caro", "caro", "ana", "beto", "ana",
                "dani", "eze", "eze", "dani", "eze", "fede", "fede", "dani",
                "caro", "dani");
        return red;
    }

    public void testCantidadDeSeguidoresConVinculosReciprocos() {
        Map<String, Integer> seguidores = SocialGraphAnalytics.cantidadDeSeguidores(redDosComunidades());
        assertEquals(Integer.valueOf(2), seguidores.get("ana"));
        assertEquals(Integer.valueOf(1), seguidores.get("beto"));
        assertEquals(Integer.valueOf(3), seguidores.get("dani"));
        assertEquals(Integer.valueOf(0), seguidores.get("gabi"));
    }

    public void testRankingPorSeguidores() {
        List<String> ranking = SocialGraphAnalytics.rankingPorSeguidores(redDosComunidades());
        assertEquals(Arrays.asList("dani", "ana", "beto", "caro", "eze", "fede", "gabi"), ranking);
    }

    public void testAlcanceCuentaSeguidoresDeSeguidores() {
        DirectedGraph<String, Integer> red = redDosComunidades();
        assertEquals(5, SocialGraphAnalytics.alcance(red, criterio("dani")));
        assertEquals(2, SocialGraphAnalytics.alcance(red, criterio("ana")));
        assertEquals(0, SocialGraphAnalytics.alcance(red, criterio("gabi")));
    }

    public void testRankingPorAlcanceDifiereDelDeSeguidores() {
        List<String> ranking = SocialGraphAnalytics.rankingPorAlcance(redDosComunidades());
        assertEquals(Arrays.asList("dani", "eze", "fede", "ana", "beto", "caro", "gabi"), ranking);
    }

    public void testAlcanceUsuarioInexistente() {
        try {
            SocialGraphAnalytics.alcance(redDosComunidades(), criterio("nadie"));
            fail();
        } catch (IllegalArgumentException esperado) {
        }
    }

    public void testComunidadesSonComponentesFuertes() {
        List<Set<String>> comunidades = SocialGraphAnalytics.comunidades(redDosComunidades());
        assertEquals(3, comunidades.size());
        assertTrue(comunidades.contains(Set.of("ana", "beto", "caro")));
        assertTrue(comunidades.contains(Set.of("dani", "eze", "fede")));
        assertEquals(Set.of("gabi"), comunidades.get(2));
    }

    public void testConectividadDebilNoAlcanzaParaDefinirComunidad() {
        DirectedGraph<String, Integer> red = redDosComunidades();
        red.removerVertice(criterio("gabi"));
        assertTrue(red.esConexo());
        assertEquals(2, SocialGraphAnalytics.comunidades(red).size());
    }

    public void testCicloEsUnaSolaComunidad() {
        DirectedGraph<String, Integer> red = red("a", "b", "c", "d");
        sigue(red, "a", "b", "b", "c", "c", "d", "d", "a");
        assertEquals(1, SocialGraphAnalytics.comunidades(red).size());
    }

    public void testCadenaSinVueltaSonComunidadesSeparadas() {
        DirectedGraph<String, Integer> red = red("a", "b", "c");
        sigue(red, "a", "b", "b", "c");
        assertEquals(3, SocialGraphAnalytics.comunidades(red).size());
    }

    public void testRedVacia() {
        DirectedGraph<String, Integer> red = red();
        assertTrue(SocialGraphAnalytics.comunidades(red).isEmpty());
        assertTrue(SocialGraphAnalytics.rankingPorSeguidores(red).isEmpty());
        assertTrue(SocialGraphAnalytics.usuariosPuente(red).isEmpty());
    }

    public void testUsuariosPuenteEntreDosComunidades() {
        assertEquals(Set.of("caro", "dani"), SocialGraphAnalytics.usuariosPuente(redDosComunidades()));
    }

    public void testUsuarioPopularNoEsPuente() {
        DirectedGraph<String, Integer> red = red("famoso", "a", "b", "c", "d");
        sigue(red,
                "a", "famoso", "b", "famoso", "c", "famoso", "d", "famoso",
                "a", "b", "b", "c", "c", "d", "d", "a");
        assertEquals("famoso", SocialGraphAnalytics.rankingPorSeguidores(red).get(0));
        assertTrue(SocialGraphAnalytics.usuariosPuente(red).isEmpty());
    }

    public void testUsuarioConPocosSeguidoresPuedeSerPuente() {
        DirectedGraph<String, Integer> red = red("a1", "a2", "nexo", "b1", "b2");
        sigue(red, "a1", "a2", "a2", "a1", "a2", "nexo", "nexo", "b1", "b1", "b2", "b2", "b1");
        assertEquals(Integer.valueOf(1), SocialGraphAnalytics.cantidadDeSeguidores(red).get("nexo"));
        assertTrue(SocialGraphAnalytics.usuariosPuente(red).contains("nexo"));
    }
} 