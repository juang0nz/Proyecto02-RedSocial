package ucu.edu.aed.redsocial.modelo;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

// Representa a un usuario y se usa como vértice del grafo.
// Se identifica por el id, no por el nombre ni por la instancia.
// El id no cambia: cambiar una clave ya guardada rompería las búsquedas en el mapa.
public final class Usuario {

    // El id permite letras minúsculas sin tilde, números, punto y guion bajo; hasta 30 caracteres.
    private static final Pattern FORMATO_ID = Pattern.compile("[a-z0-9._]{1,30}");

    private final String id;
    private final String nombre;

    // Normaliza el id y guarda el nombre sin espacios en los extremos.
    // Si alguno de los datos es inválido, lanza una excepción.
    public Usuario(String id, String nombre) {
        this.id = normalizarId(id);
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del usuario no puede ser vacío");
        }
        this.nombre = nombre.trim();
    }

    // Quita espacios de los extremos y un @ inicial, y pasa el id a minúsculas.
    // Así "@Ana" y " ana " se buscan como "ana". Cuesta O(largo del id).
    // Rechaza ids nulos o que no cumplan el formato.
    public static String normalizarId(String id) {
        if (id == null) {
            throw new IllegalArgumentException("El id del usuario no puede ser nulo");
        }
        String normalizado = id.trim();
        if (normalizado.startsWith("@")) {
            normalizado = normalizado.substring(1);
        }
        // ROOT evita que el resultado dependa del idioma de la computadora.
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

    // Dos usuarios con el mismo id representan la misma cuenta.
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
