package ucu.edu.aed.redsocial;

import junit.framework.TestCase;
import ucu.edu.aed.redsocial.modelo.Comentario;
import ucu.edu.aed.redsocial.modelo.EntradaFeed;
import ucu.edu.aed.redsocial.modelo.Posteo;
import ucu.edu.aed.redsocial.modelo.Usuario;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class PublicacionesTest extends TestCase {

    private RedSocial red;

    /**
     * ana <-> bruno, carla -> bruno, diego -> carla. elena aislada.
     * El contenido de ana le llega a bruno; el de bruno, a ana y carla; el de carla, a diego.
     */
    @Override
    protected void setUp() {
        red = new RedSocial();
        for (String id : List.of("ana", "bruno", "carla", "diego", "elena")) {
            red.registrarUsuario(id, id.toUpperCase());
        }
        red.seguir("ana", "bruno");
        red.seguir("bruno", "ana");
        red.seguir("carla", "bruno");
        red.seguir("diego", "carla");
    }

    private static List<String> ids(Collection<Usuario> usuarios) {
        return usuarios.stream().map(Usuario::getId).collect(Collectors.toList());
    }

    private static List<String> posteosDelFeed(List<EntradaFeed> feed) {
        return feed.stream().map(e -> e.getPosteo().getId()).collect(Collectors.toList());
    }

    private static void assertRechaza(String motivo, Runnable operacion) {
        try {
            operacion.run();
            fail("Se esperaba IllegalArgumentException: " + motivo);
        } catch (IllegalArgumentException esperado) {
            // La red valida la entrada.
        }
    }

    // ------------------------------------------------------------ publicar

    public void testPublicarAsignaIdsYOrdenCrecientes() {
        Posteo p1 = red.publicar("ana", "  hola  ");
        Posteo p2 = red.publicar("bruno", "chau");

        assertEquals("p1", p1.getId());
        assertEquals("p2", p2.getId());
        assertEquals("hola", p1.getTexto());
        assertEquals("ana", p1.getAutor().getId());
        assertTrue(p2.getOrden() > p1.getOrden());
        assertEquals(2, red.cantidadDePosteos());
        assertEquals(List.of(p1), red.posteosDe("ana"));
        assertSame(p1, red.obtenerPosteo("p1"));
    }

    public void testPublicarConDatosInvalidosFalla() {
        assertRechaza("autor inexistente", () -> red.publicar("zoe", "hola"));
        assertRechaza("texto vacío", () -> red.publicar("ana", "   "));
        assertRechaza("texto nulo", () -> red.publicar("ana", null));
        assertRechaza("posteo inexistente", () -> red.obtenerPosteo("p9"));
        assertEquals(0, red.cantidadDePosteos());
    }

    // ------------------------------------------------------------ menciones

    public void testMencionesSeDetectanEnElTexto() {
        Posteo p = red.publicar("ana", "Hola @Bruno y @carla. Saludos @carla");

        assertEquals("sin repetir y en orden", List.of("bruno", "carla"), ids(p.getMencionados()));
    }

    public void testMencionesQueNoCuentan() {
        Posteo p = red.publicar("ana", "Yo @ana, mi mail ana@bruno.com y @nadie existen?");

        assertTrue("ni el autor, ni un mail, ni un usuario inexistente", p.getMencionados().isEmpty());
    }

    public void testMencionadoRecibeAunqueNoSigaAlAutor() {
        red.publicar("diego", "Mirá esto @elena");

        assertEquals(List.of("elena"), ids(red.alcance("p1")));
        assertEquals(List.of("p1"), posteosDelFeed(red.feed("elena")));
        assertEquals(EntradaFeed.Motivo.MENCION, red.feed("elena").get(0).getMotivo());
    }

    public void testMencionEnComentarioAgregaReceptor() {
        red.publicar("ana", "hola");
        assertEquals(List.of("bruno"), ids(red.alcance("p1")));

        Comentario c = red.comentar("p1", "bruno", "@elena mirá");

        assertEquals(List.of("elena"), ids(c.getMencionados()));
        assertEquals(List.of("elena"), ids(red.mencionados("p1")));
        assertEquals(List.of("elena", "bruno"), ids(red.alcance("p1")));
        EntradaFeed entrada = red.feed("elena").get(0);
        assertEquals(EntradaFeed.Motivo.MENCION, entrada.getMotivo());
        assertEquals("bruno", entrada.getVia().getId());
    }

    // ------------------------------------------------------------ alcance

    public void testAlcanceSinCompartidosEsLosSeguidoresDelAutor() {
        red.publicar("bruno", "hola");

        assertEquals(List.of("ana", "carla"), ids(red.alcance("p1")));
    }

    public void testCadenaDeCompartidos() {
        red.publicar("ana", "hola");                 // llega a bruno
        assertEquals(List.of("bruno"), ids(red.alcance("p1")));

        red.compartir("p1", "bruno");                // llega a carla
        assertEquals(List.of("bruno", "carla"), ids(red.alcance("p1")));

        red.compartir("p1", "carla");                // llega a diego
        assertEquals(List.of("bruno", "carla", "diego"), ids(red.alcance("p1")));
    }

    public void testSeguirAQuienLoRecibioNoAlcanza() {
        red.publicar("ana", "hola");
        assertFalse("carla sigue a bruno, pero bruno no lo compartió", red.alcance("p1")
                .contains(red.obtenerUsuario("carla")));
    }

    public void testCicloMutuoNoRepiteUsuarios() {
        red.publicar("ana", "hola");
        red.compartir("p1", "bruno");                // bruno -> sus seguidores: ana (autora) y carla

        assertEquals(List.of("bruno", "carla"), ids(red.alcance("p1")));
        assertFalse(red.alcance("p1").contains(red.obtenerUsuario("ana")));
    }

    public void testCasoConsignaMencionadoQueComparteLlegaASusSeguidores() {
        red.publicar("elena", "Busco banda @bruno");     // elena aislada; bruno no la sigue
        assertEquals(List.of("bruno"), ids(red.alcance("p1")));

        assertTrue(red.compartir("p1", "bruno"));

        assertEquals(List.of("bruno", "ana", "carla"), ids(red.alcance("p1")));
        EntradaFeed entrada = red.feed("carla").get(0);
        assertEquals(EntradaFeed.Motivo.COMPARTIDO, entrada.getMotivo());
        assertEquals("bruno", entrada.getVia().getId());
    }

    public void testUsuarioAisladoTieneAlcanceVacio() {
        red.publicar("elena", "hola");
        assertTrue(red.alcance("p1").isEmpty());
        assertTrue(red.feed("elena").isEmpty());
    }

    // ------------------------------------------------------------ compartir y comentar

    public void testCompartirDosVecesDevuelveFalse() {
        red.publicar("ana", "hola");
        assertTrue(red.compartir("p1", "bruno"));
        assertFalse(red.compartir("p1", "bruno"));
        assertEquals(List.of("bruno"), ids(red.compartidoPor("p1")));
    }

    public void testNoSePuedeCompartirOComentarLoQueNoLlego() {
        red.publicar("ana", "hola");

        assertRechaza("carla no lo recibió", () -> red.compartir("p1", "carla"));
        assertRechaza("elena no lo recibió", () -> red.comentar("p1", "elena", "hola"));
        assertRechaza("el autor no comparte lo suyo", () -> red.compartir("p1", "ana"));
        assertRechaza("posteo inexistente", () -> red.compartir("p9", "bruno"));
        assertRechaza("usuario inexistente", () -> red.comentar("p1", "zoe", "hola"));
        assertTrue(red.compartidoPor("p1").isEmpty());
        assertTrue(red.comentarios("p1").isEmpty());
    }

    public void testElAutorYQuienLoRecibioPuedenComentar() {
        red.publicar("ana", "hola");
        red.comentar("p1", "ana", "me respondo");
        red.comentar("p1", "bruno", "buenísimo");

        List<String> autores = red.comentarios("p1").stream()
                .map(c -> c.getAutor().getId()).collect(Collectors.toList());
        assertEquals(List.of("ana", "bruno"), autores);
    }

    // ------------------------------------------------------------ feed

    public void testFeedOrdenadoDelMasNuevoAlMasViejoSinPropios() {
        red.publicar("bruno", "primero");   // p1
        red.publicar("ana", "propio");      // p2: de ana, no va a su feed
        red.publicar("bruno", "segundo");   // p3

        assertEquals(List.of("p3", "p1"), posteosDelFeed(red.feed("ana")));
    }

    public void testUnCompartidoRecienteSubeElPosteoUnaSolaVez() {
        red.publicar("ana", "viejo");       // p1, carla todavía no lo recibe
        red.publicar("bruno", "nuevo");     // p2, carla lo recibe (sigue a bruno)
        red.compartir("p1", "bruno");       // ahora p1 le llega a carla y es lo más reciente

        List<EntradaFeed> feed = red.feed("carla");
        assertEquals(List.of("p1", "p2"), posteosDelFeed(feed));
        assertEquals(EntradaFeed.Motivo.COMPARTIDO, feed.get(0).getMotivo());
        assertEquals(EntradaFeed.Motivo.PUBLICACION, feed.get(1).getMotivo());
    }

    public void testFeedYAlcanceCoinciden() {
        red.publicar("ana", "hola @elena");
        red.compartir("p1", "bruno");
        red.publicar("carla", "otro");
        red.comentar("p2", "diego", "@ana fijate");

        for (Usuario u : red.usuarios()) {
            List<String> feed = posteosDelFeed(red.feed(u.getId()));
            for (Posteo p : red.posteos()) {
                assertEquals(u + " / " + p.getId(),
                        red.alcance(p.getId()).contains(u), feed.contains(p.getId()));
            }
        }
    }

    // ------------------------------------------------------------ eliminar usuarios

    public void testEliminarUsuarioBorraSuActividadYSusMenciones() {
        red.publicar("ana", "hola @elena");      // p1
        red.compartir("p1", "bruno");
        red.comentar("p1", "carla", "ok @elena");
        red.publicar("bruno", "chau");           // p2
        red.comentar("p2", "ana", "@carla mirá");

        red.eliminarUsuario("ana");

        assertEquals("se borra su posteo", List.of("p2"),
                red.posteos().stream().map(Posteo::getId).collect(Collectors.toList()));
        assertTrue("se borra su comentario en p2", red.comentarios("p2").isEmpty());
        assertRechaza("p1 ya no existe", () -> red.alcance("p1"));
        assertEquals(List.of("carla"), ids(red.alcance("p2")));
    }

    public void testEliminarUnMencionadoYVolverARegistrarloNoHeredaLaMencion() {
        red.publicar("ana", "hola @elena");
        red.comentar("p1", "bruno", "@elena dale");

        red.eliminarUsuario("elena");
        red.registrarUsuario("elena", "Otra Elena");

        assertTrue(red.obtenerPosteo("p1").getMencionados().isEmpty());
        assertTrue(red.comentarios("p1").get(0).getMencionados().isEmpty());
        assertTrue(red.mencionados("p1").isEmpty());
        assertTrue(red.feed("elena").isEmpty());
        assertEquals(List.of("bruno"), ids(red.alcance("p1")));
    }

    public void testEliminarQuienCompartioCortaLaDifusion() {
        red.publicar("ana", "hola");
        red.compartir("p1", "bruno");
        red.compartir("p1", "carla");
        assertEquals(List.of("bruno", "carla", "diego"), ids(red.alcance("p1")));

        red.eliminarUsuario("bruno");

        assertTrue("sin bruno, a carla ya no le llega", red.alcance("p1").isEmpty());
        assertTrue(red.compartidoPor("p1").stream().noneMatch(u -> u.getId().equals("bruno")));
    }
}
