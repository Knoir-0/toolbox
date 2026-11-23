package com.example.toolbox.ui.tools

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.Locale
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cbrt
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

class CalculatorException(message: String) : RuntimeException(message)

sealed interface EvaluationResult {
  data class Success(val value: Double, val formatted: String) : EvaluationResult
  data class Error(val message: String) : EvaluationResult
  data object Incomplete : EvaluationResult
}

data class CalculatorHistoryItem(
  val id: Long = System.currentTimeMillis(),
  val expression: String,
  val result: String,
  val isDegreeMode: Boolean = true,
)

data class GraphPoint(
  val x: Double,
  val y: Double,
)

enum class GraphFeatureType {
  ROOT,
  Y_INTERCEPT,
  LOCAL_MAX,
  LOCAL_MIN,
  INTERSECTION,
}

data class GraphFeaturePoint(
  val x: Double,
  val y: Double,
  val type: GraphFeatureType,
  val label: String,
  val functionIndex: Int = 0,
  val secondaryFunctionIndex: Int? = null,
)

object CalculatorEngine {

  /**
   * Strips optional leading function assignment prefixes such as `y =`, `f(x) =`, or `f1(x) =`
   * so expressions entered in Graph mode evaluate cleanly.
   */
  fun normalizeGraphExpression(expression: String): String {
    val trimmed = expression.trim()
    val prefixRegex = Regex("""^(?:y|f[₀₁₂₃₄₅₆₇₈₉\d]*\s*\(\s*x\s*\))\s*=\s*""", RegexOption.IGNORE_CASE)
    return prefixRegex.replace(trimmed, "").trim()
  }

  /**
   * Returns true if [expression] contains the variable `x` as an identifier token.
   */
  fun containsVariableX(expression: String): Boolean {
    return try {
      val sanitized = sanitize(normalizeGraphExpression(expression))
      if (sanitized.isBlank()) return false
      tokenize(sanitized).any { it is Token.Identifier && it.name == "x" }
    } catch (_: Exception) {
      false
    }
  }

  /**
   * Evaluates a mathematical expression string.
   * Supports standard arithmetic, scientific functions, constants (pi, e),
   * optional variable `x` ([xValue]), implicit multiplication, percentages, factorials, and powers.
   */
  fun evaluate(
    expression: String,
    isDegreeMode: Boolean = true,
    xValue: Double? = null,
  ): Double {
    val normalized = if (xValue != null) normalizeGraphExpression(expression) else expression
    val sanitized = sanitize(normalized)
    if (sanitized.isBlank()) {
      throw CalculatorException("Empty expression")
    }
    val tokens = tokenize(sanitized)
    val tokensWithImplicitMul = insertImplicitMultiplication(tokens)
    val parser = Parser(tokensWithImplicitMul, isDegreeMode, xValue)
    val result = parser.parse()
    if (result.isNaN()) {
      throw CalculatorException("Invalid calculation")
    }
    return cleanDouble(result)
  }

  /**
   * Pre-tokenizes [expression] into a reusable [CompiledFunction] for fast repeated evaluation
   * across hundreds of `x` samples during graphing.
   */
  class CompiledFunction internal constructor(
    val expression: String,
    private val tokens: List<Token>,
    val isDegreeMode: Boolean,
  ) {
    fun evaluateAtOrNull(x: Double): Double? {
      if (!x.isFinite()) return null
      return try {
        val raw = Parser(tokens, isDegreeMode, x).parse()
        if (raw.isFinite()) cleanDouble(raw) else null
      } catch (_: Exception) {
        null
      }
    }
  }

