package ucu.edu.aed.redsocial;

import ucu.edu.aed.redsocial.modelo.Usuario;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Dataset de ejemplo reproducible. Siempre arma la misma red, en el mismo orden,
 * así los tests y la demo dan siempre la misma salida.
 *
 *  "Facultad"                    puente                  "Música"
 *  ana   <-> bruno               ana  <-> ivan            elena    <-> facundo
 *  bruno <-> carla               ivan <-> elena           facundo  <-> gabriela
 *  carla  -> ana                                         gabriela  -> elena
 *  diego  -> ana, bruno                                  hernan    -> gabriela, facundo
 *
 *  julia: aislada (no sigue ni la siguen)
 *
 * Casos que cubre:
 * - Mutuos: ana-bruno, bruno-carla, ana-ivan, ivan-elena, elena-facundo, facundo-gabriela.
 * - Ciclo dirigido: ana -> bruno -> carla -> ana.
 * - Seguimiento no recíproco: carla -> ana, gabriela -> elena.
 * - Solo sigue, nadie lo sigue: diego y hernan (ningún camino llega a ellos).
 * - Puente: ivan es el único vínculo entre las dos comunidades; en la proyección no
 *   dirigida, ana, ivan y elena son puntos de articulación.
 * - Aislado: julia.
 * - Distancia larga: diego llega a facundo en 4 saltos (diego, ana, ivan, elena, facundo).
 */
public final class DatosDemo {

    private DatosDemo() {
    }

    /** Cantidad de usuarios y de seguimientos que genera crearRedDemo(). */
    public static final int USUARIOS = 10;
    public static final int SEGUIMIENTOS = 18;

    /** Crea una red nueva con el dataset de ejemplo. */
    public static RedSocial crearRedDemo() {
        RedSocial red = new RedSocial();
        cargarUsuarios(red);
        cargarSeguimientos(red);
        return red;
    }

    private static void cargarUsuarios(RedSocial red) {
        // Comunidad "Facultad"
        red.registrarUsuario("ana", "Ana Pereira");
        red.registrarUsuario("bruno", "Bruno Silva");
        red.registrarUsuario("carla", "Carla Méndez");
        red.registrarUsuario("diego", "Diego Rodríguez");
        // Puente
        red.registrarUsuario("ivan", "Iván Fernández");
        // Comunidad "Música"
        red.registrarUsuario("elena", "Elena Suárez");
        red.registrarUsuario("facundo", "Facundo López");
        red.registrarUsuario("gabriela", "Gabriela Núñez");
        red.registrarUsuario("hernan", "Hernán García");
        // Aislada
        red.registrarUsuario("julia", "Julia Martínez");
    }

    private static void cargarSeguimientos(RedSocial red) {
        // Comunidad "Facultad": ana <-> bruno <-> carla, carla -> ana cierra el ciclo
        mutuo(red, "ana", "bruno");
        mutuo(red, "bruno", "carla");
        red.seguir("carla", "ana");
        red.seguir("diego", "ana");
        red.seguir("diego", "bruno");

        // Puente: ana <-> ivan <-> elena
        mutuo(red, "ana", "ivan");
        mutuo(red, "ivan", "elena");

        // Comunidad "Música": elena <-> facundo <-> gabriela, gabriela -> elena
        mutuo(red, "elena", "facundo");
        mutuo(red, "facundo", "gabriela");
        red.seguir("gabriela", "elena");
        red.seguir("hernan", "gabriela");
        red.seguir("hernan", "facundo");
    }

    private static void mutuo(RedSocial red, String a, String b) {
        red.seguir(a, b);
        red.seguir(b, a);
    }

    /** Imprime un resumen de la red demo. Sirve para la defensa y para comparar con la salida esperada. */
    public static void main(String[] args) {
        RedSocial red = crearRedDemo();
        System.out.println("Usuarios: " + red.cantidadDeUsuarios()
                + " | Seguimientos: " + red.cantidadDeSeguimientos());
        System.out.printf("%-10s %-32s %-32s %s%n", "usuario", "sigue a", "lo siguen", "mutuos");
        for (Usuario u : red.usuarios()) {
            System.out.printf("%-10s %-32s %-32s %s%n", u,
                    ids(red.seguidos(u.getId())),
                    ids(red.seguidores(u.getId())),
                    ids(red.seguidoresMutuos(u.getId())));
        }
    }

    private static String ids(Collection<Usuario> usuarios) {
        if (usuarios.isEmpty()) {
            return "-";
        }
        return usuarios.stream().map(Usuario::getId).collect(Collectors.joining(", "));
    }
}
