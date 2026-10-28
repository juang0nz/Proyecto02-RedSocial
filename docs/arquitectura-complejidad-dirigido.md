# Arquitectura y complejidad — biblioteca de grafo dirigido

## Alcance

Esta sección documenta el módulo dirigido desarrollado y validado por Juanchi. No representa todavía el informe final del equipo: faltan la integración con el grafo no dirigido y el modelo social, además de confirmar las decisiones conjuntas.

## Organización técnica

El proyecto utiliza Maven en `Proyecto/pom.xml`. El paquete `ucu.edu.aed.tda.grafo` declara las interfaces, el subpaquete `model` reúne aristas y resultados, y `impl` contiene `DirectedGraph` y `DirectedGraphAlgorithms`. Las pruebas están bajo `Proyecto/src/test/java`.

`DirectedGraph<V,D>` almacena un `LinkedHashMap<V, List<Edge<V,D>>>`: cada vértice tiene una lista de aristas salientes. Se conserva el orden de inserción de vértices y adyacencias. Una arista `A -> B` representa una relación dirigida de A hacia B. En la aplicación social se propone interpretar `A -> B` como «A sigue a B», sujeto al contrato de integración con Dalton.

La implementación actual impide aristas duplicadas para el mismo origen y destino; permite autoaristas si el origen y destino existen. `esConexo()` evalúa **conectividad débil** al recorrer sucesores y predecesores, no conectividad fuerte. `buscarVertice` realiza una búsqueda lineal usando `Comparable<V>`, por lo que algunas operaciones de alto nivel tienen un costo adicional respecto de la formulación teórica de los algoritmos.

## Algoritmos

- **Dijkstra:** caminos mínimos desde un origen, con pesos finitos no negativos. Implementación con selección lineal del siguiente vértice; no usa cola de prioridad. Rechaza pesos negativos y no finitos.
- **Floyd:** costos mínimos entre todos los pares y reconstrucción de rutas; acepta pesos negativos finitos, pero rechaza ciclos de costo negativo.
- **Warshall:** alcanzabilidad dirigida y reconstrucción de rutas por saltos (PR #4); no equivale a minimizar los pesos de las aristas.
- **DFS y BFS:** recorridos desde un origen con control de visitados para evitar repetir vértices, incluidos los ciclos (PR #5).
- **Todos los caminos:** enumeración recursiva de caminos simples, con retroceso y acumulación de pesos. No enumera recorridos que repiten vértices.
- **Clasificación topológica:** algoritmo de Kahn para grafos dirigidos acíclicos; si existe un ciclo lanza `IllegalArgumentException` (PR #5).
- **Centro y excentricidad:** el código calcula la **excentricidad de entrada**: máximo costo desde los demás vértices hacia el vértice consultado. El centro minimiza esa medida. En grafos no fuertemente conexos pueden aparecer valores infinitos. Esta definición debe contrastarse con la consigna.
- **Grados de separación:** BFS sobre aristas dirigidas con costo unitario por salto; retorna -1 cuando no existe ruta (PR #6).
- **Sugerencias:** seguidos de seguidos, excluyendo usuario actual y seguidos directos; prioriza cantidad de conexiones compartidas y desempata según orden de vértices (PR #6).

## Complejidad

Notación: V = cantidad de vértices; E = cantidad de aristas; P = cantidad de caminos simples encontrados; L = longitud de un camino; S = cantidad de sugerencias; Δ = grado máximo de salida.

| Operación | Tiempo | Espacio auxiliar | Observaciones |
| --- | --- | --- | --- |
| Agregar vértice | O(1) promedio | O(1) | HashMap; sin redimensionamiento |
| Agregar arista | O(Δ) promedio | O(1) | Revisa duplicados en la lista de origen |
| Buscar vértice por criterio | O(V) | O(1) | Itera y compara |
| Obtener sucesores | O(V + Δ) | O(Δ) | Busca primero el vértice |
| Obtener predecesores | O(V + E) | O(V) | Inspecciona todas las aristas |
| Dijkstra actual | O(V² + EV) | O(V) | La búsqueda lineal de vértices en adyacencias agrega el factor V |
| Floyd | O(V³ + E) | O(V²) | Matrices de costos y sucesores |
| Warshall | O(V³ + E) | O(V²) | Matrices de cierre y rutas |
| DFS/BFS actuales | O(V + EV) | O(V) | Cada expansión llama a buscarVertice lineal |
| Topológico actual | O(V + EV) | O(V) | Kahn con búsqueda lineal al acceder a adyacencias |
| Todos los caminos | Exponencial en el peor caso; O(P · L · V) como cota de emisión/búsquedas | O(V + P · L) | Puede haber factorialmente muchos caminos simples |
| Grados de separación | O(V + EV) | O(V) | BFS, con búsqueda lineal de vértices |
| Sugerencias | O(V² + EV + S log S) cota conservadora | O(V + S) | Consulta sucesores y ordena candidatos |
| Excentricidad (una consulta) | O(V³ + E) | O(V²) | Recalcula Floyd |
| Centro | O(V³ + E + V²) | O(V²) | Un Floyd y comparación de excentricidades |

Las complejidades de los recorridos incluyen la implementación concreta de `buscarVertice`, por lo que difieren de la cota ideal O(V+E) de BFS/DFS con acceso O(1) a vecinos. Las cotas son conservadoras y deben revisarse cuando se integren los módulos.

## Pruebas y estado de integración

- PR #3: pruebas de Dijkstra/Floyd y automatización de tests.
- PR #4: Warshall, centro y excentricidad.
- PR #5: DFS, BFS, clasificación topológica y caminos.
- PR #6: sugerencias y grados de separación.

Los checks de las PR #3, #4 y #5 fueron confirmados por el equipo; la PR #6 aún requiere confirmación. **No confundir una PR verde con la validación de todas las ramas integradas**. Falta ejecutar la suite completa sobre la versión integrada, verificar el contrato de seguimiento con Dalton y la compatibilidad con el módulo de Gerónimo.

## Puntos para la defensa oral

1. Justificar la elección de un grafo dirigido: seguir a alguien no implica reciprocidad.
2. Explicar por qué BFS sirve para grados de separación sin pesos y Dijkstra para costos ponderados no negativos.
3. Diferenciar Floyd (costo mínimo) de Warshall (alcanzabilidad).
4. Explicar cómo se evita el bucle infinito en DFS, BFS y enumeración de caminos simples.
5. Justificar por qué el orden topológico solo existe en grafos dirigidos acíclicos.
6. Distinguir excentricidad de entrada de excentricidad de salida y discutir vértices inalcanzables.
7. Reconocer que la búsqueda lineal de vértices afecta la complejidad real de los recorridos.

## Pendientes antes de entregar

- Confirmar que la definición de centro/excentricidad coincide con el Problem Set.
- Integrar PR #3, #4, #5 y #6 tras revisión y resolución de conflictos, sin fusionar unilateralmente.
- Validar contratos con Gerónimo y Dalton.
- Actualizar la sección con resultados de ejecución integrada, demo y decisiones del equipo.