  fun compileFunctionOrNull(
    expression: String,
    isDegreeMode: Boolean = true,
  ): CompiledFunction? {
    return try {
      val normalized = normalizeGraphExpression(expression)
      val sanitized = sanitize(normalized)
      if (sanitized.isBlank()) return null
      val tokens = insertImplicitMultiplication(tokenize(sanitized))
      if (tokens.isEmpty()) return null
      // Verify syntactic validity by running a dry parse at a probe x value (ignoring domain errors)
      val probeParser = Parser(tokens, isDegreeMode, 1.2345)
      try {
        probeParser.parse()
      } catch (e: CalculatorException) {
        val msg = e.message.orEmpty()
        if (
          msg.startsWith("Unexpected") ||
          msg.startsWith("Unknown function") ||
          msg.startsWith("Invalid number") ||
          msg.startsWith("Empty expression")
        ) {
          return null
        }
      }
      CompiledFunction(normalized, tokens, isDegreeMode)
    } catch (_: Exception) {
      null
    }
  }

  fun evaluateAtOrNull(
    expression: String,
    x: Double,
    isDegreeMode: Boolean = true,
  ): Double? {
    val compiled = compileFunctionOrNull(expression, isDegreeMode) ?: return null
    return compiled.evaluateAtOrNull(x)
  }

  /**
   * Samples [expression] across `[xMin, xMax]` and splits the curve into continuous segments,
   * automatically breaking segments at domain gaps and vertical asymptotes (such as `1/x` or `tan(x)`).
   */
  fun sampleFunctionSegments(
    expression: String,
    xMin: Double,
    xMax: Double,
    yMin: Double,
    yMax: Double,
    sampleCount: Int = 320,
    isDegreeMode: Boolean = true,
  ): List<List<GraphPoint>> {
    if (xMax <= xMin || sampleCount < 2) return emptyList()
    val compiled = compileFunctionOrNull(expression, isDegreeMode) ?: return emptyList()
    val segments = mutableListOf<MutableList<GraphPoint>>()
    var currentSegment = mutableListOf<GraphPoint>()
    val ySpan = (yMax - yMin).coerceAtLeast(1e-6)
    val asymptoteJumpThreshold = ySpan * 2.5
    val yClipMargin = ySpan * 6.0
    val step = (xMax - xMin) / sampleCount.toDouble()

    var prevPoint: GraphPoint? = null
    var prevDy: Double? = null

    for (i in 0..sampleCount) {
      val x = if (i == sampleCount) xMax else xMin + i * step
      val y = compiled.evaluateAtOrNull(x)
      if (y == null || !y.isFinite() || y < yMin - yClipMargin || y > yMax + yClipMargin) {
        if (currentSegment.isNotEmpty()) {
          segments.add(currentSegment)
          currentSegment = mutableListOf()
        }
        prevPoint = null
        prevDy = null
        continue
      }

      val point = GraphPoint(x, y)
      val last = prevPoint
      if (last != null) {
        val dy = y - last.y
        val crossesZeroSteeply = (last.y * y < 0.0) && abs(dy) > asymptoteJumpThreshold
        val reversesSteeply = prevDy != null && (prevDy * dy < 0.0) && abs(dy) > asymptoteJumpThreshold
        if (crossesZeroSteeply || reversesSteeply) {
          if (currentSegment.isNotEmpty()) {
            segments.add(currentSegment)
            currentSegment = mutableListOf()
          }
          prevDy = null
        } else {
          prevDy = dy
        }
      }

      currentSegment.add(point)
      prevPoint = point
    }

    if (currentSegment.isNotEmpty()) {
      segments.add(currentSegment)
    }
    return segments
  }

  /**
   * Computes the numerical derivative `f'(x)` (`dy/dx`) at [x] using central finite differences.
   */
  fun numericalDerivative(
    expression: String,
    x: Double,
    isDegreeMode: Boolean = true,
  ): Double? {
    val compiled = compileFunctionOrNull(expression, isDegreeMode) ?: return null
    val h = (abs(x) * 1e-5).coerceAtLeast(1e-6)
    val yPlus = compiled.evaluateAtOrNull(x + h) ?: return null
    val yMinus = compiled.evaluateAtOrNull(x - h) ?: return null
    val slope = (yPlus - yMinus) / (2.0 * h)
    return if (slope.isFinite()) cleanDouble(slope) else null
  }

