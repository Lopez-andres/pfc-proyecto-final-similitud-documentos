# Informe de proceso: Proyecto final, detección de similitud entre documentos

Dado un conjunto de documentos, se quiere saber cuáles se parecen entre sí. Cada documento
se resume por su **huella**: el conjunto de sus fragmentos de `k` palabras consecutivas. Dos
documentos se comparan con el **índice de Jaccard** de sus huellas, y un **índice invertido**
evita comparar las parejas que no comparten ningún fragmento. 

Este informe muestra cómo se ejecuta cada función: qué llamadas hace, qué queda en la pila 
y cómo se llega al resultado. Cada función tiene su sección, con la misma estructura. 

## Tipos de datos

```Scala
type Fragmento = Vector[String]
type Documento = (String, Vector[String])
type Corpus = Vector[Documento]
type Huella = Set[Fragmento]
type Indice = Map[Fragmento, Set[Int]]
type Pareja = (Int, Int)
type Medicion = (Int, Int, Double)
```

## Corpus de ejemplo 

Todas las trazas usan el ejemplo 1 del enunciado, con `k = 3`.

| Documento | Texto                           | Palabras | 
|-----------|---------------------------------|----------|
| `d0`      | `el gato duerme en el sofa`     | 6        |
| `d1`      | `el gato duerme en la alfombra` | 6        |
| `d2`      | `la lluvia moja el parque`      | 5        |

Sus huellas con `k = 3` son: 

| Documento | Fragmentos de la huella                                                         | Tamaño |
|-----------|---------------------------------------------------------------------------------|--------|
| `d0`      | `(el, gato, duerme)`, `(gato, duerme, en)`, `(duerme, en, el)`, `(en, el, sofa)` | 4      |
| `d1`      | `(el, gato, duerme)`, `(gato, duerme, en)`, `(duerme, en, la)`, `(en, la, alfombra)` | 4  |
| `d2`      | `(la, lluvia, moja)`, `(lluvia, moja, el)`, `(moja, el, parque)`                | 3      |

# `normalizar`

## Definición del algoritmo 

```Scala
def normalizar(texto: String): Vector[String] = {
    @tailrec
    def recorrer(cs: List[Char], actual: String, acc: Vector[String]) : Vector[String] = cs match {
      case Nil => if (actual.isEmpty) acc else acc :+ actual
      case c :: resto =>
        if (c.isLetterOrDigit) recorrer(resto, actual + c, acc)
        else if (actual.isEmpty) recorrer(resto, "", acc)
        else recorrer(resto, "", acc :+ actual)
    }
    recorrer(texto.toLowerCase.toList, "", Vector())
  }
```

* `normalizar` recibe un texto y devuelve el vector de sus palabras en minúsculas, sin signos
  de puntuación y sin cadenas vacías.

* El trabajo lo hace la función interna `recorrer`, que revisa el texto carácter por carácter.
  Recibe tres parámetros:
    * `cs`: los caracteres que faltan por revisar. 
    * `actual`: la palabra que se está armando.
    * `acc`: el acumulador con las palabras ya terminadas.
* `@tailrec` comprueba que `recorrer` sea recursión de cola.

## Explicación paso a paso

### Caso base

```Scala
case Nil => if (actual.isEmpty) acc else acc :+ actual
```

Cuando no quedan caracteres, se devuelve el acumulador. Si había una palabra a medio armar 
se agrega primero: así no se pierde la última palabra de un texto que termina en letra. 

### Caso recursivo 

```Scala
case c :: resto =>
```

Se toma el primer carácter `c` y hay tres posibilidades:

* **`c` es letra o dígito:** se pega a `actual` y se sigue con el resto. 
* **`c` es separador y `actual` está vacía:** no hay palabra que cerrar, se salta. Esto evita
  las cadenas vacías cuando hay dos separadores seguidos. 
