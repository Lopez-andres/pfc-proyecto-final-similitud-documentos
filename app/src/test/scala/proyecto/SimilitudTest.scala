package proyecto

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
import Similitud._

/**
 * Pruebas de contrato de la versión secuencial: una por función, con los
 * valores de los dos ejemplos del enunciado. No se modifican.
 *
 * La rúbrica pide además cinco casos propios por función, distintos de estos y
 * de los ejemplos del enunciado. Van al final de este archivo o en un archivo
 * nuevo de esta misma carpeta; las dos formas valen.
 */
@RunWith(classOf[JUnitRunner])
class SimilitudTest extends AnyFunSuite {

  val eps = 1e-9

  // Ejemplo 1 del enunciado, con las palabras ya normalizadas para que estas
  // pruebas no dependan unas de otras.
  val d0: Documento = ("d0", Vector("el", "gato", "duerme", "en", "el", "sofa"))
  val d1: Documento = ("d1", Vector("el", "gato", "duerme", "en", "la", "alfombra"))
  val d2: Documento = ("d2", Vector("la", "lluvia", "moja", "el", "parque"))
  val c1: Corpus = Vector(d0, d1, d2)

  // Ejemplo 2 del enunciado.
  val e0: Documento = ("e0", Vector("la", "funcion", "recursiva", "termina",
    "cuando", "llega", "al", "caso", "base"))
  val e1: Documento = ("e1", Vector("toda", "funcion", "recursiva", "termina",
    "cuando", "llega", "al", "caso", "base"))
  val e2: Documento = ("e2", Vector("el", "paralelismo", "divide", "el",
    "trabajo", "en", "tareas", "independientes"))
  val e3: Documento = ("e3", Vector("el", "paralelismo", "divide", "el",
    "trabajo", "en", "hilos", "independientes"))
  val c2: Corpus = Vector(e0, e1, e2, e3)

  test("normalizar: baja a minúsculas y parte en palabras") {
    assert(normalizar("El Gato Duerme") == Vector("el", "gato", "duerme"))
  }

  test("fragmentos: son las ventanas consecutivas, en orden") {
    val ts = Vector("a", "b", "c", "d")
    assert(fragmentos(ts, 2) ==
      Vector(Vector("a", "b"), Vector("b", "c"), Vector("c", "d")))
  }

  test("huella: el ejemplo 1 tiene 4, 4 y 3 fragmentos") {
    assert(huella(d0, 3).size == 4)
    assert(huella(d1, 3).size == 4)
    assert(huella(d2, 3).size == 3)
  }

  test("huellas: hay una huella por documento") {
    assert(huellas(c1, 3).length == 3)
  }

  test("jaccard: ejemplo 2, las dos parejas dan 0.75 y 0.5") {
    assert(math.abs(jaccard(huella(e0, 3), huella(e1, 3)) - 0.75) < eps)
    assert(math.abs(jaccard(huella(e2, 3), huella(e3, 3)) - 0.5) < eps)
  }

  test("parejas: hay n (n - 1) / 2") {
    assert(parejas(5).length == 10)
  }

  test("similitudes: el ejemplo 1 da 1/3, 0 y 0") {
    val hs = huellas(c1, 3)
    val ms = similitudes(hs, parejas(3)).map(_._3)
    assert(math.abs(ms(0) - 2.0 / 6.0) < eps && ms(1) == 0.0 && ms(2) == 0.0)
  }

  test("indiceInvertido: el fragmento inicial de d0 está en d0 y en d1") {
    val idx = indiceInvertido(huellas(c1, 3))
    assert(idx(Vector("el", "gato", "duerme")) == Set(0, 1))
  }

  test("parejasCandidatas: en el ejemplo 2 quedan dos de seis") {
    val idx = indiceInvertido(huellas(c2, 3))
    assert(parejasCandidatas(idx) == Vector((0, 1), (2, 3)))
  }

  test("ranking: ordena de mayor a menor y trunca a p") {
    val ms: Vector[Medicion] = Vector((0, 1, 0.4), (0, 2, 0.1), (1, 2, 0.9))
    assert(ranking(ms, 0.0, 10).map(_._3) == Vector(0.9, 0.4, 0.1))
    assert(ranking(ms, 0.0, 2).length == 2)
  }