  /**
   * Computes the definite integral `∫ f(x) dx` from [a] to [b] using composite Simpson's rule.
   */
  fun definiteIntegral(
    expression: String,
    a: Double,
    b: Double,
    isDegreeMode: Boolean = true,
    intervals: Int = 200,
  ): Double? {
    if (!a.isFinite() || !b.isFinite()) return null
    if (abs(b - a) < 1e-12) return 0.0
    val compiled = compileFunctionOrNull(expression, isDegreeMode) ?: return null
    val n = if (intervals % 2 == 0) intervals else intervals + 1
    val h = (b - a) / n.toDouble()
    var sum = 0.0
    for (i in 0..n) {
      val x = a + i * h
      val y = compiled.evaluateAtOrNull(x) ?: return null
      val weight = when {
        i == 0 || i == n -> 1.0
        i % 2 == 1 -> 4.0
        else -> 2.0
      }
      sum += weight * y
    }
    val integral = (h / 3.0) * sum
    return if (integral.isFinite()) cleanDouble(integral) else null
  }

  /**
   * Finds key feature points (roots, y-intercept, and local extrema) for [expression] within `[xMin, xMax]`.
   */
  fun findFunctionFeatures(
    expression: String,
    xMin: Double,
    xMax: Double,
    yMin: Double,
    yMax: Double,
    isDegreeMode: Boolean = true,
    functionIndex: Int = 0,
    steps: Int = 240,
  ): List<GraphFeaturePoint> {
    if (xMax <= xMin || steps < 4) return emptyList()
    val compiled = compileFunctionOrNull(expression, isDegreeMode) ?: return emptyList()
    val features = mutableListOf<GraphFeaturePoint>()
    val ySpan = (yMax - yMin).coerceAtLeast(1e-6)
    val dx = (xMax - xMin) / steps.toDouble()

    // 1. Y-intercept (x = 0)
    if (0.0 in xMin..xMax) {
      val y0 = compiled.evaluateAtOrNull(0.0)
      if (y0 != null && y0.isFinite() && y0 in (yMin - ySpan)..(yMax + ySpan)) {
        features.add(
          GraphFeaturePoint(
            x = 0.0,
            y = y0,
            type = GraphFeatureType.Y_INTERCEPT,
            label = "Y-intercept (0, ${formatResult(y0)})",
            functionIndex = functionIndex,
          ),
        )
      }
    }

    // 2. Roots (f(x) = 0) and Local Extrema (f'(x) = 0)
    var prevX = xMin
    var prevY = compiled.evaluateAtOrNull(prevX)
    var prevSlope: Double? = null

    for (i in 1..steps) {
      val currX = if (i == steps) xMax else xMin + i * dx
      val currY = compiled.evaluateAtOrNull(currX)

      if (prevY != null && currY != null) {
        // Check root bracket
        if (abs(prevY) < 1e-10) {
          addUniqueFeature(
            features,
            GraphFeaturePoint(
              x = cleanDouble(prevX),
              y = 0.0,
              type = GraphFeatureType.ROOT,
              label = "Root (${formatResult(prevX)}, 0)",
              functionIndex = functionIndex,
            ),
            minDistance = dx * 0.6,
          )
        } else if (prevY * currY < 0.0 && abs(currY - prevY) < ySpan * 2.5) {
          val rootX = refineRootBisection(compiled, prevX, currX)
          val rootY = compiled.evaluateAtOrNull(rootX)
          if (rootY != null && abs(rootY) < ySpan * 0.05) {
            addUniqueFeature(
              features,
              GraphFeaturePoint(
                x = cleanDouble(rootX),
                y = 0.0,
                type = GraphFeatureType.ROOT,
                label = "Root (${formatResult(rootX)}, 0)",
                functionIndex = functionIndex,
              ),
              minDistance = dx * 0.6,
            )
          }
        }

        // Check local extrema via slope sign change
        val currSlope = (currY - prevY) / dx
        val lastSlope = prevSlope
        if (
          lastSlope != null &&
          abs(lastSlope) > 1e-7 &&
          abs(currSlope) > 1e-7 &&
          lastSlope * currSlope < 0.0 &&
          abs(currY - prevY) < ySpan * 1.5
        ) {
          val isMax = lastSlope > 0.0 && currSlope < 0.0
          val extremum = refineExtremumGoldenSection(
            compiled = compiled,
            left = (prevX - dx).coerceAtLeast(xMin),
            right = currX,
            findMax = isMax,
          )
          if (extremum != null && extremum.y in (yMin - ySpan)..(yMax + ySpan)) {
            val type = if (isMax) GraphFeatureType.LOCAL_MAX else GraphFeatureType.LOCAL_MIN
            val title = if (isMax) "Max" else "Min"
            addUniqueFeature(
              features,
              GraphFeaturePoint(
                x = cleanDouble(extremum.x),
                y = cleanDouble(extremum.y),
                type = type,
                label = "$title (${formatResult(extremum.x)}, ${formatResult(extremum.y)})",
                functionIndex = functionIndex,
              ),
              minDistance = dx * 0.75,
            )
          }
        }
        prevSlope = currSlope
      } else {
        prevSlope = null
      }

      prevX = currX
      prevY = currY
    }

    return features.take(24)
  }

