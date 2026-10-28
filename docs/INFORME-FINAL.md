# Proyecto Integrador 2 — Red Social
## Algoritmos y Estructuras de Datos — Equipo 2

**Entrega:** 28 de octubre de 2026.

## 1. Descripción de la solución

Se implementó una biblioteca genérica de grafos dirigidos y no dirigidos y se utilizó para construir el núcleo de una red social. La solución permite registrar usuarios, crear y eliminar seguimientos, consultar relaciones, publicar, comentar, compartir, mencionar usuarios y calcular el feed y el alcance de contenidos. Incluye algoritmos para recomendaciones, cercanía, influencia, comunidades y usuarios puente, además de cinco consultas estructurales adicionales.

## 2. Modelo y decisiones de diseño

Cada **usuario** es un vértice del grafo dirigido. Una arista **A → B** significa «A sigue a B»; no implica reciprocidad. Los seguidos de A son sus sucesores y los seguidores de B son sus predecesores. Las aristas de seguimiento tienen peso **1**, porque los grados de separación se miden por cantidad de saltos. El modelo impide auto-seguimiento y relaciones duplicadas desde la capa de dominio.

Se usa representación por **listas de adyacencia**, apropiada para redes donde la cantidad de seguimientos es menor que la cantidad máxima de pares posibles. Las colecciones auxiliares indexan usuarios y publicaciones, sin sustituir el grafo para las operaciones estructurales.

Los posteos, comentarios y compartidos son entidades del dominio, no vértices del grafo de seguimiento, para no mezclar relaciones de naturaleza distinta. Las menciones se registran como destinatarios asociados a posteos y comentarios. La capa `RedSocial` ofrece una vista de solo lectura del grafo a los algoritmos.

## 3. Biblioteca de grafos y algoritmos

- **Dirigidos:** Dijkstra (caminos mínimos), Floyd (distancias entre pares), Warshall (alcanzabilidad), excentricidad y centro, enumeración de caminos, DFS, BFS y clasificación topológica.
- **No dirigidos:** Prim y Kruskal (bosques/árboles abarcadores mínimos), BFS y puntos de articulación.
- **Análisis social:** cantidad y ranking de seguidores, ranking por alcance, comunidades por componentes fuertemente conexas y usuarios puente por puntos de articulación en proyección no dirigida.

Se incluyen pruebas unitarias de los módulos. Los algoritmos de recorridos BFS/DFS trabajan sobre vértices y aristas; Floyd y Warshall utilizan matrices para sus cálculos. La biblioteca propia realiza las operaciones principales, sin depender de una biblioteca externa de grafos.

## 4. Funcionalidades del dominio

**Seguimiento:** registrar/eliminar usuarios, seguir/dejar de seguir, consultar seguidos, seguidores y relaciones mutuas. **Cercanía:** BFS dirigido para grados de separación; recomendaciones por conexiones de segundo grado, ordenadas por cantidad de contactos compartidos. **Influencia:** ranking por seguidores y alcance estructural. **Comunidades:** componentes fuertemente conexas; **puentes:** puntos de articulación de la proyección no dirigida.

**Contenido y menciones:** el contenido viaja en sentido contrario a las aristas de seguimiento: del autor hacia sus seguidores. Las menciones entregan el posteo directamente al mencionado, incluso si no sigue al autor. Si un receptor comparte, se convierte en emisor y el contenido alcanza a sus propios seguidores. `Difusion.alcance` realiza un BFS con conjunto de visitados, evitando ciclos infinitos y destinatarios duplicados. El feed refleja publicaciones, compartidos y menciones, ordenados por actividad.

El conjunto de datos de ejemplo incluye usuarios conectados, grupos, enlaces puente, relaciones mutuas, usuario aislado y posteos con menciones y compartidos.

## 5. Cinco consultas adicionales

Se implementaron en `ConsultasAdicionales` y se justifican con casos de uso, algoritmos y complejidad en `docs/cinco-consultas-adicionales.md`:

1. **Alcanzables hasta k saltos:** BFS acotado, O(V+E).
2. **Seguidores comunes de dos cuentas:** intersección de predecesores, O(V+E).
3. **Camino mínimo evitando una cuenta:** BFS con vértice excluido, O(V+E).
4. **Grupos conectados por seguimiento mutuo:** componentes en la subred recíproca, O(V+E) esperado.
5. **Pérdida de alcance al retirar una relación:** comparación de dos BFS, O(V+E).