* **`c` es separador y `actual` tiene letras:** la palabra terminó. Se agrega a `acc` y
  `actual` vuelve a quedar vacía. 

En los tres casos la llamada a `recorrer` es la última operación: no queda nada pendiente. 

## Llamados de pila en recursión de cola 

Ejemplo:

```Scala
normalizar("El, gato")
```

Primero `toLowerCase` deja `"el, gato"` y `toList` lo convierte en una lista de caracteres. 
En la tabla, `cs` se escribe como texto para abreviar. 

| Paso | `cs`         | `c`     | `actual` | `acc`          | Qué pasa                                  |
|------|--------------|---------|----------|----------------|-------------------------------------------|
| 1    | `"el, gato"` | `e`     | `""`     | `Vector()`     | letra: se pega a `actual`                 |
| 2    | `"l, gato"`  | `l`     | `"e"`    | `Vector()`     | letra: se pega a `actual`                 |
| 3    | `", gato"`   | `,`     | `"el"`   | `Vector()`     | separador: se cierra la palabra `"el"`    |
| 4    | `" gato"`    | espacio | `""`     | `Vector("el")` | separador con `actual` vacía: se salta    |
| 5    | `"gato"`     | `g`     | `""`     | `Vector("el")` | letra: se pega a `actual`                 |
| 6    | `"ato"`      | `a`     | `"g"`    | `Vector("el")` | letra: se pega a `actual`                 |
| 7    | `"to"`       | `t`     | `"ga"`   | `Vector("el")` | letra: se pega a `actual`                 |
| 8    | `"o"`        | `o`     | `"gat"`  | `Vector("el")` | letra: se pega a `actual`                 |
| 9    | `Nil`        | —       | `"gato"` | `Vector("el")` | caso base: se agrega `"gato"` y se devuelve |

Resultado: `Vector("el", "gato")`.

## Comportamiento de la pila

Son 9 llamadas a `recorrer`, pero nunca hay 9 marcos en la pila. Como la llamada recursiva es
lo último que se hace, el marco actual se **reemplaza** por el siguiente: lo único que cambia
son los valores de `cs`, `actual` y `acc`. La pila se mantiene de tamaño constante sin importar 
el largo del texto, y el resultado se devuelve una sola vez, desde el caso base. 

## Ejemplo de uso 

```Scala
println(normalizar("El, gato"))           // Vector(el, gato)
println(normalizar("¡Hola, Mundo 2026!")) // Vector(hola, mundo, 2026)
println(normalizar(" ,.;! "))             // Vector()
```

## Diagrama de llamados de pila con recursión de cola 

```mermaid
sequenceDiagram
    participant U as Programa
    participant N as normalizar("El, gato")
    participant R as recorrer (un solo marco)

    U->>N: llamada inicial
    N->>R: cs = "el, gato", actual = "", acc = Vector()
    R->>R: 'e' y 'l' son letras, actual = "el"
    R->>R: ',' cierra la palabra, acc = Vector("el")
    R->>R: el espacio se salta, actual sigue vacía
    R->>R: 'g', 'a', 't', 'o' son letras, actual = "gato"
    R->>R: cs = Nil, se agrega "gato"
    R-->>N: Vector("el", "gato")
    N-->>U: Vector("el", "gato")
```

## Conclusión 

`normalizar` recorre el texto una sola vez con recursión de cola. El estado completo viaja en 
los parámetros `actual` y `acc`, por eso no quedan operaciones pendientes y la pila no crece. 

# `fragmentos`

## Definición del algoritmo

```Scala
def fragmentos(ts: Vector[String], k: Int): Vector[Fragmento] = {
    if (ts.length < k) Vector()
    else ts.take(k) +: fragmentos(ts.tail, k)
}
```

* `fragmentos` recibe las palabras `ts` de un documento y un entero `k`, y devuelve todos los 
  fragmentos de `k` palabras consecutivas, en el orden en que aparecen. 
