# Informe de corrección: Proyecto final, detección de similitud entre documentos 

Asignatura de Fundamentos de Programación Funcional y Concurrente. 

## Argumentar la corrección 

Sea $f : A \to B$ la especificación de una función y $P_f$ el programa en Scala que la 
implementa. Se demuestra que $P_f$ es correcto probando

$$\forall a \in A : P_f(a) == f(a)$$

* Para los programas recursivos se usa inducción: se prueba el caso base y, en el caso de 
  inducción, se supone la hipótesis de inducción (HI) sobre un valor más pequeño. 
* Para los programas con recursión de cola se define el estado, el invariante y la transformación,
  y se prueba que el invariante se conserva hasta el estado final. 

**Notación.** Una secuencia se escribe $\langle a_0, \dots, a_{n-1} \rangle$ y la secuencia
vacía $\langle \rangle$. El tamaño de un conjunto $A$ se escribe $\lvert A \rvert$.

## `normalizar`

### Especificación 

Sea $u$ el texto en minúsculas y $\alpha(c)$ el predicado «$c$ es letra o dígito», que en el 
código es `c.isLetterOrDigit`. Para una cadena $p$:

* $W(p)$ es la secuencia, en orden, de los tramos máximos no vacíos de caracteres de $p$ que 
  cumplen $\alpha$. Son las palabras de $p$.
* $\sigma(p)$ es el sufijo más largo de $p$ cuyos caracteres cumplen todos $\alpha$. Puede ser vacío. 
* $p^-$ es $p$ sin ese sufijo.

Como $p^-$ es vacía o termina en un separador, se cumple:

$$W(p) = \begin{cases} W(p^-) & \text{si } \sigma(p) \text{ es vacío} \\ W(p^-) \mathbin{:+} \sigma(p) & \text{si no} \end{cases}$$

La especificación es $f(\text{texto}) = W(u)$.

### Estado, invariante y transformación 

`recorrer` es recursión de cola, así que se argumenta como una iteración. 

* **Estado:** $s = (cs, actual, acc)$. Sea $p$ la parte ya revisada, de modo que $u = p \cdot cs$.
* **Estado inicial:** $s_0 = (u, \text{""}, \langle \rangle)$, con $p$ vacía.
* **Estado final:** $cs = \text{Nil}$, con $p = u$.
* **Invariante:** $\text{Inv}(s) \equiv actual = \sigma(p) \ \wedge\ acc = W(p^-)$.

**El invariante vale al inicio.** Con $p$ vacía, $\sigma(p)$ es vacío y $W(p^-) = \langle \rangle$.

**El invariante se conserva.** Sea $cs = c :: resto$ y supóngase $\text{Inv}(s)$. La parte
revisada pasa a ser $p \cdot c$.

* Si $\alpha(c)$: el estado nuevo es $(resto, actual + c, acc)$. Se tiene
  $\sigma(p \cdot c) = \sigma(p) \cdot c = actual + c$ y $(p \cdot c)^- = p^-$, luego
  $acc = W((p \cdot c)^-)$.
* Si no $\alpha(c)$ y $actual$ es vacío: el estado nuevo es $(resto, \text{""}, acc)$. Un
  separador no extiende ningún tramo, así que $\sigma(p \cdot c)$ es vacío y
  $(p \cdot c)^- = p \cdot c$. Como $\sigma(p)$ es vacío, $W(p \cdot c) = W(p) = W(p^-) = acc$.
* Si no $\alpha(c)$ y $actual$ no es vacío: el estado nuevo es
  $(resto, \text{""}, acc \mathbin{:+} actual)$. De nuevo $\sigma(p \cdot c)$ es vacío y
  $(p \cdot c)^- = p \cdot c$, y
  $W(p \cdot c) = W(p) = W(p^-) \mathbin{:+} \sigma(p) = acc \mathbin{:+} actual$.

**En el estado final se obtiene la respuesta.** Con $cs = \text{Nil}$ y $p = u$, el programa
devuelve $acc$ si $actual$ es vacío y $acc \mathbin{:+} actual$ si no. Por el invariante y la 
igualdad de $W$, en ambos casos el resultado es $W(u)$.

**Terminación.** Cada paso quita un carácter de $cs$, así que se llega a $\text{Nil}$ en 
$\lvert u \rvert$ pasos. 

Por lo tanto $\text{normalizar}(\text{texto}) == W(u)$. $\blacksquare$

De la definición de $W$ se sigue que el resultado no tiene cadenas vacías, porque los tramos 
son no vacíos. 

## `fragmentos`

### Especificación 

Sea $ts = \langle w_0, \dots, w_{t-1} \rangle$ y $k \ge 1$. El fragmento que empieza en $i$ es
$g_i = \langle w_i, \dots, w_{i+k-1} \rangle$, y la especificación es:

$$f(ts, k) = \begin{cases} \langle g_0, \dots, g_{t-k} \rangle & \text{si } t \ge k \\ \langle \rangle & \text{si } t < k \end{cases}$$

### Teorema 1: `fragmentos` cumple su especificación 

Se demuestra $\forall t \ge 0 : P_f(ts, k) == f(ts, k)$ por inducción sobre $t$, el largo de $ts$.

**Caso base:** $t < k$.

$$P_f(ts, k) \rightarrow \text{if } (t < k)\ \text{Vector}() \rightarrow \langle \rangle = f(ts, k)$$

**Caso de inducción:** $t \ge k$. La HI es que el programa es correcto para los vectores de 
largo $t - 1$, en particular para $ts.\text{tail} = \langle w_1, \dots, w_{t-1} \rangle$.

