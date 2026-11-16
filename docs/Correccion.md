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

**Caso base:** $m \le 1$. Entonces $n = \lfloor m / 2 \rfloor =0$ y el programa devuelve
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

| Función             | Qué cubren los cinco casos                                                                 |
|---------------------|--------------------------------------------------------------------------------------------|
| `normalizar`        | signos de puntuación, separadores repetidos, texto vacío, dígitos y última palabra, tildes y ñ |
| `fragmentos`        | `k = 1`, `k` igual al largo, menos de `k` palabras, fragmentos repetidos, cantidad `t - k + 1` |
| `jaccard`           | huellas iguales, disjuntas, vacías, una contenida en otra, solapamiento parcial y simetría  |
| `parejas`           | `n = 4` completo, `n = 0` y `n = 1`, `n = 2`, cantidad e `i < j`, sin repetidos y en orden  |
| `parejasCandidatas` | índice vacío, conjuntos de un documento, pareja repetida, tres documentos, entradas mezcladas |
| `ranking`           | umbral inclusivo, empates, truncar a `p`, vacío, vector largo                                |

Cada caso corresponde a una parte de la demostración: los vacíos a los casos base, los 
repetidos y los empates a los lemas, y las cantidades a las fórmulas.