* Con `t` palabras hay $t - k + 1$ fragmentos cuando $t \geq k$, y ninguno cuando $t < k$.
* Es recursión lineal: cada llamada toma un fragmento y se vuelve a llamar con el resto. 

## Explicación paso a paso 

### Caso base

```Scala
if (ts.length < k ) Vector()
```

Si quedan menos de `k` palabras ya no se puede formar ningún fragmento, y se devuelve el 
vector vacío sin hacer más llamados. 

### Caso recursivo

```Scala 
ts.take(k) +: fragmentos(ts.tail, k)
```

En cada llamada:

* `ts.take(k)` toma las primeras `k` palabras: ese es el fragmento que empieza en la posición actual. 
* `ts.tail` quita la primera palabra, de modo que la ventana se corre una posición.
* `+:` pone el fragmento al frente del resultado de la llamada recursiva. 
* Como falta hacer `+:` después de que la llamada recursiva responda, cada llamada deja una 
  operación pendiente en la pila. 

## Llamados de pila en recursión lineal 

Ejemplo, con las palabras de `d2`:

```scala
fragmentos(Vector("la", "lluvia", "moja", "el", "parque"), 3)
```

### Paso 1: Llamada inicial

```Scala
fragmentos(Vector(la, lluvia, moja, el, parque), 3)
// 5 >= 3 -> Vector(la, lluvia, moja) +: fragmentos(Vector(lluvia, moja, el, parque), 3)
```
### Paso 2: Primera iteración

```Scala
fragmentos(Vector(lluvia, moja, el, parque), 3)
// 4 >= 3 -> Vector(lluvia, moja, el) +: fragmentos(Vector(moja, el, parque), 3)
```

### Paso 3: Segunda iteración 

```Scala
fragmentos(Vector(moja, el, parque), 3)
// 3 >= 3 -> Vector(moja, el, parque) +: fragmentos(Vector(el, parque), 3)
```

### Paso 4: Caso base

```Scala
fragmentos(Vector(el, parque), 3)
// 2 < 3 -> Vector()
```

### Paso 5: Se desenvuelve lo pendiente 

```Scala
fragmentos(Vector(el, parque), 3)               // Vector()
fragmentos(Vector(moja, el, parque), 3)         // Vector((moja, el, parque))
fragmentos(Vector(lluvia, moja, el, parque), 3) // Vector((lluvia, moja, el), (moja, el, parque))
fragmentos(Vector(la, lluvia, moja, el, parque), 3)
// Vector((la, lluvia, moja), (lluvia, moja, el), (moja, el, parque))
```

En el punto más profundo hay 4 llamadas en la pila: 3 esperando su `+:` y el caso base.
Salen $5 - 3 + 1 = 3$ fragmentos, los mismos de la huella de `d2`.

## Diferencia con recursión de cola 

* En `fragmentos` cada llamada espera el resultado de la siguiente para ponerle su fragmento
  al frente. Por eso la pila crece con el documento: con `t` palabras se apilan $t - k + 2$ llamadas.
* En `normalizar` el resultado parcial viaja en el acumulador y la pila no crece. 

## Ejemplo de uso

```Scala
println(fragmentos(Vector("la", "lluvia", "moja", "el", "parque"), 3))
// Vector(Vector(la, lluvia, moja), Vector(lluvia, moja, el), Vector(moja, el, parque))
println(fragmentos(Vector("el", "parque"), 3)) // Vector()
```

## Diagrama de llamados de pila con recursión lineal

```mermaid
sequenceDiagram
    participant U as Programa
    participant L0 as fragmentos(5 palabras, 3)
    participant L1 as fragmentos(4 palabras, 3)
    participant L2 as fragmentos(3 palabras, 3)
    participant L3 as fragmentos(2 palabras, 3)

    U->>L0: llamada inicial
    L0->>L1: (la, lluvia, moja) pendiente
    L1->>L2: (lluvia, moja, el) pendiente
    L2->>L3: (moja, el, parque) pendiente, caso base

    L3-->>L2: retorna Vector()
    L2-->>L1: retorna 1 fragmento
    L1-->>L0: retorna 2 fragmentos
    L0-->>U: retorna 3 fragmentos
```