Las cinco usan el grafo y no son simples lecturas de datos almacenados.

## 6. Análisis de complejidad

V = usuarios, E = seguimientos, P = publicaciones y A = eventos de actividad.

| Operación | Tiempo estimado |
|---|---|
| Alta y búsqueda de usuario por índice | O(1) promedio |
| Agregar seguimiento | O(grado de salida del origen) |
| Consultar todos los seguidores con implementación original | O(V(V+E)) |
| Construir índice de seguidores optimizado | O(V+E) |
| BFS/DFS con adyacencias construidas | O(V+E) |
| Floyd / Warshall | O(V³) |
| Difusión de un posteo, más obtención de su actividad | O(V+E+A) |
| Cinco consultas adicionales | O(V+E) cada una, según documentación |

Los costos de operaciones que usan `buscarVertice` deben contemplar su búsqueda lineal en la implementación concreta. Las tablas detalladas de dominio y algoritmos están en `docs/modelo-social.md` y `docs/arquitectura-complejidad-dirigido.md`.

## 7. Desafío 3: optimización y experimento

**Problema:** recuperar los seguidores de cada usuario mediante llamadas repetidas a `predecessors` reexplora el grafo. **Alternativa:** `Difusion.seguidoresDeCadaVertice` indexa todos los seguidores con una pasada por las aristas.

- Original: O(V(V+E)).
- Optimizada: O(V+E) tiempo y O(V+E) memoria auxiliar.
- Verificación: `ExperimentoOptimizacionTest` compara resultados para grafos de distintos tamaños.
- Experimento temporal: ejecutar `mvn -B -ntp -f Proyecto/pom.xml -Dtest=ExperimentoOptimizacionTest -Dexperimento=true test`.
- **Resultados numéricos:** pendientes de registrar desde una ejecución real. No se deben presentar valores estimados como mediciones.

El procedimiento, los tamaños y la tabla para transcribir resultados se encuentran en `docs/experimento-optimizacion.md`.

## 8. Pruebas y reproducibilidad

Ejecutar desde la raíz del repositorio:

```bash
mvn -B -ntp -f Proyecto/pom.xml clean test
```

Las pruebas de biblioteca y dominio cubren caminos, recorridos, relaciones de seguimiento, publicaciones, menciones, compartidos y difusión. `ConsultasAdicionalesTest` agrega pruebas de las cinco operaciones nuevas, incluidos aislados, ciclos, entradas inválidas y grafo vacío. `ExperimentoOptimizacionTest` valida la equivalencia de resultados entre ambas variantes.

**Nota de verificación:** registrar el resultado de la ejecución más reciente de GitHub Actions antes de entregar. El éxito de compilación no sustituye una demostración funcional de todos los requisitos.

## 9. Registro de uso de herramientas de IA

La consigna permite herramientas de IA y exige documentar su uso. Durante la integración y el cierre se utilizó asistencia de ChatGPT para analizar requisitos, preparar código de consultas adicionales, generar casos de prueba y redactar documentación técnica. Los cambios se incorporaron mediante commits y deben validarse con pruebas y revisión humana. La asistencia no sustituye la comprensión individual de los algoritmos.

**A completar por el equipo:** cualquier uso adicional de Copilot, ChatGPT u otras herramientas por cada integrante, con propósito, código aceptado y comprobaciones realizadas. No atribuir usos no confirmados a otros integrantes.

## 10. Defensa

Explicar el significado de A→B, por qué el contenido viaja por predecesores, por qué el modelo distingue posteos de usuarios, cómo funciona el BFS de difusión, cómo se identifican comunidades y puentes, las diferencias entre Dijkstra/Floyd/Warshall y Prim/Kruskal, y la complejidad antes/después de la optimización. Preparar una demo con usuario aislado, seguimiento mutuo y un mencionado que comparte contenido.

## Estado antes de entregar

Este informe es una base integrada de los módulos revisados. **Falta completar las cifras reales del experimento, confirmar el último CI y revisar que la entrega final incluya todas las piezas requeridas.** No declarar la entrega finalizada hasta completar esos pasos.
