# Desafío 3 — Optimización de consulta de seguidores

## Operación y cuello de botella

La difusión de publicaciones necesita conocer los **seguidores de cada usuario**. En el grafo dirigido, una arista A→B indica «A sigue a B», por lo que los seguidores de B son sus predecesores.

**Solución original:** para cada vértice `v`, llamar a `predecessors(v)`. Como la representación guarda principalmente adyacencias salientes, cada llamada revisa los seguimientos para hallar aristas entrantes. Para todos los usuarios, el costo es **O(V(V+E))** (incluyendo búsqueda lineal del vértice).

**Solución optimizada implementada:** `Difusion.seguidoresDeCadaVertice(grafo)` crea primero una lista vacía por usuario y recorre **una sola vez** las aristas, agregando cada origen a la lista de seguidores de su destino. Tiempo **O(V+E)**, espacio auxiliar **O(V+E)**. `Difusion.alcance` utiliza esta estructura para su BFS. Es un intercambio de memoria temporal por menor tiempo al realizar consultas globales.

## Comparación y verificación

Se añadió `ExperimentoOptimizacionTest`:
- Prueba de equivalencia entre ambas soluciones para grafos de 0, 1, 10, 50 y 150 vértices.
- Benchmark optativo con redes deterministas de 100, 300 y 600 vértices, aproximadamente cuatro aristas salientes por vértice.
- Cinco repeticiones por tamaño, un calentamiento básico y medición con `System.nanoTime()`.
- La salida informa V, E, tiempo medio original, tiempo medio optimizado y cociente original/optimizado.

**Ejecutar:**

```bash
mvn -B -ntp -f Proyecto/pom.xml clean test
mvn -B -ntp -f Proyecto/pom.xml -Dtest=ExperimentoOptimizacionTest -Dexperimento=true test
```

Los resultados impresos por la segunda orden son los **datos experimentales reales** que deben copiarse al informe. No se consignan cifras inventadas. La comparación de complejidad predice mayor ventaja a medida que crece V; los tiempos concretos dependen de JVM, hardware, calentamiento y carga del equipo.

## Tabla para completar con la ejecución

| V | E | Original (ms) | Optimizada (ms) | Aceleración |
|---:|---:|---:|---:|---:|
| 100 | salida del test | salida del test | salida del test | salida del test |
| 300 | salida del test | salida del test | salida del test | salida del test |
| 600 | salida del test | salida del test | salida del test | salida del test |

## Limitaciones

La medición es un microbenchmark orientativo, no JMH: hay ruido de GC y JIT. Los tiempos no son un criterio de aprobación de las pruebas. La equivalencia de resultados se verifica independientemente del rendimiento. Esta optimización es la de la **consulta global de seguidores**, no una afirmación sobre Prim ni sobre todo el sistema.
