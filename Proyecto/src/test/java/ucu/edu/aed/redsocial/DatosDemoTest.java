package ucu.edu.aed.redsocial;

import junit.framework.TestCase;
import ucu.edu.aed.redsocial.modelo.Usuario;
import ucu.edu.aed.tda.grafo.IDirectedIGraph;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;

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

    private static List<String> ids(Set<Usuario> usuarios) {
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
}