$$P_f(ts, k) \rightarrow ts.\text{take}(k) \mathbin{+:} P_f(ts.\text{tail}, k)$$

* $ts.\text{take}(k) = \langle w_0, \dots, w_{k-1} \rangle = g_0$.
* Los fragmentos de $ts.\text{tail}$ son los de $ts$ corridos una posición: el que empieza en
  $i$ dentro de la cola es $\langle w_{i+1}, \dots, w_{i+k} \rangle = g_{i+1}$. Por la HI:

$$P_f(ts.\text{tail}, k) \overset{\text{HI}}{=} f(ts.\text{tail}, k) = \langle g_1, \dots, g_{t-k} \rangle$$

   Cuando $t = k$ la cola tiene $k - 1$ palabras y esta secuencia es vacía. 

Entonces

$$P_f(ts, k) = g_0 \mathbin{+:} \langle g_1, \dots, g_{t-k} \rangle = \langle g_0, \dots, g_{t-k} \rangle = f(ts, k)$$

Por inducción, $\forall t \ge 0 : P_f(ts, k) == f(ts, k)$. $\blacksquare$

### Corolario 1: el tamaño de la huella

Si $t \ge k$, `fragmentos` devuelve exactamente $t - k + 1$ fragmentos, y ninguno si $t < k$.
La huella $H_k(d)$ es el conjunto de esos fragmentos, y pasar una secuencia a conjunto no 
puede aumentar su tamaño. Por lo tanto

$$\lvert H_k(d) \rvert \le t - k + 1$$

con igualdad si y solo si el documento no repite ningún fragmento, porque cada repetición 
quita exactamente un elemento al conjunto. 

## `jaccard`

### Especificación 

$$f(A, B) = \begin{cases} \dfrac{\lvert A \cap B \rvert}{\lvert A \cup B \rvert} & \text{si } A \cup B \neq \emptyset \\ 0 & \text{si } A = B = \emptyset \end{cases}$$

### Demostración 

El programa no es recursivo, así que se sigue por sustitución.

* $comunes = \lvert A \cap B \rvert$, porque `intersect` es la intersección de conjuntos.
* $total = \lvert A \rvert + \lvert B \rvert - \lvert A \cap B \rvert = \lvert A \cup B \rvert$,
  por el principio de inclusión y exclusión.
* $total = 0$ si y solo si $A \cup B = \emptyset$, es decir $A = B = \emptyset$. En ese caso
  el programa devuelve `0.0`, que es $f(\emptyset, \emptyset)$.
* Si $total \neq 0$, el programa devuelve $comunes / total$ como `Double`, que es
  $\lvert A \cap B \rvert / \lvert A \cup B \rvert = f(A, B)$.

Por lo tanto $\text{jaccard}(A, B) == f(A, B)$. $\blacksquare$

**Propiedades.** Como $A \cap B \subseteq A \cup B$, se tiene $0 \le f(A, B) \le 1$. Vale 1
cuando $A = B \neq \emptyset$ y vale 0 cuando $A \cap B = \emptyset$. Además
$f(A, B) = f(B, A)$, porque la intersección y la unión son conmutativas.

## `parejas`

### Especificación

$$f(n) = \langle\, (i, j) \mid 0 \le i < j < n \,\rangle \quad \text{en orden lexicográfico}$$

### Demostración 

El `for` se traduce a (ver `Proceso.md`):

```Scala
(0 until n).flatMap(i => ((i + 1) until n).map(j => (i, j))).toVector
```

* Para un $i$ fijo, el `map` interno produce el bloque
  $B_i = \langle (i, i+1), \dots, (i, n-1) \rangle$, que es vacío cuando $i = n - 1$.
* `flatMap` concatena los bloques en el orden de $i$: $B_0, B_1, \dots, B_{n-1}$.

**Están todas y solo ellas.** Una pareja $(i, j)$ con $0 \le i < j < n$ aparece en $B_i$,
porque $j$ está en el rango de $i + 1$ a $n - 1$. Y toda pareja de $B_i$ cumple
$0 \le i < j < n$ por los límites de los dos rangos.

**No hay repetidas.** Dos bloques distintos difieren en la primera componente, y dentro de un 
bloque cada $j$ aparece una vez. 

**El orden es el lexicográfico.** Los bloques van por $i$ creciente, y dentro de cada bloque
$j$ crece. 

**Cantidad.** El bloque $B_i$ tiene $n - 1 - i$ parejas, luego 

$$\sum_{i=0}^{n-1} (n - 1 - i) = \frac{n(n-1)}{2}$$

Si $n \le 1$ todos los rangos son vacíos y el resultado es $\langle \rangle$.

Por lo tanto $\text{parejas}(n) == f(n)$. $\blacksquare$

## `parejasCandidatas`

### Especificación 

Sea $idx$ un índice. El conjunto de candidatas es 

$$K(idx) = \lbrace\, (i, j) \mid i < j \ \wedge\ \exists g : i \in idx(g) \wedge j \in idx(g) \,\rbrace$$

y la especificación $f(idx)$ es la secuencia de los elementos de $K(idx)$, sin repetir y en
orden lexicográfico. 

### Teorema 2: `parejasCandidatas` cumple su especificación 

**Paso 1: $cand = K(idx)$.** El `for` recorre cada conjunto $ds$ de los valores del índice y
produce $(i, j)$ para todo $i \in ds$, $j \in ds$ con $i < j$. Esa es la condición que define
$K(idx)$, y `toSet` deja cada pareja una sola vez.