## Conclusión 

`fragmentos` desliza una ventana de `k` palabras con recursión lineal. Cada llamada deja un 
`+:` pendiente, así que la pila crece con el número de palabras y el resultado se arma de atrás hacia adelante. 

# `jaccard`

## Definición del algoritmo

```Scala
def jaccard(h1: Huella, h2: Huella): Double = {
    val comunes = (h1 intersect h2).size
    val total = h1.size + h2.size - comunes
    if (total == 0) 0.0 else comunes.toDouble / total
  }
```

* `jaccard` recibe dos huellas y devuelve qué tanto se parecen, con un número entre 0 y 1:


$$J(A, B) = \frac{|A \cap B|}{|A \cup B|}, \qquad J(\emptyset, \emptyset) = 0$$

* No es recursiva: son tres operaciones seguidas y no hay llamadas esperando en la pila. 

## Explicación paso a paso 

* **`comunes`:** `h1 intersect h2` deja los fragmentos que están en las dos huellas, y `.size`
  los cuenta. 
* **`total`:** es el tamaño de la unión. No hace falta construirla, porque 
  $|A \cup B| = |A| + |B| - |A \cap B|$: al sumar los dos tamaños los comunes se cuentan dos 
  veces, y por eso se restan una vez.
* **`total == 0`:** solo pasa cuando las dos huellas están vacías. Ahí se devuelve `0.0` y se
  evita dividir por cero. 
* **`toDouble`:** `comunes` y `total` son enteros. Sin la conversión, la división sería entera 
  y el resultado solo podría ser 0 o 1.

## Seguimiento de la ejecución

Se evalúa `jaccard` con las huellas del corpus de ejemplo. 

| Pareja     | `comunes` | `total`       | Resultado       |
|------------|-----------|---------------|-----------------|
| `d0`, `d1` | 2         | 4 + 4 - 2 = 6 | 2 / 6 = 0.333   |
| `d0`, `d2` | 0         | 4 + 3 - 0 = 7 | 0 / 7 = 0.0     |
| `d1`, `d2` | 0         | 4 + 3 - 0 = 7 | 0 / 7 = 0.0     |

`d0` y `d1` comparten `(el, gato, duerme)` y `(gato, duerme, en)`. `d2` no comparte ningún 
fragmento con los otros dos. 

## Ejemplo de uso

```Scala
val h0 = huella(d0, 3)
val h1 = huella(d1, 3)
println(jaccard(h0, h1))        // 0.3333333333333333
println(jaccard(h0, h0))        // 1.0
println(jaccard(Set(), Set()))  // 0.0
```

## Diagrama de flujo 

```mermaid
flowchart TD
    A["Recibir h1 y h2"] --> B["comunes = tamaño de la intersección"]
    B --> C["total = tamaño de h1 + tamaño de h2 - comunes"]
    C --> D{"¿total es 0?"}
    D -- Sí --> E["Devolver 0.0"]
    D -- No --> F["Devolver comunes.toDouble / total"]
```

## Conclusión 

`jaccard` calcula la similitud con una intersección y una resta, sin construir la unión. El
caso de las dos huellas vacías se resuelve aparte para no dividir por cero. 

# `parejas`

## Definición del algoritmo

```Scala
def parejas(n: Int): Vector[Pareja] = 
  (for {
    i <- 0 until n
    j <- (i + 1) until n
  } yield (i, j)).toVector
```

* `parejas` recibe el número de documentos `n` y devuelve todas las parejas `(i, j)` con
  `i < j`, en orden. 
