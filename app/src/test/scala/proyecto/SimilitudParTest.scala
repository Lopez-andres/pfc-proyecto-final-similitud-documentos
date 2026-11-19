package proyecto

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
import Similitud._
import SimilitudPar._

/**
 * Pruebas de contrato de la versión paralela: cada una compara contra la
 * versión secuencial, que es la condición que pide el enunciado. No se
 * modifican.
 */
@RunWith(classOf[JUnitRunner])
class SimilitudParTest extends AnyFunSuite {

  val c1: Corpus = Vector(
    ("d0", Vector("el", "gato", "duerme", "en", "el", "sofa")),
    ("d1", Vector("el", "gato", "duerme", "en", "la", "alfombra")),
    ("d2", Vector("la", "lluvia", "moja", "el", "parque")))

  val c2: Corpus = Vector(
    ("e0", Vector("la", "funcion", "recursiva", "termina", "cuando", "llega",
      "al", "caso", "base")),
    ("e1", Vector("toda", "funcion", "recursiva", "termina", "cuando", "llega",
      "al", "caso", "base")),
    ("e2", Vector("el", "paralelismo", "divide", "el", "trabajo", "en",
      "tareas", "independientes")),
    ("e3", Vector("el", "paralelismo", "divide", "el", "trabajo", "en",
      "hilos", "independientes")))
// huellasPar
  test("huellasPar: da lo mismo que la secuencial") {
    assert(huellasPar(c1, 3) == huellas(c1, 3))
    assert(huellasPar(c2, 3) == huellas(c2, 3))
  }

  test("huellasPar: un corpus vacío da un vector vacío"){
    assert(huellasPar(Vector(), 3) == Vector())
  }

  test("huellasPar: un corpus da un solo documento da una sola huella"){
    val c: Corpus = Vector(("a", Vector("x", "y", "z", "w")))
    assert(huellasPar(c, 2) == huellas(c, 2))
    assert(huellasPar(c, 2).length == 1)
  }

  test("huellasPar: con un número impar de documentos las mitades no son iguales"){
    val c = corpusAlAzar(7, 80, 12)
    assert(huellasPar(c, 3) == huellas(c, 3))
    assert(huellasPar(c, 3).length == 7)
  }

  test("huellasPar: un corpus grande da lo mismo que huellas"){
    val c = corpusAlAzar(120, 200, 30)
    assert(huellasPar(c, 4) == huellas(c, 4))
  }

  test("huellasPar: mantiene las huellas vacías de los documentos cortos en su posición"){
    val c: Corpus = Vector (
      ("a", Vector("x", "y", "z")),
      ("b", Vector("x")),
      ("c", Vector("x", "y", "z", "w")),
      ("d", Vector()),
      ("e", Vector("x", "y", "z")))
      val  hs = huellasPar(c, 3)
      assert(hs == huellas(c, 3))
      assert(hs(1).isEmpty && hs(3).isEmpty && hs(2).size == 2)
  }


  test("similitudesPar: da lo mismo que la secuencial, y conserva el orden") {
    val hs = huellas(c2, 3)
    val ps = parejas(c2.length)
    assert(similitudesPar(hs, ps, 1) == similitudes(hs, ps))
    assert(similitudesPar(hs, ps, 100) == similitudes(hs, ps))
  }

  // Nuevos test similitud par

  val a: Fragmento = Vector("a")
  val b: Fragmento = Vector("b")
  val c: Fragmento = Vector("c")

  test("similitudesPar: Sin parejas"){
    assert(similitudesPar(Vector(), Vector(), 1) == Vector())
  }

  test("similitudesPar: Dos parejas en paralelo"){
    val hs = Vector(Set(a), Set(a), Set(b))
    val ps = Vector((0, 1), (0, 2))
    assert(similitudesPar(hs, ps, 1) ==
      Vector((0, 1, 1.0), (0, 2, 0.0))
    )
  }

