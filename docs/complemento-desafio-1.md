# Complemento de verificación del Desafío 1: biblioteca de grafos

Fecha de verificación: 28 de octubre de 2026.

## Objetivo y alcance

Se completaron los casos de prueba pendientes identificados en la revisión del
Desafío 1. Los algoritmos ya estaban implementados: faltaba comprobar algunas
situaciones especiales mediante pruebas automáticas.

Se agregaron 16 métodos de prueba en tres clases existentes. No fue necesario
modificar clases, interfaces ni algoritmos de producción. Se mantuvo JUnit y la
ejecución habitual con Maven, sin incorporar dependencias.

Los cambios corresponden al requisito 5, sobre casos especiales, y amplían la
evidencia del requisito 6, sobre pruebas de las estructuras y algoritmos.

## Pruebas incorporadas

### DFS, BFS dirigido y clasificación topológica

Archivo: [DirectedTraversalsTest.java](../Proyecto/src/test/java/ucu/edu/aed/tda/grafo/impl/DirectedTraversalsTest.java).

| Prueba nueva | Qué comprueba |
|---|---|
| `testRecorridosSinOrigenEnGrafoVacio` | DFS y BFS sin origen terminan sin visitar vértices cuando el grafo está vacío. |
| `testRecorridosConOrigenEnGrafoVacio` | Al indicar un origen en un grafo vacío, ambos recorridos lanzan `IllegalArgumentException` y no llaman al consumidor. |
| `testRecorridosEnGrafoUnitario` | Las versiones con y sin origen de DFS y BFS visitan exactamente una vez al único vértice. |
| `testRecorridosDesdeVerticeAislado` | Desde un origen aislado se visita solamente ese vértice, sin pasar a otra componente. |
| `testOrdenTopologicoUnitario` | El único vértice forma por sí solo un orden topológico válido. |
| `testOrdenTopologicoDesconectadoConAislado` | El orden incluye todos los vértices de dos componentes y un aislado, sin repeticiones y respetando las dependencias de cada componente. |

En la clasificación topológica no se exige una secuencia única, porque pueden
existir varios órdenes correctos. Se comprueba que cada origen aparezca antes
que su destino y que no falte ni se repita ningún vértice.

### BFS no dirigido

Archivo: [UndirectedGraphAlgorithmsTest.java](../Proyecto/src/test/java/ucu/edu/aed/tda/grafo/impl/UndirectedGraphAlgorithmsTest.java).

| Prueba nueva | Qué comprueba |
|---|---|
| `testBeaGrafoVacio` | El recorrido termina sin visitas cuando no existen vértices. |
| `testBeaUnSoloVertice` | El único vértice se visita aunque no tenga aristas. |
| `testBeaConCicloYAislados` | El ciclo no provoca visitas repetidas y también se recorren los dos vértices aislados. Se conservan los conteos de vértices y aristas. |

Estas pruebas complementan las existentes sobre recorrido por niveles y visita
de componentes desconectadas.

### Todos los caminos entre dos vértices

Archivo: [DirectedAllPathsTest.java](../Proyecto/src/test/java/ucu/edu/aed/tda/grafo/impl/DirectedAllPathsTest.java).

| Prueba nueva | Qué comprueba |
|---|---|
| `testGrafoVacio` | No se obtienen caminos cuando el grafo no tiene vértices. |
| `testOrigenInexistente` | Se devuelve una lista vacía si falta el origen. |
| `testDestinoInexistente` | Se devuelve una lista vacía si falta el destino. |
| `testAmbosExtremosInexistentes` | También se devuelve vacío si faltan ambos extremos en un grafo no vacío. |
| `testGrafoUnitario` | El camino del único vértice a sí mismo contiene ese vértice y tiene costo cero. |
| `testAisladoYSentidoInverso` | No se inventan conexiones hacia un aislado, desde él ni en sentido contrario a una arista dirigida. |
| `testCaminosAlternativosDelMismoCosto` | Se obtienen las dos rutas distintas de igual costo, con sus secuencias y costos correctos, sin cambiar los conteos del grafo. |

A diferencia de Dijkstra y Floyd, que pueden conservar una sola ruta mínima,
la enumeración debe devolver ambas alternativas. Por eso se verifican las dos
secuencias, sin depender del orden en que aparezcan.

## Resultados de ejecución

