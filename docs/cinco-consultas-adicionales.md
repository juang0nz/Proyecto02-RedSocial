# Cinco consultas adicionales de la red social

Se implementaron en `ucu.edu.aed.redsocial.ConsultasAdicionales`, con pruebas en `ConsultasAdicionalesTest`. Reciben el grafo dirigido de seguimiento `IDirectedIGraph<V,D>`; se invocan, por ejemplo, con `red.grafoDeSeguimiento()` y objetos `Usuario`. Son consultas **adicionales** a las funcionalidades mínimas de sugerencias, grados de separación, influencia, comunidades, usuarios puente y difusión de publicaciones.

Convención: la arista `A -> B` significa «A sigue a B». Los resultados usan la dirección de las aristas. Todas las consultas crean un índice temporal de sucesores y predecesores recorriendo vértices y aristas; no modifican el grafo original. Notación: V usuarios y E seguimientos.

| Consulta | Utilidad y criterio | Algoritmo | Tiempo | Espacio |
|---|---|---|---|---|
| `alcanzablesHastaK(g, origen, k)` | Explorar hasta qué cuentas se puede llegar siguiendo como máximo k relaciones, sin incluir el origen | BFS con distancia y corte en k | O(V+E) | O(V+E) |
| `seguidoresComunes(g, a, b)` | Encontrar cuentas que siguen simultáneamente a dos usuarios, útil para detectar audiencias compartidas | Construcción de adyacencia inversa e intersección de predecesores | O(V+E) | O(V+E) |
| `caminoEvitando(g, origen, destino, bloqueado)` | Comprobar si existe una vía alternativa de conexión que no pase por una cuenta concreta | BFS dirigido con reconstrucción de padres y exclusión del vértice | O(V+E) | O(V+E) |
| `gruposDeSeguimientoMutuo(g)` | Identificar grupos conectados exclusivamente por relaciones recíprocas; no equivale a componentes fuertemente conexas generales | Proyección de aristas recíprocas y BFS por componente | O(V+E) esperado | O(V+E) |
| `perdidaPorEliminarSeguimiento(g, origen, a, b)` | Simular qué cuentas quedarían fuera del alcance estructural de un usuario al retirar una relación concreta, sin cambiar el grafo | Dos BFS: antes y después de omitir A -> B; diferencia de conjuntos | O(V+E) | O(V+E) |

## Diferencia respecto a los requisitos obligatorios

- Alcanzables hasta k devuelve **todos** los usuarios de un entorno acotado, no solo la distancia entre un par.
- Seguidores comunes analiza la intersección de dos audiencias entrantes, no las sugerencias de cuentas a seguir.
- Camino evitando una cuenta calcula una ruta bajo una **restricción de exclusión**, no una distancia simple.
- Grupos de seguimiento mutuo usa únicamente vínculos recíprocos, diferente de las comunidades por conectividad fuerte.
- Pérdida por eliminar un seguimiento simula el efecto de quitar una **arista específica** desde un origen, distinto de detectar puntos de articulación de la proyección no dirigida.

## Casos de prueba

`ConsultasAdicionalesTest` incluye ciclos, usuarios aislados, alcance con k=0 y k negativo, intersección vacía, rutas alternativas y bloqueadas, grupos unitarios, grafo vacío, eliminación de arista existente e inexistente, y validación de usuarios desconocidos. La simulación comprueba que el grafo original sigue intacto.

## Ejecución

Desde la raíz del repositorio:

```bash
mvn -B -ntp -f Proyecto/pom.xml clean test
```

Los resultados de CI deben verificarse en la PR de integración antes de fusionar. Este documento no sustituye el informe global ni el experimento de optimización del Desafío 3.