**Paso 2: si $cand$ es vacío.** El programa devuelve `Vector()`, que es $f(idx)$.

**Paso 3: si $cand$ no es vacío.** Sea $m$ el mayor $j$ entre las parejas de $cand$, que es lo
que calcula `cand.map(par => par._2).max`. Toda pareja $(i, j) \in cand$ cumple
$0 \le i < j \le m$, es decir $0 \le i < j < m + 1$. Por la corrección de `parejas`, está en
$\text{parejas}(m + 1)$.

`filter` deja exactamente los elementos de $\text{parejas}(m + 1)$ que están en $cand$ y
conserva su orden. Como $cand \subseteq \text{parejas}(m + 1)$, el resultado tiene todos los 
elementos de $cand$; como `parejas` no tiene repetidos y está en orden lexicográfico, el
resultado tampoco tiene repetidos y está en ese orden. 

Por lo tanto $\text{parejasCandidatas}(idx) == f(idx)$. $\blacksquare$

### Teorema 3: una pareja que no es candidata tiene similitud cero

Sea $idx$ el índice invertido del corpus, que por su especificación cumple
$i \in idx(g) \iff g \in H_k(d_i)$. La corrección de `indiceInvertido` se argumenta en su
propia sección.

Sea $(i, j)$ con $i < j$ que **no** es candidata. Entonces no existe $g$ con $i \in idx(g)$ y
$j \in idx(g)$, es decir, no existe $g$ que esté en $H_k(d_i)$ y en $H_k(d_j)$:

$$H_k(d_i) \cap H_k(d_j) = \emptyset$$

Por la corrección de `jaccard`, $comunes = 0$ y el resultado es 0, tanto si $total = 0$ como
si no. Por lo tanto $J(H_k(d_i), H_k(d_j)) == 0$. $\blacksquare$

**Consecuencia.** Con un umbral $\tau > 0$, una pareja de similitud 0 nunca entra al ranking. 
Las parejas que `detectarConIndice` deja de comparar son justamente esas, así que las dos soluciones filtran el mismo conjunto
de mediciones. Como el resultado de `ranking` no depende del orden de llegada (Teorema 5), `detectarDirecto` y `detectarConIndice` 
producen el mismo ranking.

## `ranking`

### Especificación 

Sobre las mediciones se define el orden $m_1 \prec m_2$, que en el código es `vaAntes`:

$$m_1 \prec m_2 \iff s_1 > s_2 \ \vee\ \big( s_1 = s_2 \wedge ( i_1 < i_2 \vee ( i_1 = i_2 \wedge j_1 < j_2 ) ) \big)$$

donde $m_1 = (i_1, j_1, s_1)$ y $m_2 = (i_2, j_2, s_2)$. La especificación $f(ss, \tau, p)$ es:
tomar las mediciones de $ss$ con $s \ge \tau$, ordenarlas según $\prec$ y dejar las primeras $p$.

### Lema 1: $\prec$ es un orden total estricto 

$\prec$ compara primero por similitud de mayor a menor, luego por $i$ y luego por $j$ de menor
a mayor: es un orden lexicográfico sobre tres componentes que tienen cada una orden total. 
Dos mediciones con parejas $(i, j)$ distintas difieren en alguna componente, así que siempre 
una va antes que la otra. Las mediciones del proyecto tienen parejas distintas, porque 
`parejas` y `parejasCandidatas` no repiten. 

### Lema 2: `mezclar` junta dos listas ordenadas

`mezclar` es recursión de cola, así que se argumenta con un invariante.

* **Estado:** $s = (xs, ys, acc)$.
* **Estado inicial:** $s_0 = (X, Y, \text{Nil})$, con $X$ e $Y$ ordenadas según $\prec$.
* **Estado final:** $xs = \text{Nil}$ o $ys = \text{Nil}$.
* **Invariante:** $xs$ e $ys$ están ordenadas; $acc.\text{reverse}$ está ordenada; todo
  elemento de $acc$ va antes que todos los de $xs$ y de $ys$; y los elementos de $acc$, $xs$
  e $ys$ juntos son los de $X$ e $Y$.

**Al inicio** $acc$ es vacío y el invariante vale porque $X$ e $Y$ están ordenadas. 

**Se conserva.** Sean $xs = x :: xs_1$ e $ys = y :: ys_1$.

* Si $x \prec y$: $x$ va antes que el resto de $xs$, porque $xs$ está ordenada, y antes que
  todos los de $ys$, porque va antes que su primer elemento. Al pasar $x$ al acumulador,
  $acc.\text{reverse}$ sigue ordenada, porque todos los de $acc$ iban antes que $x$.
* Si no $x \prec y$: por el Lema 1, $y$ va antes que $x$ o son iguales, y el argumento es el
  mismo con $y$.

**En el estado final** una lista está vacía y el programa devuelve $acc.\text{reverse}$
seguido de la otra. Por el invariante el resultado está ordenado y tiene los elementos de
$X$ e $Y$.

**Terminación.** Cada paso quita un elemento de $xs$ o de $ys$.

Por lo tanto $\text{mezclar}(X, Y, \text{Nil})$ es la lista ordenada con los elementos de 
$X$ e $Y$. $\blacksquare$

### Teorema 4: `msort` ordena

Se demuestra por inducción fuerte sobre el largo $m$ de $xs$ que $\text{msort}(xs)$ es la 
lista ordenada según $\prec$ con los mismos elementos de $xs$. 

