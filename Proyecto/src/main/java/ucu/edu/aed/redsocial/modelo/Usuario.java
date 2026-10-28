package ucu.edu.aed.redsocial.modelo;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Usuario de la red social. Es el tipo de vértice del grafo de seguimiento.
 *
 * La identidad del usuario es su id (el "handle" que se usa al arrobar, sin la @).
 * equals y hashCode dependen solo del id, para que el grafo, que guarda los
 * vértices en un LinkedHashMap, encuentre al usuario aunque se use otra instancia
 * con el mismo id. La clase es inmutable: si el id cambiara, el vértice quedaría mal ubicado
 * en el mapa del grafo.
 */
public final class Usuario {

    /** Letras minúsculas, dígitos, punto o guion bajo; entre 1 y 30 caracteres. */
    private static final Pattern FORMATO_ID = Pattern.compile("[a-z0-9._]{1,30}");

    private final String id;
    private final String nombre;

    /**
     * Crea un usuario. El id se normaliza con normalizarId y el nombre visible no puede ser vacío.
     * Lanza IllegalArgumentException si el id o el nombre son inválidos.
     */
    public Usuario(String id, String nombre) {
        this.id = normalizarId(id);
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del usuario no puede ser vacío");
        }
        this.nombre = nombre.trim();
    }

    /**
     * Normaliza un id: quita espacios, una @ inicial opcional y pasa a minúsculas.
     * Así "@Ana", " ana " y "ana" refieren al mismo usuario. O(largo del id).
     * Lanza IllegalArgumentException si el id es nulo, vacío o tiene caracteres no permitidos.
     */
    public static String normalizarId(String id) {
        if (id == null) {
            throw new IllegalArgumentException("El id del usuario no puede ser nulo");
        }
        String normalizado = id.trim();
        if (normalizado.startsWith("@")) {
            normalizado = normalizado.substring(1);
        }
        normalizado = normalizado.toLowerCase(Locale.ROOT);
        if (!FORMATO_ID.matcher(normalizado).matches()) {
            throw new IllegalArgumentException("Id de usuario inválido: '" + id
                    + "' (se permiten letras sin tilde, dígitos, '.' y '_', hasta 30 caracteres)");
        }
        return normalizado;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario otro)) return false;
        return id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "@" + id;
    }
}
