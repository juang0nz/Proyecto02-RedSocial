package ucu.edu.aed.redsocial;

import junit.framework.TestCase;
import ucu.edu.aed.redsocial.modelo.Usuario;
import ucu.edu.aed.tda.grafo.IDirectedIGraph;
import ucu.edu.aed.tda.grafo.model.edge.Edge;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RedSocialTest extends TestCase {

    private RedSocial red;

    @Override
    protected void setUp() {
        red = new RedSocial();
        red.registrarUsuario("ana", "Ana");
        red.registrarUsuario("bruno", "Bruno");
        red.registrarUsuario("carla", "Carla");
    }

    private static List<String> ids(Set<Usuario> usuarios) {
        return usuarios.stream().map(Usuario::getId).collect(Collectors.toList());
    }

    private static void assertRechaza(String motivo, Runnable operacion) {
        try {
            operacion.run();
            fail("Se esperaba IllegalArgumentException: " + motivo);
        } catch (IllegalArgumentException esperado) {
            // La red valida la entrada.
        }
    }

    private static void assertSoloLectura(Runnable operacion) {
        try {
            operacion.run();
            fail("Se esperaba UnsupportedOperationException");
        } catch (UnsupportedOperationException esperado) {
            // La vista no permite modificar.
        }
    }

    // ------------------------------------------------------------ registro de usuarios

    public void testRegistrarUsuarioLoAgregaComoVerticeSinAristas() {
        Usuario diego = red.registrarUsuario("diego", "Diego");

        assertEquals("diego", diego.getId());
        assertTrue(red.existeUsuario("diego"));
        assertEquals(4, red.cantidadDeUsuarios());
        assertEquals(4, red.grafoDeSeguimiento().cantidadDeVertices());
        assertTrue(red.seguidos("diego").isEmpty());
        assertTrue(red.seguidores("diego").isEmpty());
    }

    public void testIdSeNormalizaArrobaEspaciosYMayusculas() {
        red.registrarUsuario("  @Diego ", "Diego");

        assertTrue(red.existeUsuario("diego"));
        assertTrue(red.existeUsuario("@DIEGO"));
        assertEquals("diego", red.obtenerUsuario("@diego").getId());
    }

    public void testRegistrarIdRepetidoFallaAunqueCambienMayusculas() {
        assertRechaza("id repetido", () -> red.registrarUsuario("ANA", "Otra Ana"));
        assertEquals(3, red.cantidadDeUsuarios());
        assertEquals("Ana", red.obtenerUsuario("ana").getNombre());
    }

    public void testRegistrarConIdONombreInvalidoFallaSinDejarRastro() {
        assertRechaza("id nulo", () -> red.registrarUsuario(null, "X"));
        assertRechaza("id vacío", () -> red.registrarUsuario("   ", "X"));
        assertRechaza("id con espacio", () -> red.registrarUsuario("con espacio", "X"));
        assertRechaza("solo arroba", () -> red.registrarUsuario("@", "X"));
        assertRechaza("nombre vacío", () -> red.registrarUsuario("valido", " "));
        assertRechaza("nombre nulo", () -> red.registrarUsuario("valido", null));

        assertEquals(3, red.cantidadDeUsuarios());
        assertEquals(3, red.grafoDeSeguimiento().cantidadDeVertices());
    }

    public void testUsuariosSeListanEnOrdenDeRegistro() {
        List<String> orden = red.usuarios().stream().map(Usuario::getId).collect(Collectors.toList());
        assertEquals(List.of("ana", "bruno", "carla"), orden);
    }

    public void testExisteUsuarioConIdInvalidoOInexistenteDevuelveFalse() {
        assertFalse(red.existeUsuario("no existe"));
        assertFalse(red.existeUsuario(null));
        assertFalse(red.existeUsuario("zoe"));
    }

    public void testUsuariosConMismoIdSonIguales() {
        assertEquals(new Usuario("ana", "Ana"), new Usuario("@ANA", "Otro nombre"));
        assertEquals(new Usuario("ana", "Ana").hashCode(), new Usuario("ana", "X").hashCode());
        assertFalse(new Usuario("ana", "Ana").equals(new Usuario("bruno", "Ana")));
    }

    // ------------------------------------------------------------ seguir / dejar de seguir

    public void testSeguirCreaUnaSolaAristaEnElSentidoSeguidorSeguido() {
        assertTrue(red.seguir("ana", "bruno"));

        assertTrue(red.sigueA("ana", "bruno"));
        assertFalse("seguir no es recíproco", red.sigueA("bruno", "ana"));
        assertEquals(List.of("bruno"), ids(red.seguidos("ana")));
        assertEquals(List.of("ana"), ids(red.seguidores("bruno")));
        assertTrue(red.seguidores("ana").isEmpty());
        assertTrue(red.seguidos("bruno").isEmpty());

        Edge<Usuario, WeightedEdge> arista = red.grafoDeSeguimiento()
                .obtenerArista(red.obtenerUsuario("ana"), red.obtenerUsuario("bruno"));
        assertEquals("ana", arista.source().getId());
        assertEquals("bruno", arista.target().getId());
        assertEquals(RedSocial.PESO_SEGUIMIENTO, arista.dato().getWeight(), 0.0);
    }

    public void testSeguirDosVecesNoDuplicaLaArista() {
        assertTrue(red.seguir("ana", "bruno"));
        assertFalse(red.seguir("ana", "bruno"));
        assertFalse(red.seguir("@ANA", "bruno"));

        assertEquals(1, red.cantidadDeSeguimientos());
        assertEquals(1, red.cantidadDeSeguidores("bruno"));
    }

    public void testNoSePuedeSeguirseASiMismo() {
        assertRechaza("auto-seguimiento", () -> red.seguir("ana", "ana"));
        assertRechaza("auto-seguimiento con otro formato de id", () -> red.seguir("ana", "@Ana"));
        assertEquals(0, red.cantidadDeSeguimientos());
    }

    public void testDejarDeSeguirQuitaSoloEseSentido() {
        red.seguir("ana", "bruno");
        red.seguir("bruno", "ana");

        assertTrue(red.dejarDeSeguir("ana", "bruno"));

        assertFalse(red.sigueA("ana", "bruno"));
        assertTrue("la arista inversa sigue existiendo", red.sigueA("bruno", "ana"));
        assertEquals(1, red.cantidadDeSeguimientos());
    }

    public void testDejarDeSeguirAQuienNoSeguiaDevuelveFalse() {
        assertFalse(red.dejarDeSeguir("ana", "bruno"));
        red.seguir("bruno", "ana");
        assertFalse("seguir en el otro sentido no cuenta", red.dejarDeSeguir("ana", "bruno"));
        assertEquals(1, red.cantidadDeSeguimientos());
    }

    public void testSePuedeVolverASeguirDespuesDeDejarDeSeguir() {
        red.seguir("ana", "bruno");
        red.dejarDeSeguir("ana", "bruno");

        assertTrue(red.seguir("ana", "bruno"));
        assertTrue(red.sigueA("ana", "bruno"));
    }

    // ------------------------------------------------------------ mutuos

    public void testMutuosRequiereAmbasAristas() {
        red.seguir("ana", "bruno");
        assertFalse(red.sonMutuos("ana", "bruno"));
        assertTrue(red.seguidoresMutuos("ana").isEmpty());

        red.seguir("bruno", "ana");
        assertTrue(red.sonMutuos("ana", "bruno"));
        assertTrue(red.sonMutuos("bruno", "ana"));
        assertEquals(List.of("bruno"), ids(red.seguidoresMutuos("ana")));
        assertEquals(List.of("ana"), ids(red.seguidoresMutuos("bruno")));
    }

    public void testMutuosExcluyeSeguidoresYSeguidosNoReciprocos() {
        red.registrarUsuario("diego", "Diego");
        red.seguir("ana", "bruno");   // mutuo
        red.seguir("bruno", "ana");
        red.seguir("ana", "carla");   // ana sigue a carla, carla no la sigue
        red.seguir("diego", "ana");   // diego sigue a ana, ana no lo sigue

        assertEquals(List.of("bruno"), ids(red.seguidoresMutuos("ana")));
        assertEquals(List.of("bruno", "carla"), ids(red.seguidos("ana")));
        assertEquals(List.of("bruno", "diego"), ids(red.seguidores("ana")));
    }

    public void testUsuarioAisladoNoTieneRelaciones() {
        red.seguir("ana", "bruno");
        red.seguir("bruno", "ana");

        assertTrue(red.seguidos("carla").isEmpty());
        assertTrue(red.seguidores("carla").isEmpty());
        assertTrue(red.seguidoresMutuos("carla").isEmpty());
        assertFalse(red.sonMutuos("carla", "ana"));
    }

    // ------------------------------------------------------------ eliminar usuarios

    public void testEliminarUsuarioBorraSusAristasEntrantesYSalientes() {
        red.seguir("ana", "bruno");
        red.seguir("bruno", "ana");
        red.seguir("carla", "bruno");
        red.seguir("bruno", "carla");
        red.seguir("ana", "carla");

        red.eliminarUsuario("bruno");

        assertFalse(red.existeUsuario("bruno"));
        assertEquals(2, red.cantidadDeUsuarios());
        assertEquals(2, red.grafoDeSeguimiento().cantidadDeVertices());
        assertEquals("solo queda ana -> carla", 1, red.cantidadDeSeguimientos());
        assertEquals(List.of("carla"), ids(red.seguidos("ana")));
        assertTrue(red.seguidores("ana").isEmpty());
        assertEquals(List.of("ana"), ids(red.seguidores("carla")));
        assertTrue(red.seguidos("carla").isEmpty());
    }

    public void testUsuarioEliminadoPuedeVolverARegistrarseSinRelacionesViejas() {
        red.seguir("ana", "bruno");
        red.seguir("bruno", "ana");
        red.eliminarUsuario("bruno");

        red.registrarUsuario("bruno", "Bruno nuevo");

        assertEquals("Bruno nuevo", red.obtenerUsuario("bruno").getNombre());
        assertTrue(red.seguidores("bruno").isEmpty());
        assertTrue(red.seguidos("bruno").isEmpty());
        assertFalse(red.sigueA("ana", "bruno"));
    }

    // ------------------------------------------------------------ usuarios inexistentes

    public void testOperacionesConUsuarioInexistenteFallan() {
        assertRechaza("obtener", () -> red.obtenerUsuario("zoe"));
        assertRechaza("eliminar", () -> red.eliminarUsuario("zoe"));
        assertRechaza("seguir a inexistente", () -> red.seguir("ana", "zoe"));
        assertRechaza("inexistente sigue", () -> red.seguir("zoe", "ana"));
        assertRechaza("dejar de seguir", () -> red.dejarDeSeguir("ana", "zoe"));
        assertRechaza("sigueA", () -> red.sigueA("zoe", "ana"));
        assertRechaza("sonMutuos", () -> red.sonMutuos("ana", "zoe"));
        assertRechaza("seguidos", () -> red.seguidos("zoe"));
        assertRechaza("seguidores", () -> red.seguidores("zoe"));
        assertRechaza("mutuos", () -> red.seguidoresMutuos("zoe"));

        assertEquals("los fallos no crean vértices", 3, red.grafoDeSeguimiento().cantidadDeVertices());
        assertEquals(0, red.cantidadDeSeguimientos());
    }

    public void testUsuarioEliminadoPasaASerInexistente() {
        red.eliminarUsuario("carla");
        assertRechaza("seguir a eliminado", () -> red.seguir("ana", "carla"));
        assertRechaza("eliminar dos veces", () -> red.eliminarUsuario("carla"));
    }

    // ------------------------------------------------------------ encapsulamiento

    public void testGrafoExpuestoEsDeSoloLecturaPeroReflejaLosCambios() {
        IDirectedIGraph<Usuario, WeightedEdge> grafo = red.grafoDeSeguimiento();
        Usuario ana = red.obtenerUsuario("ana");

        assertSoloLectura(() -> grafo.agregarArista(ana, ana, new WeightedEdge(1)));
        assertSoloLectura(() -> grafo.agregarVertice(new Usuario("zoe", "Zoe")));
        assertSoloLectura(() -> grafo.removerVertice(grafo.construirComparable(ana)));
        assertSoloLectura(grafo::vaciar);

        red.seguir("bruno", "ana");
        assertEquals(1, grafo.gradoDeEntrada(grafo.construirComparable(ana)));
    }

    public void testConjuntosDevueltosNoSePuedenModificar() {
        red.seguir("ana", "bruno");
        Set<Usuario> seguidos = red.seguidos("ana");
        assertSoloLectura(() -> seguidos.add(red.obtenerUsuario("carla")));
        assertSoloLectura(() -> red.usuarios().clear());
    }
}
