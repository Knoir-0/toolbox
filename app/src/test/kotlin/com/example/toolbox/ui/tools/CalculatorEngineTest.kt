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

  @Test
  fun testExtractParseableValueForUnitConverter() {
    assertEquals("0", CalculatorEngine.extractParseableValue(""))
    assertEquals("42", CalculatorEngine.extractParseableValue("42"))
    assertEquals("15", CalculatorEngine.extractParseableValue("7+8"))
    assertEquals("15", CalculatorEngine.extractParseableValue("7+8+"))
    assertEquals("-25.5", CalculatorEngine.extractParseableValue("−25.5"))
    assertEquals("1", CalculatorEngine.extractParseableValue("sin(90)", isDegreeMode = true))
    assertEquals("10", CalculatorEngine.extractParseableValue("10÷0"))
  }

  @Test
  fun testVariableXEvaluationAndImplicitMultiplication() {
    assertTrue(CalculatorEngine.containsVariableX("2x + 1"))
    assertTrue(CalculatorEngine.containsVariableX("sin(x)"))
    assertFalse(CalculatorEngine.containsVariableX("exp(2) + 5"))

    assertEquals(7.0, CalculatorEngine.evaluate("2x + 1", xValue = 3.0), 1e-6)
    assertEquals(12.0, CalculatorEngine.evaluate("x(x + 1)", xValue = 3.0), 1e-6)
    assertEquals(-9.0, CalculatorEngine.evaluate("-x^2", xValue = 3.0), 1e-6)
    assertEquals(9.0, CalculatorEngine.evaluate("x^2", xValue = -3.0), 1e-6)
    assertEquals(5.0, CalculatorEngine.evaluate("y = 2x - 1", xValue = 3.0), 1e-6)
    assertEquals(10.0, CalculatorEngine.evaluate("f(x) = x^2 + 1", xValue = 3.0), 1e-6)
  }

  @Test
  fun testGraphSamplingSplitsDiscontinuities() {
    val segments = CalculatorEngine.sampleFunctionSegments(
      expression = "1/x",
      xMin = -5.0,
      xMax = 5.0,
      yMin = -10.0,
      yMax = 10.0,
      sampleCount = 200,
    )
    // 1/x across [-5, 5] must split across the vertical asymptote at x = 0 into at least 2 segments
    assertTrue(segments.size >= 2)
  }

  @Test
  fun testFindRootsExtremaAndIntersections() {
    // f(x) = x^2 - 4 has roots at x = -2 and x = 2, and local min at (0, -4)
    val features = CalculatorEngine.findFunctionFeatures(
      expression = "x^2 - 4",
      xMin = -5.0,
      xMax = 5.0,
      yMin = -10.0,
      yMax = 10.0,
    )
    val roots = features.filter { it.type == GraphFeatureType.ROOT }.sortedBy { it.x }
    assertEquals(2, roots.size)
    assertEquals(-2.0, roots[0].x, 1e-3)
    assertEquals(2.0, roots[1].x, 1e-3)

    val minima = features.filter { it.type == GraphFeatureType.LOCAL_MIN }
    assertTrue(minima.isNotEmpty())
    assertEquals(0.0, minima.first().x, 1e-2)
    assertEquals(-4.0, minima.first().y, 1e-2)

    // Intersection of y = x^2 and y = x + 2 at x = -1 (y = 1) and x = 2 (y = 4)
    val intersections = CalculatorEngine.findIntersections(
      exprA = "x^2",
      exprB = "x + 2",
      xMin = -5.0,
      xMax = 5.0,
      yMin = -10.0,
      yMax = 10.0,
    ).sortedBy { it.x }
    assertEquals(2, intersections.size)
    assertEquals(-1.0, intersections[0].x, 1e-3)
    assertEquals(1.0, intersections[0].y, 1e-3)
    assertEquals(2.0, intersections[1].x, 1e-3)
    assertEquals(4.0, intersections[1].y, 1e-3)
  }

  @Test
  fun testNumericalDerivativeAndDefiniteIntegral() {
    // Derivative of x^3 at x = 2 is 3 * (2^2) = 12
    val slope = CalculatorEngine.numericalDerivative("x^3", 2.0)
    assertEquals(12.0, slope ?: Double.NaN, 1e-4)

    // Integral of x^2 from 0 to 3 is 3^3 / 3 = 9
    val integral = CalculatorEngine.definiteIntegral("x^2", 0.0, 3.0)
    assertEquals(9.0, integral ?: Double.NaN, 1e-4)
  }
}