  test("detectar: el ejemplo 2 reporta (0, 1) con 0.75 y (2, 3) con 0.5") {
    val r = detectarDirecto(c2, 3, 0.3, 5)
    assert(r.map(m => (m._1, m._2)) == Vector((0, 1), (2, 3)))
    assert(math.abs(r(0)._3 - 0.75) < eps && math.abs(r(1)._3 - 0.5) < eps)
  }

  test("detectar: las dos soluciones coinciden en los ejemplos") {
    assert(detectarDirecto(c1, 3, 0.2, 5) == detectarConIndice(c1, 3, 0.2, 5))
    assert(detectarDirecto(c2, 3, 0.3, 5) == detectarConIndice(c2, 3, 0.3, 5))
  }

  // normalizar
    test("normalizar: quita signos de puntuación") {
      assert(normalizar("¡Hola, Mundo!") == Vector("hola", "mundo"))
    }

    test("normalizar: separadores repetidos no generan palabras vacías") {
      assert(normalizar("  a,,  b ;; ") == Vector("a", "b"))
    }

    test("normalizar: texto vacío o solo separadores da vector vacío") {
      assert(normalizar("") == Vector())
      assert(normalizar(" ,.;! ") == Vector())
    }

    test("normalizar: conserva dígitos y la última palabra sin separador") {
      assert(normalizar("Taller 3: 100") == Vector("taller", "3", "100"))
    }

    test("normalizar: conserva tildes y la ñ en minúscula") {
      assert(normalizar("AÑO Canción") == Vector("año", "canción"))
    }

  // fragmentos

  test("fragmentos: con k = 1 cada palabra es un fragmento") {
    assert(fragmentos(Vector("a", "b", "c"), 1) == Vector(Vector("a"), Vector("b"), Vector("c")))
  }

  test("fragmentos: con k igual al largo hay un solo fragmento") {
    assert(fragmentos(Vector("a", "b", "c"), 3) == Vector(Vector("a", "b", "c")))
  }

  test("fragmentos: con menos de k palabras, o ninguna, no hay fragmentos") {
    assert(fragmentos(Vector("a", "b"), 3) == Vector())
    assert(fragmentos(Vector(), 1) == Vector())
  }

  test("fragmentos: conserva los repetidos, no es un conjunto") {
    assert(fragmentos(Vector("a", "a", "a", "a"), 2) ==
      Vector(Vector("a", "a"), Vector("a", "a"), Vector("a", "a")))
  }

  test("fragmentos: son t - k + 1 y el último termina en la última palabra") {
    val ts = Vector.tabulate(10)(i => "w" + i)
    val fs = fragmentos(ts, 4)
    assert(fs.length == 7)
    assert(fs.last == Vector("w6", "w7", "w8", "w9"))
  }

  // jaccard

  test("jaccard: dos huellas iguales dan 1.0") {
    val h: Huella = Set(Vector("a", "b"), Vector("b", "c"))
    assert(math.abs(jaccard(h, h) - 1.0) < eps)
  }

  test("jaccard: huellas sin fragmentos en común dan 0.0") {
    val h1: Huella = Set(Vector("a"), Vector("b"))
    val h2: Huella = Set(Vector("c"), Vector("d"))
    assert(jaccard(h1, h2) == 0.0)
  }

  test("jaccard: dos huellas vacías, o una vacía, dan 0.0 sin dividir por cero") {
    val vacia: Huella = Set()
    val h: Huella = Set(Vector("a"))
    assert(jaccard(vacia, vacia) == 0.0)
    assert(jaccard(vacia, h) == 0.0)
    assert(jaccard(h, vacia) == 0.0)
  }

  test("jaccard: una huella contenida en otra") {
    val h1: Huella = Set(Vector("a"), Vector("b"))
    val h2: Huella = Set(Vector("a"), Vector("b"), Vector("c"), Vector("d"))
    assert(math.abs(jaccard(h1, h2) - 0.5) < eps)
  }

  test("jaccard: solapamiento parcial y simétrico") {
    val h1: Huella = Set(Vector("a"), Vector("b"), Vector("c"))
    val h2: Huella = Set(Vector("b"), Vector("c"), Vector("d"), Vector("e"))
    assert(math.abs(jaccard(h1, h2) - 0.4) < eps)
    assert(jaccard(h1, h2) == jaccard(h2, h1))
  }

  //parejas

  test("parejas: con n = 4 salen las seis, en orden") {
    assert(parejas(4) ==
    Vector((0, 1), (0, 2), (0, 3), (1, 2), (1, 3), (2, 3)))
  }

