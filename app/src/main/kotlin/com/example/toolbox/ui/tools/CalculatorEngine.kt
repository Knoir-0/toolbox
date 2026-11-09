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

object CalculatorEngine {

  /**
   * Evaluates a mathematical expression string.
   * Supports standard arithmetic, scientific functions, constants (pi, e),
   * implicit multiplication, percentages, factorials, and powers.
   */
  fun evaluate(expression: String, isDegreeMode: Boolean = true): Double {
    val sanitized = sanitize(expression)
    if (sanitized.isBlank()) {
      throw CalculatorException("Empty expression")
    }
    val tokens = tokenize(sanitized)
    val tokensWithImplicitMul = insertImplicitMultiplication(tokens)
    val parser = Parser(tokensWithImplicitMul, isDegreeMode)
    val result = parser.parse()
    if (result.isNaN()) {
      throw CalculatorException("Invalid calculation")
    }
    return cleanDouble(result)
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

  private sealed interface Token {
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
          (current is Token.Identifier && current.name in listOf("pi", "e"))

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

    // Term := Power (('*' | '/') Power)*
    private fun parseTerm(): Double {
      var left = parsePower()
      while (true) {
        val next = peek()
        if (next is Token.Operator && (next.op == '*' || next.op == '/')) {
          consume()
          val right = parsePower()
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

    // Power := Unary ('^' Power)? (right-associative)
    private fun parsePower(): Double {
      val base = parseUnary()
      val next = peek()
      return if (next is Token.Operator && next.op == '^') {
        consume()
        val exp = parsePower()
        if (base < 0 && exp != round(exp)) {
          throw CalculatorException("Invalid power of negative number")
        }
        base.pow(exp)
      } else {
        base
      }
    }

    // Unary := ('+' | '-') Unary | Postfix
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
      return parsePostfix()
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

    // Primary := Number | Constant | Function '(' Expression ')' | '(' Expression ')'
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