| Verificación | Pruebas ejecutadas | Fallos | Errores | Omitidas | Código de salida |
|---|---:|---:|---:|---:|---:|
| Suite completa antes de agregar las pruebas | 175 | 0 | 0 | 0 | 0 |
| Tres clases ampliadas, después del cambio | 36 | 0 | 0 | 0 | 0 |
| Suite completa con compilación limpia, después del cambio | 191 | 0 | 0 | 0 | 0 |

Dentro de la última ejecución, las 11 clases de prueba revisadas para el
Desafío 1 sumaron **100 pruebas**, frente a las 84 anteriores. Las otras 91
pruebas corresponden al resto del proyecto y también pasaron.

Comando de verificación focalizada, desde la raíz del repositorio:

```powershell
mvn -o -f .\Proyecto\pom.xml "-Dtest=DirectedTraversalsTest,DirectedAllPathsTest,UndirectedGraphAlgorithmsTest" test
```

Comando de verificación completa:

```powershell
mvn -o -B -ntp -f .\Proyecto\pom.xml clean test
```

La opción `-o` utilizó las dependencias disponibles localmente. La ejecución
completa recompiló los 30 archivos Java de producción y los 19 archivos de
pruebas. Los resultados detallados se generan en
`Proyecto/target/surefire-reports/`.

Además, se compararon las huellas SHA-256 de los 30 archivos Java de producción
antes y después del trabajo: todos permanecieron idénticos.

## Estado de los requisitos

| Requisito | Estado después del complemento |
|---|---|
| 1. Interfaces y operaciones de grafos | Implementación existente en `DirectedGraph` y `UndirectedGraph`, con pruebas de operaciones, conexidad y ciclos. No se modificó. |
| 2. Algoritmos dirigidos | Implementación existente en `DirectedGraphAlgorithms`. Se ampliaron las pruebas de DFS, BFS, clasificación topológica y enumeración de caminos. |
| 3. Prim, Kruskal y BFS no dirigido | Implementación existente en `UndirectedGraphAlgorithms`. Se completaron los casos pendientes de BFS. |
| 4. Puntos de articulación | Implementación existente en `puntosDeArticulacion`, con pruebas que continuaron pasando. No se modificó. |
| 5. Casos especiales | Se cerraron los faltantes de pruebas señalados en la revisión anterior. Hay evidencia para cada tipo de caso solicitado, sin afirmar cobertura exhaustiva de todas las combinaciones posibles. |
| 6. Pruebas de funcionamiento | Se agregaron 16 pruebas a las existentes, todas accesibles mediante el comando habitual de Maven. La suite completa pasó. |

Se mantienen las decisiones de la implementación: la conexidad dirigida es
débil, el centro utiliza excentricidad de entrada, Warshall ignora los pesos y
la enumeración considera caminos sin repetir vértices. Las versiones dirigidas
de DFS y BFS sin origen inician recorridos separados desde cada vértice y
pueden repetir visitas en grafos con varios vértices conectados; las nuevas
pruebas no cambian ese comportamiento.

Las pruebas incorporadas son unitarias y trabajan con grafos en memoria.
No se requieren servidor, base de datos, Docker ni navegador. No se realizaron
mediciones de rendimiento ni se afirma un porcentaje de cobertura de líneas.

## Texto breve para incorporar al informe del proyecto

Como complemento de la verificación del Desafío 1, se incorporaron 16 pruebas
automáticas para los casos especiales que habían quedado pendientes. Se
comprobaron los recorridos DFS y BFS dirigidos en grafos vacíos, unitarios y
desde vértices aislados; la búsqueda en amplitud no dirigida en grafos vacíos,
unitarios y con ciclos y aislados; la clasificación topológica en grafos
unitarios y desconectados; y la enumeración de caminos con extremos
inexistentes, vértices aislados y alternativas del mismo costo.

No fue necesario modificar los algoritmos existentes. Las nuevas pruebas se
integraron a las clases de JUnit y al proceso habitual de Maven. Luego de una
compilación limpia se ejecutaron 191 pruebas del proyecto, de las cuales 100
corresponden a las 11 clases revisadas para la biblioteca de grafos, sin fallos,
errores ni pruebas omitidas. De esta forma se completaron los faltantes
identificados, sin considerar esta verificación como una prueba exhaustiva de
todas las entradas posibles.
