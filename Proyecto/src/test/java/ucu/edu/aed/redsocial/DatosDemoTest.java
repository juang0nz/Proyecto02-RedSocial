package ucu.edu.aed.redsocial;

import junit.framework.TestCase;
import ucu.edu.aed.redsocial.modelo.EntradaFeed;
import ucu.edu.aed.redsocial.modelo.Posteo;
import ucu.edu.aed.redsocial.modelo.Usuario;
import ucu.edu.aed.tda.grafo.IDirectedIGraph;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Fija la salida esperada del dataset demo. Si alguien cambia DatosDemo,
 * estos tests avisan y obligan a actualizar también la documentación.
 */
public class DatosDemoTest extends TestCase {

    private RedSocial red;

    @Override
    protected void setUp() {
        red = DatosDemo.crearRedDemo();
    }

    private static List<String> posteosDelFeed(List<EntradaFeed> feed) {
        return feed.stream().map(e -> e.getPosteo().getId()).collect(Collectors.toList());
    }

    private static List<String> ids(Collection<Usuario> usuarios) {
        return usuarios.stream().map(Usuario::getId).collect(Collectors.toList());
    }

    public void testTamanioDelDataset() {
        assertEquals(DatosDemo.USUARIOS, red.cantidadDeUsuarios());
        assertEquals(DatosDemo.SEGUIMIENTOS, red.cantidadDeSeguimientos());
        assertEquals(DatosDemo.SEGUIMIENTOS, red.grafoDeSeguimiento().cantidadDeAristas());
    }

    public void testEsReproducible() {
        RedSocial otra = DatosDemo.crearRedDemo();
        for (Usuario u : red.usuarios()) {
            assertEquals(ids(red.seguidos(u.getId())), ids(otra.seguidos(u.getId())));
            assertEquals(ids(red.seguidores(u.getId())), ids(otra.seguidores(u.getId())));
        }
    }

    public void testRelacionesDeAna() {
        assertEquals(List.of("bruno", "ivan"), ids(red.seguidos("ana")));
        assertEquals(List.of("bruno", "carla", "diego", "ivan"), ids(red.seguidores("ana")));
        assertEquals(List.of("bruno", "ivan"), ids(red.seguidoresMutuos("ana")));
    }

    public void testSeguimientosNoReciprocos() {
        assertTrue(red.sigueA("carla", "ana"));
        assertFalse(red.sigueA("ana", "carla"));
        assertTrue(red.sigueA("gabriela", "elena"));
        assertFalse(red.sigueA("elena", "gabriela"));
        assertEquals(List.of("bruno"), ids(red.seguidoresMutuos("carla")));
    }

    public void testIvanEsElUnicoVinculoEntreComunidades() {
        Set<String> facultad = Set.of("ana", "bruno", "carla", "diego");
        Set<String> musica = Set.of("elena", "facundo", "gabriela", "hernan");

        assertEquals(List.of("ana", "elena"), ids(red.seguidoresMutuos("ivan")));
        for (String f : facultad) {
            for (String m : musica) {
                assertFalse(f + " -> " + m, red.sigueA(f, m));
                assertFalse(m + " -> " + f, red.sigueA(m, f));
            }
        }
    }

    public void testQuienesSoloSiguenYNadieLosSigue() {
        assertTrue(red.seguidores("diego").isEmpty());
        assertEquals(List.of("ana", "bruno"), ids(red.seguidos("diego")));
        assertTrue(red.seguidores("hernan").isEmpty());
        assertEquals(List.of("gabriela", "facundo"), ids(red.seguidos("hernan")));
    }

    public void testJuliaEstaAislada() {
        assertTrue(red.seguidos("julia").isEmpty());
        assertTrue(red.seguidores("julia").isEmpty());
    }

    public void testElGrafoTieneCiclosYEsDebilmenteConexoSalvoJulia() {
        IDirectedIGraph<Usuario, WeightedEdge> grafo = red.grafoDeSeguimiento();
        assertTrue("ana -> bruno -> carla -> ana", grafo.tieneCiclos());
        assertFalse("julia está aislada", grafo.esConexo());

        RedSocial sinJulia = DatosDemo.crearRedDemo();
        sinJulia.eliminarUsuario("julia");
        assertTrue(sinJulia.grafoDeSeguimiento().esConexo());
    }

    // ------------------------------------------------------------ publicaciones

    public void testCantidadDePublicaciones() {
        assertEquals(DatosDemo.POSTEOS, red.cantidadDePosteos());
        assertEquals(List.of("gabriela", "bruno"), ids(red.compartidoPor("p1")));
        assertEquals(List.of("ivan", "ana"), ids(red.compartidoPor("p2")));
        assertEquals(1, red.comentarios("p1").size());
        assertEquals(1, red.comentarios("p2").size());
    }

    public void testCasoConsignaMencionadaDeOtraComunidadComparte() {
        assertFalse("gabriela no sigue a carla", red.sigueA("gabriela", "carla"));
        assertEquals(List.of("gabriela"), ids(red.mencionados("p1")));
        assertEquals(List.of("gabriela", "bruno", "facundo", "hernan", "ana", "diego"),
                ids(red.alcance("p1")));
        EntradaFeed deHernan = red.feed("hernan").get(0);
        assertEquals("p1", deHernan.getPosteo().getId());
        assertEquals(EntradaFeed.Motivo.COMPARTIDO, deHernan.getMotivo());
        assertEquals("gabriela", deHernan.getVia().getId());
    }

    public void testCadenaConCicloYMencionEnComentario() {
        assertEquals(List.of("julia", "ivan", "facundo", "gabriela", "ana", "bruno", "carla", "diego"),
                ids(red.alcance("p2")));
        assertFalse("hernan sigue a quienes lo recibieron, pero no lo compartieron",
                red.alcance("p2").contains(red.obtenerUsuario("hernan")));
        EntradaFeed deJulia = red.feed("julia").get(0);
        assertEquals(EntradaFeed.Motivo.MENCION, deJulia.getMotivo());
        assertEquals("diego", deJulia.getVia().getId());
    }

    public void testPosteoDeUsuariaAisladaNoLlegaANadie() {
        assertTrue(red.alcance("p3").isEmpty());
    }

    public void testFeedsEsperados() {
        assertEquals(List.of("p2", "p1"), posteosDelFeed(red.feed("ana")));
        assertEquals(List.of("p2"), posteosDelFeed(red.feed("carla")));
        assertEquals(List.of("p1"), posteosDelFeed(red.feed("hernan")));
        assertEquals(List.of("p2"), posteosDelFeed(red.feed("julia")));
        assertTrue(red.feed("elena").isEmpty());
    }

    public void testFeedYAlcanceCoincidenEnTodaLaRed() {
        for (Usuario u : red.usuarios()) {
            List<String> feed = posteosDelFeed(red.feed(u.getId()));
            for (Posteo p : red.posteos()) {
                assertEquals(u + " / " + p.getId(),
                        red.alcance(p.getId()).contains(u), feed.contains(p.getId()));
            }
        }
    }
}