  /**
   * Finds intersections between [exprA] and [exprB] within `[xMin, xMax]`.
   */
  fun findIntersections(
    exprA: String,
    exprB: String,
    xMin: Double,
    xMax: Double,
    yMin: Double,
    yMax: Double,
    isDegreeMode: Boolean = true,
    indexA: Int = 0,
    indexB: Int = 1,
    steps: Int = 240,
  ): List<GraphFeaturePoint> {
    if (xMax <= xMin || steps < 4) return emptyList()
    val compiledA = compileFunctionOrNull(exprA, isDegreeMode) ?: return emptyList()
    val compiledB = compileFunctionOrNull(exprB, isDegreeMode) ?: return emptyList()
    val results = mutableListOf<GraphFeaturePoint>()
    val ySpan = (yMax - yMin).coerceAtLeast(1e-6)
    val dx = (xMax - xMin) / steps.toDouble()

    var prevX = xMin
    var prevDiff: Double? = run {
      val ya = compiledA.evaluateAtOrNull(prevX)
      val yb = compiledB.evaluateAtOrNull(prevX)
      if (ya != null && yb != null) ya - yb else null
    }

    for (i in 1..steps) {
      val currX = if (i == steps) xMax else xMin + i * dx
      val ya = compiledA.evaluateAtOrNull(currX)
      val yb = compiledB.evaluateAtOrNull(currX)
      val currDiff = if (ya != null && yb != null) ya - yb else null

      val lastDiff = prevDiff
      if (lastDiff != null && currDiff != null) {
        if (abs(lastDiff) < 1e-10) {
          val yVal = compiledA.evaluateAtOrNull(prevX)
          if (yVal != null && yVal in (yMin - ySpan)..(yMax + ySpan)) {
            addUniqueFeature(
              results,
              GraphFeaturePoint(
                x = cleanDouble(prevX),
                y = cleanDouble(yVal),
                type = GraphFeatureType.INTERSECTION,
                label = "Intersection (${formatResult(prevX)}, ${formatResult(yVal)})",
                functionIndex = indexA,
                secondaryFunctionIndex = indexB,
              ),
              minDistance = dx * 0.6,
            )
          }
        } else if (lastDiff * currDiff < 0.0 && abs(currDiff - lastDiff) < ySpan * 2.5) {
          var lo = prevX
          var hi = currX
          var fLo: Double = lastDiff
          for (iter in 0 until 28) {
            val mid = 0.5 * (lo + hi)
            val mA = compiledA.evaluateAtOrNull(mid) ?: break
            val mB = compiledB.evaluateAtOrNull(mid) ?: break
            val fMid = mA - mB
            if (abs(fMid) < 1e-11) {
              lo = mid
              hi = mid
              break
            }
            if (fLo * fMid <= 0.0) {
              hi = mid
            } else {
              lo = mid
              fLo = fMid
            }
          }
          val ix = 0.5 * (lo + hi)
          val iyA = compiledA.evaluateAtOrNull(ix)
          val iyB = compiledB.evaluateAtOrNull(ix)
          if (
            iyA != null &&
            iyB != null &&
            abs(iyA - iyB) < ySpan * 0.05 &&
            iyA in (yMin - ySpan)..(yMax + ySpan)
          ) {
            val iy = 0.5 * (iyA + iyB)
            addUniqueFeature(
              results,
              GraphFeaturePoint(
                x = cleanDouble(ix),
                y = cleanDouble(iy),
                type = GraphFeatureType.INTERSECTION,
                label = "Intersection (${formatResult(ix)}, ${formatResult(iy)})",
                functionIndex = indexA,
                secondaryFunctionIndex = indexB,
              ),
              minDistance = dx * 0.6,
            )
          }
        }
      }
      prevX = currX
      prevDiff = currDiff
    }

    return results.take(16)
  }

