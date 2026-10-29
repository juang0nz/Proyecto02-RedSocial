package ucu.edu.aed.redsocial;

import ucu.edu.aed.redsocial.modelo.Posteo;
import ucu.edu.aed.redsocial.modelo.Usuario;
import ucu.edu.aed.tda.grafo.IDirectedIGraph;
import ucu.edu.aed.tda.grafo.impl.SocialGraphAnalytics;
import ucu.edu.aed.tda.grafo.impl.SocialGraphQueries;
import ucu.edu.aed.tda.grafo.model.edge.WeightedEdge;

// Presentacion breve: reutiliza la red de DatosDemo y muestra resultados, no reemplaza los tests.
public final class DemoRedSocial {

    private DemoRedSocial() {
    }

    public static void main(String[] args) {
        RedSocial red = DatosDemo.crearRedDemo();
        IDirectedIGraph<Usuario, WeightedEdge> grafo = red.grafoDeSeguimiento();
        Usuario ana = red.obtenerUsuario("ana");
        Usuario bruno = red.obtenerUsuario("bruno");
        Usuario carla = red.obtenerUsuario("carla");
        Usuario ivan = red.obtenerUsuario("ivan");
        Usuario elena = red.obtenerUsuario("elena");

        System.out.println("DEMO: RED SOCIAL");
        System.out.println("A -> B significa que A sigue a B. Cargamos 10 usuarios y 18 seguimientos.");

        System.out.println("\n1. USUARIOS Y RELACIONES");
        System.out.println("Cada cuenta es un vertice; seguir agrega una arista dirigida.");
        red.registrarUsuario("lucas", "Lucas");
        red.seguir("lucas", "ana");
        System.out.println("Registramos a Lucas y sigue a: " + red.seguidos("lucas"));
        red.dejarDeSeguir("lucas", "ana");
        red.eliminarUsuario("lucas");
        System.out.println("Deja de seguir a Ana y lo eliminamos. Existe Lucas: " + red.existeUsuario("lucas"));
        System.out.println("Ana sigue a: " + red.seguidos("ana"));
        System.out.println("Seguidores de Ana: " + red.seguidores("ana"));
        System.out.println("Mutuos de Ana: " + red.seguidoresMutuos("ana"));

        System.out.println("\n2. CONSULTAS DE LA RED");
        System.out.println("Sugerimos seguidos de seguidos; BFS calcula la distancia minima dirigida.");
        System.out.println("Sugerencias para Ana: "
                + SocialGraphQueries.sugerencias(grafo, grafo.construirComparable(ana)));
        System.out.println("Saltos Ana -> Elena: " + SocialGraphQueries.gradosDeSeparacion(
                grafo, grafo.construirComparable(ana), grafo.construirComparable(elena)));
        System.out.println("Ranking por seguidores: " + SocialGraphAnalytics.rankingPorSeguidores(grafo));
        System.out.println("Ranking por alcance potencial (cadenas de seguidores, no compartidos): "
                + SocialGraphAnalytics.rankingPorAlcance(grafo));
        System.out.println("Comunidades (Kosaraju, caminos de ida y vuelta): "
                + SocialGraphAnalytics.comunidades(grafo));
        System.out.println("Puentes (articulaciones al ignorar direcciones): "
                + SocialGraphAnalytics.usuariosPuente(grafo));

        System.out.println("\n3. PUBLICACION Y DIFUSION");
        Posteo posteo = red.publicar("carla", "Repaso de grafos con @gabriela");
        String id = posteo.getId();
        System.out.println("Carla publica: " + posteo.getTexto());
        System.out.println("Gabriela recibe por mencion, aunque no sigue a Carla.");
        System.out.println("Antes de compartir reciben: " + red.alcance(id));
        red.compartir(id, "gabriela");
        System.out.println("Gabriela comparte; ahora reciben: " + red.alcance(id));
        System.out.println("La difusion usa una cola y un conjunto; solo reenvian quienes comparten.");
        red.comentar(id, "gabriela", "@julia sumate al repaso");
        System.out.println("Gabriela comenta mencionando a Julia. Mencionados: " + red.mencionados(id));
        System.out.println("Julia recibe aunque esta aislada. Su feed: " + red.feed("julia"));
        System.out.println("Feed de Hernan: " + red.feed("hernan"));
        System.out.println("El feed no repite posteos y muestra primero el evento mas reciente.");

        System.out.println("\n4. CINCO CONSULTAS ADICIONALES");
        System.out.println("1) BFS hasta K saltos. Desde Ana con K=2: "
                + ConsultasAdicionales.alcanzablesHastaK(grafo, ana, 2));
        System.out.println("2) Interseccion de seguidores de Ana y Bruno: "
                + ConsultasAdicionales.seguidoresComunes(grafo, ana, bruno));
        System.out.println("3) BFS evitando a Ivan. Camino Ana -> Carla: "
                + ConsultasAdicionales.caminoEvitando(grafo, ana, carla, ivan));
        System.out.println("4) Grupos unidos por conexiones reciprocas: "
                + ConsultasAdicionales.gruposDeSeguimientoMutuo(grafo));
        System.out.println("5) Dos BFS comparan el antes y despues de ignorar Ivan -> Elena.");
        System.out.println("Desde Ana se perderia acceso a: "
                + ConsultasAdicionales.perdidaPorEliminarSeguimiento(grafo, ana, ivan, elena));
        System.out.println("La consulta no borra el seguimiento. Ivan sigue a Elena: " + red.sigueA("ivan", "elena"));
        System.out.println("\nFIN. Esta es una demostracion; las verificaciones completas estan en las pruebas.");
    }
}