  test("similitudesPar: Huellas iguales"){
    val hs = Vector(Set(a), Set(a))
    assert(similitudesPar(hs, Vector((0, 1)), 1) ==
    Vector((0, 1, 1.0)))
  }

  test("similitudesPar: Huellas diferentes"){
    val hs = Vector(Set(a), Set(b))
    assert(similitudesPar(hs, Vector((0, 1)), 1) ==
      Vector((0, 1, 0.0)))
  }

  test("similitudesPar: Comparten una parte"){
    val hs = Vector(Set(a, b), Set(a))
    val pareja = (0, 1)
    assert(similitudesPar(hs, Vector(pareja), 1) ==
    Vector((0, 1, 0.5)))
  }

  test("fusionar: une los conjuntos del fragmento que está en los dos") {
    val g: Fragmento = Vector("a", "b")
    val h: Fragmento = Vector("c", "d")
    val i1: Indice = Map(g -> Set(0), h -> Set(1))
    val i2: Indice = Map(g -> Set(2))
    assert(fusionar(i1, i2) == Map(g -> Set(0, 2), h -> Set(1)))
    assert(fusionar(i1, Map()) == i1)
  }

  // Nuevos test fusionar

  test("fusionar: Dos indices vacios"){
    val vacio: Indice = Map()
    assert(fusionar(vacio, vacio) == vacio)
  }

  test("fusionar: Primer indice vacio"){
    val i1: Indice = Map()
    val i2: Indice = Map(a -> Set(0))
    assert(fusionar(i1, i2) == i2)
  }

  test("fusionar: Segundo indice vacio"){
    val i1: Indice = Map(a -> Set(0))
    val i2: Indice = Map()
    assert(fusionar(i1, i2) == i1)
  }

  test("fusionar: fragmentos diferentes"){
    val i1: Indice = Map(a -> Set(0))
    val i2: Indice = Map(b -> Set(1))
    assert(fusionar(i1, i2) ==
      Map(a -> Set(0), b -> Set(1)))
  }

  test("fusionar: Sin repetidos"){
    val i1: Indice = Map(a -> Set(0))
    val i2: Indice = Map(a -> Set(0, 1))
    assert(fusionar(i1, i2) ==
      Map(a -> Set(0, 1)))
  }


  test("indiceInvertidoPar: da lo mismo que la secuencial en los ejemplos") {
    assert(indiceInvertidoPar(huellas(c1, 3)) == indiceInvertido(huellas(c1, 3)))
    assert(indiceInvertidoPar(huellas(c2, 3)) == indiceInvertido(huellas(c2, 3)))
  }

  // Nuevos test IndiceInvertidoPar

  test("indiceInvertidoPar: Sin huellas"){
    val hs: Vector[Huella] = Vector()
    val esperado: Indice = Map()
    assert(indiceInvertidoPar(hs) == esperado)
  }

  test("indiceInvertidoPar: Una Huella"){
    val hs: Vector[Huella] = Vector(Set(a))
    assert(indiceInvertidoPar(hs) == Map(a -> Set(0)))
  }

  test("indiceInvertidoPar: Fragmento igual"){
    assert(indiceInvertidoPar(Vector(Set(a), Set(a))) ==
      Map(a -> Set(0, 1)))
  }

  test("indiceInvertidoPar: Fragmentos diferentes"){
    assert(indiceInvertidoPar(Vector(Set(a), Set(b))) ==
      Map(a -> Set(0), b -> Set(1)))
  }

  test("indiceInvertidoPar: Posiciones correctas"){
    assert(indiceInvertidoPar(Vector(Set(a), Set(b), Set(a))) ==
      Map(a -> Set(0, 2), b -> Set(1)))
  }