* Son $n(n-1)/2$ parejas: cada documento se empareja con los que están después de él.
* No es recursiva. Usa una expresión `for`, que el compilador convierte en llamadas a 
  funciones de alto orden. 

## Cómo traduce Scala una expresión `for`

Una expresión `for` con `yield` no es un ciclo: el compilador la reescribe como llamadas a 
`map`, `flatMap` y `withFilter`, que reciben una función como parámetro. 

| Forma del `for`                  | Se traduce a                       |
|----------------------------------|------------------------------------|
| `for (x <- e) yield f`           | `e.map(x => f)`                    |
| `for (x <- e if p) yield f`      | `e.withFilter(x => p).map(x => f)` |
| `for (x <- e1; y <- e2) yield f` | `e1.flatMap(x => e2.map(y => f))`  |

El **último** generador se traduce a `map` y todos los anteriores a `flatMap`.

## Traducción del `for`

```Scala
(0 until n).flatMap(i => ((i + 1) until n).map(j => (i, j))).toVector
```

* `i <- 0 until n` es el generador exterior, por eso es `flatMap`: para cada `i` produce una 
  colección de parejas y las concatena. 
* `j <- (i + 1) until n` es el último generador, por eso es `map`.
* `toVector` hace falta porque un `for` sobre un rango produce una `IndexedSeq`, y la firma
  pide un `Vector`.

## Seguimiento de la ejecución 

Se evalúa `parejas(3)`, porque el corpus de ejemplo tiene 3 documentos.

| `i` | Rango de `j`  | Parejas que aporta |
|-----|---------------|--------------------|
| 0   | `1 until 3`   | `(0, 1)`, `(0, 2)` |
| 1   | `2 until 3`   | `(1, 2)`           |
| 2   | `3 until 3`   | ninguna: rango vacío |


`flatMap` concatena lo que aportó cada `i`. Resultado: `Vector((0, 1), (0, 2), (1, 2))`, que 
son $3 \cdot 2 / 2 = 3$ parejas. 

Como `j` empieza en `i + 1`, nunca sale una pareja `(i, i)` ni una pareja repetida al revés.

## Ejemplo de uso

```Scala
println(parejas(3)) // Vector((0,1), (0,2), (1,2))
println(parejas(1)) // Vector()
println(parejas(0)) // Vector()
```

## Diagrama

```mermaid
flowchart TD
    R["parejas(3)"] --> I0["i = 0"]
    R --> I1["i = 1"]
    R --> I2["i = 2"]
    I0 --> A0["j = 1 y j = 2: aporta (0, 1) y (0, 2)"]
    I1 --> A1["j = 2: aporta (1, 2)"]
    I2 --> A2["rango vacío: no aporta"]
    A0 --> F["flatMap concatena y toVector"]
    A1 --> F
    A2 --> F
```

## Conclusión 

`parejas` es un `flatMap` con un `map` adentro. El rango `(i + 1) until n` garantiza que cada pareja
salga una sola vez y en orden. 

# `parejasCandidatas`

## Definición del algoritmo

```Scala
def parejasCandidatas(idx: Indice): Vector[Pareja] = {
    val cand = (for {
      ds <- idx.values.toList
      i <- ds
      j <- ds
      if i < j
    } yield (i, j)).toSet
    if (cand.isEmpty) Vector()
    else parejas(cand.map(par => par._2).max + 1).filter(par => cand.contains(par))
  }
```

* `parejasCandidatas` recibe el índice invertido y devuelve, sin repetir y en orden, las 
  parejas de documentos que comparten al menos un fragmento. 
* No es recursiva: combina una expresión `for` con las funciones de alto orden `map` y `filter`.

## Explicación paso a paso 

* **`idx.values.toList`:** toma los conjuntos de documentos del índice, uno por fragmento.
* **`i <- ds` y `j <- ds` con `if i < j`:** de cada conjunto saca todas las parejas de
  documentos, dejando solo las que tienen `i < j`.