  private fun refineRootBisection(
    compiled: CompiledFunction,
    left: Double,
    right: Double,
  ): Double {
    var lo = left
    var hi = right
    var fLo = compiled.evaluateAtOrNull(lo) ?: return 0.5 * (left + right)
    for (iter in 0 until 28) {
      val mid = 0.5 * (lo + hi)
      val fMid = compiled.evaluateAtOrNull(mid) ?: break
      if (abs(fMid) < 1e-12) return mid
      if (fLo * fMid <= 0.0) {
        hi = mid
      } else {
        lo = mid
        fLo = fMid
      }
    }
    return 0.5 * (lo + hi)
  }

  private fun refineExtremumGoldenSection(
    compiled: CompiledFunction,
    left: Double,
    right: Double,
    findMax: Boolean,
  ): GraphPoint? {
    val phi = (sqrt(5.0) - 1.0) / 2.0
    var a = left
    var b = right
    var c = b - phi * (b - a)
    var d = a + phi * (b - a)
    var fc = compiled.evaluateAtOrNull(c) ?: return null
    var fd = compiled.evaluateAtOrNull(d) ?: return null

    for (iter in 0 until 26) {
      val cBetter = if (findMax) fc > fd else fc < fd
      if (cBetter) {
        b = d
        d = c
        fd = fc
        c = b - phi * (b - a)
        fc = compiled.evaluateAtOrNull(c) ?: return null
      } else {
        a = c
        c = d
        fc = fd
        d = a + phi * (b - a)
        fd = compiled.evaluateAtOrNull(d) ?: return null
      }
    }
    val x = 0.5 * (a + b)
    val y = compiled.evaluateAtOrNull(x) ?: return null
    return GraphPoint(x, y)
  }

  private fun addUniqueFeature(
    list: MutableList<GraphFeaturePoint>,
    candidate: GraphFeaturePoint,
    minDistance: Double,
  ) {
    val exists = list.any { existing ->
      existing.type == candidate.type &&
        existing.functionIndex == candidate.functionIndex &&
        abs(existing.x - candidate.x) <= minDistance
    }
    if (!exists) {
      list.add(candidate)
    }
  }

  /**
   * Safely evaluates an expression, returning [EvaluationResult.Success],
   * [EvaluationResult.Error], or [EvaluationResult.Incomplete] if expression is mid-typing.
   */
  fun evaluateSafe(expression: String, isDegreeMode: Boolean = true): EvaluationResult {
    val trimmed = expression.trim()
    if (trimmed.isEmpty()) return EvaluationResult.Incomplete

    // If it clearly ends with an operator, it's incomplete for live preview
    val lastChar = trimmed.last()
    if (lastChar in "+-−×*÷/^(") {
      return EvaluationResult.Incomplete
    }

    return try {
      val value = evaluate(trimmed, isDegreeMode)
      EvaluationResult.Success(value, formatResult(value))
    } catch (e: CalculatorException) {
      EvaluationResult.Error(e.message ?: "Error")
    } catch (_: Exception) {
      EvaluationResult.Incomplete
    }
  }