**Caso base:** $m \le 1$. Entonces $n = \lfloor m / 2 \rfloor = 0$ y el programa devuelve
$xs$, que con cero o un elemento ya está ordenada. 

**Caso de inducción:** $m \ge 2$. La HI es que `msort` ordena toda lista de largo menor que $m$.
Aquí $n = \lfloor m / 2 \rfloor$ cumple $1 \le n < m$, así que las dos mitades $l_1$ y $l_2$
tienen largos $n$ y $m - n$, ambos menores que $m$.

$$\text{msort}(xs) \rightarrow \text{mezclar}(\text{msort}(l_1), \text{msort}(l_2), \text{Nil})$$

Por la HI, $\text{msort}(l_1)$ y $\text{msort}(l_2)$ están ordenadas y tienen los elementos
de $l_1$ y de $l_2$. Por el Lema 2, la mezcla está ordenada y tiene los elementos de las dos,
que son los de $xs$.

Por inducción fuerte, `msort` ordena toda lista. $\blacksquare$

### Teorema 5: `ranking` cumple su especificación 

$$\text{ranking}(ss, \tau, p) \rightarrow \text{msort}(\text{filter}(ss)).\text{take}(p)$$

* `filter(m => m._3 >= umbral)` deja exactamente las mediciones con $s \ge \tau$.
* Por el Teorema 4, `msort` las devuelve ordenadas según $\prec$.
* `take(p)` deja las primeras $p$, o todas si hay menos.

Eso es $f(ss, \tau, p)$. $\blacksquare$