* **`toSet`:** una pareja puede salir de varios fragmentos. Al pasar a conjunto queda una sola vez.
* **`cand.isEmpty`:** si ningún fragmento está en dos documentos no hay candidatas, y se
  devuelve el vector vacío. Además evita aplicar `max` a un conjunto vacío.
* **`cand.map(par => par._2).max + 1`:** como `i < j`, el mayor `j` es la posición más alta
  que aparece en alguna candidata. Sumarle 1 da cuántos documentos hay que considerar.
* **`parejas(...).filter(par => cand.contains(par))`:** `parejas` genera todas las parejas ya
  ordenadas, y `filter` deja solo las candidatas. Así el resultado sale ordenado sin escribir
  un ordenamiento.

## Traducción del `for`

```Scala
idx.values.toList.flatMap(ds =>
  ds.flatMap(i =>
    ds.withFilter(j => i < j).map(j => (i, j))))
```

Hay tres generadores: los dos primeros se vuelven `flatMap` y el último `map`. El `if` queda 
como `withFilter` pegado a `j`, y descarta sin construir una colección intermedia. 

## Seguimiento de la ejecución 

El índice invertido del corpus de ejemplo tiene 9 fragmentos:

| Fragmento                                       | Documentos | Parejas con `i < j` |
|-------------------------------------------------|------------|---------------------|
| `(el, gato, duerme)`                            | `Set(0, 1)` | `(0, 1)`           |
| `(gato, duerme, en)`                            | `Set(0, 1)` | `(0, 1)`           |
| `(duerme, en, el)`, `(en, el, sofa)`            | `Set(0)`    | ninguna            |
| `(duerme, en, la)`, `(en, la, alfombra)`        | `Set(1)`    | ninguna            |
| los tres fragmentos de `d2`                     | `Set(2)`    | ninguna            |

En un conjunto de un solo documento `i` y `j` son iguales, y `i < j` lo descarta. 


| Paso | Operación                               | Resultado                |
|------|-----------------------------------------|--------------------------|
| 1    | el `for` junta las parejas              | `List((0, 1), (0, 1))`   |
| 2    | `toSet` quita la repetida               | `Set((0, 1))`            |
| 3    | `cand.map(par => par._2).max + 1`       | `1 + 1 = 2`              |
| 4    | `parejas(2)`                            | `Vector((0, 1))`         |
| 5    | `filter(par => cand.contains(par))`     | `Vector((0, 1))`         |

Resultado: `Vector((0, 1))`. De las 3 parejas posibles solo hay que comparar una como dice el enunciado. 

## Ejemplo de uso

```Scala
val idx = indiceInvertido(huellas(c1, 3))
println(parejasCandidatas(idx))                    // Vector((0,1))
println(parejasCandidatas(Map(Vector("a") -> Set(2, 0, 1))))
// Vector((0,1), (0,2), (1,2))
println(parejasCandidatas(Map()))                  // Vector()
```

## Diagrama de flujo

```mermaid
flowchart TD
    A["Recibir el índice"] --> B["for: parejas de cada conjunto con i menor que j"]
    B --> C["toSet: quita las repetidas"]
    C --> D{"¿cand está vacío?"}
    D -- Sí --> E["Devolver Vector()"]
    D -- No --> F["parejas hasta el mayor j"]
    F --> G["filter: deja las que están en cand"]
    G --> H["Devolver las candidatas en orden"]
```

## Conclusión 

`parejasCandidatas` saca las parejas de cada entrada del índice con un `for` de tres
generadores, quita las repetidas con `toSet` y las ordena filtrando el resultado de `parejas`.

# `ranking`

## Definición del algoritmo