  test("parejas: con n = 0 o n = 1 no hay parejas") {
    assert(parejas(0) == Vector())
    assert(parejas(1) == Vector())
  }

  test("parejas: con n = 2 hay una sola") {
    assert(parejas(2) == Vector((0, 1)))
  }

  test("parejas: siempre i < j, y son n (n - 1) / 2") {
    val ps = parejas(10)
    assert(ps.length == 45)
    assert(ps.forall(p => p._1 < p._2))
  }

  test("parejas: sin repetidos, dentro del rango y ordenadas") {
    val ps = parejas(7)
    assert(ps.distinct == ps)
    assert(ps.forall(p => p._1 >= 0 && p._2 < 7))
    assert(ps == ps.sortBy(p => (p._1, p._2)))
  }

  // ranking

  test("ranking: el umbral es inclusivo (>=)") {
    val ss: Vector[Medicion] = Vector((0, 1, 0.5), (0, 2, 0.4), (1, 2, 0.6))
    assert(ranking(ss, 0.5, 10) == Vector((1, 2, 0.6), (0, 1, 0.5)))
  }

  test("ranking: los empates se rompen por posición, sin importar el orden de llegada") {
    val ss: Vector[Medicion] = Vector((1, 2, 0.5), (0, 2, 0.5), (0, 1, 0.5))
    val esperado: Vector[Medicion] = Vector((0, 1, 0.5), (0, 2, 0.5), (1, 2, 0.5))
    assert(ranking(ss, 0.0, 10) == esperado)
    assert(ranking(ss.reverse, 0.0, 10) == esperado)
  }

  test("ranking: trunca a p, y p = 0 o p mayor al total se comportan") {
    val ss: Vector[Medicion] = Vector((0, 1, 0.4), (0, 2, 0.1), (1, 2, 0.9))
    assert(ranking(ss, 0.0, 2) == Vector((1, 2, 0.9), (0, 1, 0.4)))
    assert(ranking(ss, 0.0, 0) == Vector())
    assert(ranking(ss, 0.0, 50).length == 3)
  }

  test("ranking: vacío, o ninguna alcanza el umbral, da vacío") {
    val ss: Vector[Medicion] = Vector((0, 1, 0.4), (0, 2, 0.1))
    assert(ranking(Vector(), 0.0, 5) == Vector())
    assert(ranking(ss, 0.99, 5) == Vector())
  }

  test("ranking: ordena bien un vector más largo") {
    val ss: Vector[Medicion] = Vector((0, 1, 0.2), (0, 2, 0.9), (0, 3, 0.5),
      (1, 2, 0.7), (1, 3, 0.1), (2, 3, 0.8), (0, 4, 0.3), (1, 4, 0.6))
    assert(ranking(ss, 0.0, 10).map(m => m._3) ==
      Vector(0.9, 0.8, 0.7, 0.6, 0.5, 0.3, 0.2, 0.1))
  }

  // parejasCandidatas

  test("parejasCandidatas: un índice vacío no da parejas") {
    val idx: Indice = Map()
    assert(parejasCandidatas(idx) == Vector())
  }

  test("parejasCandidatas: fragmentos de un solo documento no dan parejas") {
    val idx: Indice = Map(Vector("a") -> Set(0), Vector("b") -> Set(1))
    assert(parejasCandidatas(idx) == Vector())
  }

  test("parejasCandidatas: una pareja en varias entradas sale una sola vez") {
    val idx: Indice = Map(
      Vector("a") -> Set(0, 1), Vector("b") -> Set(0, 1), Vector("c") -> Set(0, 1))
    assert(parejasCandidatas(idx) == Vector((0, 1)))
  }

  test("parejasCandidatas: tres documentos juntos dan sus tres parejas, ordenadas") {
    val idx: Indice = Map(Vector("a") -> Set(2, 0, 1))
    assert(parejasCandidatas(idx) == Vector((0, 1), (0, 2), (1, 2)))
  }

  test("parejasCandidatas: entradas mezcladas salen ordenadas y sin las no candidatas") {
    val idx: Indice = Map(
      Vector("a") -> Set(3, 4), Vector("b") -> Set(0, 2), Vector("c") -> Set(1, 4))
    assert(parejasCandidatas(idx) == Vector((0, 2), (1, 4), (3, 4)))
  }
}