**El resultado no depende del orden de llegada.** Por el Lema 1, $\prec$ es un orden total 
estricto, y un conjunto tiene una sola forma de quedar ordenado bajo un orden así. Si $ss'$
es una permutación de $ss$, el filtro deja las mismas mediciones y `msort` las deja en la
misma secuencia, luego $\text{ranking}(ss', \tau, p) == \text{ranking}(ss, \tau, p)$.

## Casos de prueba

Las pruebas están en `app/src/test/scala/proyecto/SimilitudTest.scala` y se ejecutan con
`./gradlew test`. Además de las de contrato, cada función tiene cinco casos propios:

| Función             | Qué cubren los cinco casos                                                                     |
|---------------------|------------------------------------------------------------------------------------------------|
| `normalizar`        | signos de puntuación, separadores repetidos, texto vacío, dígitos y última palabra, tildes y ñ |
| `fragmentos`        | `k = 1`, `k` igual al largo, menos de `k` palabras, fragmentos repetidos, cantidad `t - k + 1` |
| `jaccard`           | huellas iguales, disjuntas, vacías, una contenida en otra, solapamiento parcial y simetría     |
| `parejas`           | `n = 4` completo, `n = 0` y `n = 1`, `n = 2`, cantidad e `i < j`, sin repetidos y en orden     |
| `parejasCandidatas` | índice vacío, conjuntos de un documento, pareja repetida, tres documentos, entradas mezcladas  |
| `ranking`           | umbral inclusivo, empates, truncar a `p`, vacío, vector largo                                  |

Cada caso corresponde a una parte de la demostración: los vacíos a los casos base, los 
repetidos y los empates a los lemas, y las cantidades a las fórmulas.

## Funciones sin recursión: método y resultados que se usan

Las funciones `huella`, `huellas`, `huellasPar`, `similitudes`, `indiceInvertido` y `detectarDirectoPar` no son recursivas ni iterativas: no se llaman a sí mismas ni cambian un estado. Por eso aquí no hay hipótesis de inducción ni invariante. La demostración consiste en calcular qué devuelve el programa con el modelo de sustitución y compararlo con la especificación $f$. Un paso de evaluación se escribe $P(x) \rightarrow e$.

**Notación adicional.** Se siguen usando $g_i$, $H_k(d)$ y $J$ de las secciones anteriores.

| Símbolo     | Significado                                                                                  |
|-------------|----------------------------------------------------------------------------------------------|
| $H_i$       | abrevia $H_k(d_i)$, la huella del documento $i$, cuando $k$ se sobreentiende                 |
| $I(g)$      | $\lbrace\, i \mid g \in H_i \,\rbrace$: los documentos cuya huella contiene el fragmento $g$ |
| $\tau$, $p$ | umbral y número máximo de resultados                                                         |

**Resultados ya demostrados que se usan como hechos.**

| Función                         | Qué se usa                                                                                                                                |
|---------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------|
| `fragmentos(ts, k)`             | Teorema 1: devuelve $\langle g_0, \dots, g_{t-k} \rangle$ si $t \ge k$ y $\langle \rangle$ si $t < k$                                     |
| `jaccard(A, B)`                 | su demostración: devuelve $J(A, B)$                                                                                                       |
| `parejas(n)`                    | su demostración: todas las $(i, j)$ con $0 \le i < j < n$, sin repetir y en orden lexicográfico                                           |
| `ranking(ss, umbral, p)`        | Teorema 5: deja las mediciones con $s \ge \tau$, las ordena según $\prec$ y toma las primeras $p$. Además no depende del orden de llegada |
| `similitudesPar(hs, ps, corte)` | se usa su especificación: da el mismo resultado que `similitudes(hs, ps)` para cualquier `corte`                                          |

**Hechos sobre la biblioteca de Scala**, los vistos en clase para colecciones y para `parallel`:

* `map` sobre un `Vector` devuelve un vector del mismo largo, donde la posición $i$ es la función aplicada al elemento $i$.
* `splitAt(m)` sobre un vector $c$ devuelve $(c_1, c_2)$, con $c_1$ los primeros $m$ elementos y $c = c_1 \mathbin{++} c_2$.
* `parallel(e1, e2)` devuelve un valor idéntico a $(e1, e2)$. Solo cambia el tiempo de ejecución, no el resultado.

## `huella`

### Especificación

$$f(d, k) = H_k(d) = \lbrace\, g_i \mid 0 \le i \le t - k \,\rbrace$$

Es el conjunto de los fragmentos de $k$ palabras de $d$.
Aquí $t$ es el número de palabras de $d$ y $g_i$ es el fragmento de $k$ palabras que empieza en la palabra $i$. Si $t < k$ no hay ningún inicio válido y el conjunto es vacío.

Se desea demostrar:

$$\forall d, \forall k \ge 1 : P_{\text{huella}}(d, k) == H_k(d)$$

### Demostración 

El programa no es recursivo. Se separan dos casos, según quepa o no algún fragmento.

**Caso $t \ge k$.**

$$P_{\text{huella}}(d, k) \rightarrow \text{fragmentos}(\text{palabrasDoc}(d), k)\text{.toSet}$$

`palabrasDoc(d)` es el vector de palabras $\langle w_0, \dots, w_{t-1} \rangle$: toma la segunda componente de la pareja y deja el nombre fuera. Por el Teorema 1:

$$\rightarrow \text{Vector}(g_0, \dots, g_{t-k})\text{.toSet} \rightarrow \lbrace\, g_0, \dots, g_{t-k} \,\rbrace = H_k(d)$$

`toSet` conserva los mismos elementos y, si uno aparece varias veces, deja una sola copia. No introduce otros valores. Luego la huella implementada es $H_k(d)$.

**Caso $t < k$.** `fragmentos` devuelve `Vector()` y `Vector().toSet` es `Set()`. Por otro lado, no existe $i$ con $0 \le i\le t - k$, porque $t - k < 0$, así que $H_k(d) = \emptyset$. Son iguales.
 
Por lo tanto $P_{\text{huella}}(d, k) == H_k(d)$. $\blacksquare$

**Tamaño de la huella.** Sea $N = \max(0, t - k + 1)$. Por el Teorema 1, 
`fragmentos` produce $N$ fragmentos. Pasar a conjunto no puede aumentar el tamaño, así que por el Corolario 1 

$$\lvert H_k(d) \rvert \le N$$

con igualdad si y solo si el documento no repite ningún fragmento. Se escribe $N$ con el máximo porque, para $t < k$, $t - k + 1$ puede ser negativo y una cantidad de elementos no lo es.

### Aplicación a ejemplos

| Documento                             | $k$ | $N$ | $\lvert H \rvert$ | Caso                         |
|---------------------------------------|-----|-----|-------------------|------------------------------|
| `a b a b a b`                         | 2   | 5   | 2                 | repite: desigualdad estricta |
| `la casa es grande y la casa es azul` | 3   | 7   | 6                 | repite `(la casa es)`        |
| `el gato duerme en el sofa`           | 3   | 4   | 4                 | no repite: igualdad          |

## `huellas`

### Especificación

Sea $c = \langle d_0, \dots, d_{n-1} \rangle$:

$$f(c, k) = \langle H_k(d_0), \dots, H_k(d_{n-1}) \rangle$$

La huella del documento $i$ queda en la posición $i$. Las demás funciones dependen de esa correspondencia, porque el índice invertido guarda posiciones.

Se desea demostrar:
 
$$\forall c, \forall k \ge 1 : P_{\text{huellas}}(c, k) == f(c, k)$$

### Demostración 

$$P_{\text{huellas}}(c, k) \rightarrow c\text{.map}(d \rightarrow \text{huella}(d, k)) \rightarrow \langle \text{huella}(d_0, k), \dots, \text{huella}(d_{n-1}, k) \rangle$$

La segunda flecha vale porque `map` conserva el largo y las posiciones. Por la demostración de `huella`, $\text{huella}(d_i, k) == H_k(d_i)$ para cada $i$. Luego el resultado es $\langle H_k(d_0), \dots, H_k(d_{n-1}) \rangle = f(c, k)$. $\blacksquare$

**Casos límite.** Si $n = 0$, el programa y la especificación dan el vector vacío.
Un documento con menos de $k$ palabras da `Set()` en su propia posición: no se omite, porque `map` no descarta elementos. 

### Aplicación a ejemplos

Con `"el sol sale temprano"`, `"hoy"` y `"mañana llueve mucho hoy"`, y $k = 2$, los tamaños de las huellas son `Vector(3, 0, 3)`. El segundo documento tiene $t = 1 < 2$, su huella es `Set()` y queda en la posición 1. Con un corpus vacío el resultado es `Vector()`.

## `huellasPar`

### Especificación

`huellasPar` debe dar el mismo resultado que `huellas`, es decir, la especificación de `huellas`:

$$f(c, k) = \langle H_k(d_0), \dots, H_k(d_{n-1}) \rangle$$

Se desea demostrar:

$$\forall c, \forall k \ge 1 : P_{\text{huellasPar}}(c, k) == P_{\text{huellas}}(c, k)$$

Como $P_{\text{huellas}}(c, k) == f(c, k)$, esto prueba que `huellasPar` también cumple la especificación.

### Lema (map sobre una concatenación)

Para vectores $c_1$, $c_2$ y una función $g$:

$$(c_1 \mathbin{++} c_2)\text{.map}(g) = c_1\text{.map}(g) \mathbin{++}
c_2\text{.map}(g)$$

*Demostración.* El resultado de `map` en la posición $i$ es $g$ aplicada al elemento $i$. En $c_1 \mathbin{++} c_2$ las primeras $\lvert c_1 \rvert$ posiciones son las de $c_1$ y las siguientes son las de $c_2$, en el mismo orden. Lo mismo ocurre en el lado derecho. $\blacksquare$

Como $\text{huellas}(c, k)$ es $c\text{.map}(d \Rightarrow \text{huella}(d, k))$,
el lema da:

$$\text{huellas}(c_1 \mathbin{++} c_2, k) = \text{huellas}(c_1, k) \mathbin{++}
\text{huellas}(c_2, k)$$

### Demostración

Sea $n$ el largo de $c$ y $m = \lfloor n /2 \rfloor$, que es `c.length /2`. Por `splitAt`, `c.splitAt(m)` devuelve $(c_1, c_2)$ con $c = c_1 \mathbin{++} c_2$.

$$P_{\text{huellasPar}}(c, k) \rightarrow \text{huellasIzq} \mathbin{++}
\text{huellasDer}, \quad (\text{huellasIzq}, \text{huellasDer}) = \text{parallel}
(\text{huellas}(c_1, k), \text{huellas}(c_2, k))$$

Por el contrato de `parallel`, ese par es $(\text{huellas}(c_1, k), \text{huellas}(c_2, k))$. Entonces:

$$\rightarrow \text{huellas}(c_1, k) \mathbin{++} \text{huellas}(c_2, k) =
\text{huellas}(c_1 \mathbin{++} c_2, k) = \text{huellas}(c, k) \quad \text{(porque } c = c_1 \mathbin{++} c_2 \text{)} \quad \blacksquare$$

### Las dos ramas son independientes

Cada rama solo lee valores inmutables (`Vector`, `Set`, `String`) y produce valores nuevos: no hay `var` ni colecciones mutables. Una rama puede terminar antes que la otra y eso no cambia el resultado, porque `parallel` espera a las dos y la concatenación pone siempre la mitad izquierda primero.

### Casos límite

Si $n = 0$, `splitAt(0)` devuelve dos vectores vacíos y el resultado es `Vector()`.
Si $n = 1$, $m = 0$: la izquierda queda vacía y la derecha contiene el documento.

### Aplicación a ejemplos

| $n$ | $m$ | Izquierda  | Derecha         |
|-----|-----|------------|-----------------|
| 3   | 1   | $d_0$      | $d_1, d_2$      |
| 4   | 2   | $d_0, d_1$ | $d_2, d_3$      |
| 5   | 2   | $d_0, d_1$ | $d_2, d_3, d_4$ |

Con `e0` a `e3` y $k = 3$, el resultado tiene tamaños `Vector(7, 7, 6, 6)`, igual que `huellas`. La partición cubre $n$ par e impar, y el orden se conserva porque la mitad izquierda va primero.

## `similitudes`

### Especificación 

$$f(hs, ps) = \langle (i, j, J(hs(i), hs(j))) \mid (i, j) \in ps \rangle$$

en el mismo orden de `ps`. Se supone que cada $i$ y cada $j$ de `ps` son posiciones válidas de `hs`, como lo son las de `parejas(hs.length)` y las de `parejasCandidatas`.

Se desea demostrar:

$$\forall hs, \forall ps : P_{\text{similitudes}}(hs, ps) == f(hs, ps)$$

### Demostración

`map` conserva el largo y el orden, y asocia un resultado a cada pareja:

$$P_{\text{similitudes}}(hs, ps) \rightarrow \langle (i, j, \text{jaccard}(hs(i),hs(j))) \mid (i, j) \in ps \rangle$$

**Por posición.** Para cualquier $r$, si $ps(r) = (i, j)$, el patrón `case (primerDocumento, segundoDocumento)` recupera exactamente esos dos valores, y la posición $r$ del resultado es $(i, j, J(hs(i), hs(j)))$. No se omite ni se agrega ninguna pareja, y se conserva el orden. Por la demostración de `jaccard`, $\text{jaccard}(hs(i), hs(j)) == J(hs(i), hs(j))$. 
Luego el resultado es $f(hs, ps)$. $\blacksquare$ 

### Aplicación a ejemplos
 
`e0` a `e3`, $k = 3$, parejas `(0, 1)`, `(2, 3)` y `(0, 2)`:

| Pareja   | $\lvert A \cap B \rvert$ | $\lvert A \cup B \rvert$ | $J$          |
|----------|--------------------------|--------------------------|--------------|
| `(0, 1)` | 6                        | $7 + 7 - 6 = 8$          | $6/8 = 0.75$ |
| `(2, 3)` | 4                        | $6 + 6 - 4 = 8$          | $4/8 = 0.5$  |
| `(0, 2)` | 0                        | $7 + 6 - 0 = 13$         | $0/13 = 0$   |

El programa devuelve `Vector((0,1,0.75), (2,3,0.5), (0,2,0.0))`. Con `ps` vacío devuelve `Vector()`.

## `indiceInvertido`

### Especificación

$f(hs)$ es el mapa cuyo dominio es $\lbrace\, g \mid \exists i : g \in H_i \,\rbrace$ y que a cada $g$ le asocia $I(g) = \lbrace\, i \mid g \in H_i \,\rbrace$.

Se supone que $hs(i) = H_i$ para cada $i$, es decir, que $hs$ es el resultado $\text{huellas}(c, k)$.

Se desea demostrar:

$$\forall hs : P_{\text{indiceInvertido}}(hs) == f(hs)$$

### Método

Se demuestra qué contiene cada valor intermedio: primero `entradas`, luego el resultado de `groupBy` y por último el `map` final.

### Lema (entradas)

Sea $n$ la longitud de $hs$:

$$\text{entradas} = \langle(g, i) \mid 0\le i < n,\ g \in hs(i) \rangle$$

*Demostración.* El `for` equivale a $(0 \text{ until } n)\text{.flatMap}(i \rightarrow hs(i)\text{.map}(g\rightarrow(g, i)))$. El primer generador recorre cada posición $i$ y el segundo cada fragmento de $hs(i)$, y `yield` produce un par por cada combinación. Luego `entradas` contiene $(g, i)$ si y solo si $g \in H_i$. $\blacksquare$

### Lema (agrupación)

`groupBy(entrada => entrada._1)` produce un grupo por cada fragmento $g$ que es primera componente de alguna entrada, y ninguno más. El grupo de $g$ es $\lbrace\, (g, i) \mid g \in H_i \,\rbrace$.

*Demostración.* `groupBy` reúne las entradas con la misma clave y solo crea claves que aparecen. Por el lema anterior, las claves son los $g$ que están en alguna huella, y el grupo de $g$ contiene exactamente las entradas $(g, i)$ con $g \in H_i$. $\blacksquare$

### Teorema

$$P_{\text{indiceInvertido}}(hs) \rightarrow \lbrace\, g \mapsto \text{documentos}(\text{grupo}(g)) \,\rbrace = \lbrace\, g \mapsto I(g) \,\rbrace = f(hs)$$

donde $\text{documentos}(\text{grupo}(g))$ es el conjunto de las segundas componentes de las entradas del grupo.

*Demostración.* El `map` final recibe un mapa y devuelve parejas $(g, \text{conjunto})$. Cada clave $g$ aparece en una sola pareja, porque `groupBy` ya las dejó distintas, así que el resultado es un mapa con las mismas claves y no se pierde ninguna. Por el lema de agrupación, esas claves son exactamente los fragmentos que están en alguna huella. Para cada clave $g$, el `map` interior toma la segunda componente de cada entrada de su grupo y `toSet` las pasa a conjunto. Eso da $\lbrace\, i \mid g \in H_i \,\rbrace = I(g)$. $\blacksquare$

### Doble inclusión

Para una clave $g$ del mapa resultante:

* **No se inventan documentos.** Si $i$ pertenece al conjunto asociado a $g$, viene de una entrada $(g, i)$ del `for`. Esa entrada solo pudo generarse porque $g \in hs(i)$.
* **No se pierden documentos.** Si $g \in hs(i)$, el `for` genera $(g, i)$. `groupBy` lo coloca en el grupo de la clave $g$, el `map` interior extrae $i$ y `toSet` lo conserva.

Las dos implicaciones dan $i \in I(g) \iff g \in H_i$. Esa es la propiedad que usa el Teorema 3 de `parejasCandidatas`.

**Caso límite.** Con huellas vacías, o con `hs` vacío, el `for` no genera ninguna entrada. `groupBy` devuelve un mapa vacío y el resultado es `Map()`.

### Aplicación a ejemplos

`a b c`, `b c d` y `x y z` con $k = 2$. Las huellas son `{(a b), (b c)}`, `{(b c), (c d)}` y `{(x y), (y z)}`. Hay 6 entradas y 5 claves:


| Fragmento | Documentos  |
|-----------|-------------|
| `(a b)`   | `Set(0)`    |
| `(b c)`   | `Set(0, 1)` |
| `(c d)`   | `Set(1)`    |
| `(x y)`   | `Set(2)`    |
| `(y z)`   | `Set(2)`    |

Cada conjunto reúne los documentos cuya huella contiene el fragmento.

## `detectarDirectoPar`

### Especificación 

Sea $n$ el número de documentos y $M$ la secuencia de todas las mediciones, en el orden de `parejas(n)`:

$$M = \langle (i, j, J(H_i, H_j)) \mid 0 \le i < j < n \rangle$$

La especificación $R$ es la de `ranking` aplicada a $M$: las mediciones con $J \ge \tau$, ordenadas según $\prec$ (de mayor a menor similitud, y en empate por $(i, j)$) y truncadas a las primeras $p$.

Se desea demostrar:

$$\forall c, k, \tau, p, \text{corte} : P_{\text{detectarDirectoPar}}(c, k, \tau, p, \text{corte}) == R$$

### Demostración

Se calcula cada pieza, de adentro hacia afuera:

$$\text{huellasCorpus} \rightarrow \text{huellasPar}(c, k) == \text{huellas}(c, k) = \langle H_0, \dots, H_{n-1} \rangle \quad \text{(demostraciones de } \texttt{huellasPar} \text{ y } \texttt{huellas} \text{)}$$

$$\text{todasLasParejas} \rightarrow \text{parejas}(n) = \langle (i, j) \mid 0 \le i < j < n \rangle \quad \text{(demostración de } \texttt{parejas} \text{)}$$

$$\text{similitudesPar}(\dots) == \text{similitudes}(\dots) = \langle (i, j, J(H_i, H_j)) \mid (i, j) \in \text{parejas}(n) \rangle = M \quad \text{(especificación de } \texttt{similitudesPar} \text{ y demostración de } \texttt{similitudes} \text{)}$$ 

$$\text{ranking}(M, \tau, p) == R \quad \text{(Teorema 5)}$$

La última igualdad vale porque `ranking` conserva las mediciones con similitud al menos $\tau$, las ordena según $\prec$ y toma las primeras $p$, que es la definición de $R$. $\blacksquare$

### El valor de `corte`

Ninguna pieza usa `corte` para decidir qué se calcula. Solo determina a partir de qué tamaño `similitudesPar` deja de dividir el trabajo. Por eso el resultado no depende de `corte`. Como la versión sin paralelismo, `detectarDirecto`, es la misma composición con `huellas` y `similitudes`; por las mismas demostraciones también cumple la especificación $R$ y da el mismo resultado.

### El orden de las parejas no importa 

El Teorema 5 dice que el resultado de `ranking` no cambia si las mediciones llegan en otro orden. Por eso, aunque el reparto del trabajo en paralelo juntara las mediciones en otro orden, el resultado de `ranking` sería el mismo.

### Dependencias

Esta demostración compone las demostraciones de `huellas`, `huellasPar`, `parejas`, `similitudes` y `ranking`. De `similitudesPar` usa su especificación (da lo mismo que `similitudes` para cualquier `corte`).

### Casos límite

Con cero o un documento no existen parejas, `parejas(n)` es vacío y el resultado es `Vector()`. Con un umbral mayor que todas las similitudes no hay resultados. Con $p = 0$ el resultado es `Vector()`, porque `ranking` toma las primeras $p$ mediciones.

### Aplicación a ejemplos

`e0` a `e3`, $k = 3$, `corte = 2`. Las seis parejas dan `(0,1)` con 0.75, `(2,3)` con 0.5 y las otras cuatro con 0.0.

| $\tau$ | $p$ | Resultado                                  | Razón                                                                              |
|--------|-----|--------------------------------------------|------------------------------------------------------------------------------------|
| 0.4    | 5   | `Vector((0,1,0.75), (2,3,0.5))`            | solo esas dos parejas llegan a 0.4                                                 |
| 0.9    | 5   | `Vector()`                                 | ninguna llega a 0.9                                                                |
| 0.0    | 3   | `Vector((0,1,0.75), (2,3,0.5), (0,2,0.0))` | pasan las seis; las de 0.0 se desempatan por $(i, j)$ y se toman las tres primeras |

Con otro `corte` el resultado es el mismo, como indica la sección anterior.


## `detectarConIndice`

### Especificación

Sea $c = \langle d_0,d_1,\dots,d_{n-1}\rangle$ un corpus de $n$ documentos y sea $H_i = H_k(d_i)$ la huella del 
documento $i$, formado por fragmentos de $k$ palabras consecutivas 

Como tal debe calcular la similitud de todas las parejas de documentos y seleccionar aquellas que cumplen el umbral 
establecido el número total de parejas es: $N = \frac{n(n-1)}{2}$ 

La similitud entre dos documentos se calcula mediante el índice de Jaccard:
$J(H_i,H_j) = \frac{|H_i\cap H_j|}{|H_i\cup H_j|}$ 

Sea $M$ la secuencia de mediciones: $M = \langle(i,j,J(H_i,H_j))\mid 0\le i<j<n\rangle$, siendo su especificación:
$f(c,k,\tau,p)=ranking(M,\tau,p)$ donde se desea demostrar $P_{\text{detectarDirecto}}(c,k,\tau,p) = f(c,k,\tau,p)$

### Demostración 

La implementación usa las funciones `huella`, `parejas`, `similitudes` y `ranking`, el programa ejecuta

``` Scala
val hs = huellas(c, k)
ranking(similitudes(hs, parejas(hs.length)), umbral, p)
```

#### Paso 1: Cálculo de las huellas

Por la demostración de `huellas`, se obtienen una huella por cada documento del corpus, conservando su 
posición original: $hs = \langle H_0,H_1,\dots,H_{n-1}\rangle$
Por tanto, $hs.length=n$

#### Paso 2: Generación de parejas 

La función `parejas(n)` genera todas las combinaciones válidas $(i, j)$ con $i < j$.

Por su demostración de corrección no omite ninguna pareja, no genera repetidas y conserva el orden lexicográfico,
el número de parejas es: $\sum_{i=0}^{n-1}(n-1-i) = \frac{n(n-1)}{2}$

#### Paso 3: Cálculo de similitudes

La función `similitudes` recibe las huellas y las parejas generadas. Para cada pareja $(i, j)$ devuelve:
$(i,j,J(H_i,H_j))$, por la demostración de `similitudes`, la secuencia resultante contiene exactamente las mediciones 
de $M$

#### Paso 4: Aplicación de ranking

La función `ranking` realizar tres operaciones:
1. Conserva las mediciones cuya similitud es mayot o igual al umbral $\tau$
2. Ordena las mediciones de mayor a menor similitud, resolviendo los empates por posición
3. Selecciona las primeras $p$ mediciones

Por el Teorema 5 de `ranking` estas operaciones cumplen su especificación, entonces 
$P_{\text{detectarDirecto}}(c,k,\tau,p) = ranking(M,\tau,p) = f(c,k,\tau,p)$, por lo tanto, `detectarDirecto` cumple 
su especificación.

#### Caso límite

- Si el corpus está vacío, no existe parejas y el resultado es `Vector()`
- Si el corpus contiene un solo documento, tampococ existen parejas
- Si $p = 0$, el resultado es vacío
- Si ninguna similitud alcanza el umbral


``` Scala
```

``` Scala
```

``` Scala
```

``` Scala
```

``` Scala
```