  test("detectar: las cuatro versiones coinciden") {
    val directo = detectarDirecto(c2, 3, 0.3, 5)
    assert(detectarConIndice(c2, 3, 0.3, 5) == directo)
    assert(detectarDirectoPar(c2, 3, 0.3, 5, 2) == directo)
    assert(detectarConIndicePar(c2, 3, 0.3, 5, 2) == directo)
  }

  // Nuevos test dectectarConIndicePar

  test("detectarConIndicePar: Corpus vacio"){
    assert(detectarConIndicePar(Vector(), 2, 0.2, 5, 1) ==
      Vector())
  }

  test("detectarConIndicePar: Un documento"){
    val corpus: Corpus = Vector(("d0", Vector("a", "b")))
    assert(detectarConIndicePar(corpus, 2, 0.2, 5, 1) ==
      Vector())
  }

  test("detectarConIndicePar: Documentos iguales"){
    val corpus: Corpus = Vector(
       ("d0", Vector("a", "b")),
       ("d1", Vector("a", "b"))
    )
    assert(detectarConIndicePar(corpus, 2, 0.5, 5, 1) ==
      Vector((0, 1, 1.0)))
  }

  test("detectarConIndicePar: Documentos diferentes"){
    val corpus: Corpus = Vector(
      ("do", Vector("a", "b")),
      ("d1", Vector("x", "y"))
    )
    assert(detectarConIndicePar(corpus, 2, 0.2, 5, 1) ==
      Vector())
  }

  test("detectarConIndicePar: Similitud Parcial"){
    val corpus: Corpus = Vector(
      ("do", Vector("a", "b", "c")),
      ("d1", Vector("a", "b", "d"))
    )
    assert(detectarConIndicePar(corpus, 2, 0.2, 5, 1) ==
      Vector((0, 1, 1.0 / 3.0)))
  }

  //detectarDirectoPar

  test("detectarDirectoPar: da lo mismo que la secuencia con varios cortes"){
    val c = corpusAlAzar(40, 120, 15)
    val esperado = detectarDirecto(c, 3, 0.05, 20)
    val cortes = Vector(1, 4, 16, 64, 1000)
  assert(cortes.forall(corte => detectarDirectoPar(c, 3, 0.05, 20, corte) == esperado))
  }

  test("detectarDirectoPar: la similitud de una copia es el cociente esperado") {
    // La copia toma los primeros 160 de 200 palabras: comparte 158 fragmentos de
    //los 198 de cada documento, y la unión es 198 + 198 - 158 = 238
    val c = corpusConCopias(10, 200, 5000, 1, 0.8)
    val r = detectarDirectoPar(c, 3, 0.1, 1, 8)
    assert(math.abs(r(0)._3 - 158.0 / 238.0) < 1e-9)
  }

  test("detectarDirectoPar: un umbral mayor que todas las similitudes de vacío") {
    val c: Corpus = Vector(
      ("a", Vector("x", "y", "z", "w")),
      ("b", Vector("x", "y", "z", "q")),
      ("c", Vector("m", "n", "o", "p")))
      assert(detectarDirectoPar(c, 2, 0.6, 5, 2) == Vector())
  }

  test("detectarDirectoPar: p = 0 no devuelve nada y p grande devuelve todo") {
    val c: Corpus = Vector(
      ("a", Vector("x", "y", "z", "w")),
      ("b", Vector("x", "y", "z", "q")),
      ("c", Vector("m", "n", "o", "p")))
    assert(detectarDirectoPar(c, 2, 0.1, 0, 2) == Vector())
    assert(detectarDirectoPar(c, 2, 0.0, 100, 2).length == 3)
  }

  test("detectarDirectoPar: un corpus vacío o de un documento no tiene parejas ") {
    val uno: Corpus = Vector(("a", Vector("x", "y", "z")))
    assert(detectarDirectoPar(Vector(), 2, 0.1, 5, 4) == Vector())
    assert(detectarDirectoPar(uno, 2, 0.1, 5, 4) == Vector())
  }
}