  /**
   * Formats a calculation result into a clean, human-readable string.
   */
  fun formatResult(value: Double): String {
    if (value.isNaN()) return "Error"
    if (value.isInfinite()) return if (value > 0) "Infinity" else "-Infinity"
    val cleaned = cleanDouble(value)
    if (abs(cleaned) == 0.0) return "0"

    val absVal = abs(cleaned)
    if (absVal >= 1e12 || (absVal < 1e-6 && absVal > 0.0)) {
      return String.format(Locale.US, "%.8e", cleaned)
        .replace(Regex("0+e"), "e")
        .replace(Regex("\\.e"), "e")
    }

    return try {
      val bd = BigDecimal(cleaned.toString()).round(MathContext(12, RoundingMode.HALF_UP))
      bd.stripTrailingZeros().toPlainString()
    } catch (_: Exception) {
      cleaned.toString()
    }
  }

  fun formatDisplay(expression: String): String {
    return expression
      .replace("*", "×")
      .replace("/", "÷")
      .replace("-", "−")
  }

  /**
   * Resolves a parseable numeric string from the current calculator [expression] suitable for
   * prefilling the Unit Converter tool.
   *
   * 1. If the expression evaluates cleanly (or after trimming trailing incomplete operators /
   *    function prefixes), returns the formatted finite result.
   * 2. Otherwise falls back to a parseable numeric literal in the expression, or `"0"` when
   *    none is present.
   */
  fun extractParseableValue(expression: String, isDegreeMode: Boolean = true): String {
    val trimmed = expression.trim()
    if (trimmed.isEmpty()) return "0"

    when (val direct = evaluateSafe(trimmed, isDegreeMode)) {
      is EvaluationResult.Success -> {
        if (direct.value.isFinite()) return direct.formatted
      }
      else -> Unit
    }

    var candidate = trimmed
    val trailingCleanupRegex = Regex(
      """([+\-−×*÷/^(]+|(?:asin|acos|atan|sqrt|cbrt|sin|cos|tan|log|ln|abs)\(?)$""",
      RegexOption.IGNORE_CASE,
    )
    while (candidate.isNotEmpty()) {
      val next = trailingCleanupRegex.replace(candidate, "").trim()
      if (next == candidate) break
      candidate = next
    }
    if (candidate.isNotEmpty()) {
      when (val stripped = evaluateSafe(candidate, isDegreeMode)) {
        is EvaluationResult.Success -> {
          if (stripped.value.isFinite()) return stripped.formatted
        }
        else -> Unit
      }
    }

    val normalized = trimmed.replace("−", "-")
    val numberMatches = Regex("""-?(?:\d+\.?\d*|\.\d+)(?:[eE][+-]?\d+)?""")
      .findAll(normalized)
      .mapNotNull { it.value.toDoubleOrNull() }
      .filter { it.isFinite() }
      .toList()
    val fallbackNumber = numberMatches.lastOrNull { it != 0.0 } ?: numberMatches.lastOrNull()
    return if (fallbackNumber != null) formatResult(fallbackNumber) else "0"
  }

  private fun sanitize(input: String): String {
    return input
      .replace("×", "*")
      .replace("÷", "/")
      .replace("−", "-")
      .replace("π", "PI")
      .replace("√", "sqrt")
      .replace(" ", "")
  }

  private fun cleanDouble(value: Double): Double {
    if (abs(value) < 1e-12) return 0.0
    val rounded = round(value)
    if (abs(value - rounded) < 1e-11) {
      return rounded
    }
    return value
  }

  internal sealed interface Token {
    data class Number(val value: Double) : Token
    data class Identifier(val name: String) : Token
    data class Operator(val op: Char) : Token
    data object OpenParen : Token
    data object CloseParen : Token
    data object Factorial : Token
    data object Percent : Token
  }