```Scala
def ranking(ss: Vector[Medicion], umbral: Double, p: Int): Vector[Medicion] = {
    // m1 va antes que m2: mayor similitud, y si empatan, menor posición (i, j).
    def vaAntes(m1: Medicion, m2: Medicion): Boolean = {
      m1._3 > m2._3 ||
        (m1._3 == m2._3 && (m1._1 < m2._1 || (m1._1 == m2._1 && m1._2 < m2._2)))
    }

    @tailrec
    def mezclar(xs: List[Medicion], ys: List[Medicion],
                acc: List[Medicion]): List[Medicion] = (xs, ys) match {
      case (Nil, _) => acc.reverse ++ ys
      case (_, Nil) => acc.reverse ++ xs
      case (x :: xs1, y :: ys1) =>
        if(vaAntes(x, y)) mezclar(xs1, ys, x :: acc)
        else mezclar(xs, ys1, y :: acc)
    }

    def msort(xs: List[Medicion]) : List[Medicion] = {
      val n = xs.length / 2
      if (n == 0) xs
      else {
        val (l1, l2) = xs.splitAt(n)
        mezclar(msort(l1), msort(l2), Nil)
      }
    }

    msort(ss.filter(m => m._3 >= umbral).toList).toVector.take(p)
  }
```

* `ranking` recibe las mediciones, un `umbral` y un tope `p`. Devuelve las que alcanzan el 
  umbral, de mayor a menor similitud y truncadas a `p`. 
* Tiene tres funciones internas:
    * `vaAntes`: decide cuál de dos mediciones va primero.
    * `mezclar`: junta dos listas ya ordenadas en una sola. Es recursión de cola. 
    * `msort`: ordena partiendo la lista en dos mitades. Hace dos llamadas recursivas. 

## Explicación paso a paso

### `vaAntes`

`m1` va antes que `m2` si tiene mayor similitud. Si empatan, va antes la de menor `i`, y si
también empatan en `i`, la de menor `j`. Así dos mediciones distintas nunca quedan indecisas y el resultado
no depende del orden en que llegaron. 

### `mezclar`

* **Casos base:** si una de las dos listas se acabó, se devuelve lo acumulado seguido de lo que queda de la otra.
`acc` se arma al revés, porque `x :: acc` agrega al frente; por eso se le aplica `reverse`.
* **Caso recursivo:** se comparan las cabezas `x` e `y` con `vaAntes`. La que va primero pasa al acumulador y se sigue con el resto. La 
llamada recursiva es lo último que se hace.

### `msort`

* **Caso base:** `n == 0` pasa cuando la lista tiene cero o un elemento. Ya está ordenada y se devuelve tal cual. 
* **Caso recursivo:** `splitAt(n)` parte la lista en dos mitades, cada una se ordena con su 
  propia llamada a `msort` y `mezclar` las junta. No es recursión de cola: después de las dos
  llamadas todavía falta mezclar. 

### La última línea

* `ss.filter(m => m._3 >= umbral)`: función de alto orden que deja las mediciones que 
  alcanzan el umbral. La comparación es `>=`, así que una medición igual al umbral entra. 
* `toList`: `mezclar` y `msort` trabajan con listas para usar los patrones `Nil` y `x :: xs`. 
* `toVector.take(p)`: vuelve al tipo de la firma y deja las primeras `p`.

## Seguimiento de las llamadas recursivas

En el corpus de ejemplo solo una pareja supera cero, y con una sola medición no hay nada que ordenar.
Por eso se usa este vector de cinco mediciones:

```Scala
val ss = Vector((0, 1, 0.4), (0, 2, 0.1), (1, 2, 0.9), (0, 3, 0.4), (1, 3, 0.7))
ranking(ss, 0.3, 3)
```

### Paso 1: Filtrar por el umbral

`(0, 2, 0.1)` no alcanza `0.3` y sale. Quedan cuatro, que se nombran para abreviar:

| Nombre | Medición      |
|--------|---------------|
| `A`    | `(0, 1, 0.4)` |
| `B`    | `(1, 2, 0.9)` |
| `C`    | `(0, 3, 0.4)` |
| `D`    | `(1, 3, 0.7)` |

### Paso 2: Bajan las llamadas de `msort`

```Scala
msort(List(A, B, C, D))   // n = 2 -> mezclar(msort(List(A, B)), msort(List(C, D)), Nil)
msort(List(A, B))         // n = 1 -> mezclar(msort(List(A)), msort(List(B)), Nil)
msort(List(A))            // n = 0 -> List(A), caso base
msort(List(B))            // n = 0 -> List(B), caso base
```

### Paso 3: Suben los resultados 

```Scala
mezclar(List(A), List(B), Nil)   // B tiene mayor similitud -> List(B, A)
msort(List(C, D))                // igual que la otra mitad -> List(D, C)
mezclar(List(B, A), List(D, C), Nil)
```

### Paso 4: La mezcla final

| Paso | `xs`         | `ys`         | `acc`           | Comparación                           |
|------|--------------|--------------|-----------------|---------------------------------------|
| 1    | `List(B, A)` | `List(D, C)` | `Nil`           | `B` (0.9) va antes que `D` (0.7)      |
| 2    | `List(A)`    | `List(D, C)` | `List(B)`       | `D` (0.7) va antes que `A` (0.4)      |
| 3    | `List(A)`    | `List(C)`    | `List(D, B)`    | empate en 0.4: `A` gana por `j` menor |
| 4    | `Nil`        | `List(C)`    | `List(A, D, B)` | caso base: `acc.reverse ++ ys`        |

Resultado de la mezcla: `List(B, D, A, C)`.

En el paso 3, `A = (0, 1, 0.4)` y `C = (0, 3, 0.4)` tienen la misma similitud y el mismo `i`.
Decide `j`: como `1 < 3`, `A` va primero.

### Paso 5: Truncar

`take(3)` deja las tres primeras:

```Scala
Vector((1, 2, 0.9), (1, 3, 0.7), (0, 1, 0.4))
```

## Comportamiento de la pila 

* **`msort`:** cada llamada parte la lista por la mitad, así que la profundidad de la pila 
  crece con el logaritmo del tamaño. Con 4 elementos hay 7 llamadas en total, pero como
  máximo 3 al mismo tiempo en la pila. 
* **`mezclar`:** es de cola. Su marco se reemplaza en cada paso y la pila no crece, sin 
  importar el largo de las listas. 

## Ejemplo de uso

```Scala
val ss = Vector((0, 1, 0.4), (0, 2, 0.1), (1, 2, 0.9), (0, 3, 0.4), (1, 3, 0.7))
println(ranking(ss, 0.3, 3))  // Vector((1,2,0.9), (1,3,0.7), (0,1,0.4))
println(ranking(ss, 0.95, 3)) // Vector()
println(ranking(ss, 0.0, 1))  // Vector((1,2,0.9))
```

## Diagrama de llamados de `msort`

```mermaid
flowchart TD
    M0["msort(A, B, C, D)"] --> M1["msort(A, B)"]
    M0 --> M2["msort(C, D)"]
    M1 --> M3["msort(A): caso base"]
    M1 --> M4["msort(B): caso base"]
    M2 --> M5["msort(C): caso base"]
    M2 --> M6["msort(D): caso base"]
    M3 --> Z1["mezclar: B, A"]
    M4 --> Z1
    M5 --> Z2["mezclar: D, C"]
    M6 --> Z2
    Z1 --> Z3["mezclar: B, D, A, C"]
    Z2 --> Z3
```

## Conclusión 

`ranking` filtra con una función de alto orden, ordena con un ordenamiento por mezcla y trunca a `p`. `msort` divide el problema con 
dos llamadas recursivas, `mezclar` junta las mitades con recursión de cola, y `vaAntes` fija un orden que no depende de cómo llegaron las mediciones. 


