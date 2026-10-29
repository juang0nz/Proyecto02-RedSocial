package ucu.edu.aed.redsocial;

import ucu.edu.aed.redsocial.modelo.EntradaFeed;
import ucu.edu.aed.redsocial.modelo.Posteo;
import ucu.edu.aed.redsocial.modelo.Usuario;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

// Arma siempre la misma red de ejemplo para probar y mostrar en la defensa.
// Hay dos grupos unidos por ivan, seguimientos mutuos, ciclos y una usuaria aislada.
// Las publicaciones muestran cómo una mención o un compartido llegan a otros usuarios.
// El orden de carga es fijo para que los ids y la salida sean reproducibles.
public final class DatosDemo {

    private DatosDemo() {
    }

    // Cantidades esperadas después de cargar la demo completa.
    public static final int USUARIOS = 10;
    public static final int SEGUIMIENTOS = 18;
    public static final int POSTEOS = 3;

    // Devuelve una red nueva cargando usuarios, después seguimientos y al final publicaciones.
    public static RedSocial crearRedDemo() {
        RedSocial red = new RedSocial();
        cargarUsuarios(red);
        cargarSeguimientos(red);
        cargarPublicaciones(red);
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

    private static void cargarPublicaciones(RedSocial red) {
        // p1: mención a alguien de otra comunidad que después comparte
        red.publicar("carla", "Alguien tiene los ejercicios de grafos? @gabriela vos hiciste el curso");
        red.compartir("p1", "gabriela");
        red.compartir("p1", "bruno");
        red.comentar("p1", "facundo", "Yo tambien los quiero");

        // p2: cadena de compartidos con ciclo y mención en un comentario
        red.publicar("elena", "Ensayo abierto el sabado, estan todos invitados");
        red.compartir("p2", "ivan");
        red.compartir("p2", "ana");
        red.comentar("p2", "diego", "@julia te va a gustar");

        // p3: usuaria aislada
        red.publicar("julia", "Hola, recien llego a la red");
    }

    // El seguimiento mutuo se representa con dos aristas, una en cada sentido.
    private static void mutuo(RedSocial red, String a, String b) {
        red.seguir(a, b);
        red.seguir(b, a);
    }

    // Carga la demo e imprime relaciones, alcance de los posteos y feed de cada usuario.
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

        System.out.println();
        System.out.println("Posteos: " + red.cantidadDePosteos());
        for (Posteo p : red.posteos()) {
            System.out.println(p);
            System.out.println("    compartido por: " + ids(red.compartidoPor(p.getId())));
            System.out.println("    mencionados:    " + ids(red.mencionados(p.getId())));
            System.out.println("    alcance:        " + ids(red.alcance(p.getId())));
        }

        System.out.println();
        System.out.println("Feeds:");
        for (Usuario u : red.usuarios()) {
            List<EntradaFeed> feed = red.feed(u.getId());
            System.out.printf("%-10s %s%n", u, feed.isEmpty() ? "-" : feed);
        }
    }

    // Convierte los usuarios en una lista de ids para imprimir; usa "-" si está vacía.
    private static String ids(Collection<Usuario> usuarios) {
        if (usuarios.isEmpty()) {
            return "-";
        }
        return usuarios.stream().map(Usuario::getId).collect(Collectors.joining(", "));
    }
}