  private fun tokenize(expr: String): List<Token> {
    val tokens = mutableListOf<Token>()
    var i = 0
    val len = expr.length

    while (i < len) {
      val c = expr[i]
      when {
        c.isWhitespace() -> i++
        c.isDigit() || c == '.' -> {
          val start = i
          var hasDot = c == '.'
          i++
          while (i < len) {
            val nextC = expr[i]
            if (nextC.isDigit()) {
              i++
            } else if (nextC == '.' && !hasDot) {
              hasDot = true
              i++
            } else {
              break
            }
          }
          val numStr = expr.substring(start, i)
          val numVal = numStr.toDoubleOrNull() ?: throw CalculatorException("Invalid number: $numStr")
          tokens.add(Token.Number(numVal))
        }
        c.isLetter() -> {
          val start = i
          while (i < len && expr[i].isLetter()) {
            i++
          }
          val ident = expr.substring(start, i).lowercase(Locale.US)
          tokens.add(Token.Identifier(ident))
        }
        c == '(' -> {
          tokens.add(Token.OpenParen)
          i++
        }
        c == ')' -> {
          tokens.add(Token.CloseParen)
          i++
        }
        c == '!' -> {
          tokens.add(Token.Factorial)
          i++
        }
        c == '%' -> {
          tokens.add(Token.Percent)
          i++
        }
        c in "+-*/^" -> {
          tokens.add(Token.Operator(c))
          i++
        }
        else -> throw CalculatorException("Unexpected symbol: '$c'")
      }
    }
    return tokens
  }

  private fun insertImplicitMultiplication(tokens: List<Token>): List<Token> {
    if (tokens.isEmpty()) return tokens
    val result = mutableListOf<Token>()

    for (i in tokens.indices) {
      val current = tokens[i]
      result.add(current)
      if (i + 1 < tokens.size) {
        val next = tokens[i + 1]
        val canEndFactor = current is Token.Number ||
          current is Token.CloseParen ||
          current is Token.Factorial ||
          current is Token.Percent ||
          (current is Token.Identifier && current.name in listOf("pi", "e", "x"))

        val canStartFactor = next is Token.Number ||
          next is Token.OpenParen ||
          next is Token.Identifier

        if (canEndFactor && canStartFactor) {
          result.add(Token.Operator('*'))
        }
      }
    }
    return result
  }

