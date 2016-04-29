package optimus.optimization

import org.scalatest.{FunSpec, Matchers}
import optimus.algebra._
import scala.util.Random

/**
  * Knapsack problem: Given a set of items, each with a weight and a value,
  * determine the number of each item to include in a collection so that the
  * total weight is less than or equal to a given limit and the total value is
  * as large as possible. It origins from the problem faced by someone who is
  * constrained by a fixed-size knapsack and must fill it with the most
  * valuable items.
  */
final class KnapsackTest extends FunSpec with Matchers {

  case class Item(weight: Int, utility: Int, x: MPIntVar)

  describe("Knapsack having a couple of items") {

    val weights = Array(100, 50, 45, 20, 10, 5)
    val utility = Array(40, 35, 18, 4, 10, 2)
    val capacity = 100

    for (lib <- solvers) {

      implicit val knapsackProblem = MIProblem(lib)

      val items = Array.tabulate(weights.length) { i =>
        Item(weights(i), utility(i), MPIntVar(s"x$i", 0 to 1))
      }

      // Maximize the total utility
      maximize(sum(items)(item => item.x * item.utility))

      // Given the limited capacity of the pack
      subjectTo {
        sum(items)(item => item.x * item.weight) <= capacity
      }

      start()

      val selected = items.filter(item => item.x.value.get == 1)
      val totalWeight = selected.map(item => item.weight).sum

      it(s"$lib solution should be optimal") {
        status shouldBe ProblemStatus.OPTIMAL
      }

      it(s"$lib total utility should be 55") {
        objectiveValue shouldEqual 55.0
      }

      it(s"$lib total weight should be 100") {
        totalWeight shouldEqual capacity
      }

      it(s"$lib constraints should be satisfied") {
        checkConstraints() shouldBe true
      }

      release()
    }
  }

  describe("Knapsack having several random generated items") {

    val numOfItems = 1000
    val weights = Array.tabulate(numOfItems)(i => Random.nextInt(10))
    val utility = Array.tabulate(numOfItems)(i => Random.nextInt(50))
    val capacity = 100

    for (lib <- solvers) {

      implicit val knapsackProblem = MIProblem(lib)

      val items = Array.tabulate(weights.length) { i =>
        Item(weights(i), utility(i), MPIntVar(s"x$i", 0 to 1))
      }

      // Maximize the total utility
      maximize(sum(items)(item => item.x * item.utility))

      // Given the limited capacity of the pack
      subjectTo {
        sum(items)(item => item.x * item.weight) <= capacity
      }

      start()

      val selected = items.filter(item => item.x.value.get == 1)
      val totalWeight = selected.map(item => item.weight).sum

      it(s"$lib solution should be optimal") {
        status shouldBe ProblemStatus.OPTIMAL
      }

      info(s"$lib total utility is $objectiveValue")

      info(s"$lib total weight is $totalWeight")

      it(s"$lib constraints should be satisfied") {
        checkConstraints() shouldBe true
      }

      release()
    }
  }
}
