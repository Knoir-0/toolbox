package com.example.toolbox.ui.tools

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorEngineTest {

  @Test
  fun testBasicArithmetic() {
    assertEquals(5.0, CalculatorEngine.evaluate("2 + 3"), 1e-6)
    assertEquals(-1.0, CalculatorEngine.evaluate("2 - 3"), 1e-6)
    assertEquals(14.0, CalculatorEngine.evaluate("2 + 3 * 4"), 1e-6)
    assertEquals(20.0, CalculatorEngine.evaluate("(2 + 3) * 4"), 1e-6)
    assertEquals(2.5, CalculatorEngine.evaluate("10 / 4"), 1e-6)
    assertEquals(4.5, CalculatorEngine.evaluate("1 + 7 / 2"), 1e-6)
  }

  @Test
  fun testDecimalsAndPrecision() {
    assertEquals(0.3, CalculatorEngine.evaluate("0.1 + 0.2"), 1e-9)
    assertEquals("0.3", CalculatorEngine.formatResult(CalculatorEngine.evaluate("0.1 + 0.2")))
    assertEquals("42", CalculatorEngine.formatResult(42.0))
    assertEquals("0", CalculatorEngine.formatResult(0.0))
    assertEquals("-5", CalculatorEngine.formatResult(-5.0))
  }

  @Test
  fun testUnaryAndPowers() {
    assertEquals(-5.0, CalculatorEngine.evaluate("-5"), 1e-6)
    assertEquals(1.0, CalculatorEngine.evaluate("5 + -4"), 1e-6)
    assertEquals(8.0, CalculatorEngine.evaluate("2 ^ 3"), 1e-6)
    assertEquals(512.0, CalculatorEngine.evaluate("2 ^ 3 ^ 2"), 1e-6) // right associative 2^(3^2) = 2^9 = 512
    assertEquals(16.0, CalculatorEngine.evaluate("(-4) ^ 2"), 1e-6)
  }

  @Test
  fun testPercentagesAndFactorials() {
    assertEquals(0.5, CalculatorEngine.evaluate("50%"), 1e-6)
    assertEquals(40.0, CalculatorEngine.evaluate("50% * 80"), 1e-6)
    assertEquals(120.0, CalculatorEngine.evaluate("5!"), 1e-6)
    assertEquals(1.0, CalculatorEngine.evaluate("0!"), 1e-6)
    assertEquals(720.0, CalculatorEngine.evaluate("6!"), 1e-6)
  }

  @Test
  fun testImplicitMultiplication() {
    assertEquals(6.0, CalculatorEngine.evaluate("2(3)"), 1e-6)
    assertEquals(24.0, CalculatorEngine.evaluate("2(3)(4)"), 1e-6)
    assertEquals(20.0, CalculatorEngine.evaluate("(2+3)(4)"), 1e-6)
    assertEquals(12.0, CalculatorEngine.evaluate("3√(16)"), 1e-6)
  }

  @Test
  fun testConstantsAndScientificFunctions() {
    assertEquals(Math.PI * 2, CalculatorEngine.evaluate("2π"), 1e-6)
    assertEquals(Math.E, CalculatorEngine.evaluate("e"), 1e-6)
    assertEquals(4.0, CalculatorEngine.evaluate("sqrt(16)"), 1e-6)
    assertEquals(3.0, CalculatorEngine.evaluate("cbrt(27)"), 1e-6)
    assertEquals(5.0, CalculatorEngine.evaluate("abs(-5)"), 1e-6)
    assertEquals(1.0, CalculatorEngine.evaluate("ln(e)"), 1e-6)
    assertEquals(2.0, CalculatorEngine.evaluate("log(100)"), 1e-6)
  }

  @Test
  fun testTrigonometryDegreesAndRadians() {
    // Degree mode
    assertEquals(1.0, CalculatorEngine.evaluate("sin(90)", isDegreeMode = true), 1e-6)
    assertEquals(0.0, CalculatorEngine.evaluate("cos(90)", isDegreeMode = true), 1e-6)
    assertEquals(0.0, CalculatorEngine.evaluate("sin(180)", isDegreeMode = true), 1e-6)
    assertEquals(1.0, CalculatorEngine.evaluate("tan(45)", isDegreeMode = true), 1e-6)
    assertEquals(30.0, CalculatorEngine.evaluate("asin(0.5)", isDegreeMode = true), 1e-6)
    assertEquals(60.0, CalculatorEngine.evaluate("acos(0.5)", isDegreeMode = true), 1e-6)
    assertEquals(45.0, CalculatorEngine.evaluate("atan(1)", isDegreeMode = true), 1e-6)

    // Radian mode
    assertEquals(0.0, CalculatorEngine.evaluate("sin(pi)", isDegreeMode = false), 1e-6)
    assertEquals(-1.0, CalculatorEngine.evaluate("cos(pi)", isDegreeMode = false), 1e-6)
  }

  @Test(expected = CalculatorException::class)
  fun testDivideByZeroThrowsException() {
    CalculatorEngine.evaluate("10 / 0")
  }

  @Test(expected = CalculatorException::class)
  fun testNegativeSquareRootThrowsException() {
    CalculatorEngine.evaluate("sqrt(-4)")
  }

  @Test
  fun testSafeEvaluationLivePreview() {
    val success = CalculatorEngine.evaluateSafe("12 * (3 + 2)")
    assertTrue(success is EvaluationResult.Success)
    assertEquals("60", (success as EvaluationResult.Success).formatted)

    val incomplete = CalculatorEngine.evaluateSafe("12 + ")
    assertTrue(incomplete is EvaluationResult.Incomplete)

    val incompleteParen = CalculatorEngine.evaluateSafe("sin(30")
    assertTrue(incompleteParen is EvaluationResult.Success)
    assertEquals("0.5", (incompleteParen as EvaluationResult.Success).formatted)

    val error = CalculatorEngine.evaluateSafe("15 / 0")
    assertTrue(error is EvaluationResult.Error)
  }
}