  private class Parser(
    private val tokens: List<Token>,
    private val isDegreeMode: Boolean,
    private val xValue: Double? = null,
  ) {
    private var index = 0

    fun parse(): Double {
      if (tokens.isEmpty()) return 0.0
      val result = parseExpression()
      if (index < tokens.size) {
        throw CalculatorException("Unexpected token after expression")
      }
      return result
    }

    private fun peek(): Token? = if (index < tokens.size) tokens[index] else null

    private fun consume(): Token {
      if (index >= tokens.size) throw CalculatorException("Unexpected end of expression")
      return tokens[index++]
    }

    // Expression := Term (('+' | '-') Term)*
    private fun parseExpression(): Double {
      var left = parseTerm()
      while (true) {
        val next = peek()
        if (next is Token.Operator && (next.op == '+' || next.op == '-')) {
          consume()
          val right = parseTerm()
          left = if (next.op == '+') left + right else left - right
        } else {
          break
        }
      }
      return left
    }

    // Term := Unary (('*' | '/') Unary)*
    private fun parseTerm(): Double {
      var left = parseUnary()
      while (true) {
        val next = peek()
        if (next is Token.Operator && (next.op == '*' || next.op == '/')) {
          consume()
          val right = parseUnary()
          if (next.op == '*') {
            left *= right
          } else {
            if (abs(right) < 1e-15) {
              throw CalculatorException("Cannot divide by zero")
            }
            left /= right
          }
        } else {
          break
        }
      }
      return left
    }

    // Unary := ('+' | '-') Unary | Power
    private fun parseUnary(): Double {
      val next = peek()
      if (next is Token.Operator) {
        if (next.op == '+') {
          consume()
          return parseUnary()
        }
        if (next.op == '-') {
          consume()
          return -parseUnary()
        }
      }
      return parsePower()
    }

    // Power := Postfix ('^' Unary)? (right-associative, higher precedence than unary minus so -x^2 = -(x^2))
    private fun parsePower(): Double {
      val base = parsePostfix()
      val next = peek()
      return if (next is Token.Operator && next.op == '^') {
        consume()
        val exp = parseUnary()
        if (base < 0 && exp != round(exp)) {
          throw CalculatorException("Invalid power of negative number")
        }
        base.pow(exp)
      } else {
        base
      }
    }

    // Postfix := Primary ('!' | '%')*
    private fun parsePostfix(): Double {
      var value = parsePrimary()
      while (true) {
        when (peek()) {
          is Token.Factorial -> {
            consume()
            value = calculateFactorial(value)
          }
          is Token.Percent -> {
            consume()
            value /= 100.0
          }
          else -> break
        }
      }
      return value
    }

    // Primary := Number | Constant | Variable | Function '(' Expression ')' | '(' Expression ')'
    private fun parsePrimary(): Double {
      return when (val token = peek()) {
        is Token.Number -> {
          consume()
          token.value
        }
        is Token.OpenParen -> {
          consume()
          val value = parseExpression()
          if (peek() is Token.CloseParen) {
            consume()
          } // Auto-close unclosed parenthesis gracefully
          value
        }
        is Token.Identifier -> {
          consume()
          parseIdentifier(token.name)
        }
        else -> throw CalculatorException("Unexpected expression element: $token")
      }
    }

    private fun parseIdentifier(name: String): Double {
      when (name) {
        "pi" -> return Math.PI
        "e" -> return Math.E
        "x" -> return xValue ?: throw CalculatorException("Variable x requires Graph mode")
      }

      // Check if followed by parentheses, e.g. sin(30)
      val hasParen = peek() is Token.OpenParen
      if (hasParen) {
        consume()
      }
      val arg = parseExpression()
      if (hasParen && peek() is Token.CloseParen) {
        consume()
      }

      return when (name) {
        "sin" -> {
          val rad = if (isDegreeMode) Math.toRadians(arg) else arg
          cleanTrig(sin(rad))
        }
        "cos" -> {
          val rad = if (isDegreeMode) Math.toRadians(arg) else arg
          cleanTrig(cos(rad))
        }
        "tan" -> {
          if (isDegreeMode) {
            val norm = (arg % 180 + 180) % 180
            if (abs(norm - 90.0) < 1e-10) {
              throw CalculatorException("Tangent undefined")
            }
            cleanTrig(tan(Math.toRadians(arg)))
          } else {
            cleanTrig(tan(arg))
          }
        }
        "asin" -> {
          if (arg < -1.0 || arg > 1.0) throw CalculatorException("Invalid asin domain")
          val rad = asin(arg)
          if (isDegreeMode) Math.toDegrees(rad) else rad
        }
        "acos" -> {
          if (arg < -1.0 || arg > 1.0) throw CalculatorException("Invalid acos domain")
          val rad = acos(arg)
          if (isDegreeMode) Math.toDegrees(rad) else rad
        }
        "atan" -> {
          val rad = atan(arg)
          if (isDegreeMode) Math.toDegrees(rad) else rad
        }
        "sqrt" -> {
          if (arg < 0) throw CalculatorException("Cannot take square root of negative number")
          sqrt(arg)
        }
        "cbrt" -> cbrt(arg)
        "ln" -> {
          if (arg <= 0) throw CalculatorException("Logarithm of non-positive number")
          ln(arg)
        }
        "log" -> {
          if (arg <= 0) throw CalculatorException("Logarithm of non-positive number")
          log10(arg)
        }
        "abs" -> abs(arg)
        "exp" -> exp(arg)
        else -> throw CalculatorException("Unknown function: $name")
      }
    }

    private fun cleanTrig(v: Double): Double {
      if (abs(v) < 1e-12) return 0.0
      val r = round(v)
      if (abs(v - r) < 1e-11) return r
      return v
    }

    private fun calculateFactorial(v: Double): Double {
      if (v < 0 || v != round(v)) {
        throw CalculatorException("Factorial only defined for non-negative integers")
      }
      if (v > 170) {
        throw CalculatorException("Factorial overflow")
      }
      val n = v.toInt()
      var res = 1.0
      for (i in 2..n) {
        res *= i
      }
      return res
    }
  }
}
