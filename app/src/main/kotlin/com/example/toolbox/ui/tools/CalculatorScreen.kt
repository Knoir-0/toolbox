package com.example.toolbox.ui.tools

import com.example.toolbox.ui.theme.toolboxScreenBackground

import android.graphics.Paint
import android.graphics.Typeface
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import com.example.toolbox.ui.theme.Button
import com.example.toolbox.ui.theme.CircleShape
import com.example.toolbox.ui.theme.HorizontalHexagonShape
import com.example.toolbox.ui.theme.TextButton
import com.example.toolbox.ui.theme.currentHexagonIconButtons
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.toolbox.ui.theme.FilledTonalButton
import com.example.toolbox.ui.theme.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import com.example.toolbox.ui.theme.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.toolbox.ui.theme.ToolboxTheme
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlinx.coroutines.launch

data class GraphFunctionEntry(
  val id: Long,
  val expression: String,
  val colorIndex: Int,
  val isVisible: Boolean = true,
)

enum class GraphAnalysisTab(val label: String) {
  KEY_POINTS("Key Points"),
  TABLE("Table"),
  INTEGRAL("Integral"),
}

private data class GraphPresetSpec(
  val label: String,
  val expression: String,
)

private val GRAPH_PRESETS = listOf(
  GraphPresetSpec("sin(x)", "sin(x)"),
  GraphPresetSpec("cos(x)", "cos(x)"),
  GraphPresetSpec("x² − 4", "x^2 - 4"),
  GraphPresetSpec("x³ − 3x", "x^3 - 3*x"),
  GraphPresetSpec("1/x", "1/x"),
  GraphPresetSpec("√x", "sqrt(x)"),
  GraphPresetSpec("ln(x)", "ln(x)"),
  GraphPresetSpec("e^(−x²/2)", "exp(-x^2/2)"),
  GraphPresetSpec("tan(x)", "tan(x)"),
)

private val SUBSCRIPT_DIGITS = charArrayOf('₀', '₁', '₂', '₃', '₄', '₅', '₆', '₇', '₈', '₉')

internal fun formatFunctionLabel(index: Int): String {
  val number = index + 1
  val subscript = number.toString().map { ch ->
    if (ch in '0'..'9') SUBSCRIPT_DIGITS[ch - '0'] else ch
  }.joinToString("")
  return "f$subscript(x)"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
  modifier: Modifier = Modifier,
  bottomPadding: Dp = 0.dp,
  initialValue: String? = null,
  onOpenUnitConverter: ((String) -> Unit)? = null,
  onNavigateBack: (() -> Unit)? = null,
) {
  var expression by rememberSaveable(initialValue) {
    mutableStateOf(initialValue?.takeIf { it.isNotBlank() } ?: "")
  }
  var isDegreeMode by rememberSaveable { mutableStateOf(true) }
  var isScientificExpanded by rememberSaveable { mutableStateOf(false) }
  var isGraphMode by rememberSaveable { mutableStateOf(false) }
  var showHistorySheet by rememberSaveable { mutableStateOf(false) }
  var isJustEvaluated by rememberSaveable { mutableStateOf(false) }
  var memoryValue by rememberSaveable { mutableDoubleStateOf(0.0) }
  var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
  var inlineUnitConverterValue by rememberSaveable { mutableStateOf<String?>(null) }

  // Graph Mode State
  val graphFunctions = remember {
    mutableStateListOf(
      GraphFunctionEntry(id = 1L, expression = "sin(x)", colorIndex = 0, isVisible = true),
      GraphFunctionEntry(id = 2L, expression = "x^2 - 4", colorIndex = 1, isVisible = true),
    )
  }
  var nextGraphFnId by rememberSaveable { mutableStateOf(3L) }
  var selectedGraphFnIndex by rememberSaveable { mutableIntStateOf(0) }
  var graphXMin by rememberSaveable { mutableDoubleStateOf(-10.0) }
  var graphXMax by rememberSaveable { mutableDoubleStateOf(10.0) }
  var graphYMin by rememberSaveable { mutableDoubleStateOf(-10.0) }
  var graphYMax by rememberSaveable { mutableDoubleStateOf(10.0) }
  var graphTraceX by rememberSaveable { mutableDoubleStateOf(0.0) }
  var isTraceInteractionMode by rememberSaveable { mutableStateOf(true) }
  var showGraphKeyPoints by rememberSaveable { mutableStateOf(true) }
  var showGraphTangent by rememberSaveable { mutableStateOf(false) }
  var activeAnalysisTab by rememberSaveable { mutableStateOf(GraphAnalysisTab.KEY_POINTS) }
  var integralLowerBound by rememberSaveable { mutableDoubleStateOf(0.0) }
  var integralUpperBound by rememberSaveable { mutableDoubleStateOf(2.0) }
  var editingFunctionIndex by rememberSaveable { mutableStateOf<Int?>(null) }
  var isAddingNewFunction by rememberSaveable { mutableStateOf(false) }

  LaunchedEffect(initialValue) {
    if (!initialValue.isNullOrBlank()) {
      expression = initialValue
      errorMessage = null
    }
  }

  val historyList = remember { mutableStateListOf<CalculatorHistoryItem>() }
  val haptic = LocalHapticFeedback.current
  val clipboardManager = LocalClipboardManager.current
  val context = LocalContext.current
  val exprScrollState = rememberScrollState()

  val expressionHasVariableX = remember(expression) {
    CalculatorEngine.containsVariableX(expression)
  }

  // Live preview evaluation
  val livePreview = remember(expression, isDegreeMode, expressionHasVariableX) {
    if (errorMessage != null || expression.isBlank()) {
      null
    } else if (expressionHasVariableX) {
      val atZero = CalculatorEngine.evaluateAtOrNull(expression, 0.0, isDegreeMode)
      if (atZero != null) {
        "f(0) = ${CalculatorEngine.formatResult(atZero)} • Tap = to graph"
      } else {
        "f(x) • Tap = to graph"
      }
    } else {
      when (val res = CalculatorEngine.evaluateSafe(expression, isDegreeMode)) {
        is EvaluationResult.Success -> res.formatted
        is EvaluationResult.Error -> null
        EvaluationResult.Incomplete -> null
      }
    }
  }

  val easterEggConstantSubtext = remember(expression, isDegreeMode, expressionHasVariableX, errorMessage) {
    if (errorMessage != null || expression.isBlank() || expressionHasVariableX) {
      null
    } else {
      CalculatorEngine.resolveFamousConstantSubtextForExpression(expression, isDegreeMode)
    }
  }

  fun plotExpressionInGraph(rawExpr: String) {
    val cleaned = CalculatorEngine.normalizeGraphExpression(rawExpr).trim()
    if (cleaned.isNotEmpty() && CalculatorEngine.compileFunctionOrNull(cleaned, isDegreeMode) != null) {
      val existingIdx = graphFunctions.indexOfFirst {
        CalculatorEngine.normalizeGraphExpression(it.expression).equals(cleaned, ignoreCase = true)
      }
      if (existingIdx >= 0) {
        graphFunctions[existingIdx] = graphFunctions[existingIdx].copy(isVisible = true)
        selectedGraphFnIndex = existingIdx
      } else if (graphFunctions.isNotEmpty()) {
        val targetIdx = selectedGraphFnIndex.coerceIn(0, graphFunctions.lastIndex)
        graphFunctions[targetIdx] = graphFunctions[targetIdx].copy(
          expression = cleaned,
          isVisible = true,
        )
        selectedGraphFnIndex = targetIdx
      } else {
        graphFunctions.add(
          GraphFunctionEntry(
            id = nextGraphFnId++,
            expression = cleaned,
            colorIndex = 0,
            isVisible = true,
          ),
        )
        selectedGraphFnIndex = 0
      }
    }
    isGraphMode = true
  }

  // Scroll expression to end when it changes
  LaunchedEffect(expression) {
    exprScrollState.animateScrollTo(exprScrollState.maxValue)
  }

  fun onInputChar(char: String) {
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    errorMessage = null

    if (isJustEvaluated) {
      isJustEvaluated = false
      // If user types a digit or function after evaluation, start new expression
      // If user types an operator, continue with previous result
      val isOperator = char in listOf("+", "−", "×", "÷", "^", "%")
      if (!isOperator) {
        expression = ""
      }
    }

    expression += char
  }

  fun onBackspace() {
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    errorMessage = null
    if (expression.isNotEmpty()) {
      // Check for multi-char function names at end, e.g. sin(, cos(, sqrt(, etc.
      val functionPrefixes = listOf("asin(", "acos(", "atan(", "sqrt(", "cbrt(", "sin(", "cos(", "tan(", "log(", "ln(", "abs(")
      var removed = false
      for (prefix in functionPrefixes) {
        if (expression.endsWith(prefix)) {
          expression = expression.dropLast(prefix.length)
          removed = true
          break
        }
      }
      if (!removed) {
        expression = expression.dropLast(1)
      }
    }
    isJustEvaluated = false
  }

  fun onClear() {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    expression = ""
    errorMessage = null
    isJustEvaluated = false
  }

  fun onEvaluate() {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    if (expression.isBlank()) return

    if (CalculatorEngine.containsVariableX(expression)) {
      val compiled = CalculatorEngine.compileFunctionOrNull(expression, isDegreeMode)
      if (compiled != null) {
        plotExpressionInGraph(expression)
        errorMessage = null
      } else {
        errorMessage = "Invalid function f(x)"
        isJustEvaluated = false
      }
      return
    }

    try {
      val value = CalculatorEngine.evaluate(expression, isDegreeMode)
      val formatted = CalculatorEngine.formatResult(value)
      historyList.add(
        0,
        CalculatorHistoryItem(
          expression = CalculatorEngine.formatDisplay(expression),
          result = formatted,
          isDegreeMode = isDegreeMode,
        ),
      )
      expression = formatted
      errorMessage = null
      isJustEvaluated = true
    } catch (e: CalculatorException) {
      errorMessage = e.message ?: "Invalid expression"
      isJustEvaluated = false
    } catch (_: Exception) {
      errorMessage = "Error"
      isJustEvaluated = false
    }
  }

  fun onToggleSign() {
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    errorMessage = null
    if (expression.isEmpty()) {
      expression = "−"
      return
    }

    // Try to toggle sign of the last number or wrap with negation
    if (expression.startsWith("−(") && expression.endsWith(")")) {
      expression = expression.substring(2, expression.length - 1)
    } else if (expression.startsWith("−")) {
      expression = expression.substring(1)
    } else {
      expression = "−($expression)"
    }
  }

  fun onCopyResult() {
    val textToCopy = if (isGraphMode) {
      val activeFn = graphFunctions.getOrNull(selectedGraphFnIndex.coerceIn(0, (graphFunctions.size - 1).coerceAtLeast(0)))
      if (activeFn != null) {
        val yVal = CalculatorEngine.evaluateAtOrNull(activeFn.expression, graphTraceX, isDegreeMode)
        if (yVal != null) {
          "${formatFunctionLabel(selectedGraphFnIndex)} = ${CalculatorEngine.formatDisplay(activeFn.expression)} @ x=${CalculatorEngine.formatResult(graphTraceX)} → y=${CalculatorEngine.formatResult(yVal)}"
        } else {
          "${formatFunctionLabel(selectedGraphFnIndex)} = ${CalculatorEngine.formatDisplay(activeFn.expression)}"
        }
      } else {
        "0"
      }
    } else if (errorMessage != null) {
      errorMessage!!
    } else {
      expression.ifEmpty { "0" }
    }
    clipboardManager.setText(AnnotatedString(textToCopy))
    Toast.makeText(context, "Copied \"$textToCopy\" to clipboard", Toast.LENGTH_SHORT).show()
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
  }

  fun onSmartParenthesis() {
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    errorMessage = null

    if (isJustEvaluated) {
      isJustEvaluated = false
      expression = "("
      return
    }

    if (expression.isEmpty()) {
      expression = "("
      return
    }

    val openCount = expression.count { it == '(' }
    val closeCount = expression.count { it == ')' }
    val unclosed = openCount - closeCount
    val lastChar = expression.last()

    val canClose = lastChar in '0'..'9' || lastChar == ')' || lastChar == 'π' || lastChar == 'e' || lastChar == 'x' || lastChar == '!' || lastChar == '%'

    if (unclosed > 0 && canClose) {
      expression += ")"
    } else {
      if (canClose) {
        expression += "×("
      } else {
        expression += "("
      }
    }
  }

  fun openInUnitConverter() {
    val parseableValue = if (isGraphMode) {
      val activeFn = graphFunctions.getOrNull(selectedGraphFnIndex.coerceIn(0, (graphFunctions.size - 1).coerceAtLeast(0)))
      val yVal = activeFn?.let { CalculatorEngine.evaluateAtOrNull(it.expression, graphTraceX, isDegreeMode) }
      if (yVal != null && yVal.isFinite()) CalculatorEngine.formatResult(yVal) else CalculatorEngine.formatResult(graphTraceX)
    } else {
      CalculatorEngine.extractParseableValue(expression, isDegreeMode)
    }
    if (onOpenUnitConverter != null) {
      onOpenUnitConverter(parseableValue)
    } else {
      inlineUnitConverterValue = parseableValue
    }
  }

  val activeInlineConverterValue = inlineUnitConverterValue
  if (activeInlineConverterValue != null) {
    BackHandler {
      inlineUnitConverterValue = null
    }
    UnitConverterScreen(
      modifier = modifier.padding(bottom = bottomPadding),
      initialValue = activeInlineConverterValue,
      onNavigateBack = { inlineUnitConverterValue = null },
    )
    return
  }

  val visibleGraphCount = graphFunctions.count { it.isVisible }
  val headerSubtext = remember(
    isDegreeMode,
    memoryValue,
    historyList.size,
    isGraphMode,
    visibleGraphCount,
    graphFunctions.size,
    easterEggConstantSubtext,
  ) {
    if (!isGraphMode && easterEggConstantSubtext != null) {
      easterEggConstantSubtext
    } else {
      buildString {
        append(if (isDegreeMode) "Degrees (DEG)" else "Radians (RAD)")
        if (isGraphMode) {
          append(" • Graphing ($visibleGraphCount/${graphFunctions.size} active)")
        } else {
          if (memoryValue != 0.0) {
            append(" • M: ${CalculatorEngine.formatResult(memoryValue)}")
          }
          if (historyList.isNotEmpty()) {
            append(" • ${historyList.size} in history")
          } else if (memoryValue == 0.0) {
            append(" • Standard, scientific & graphing")
          }
        }
      }
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .toolboxScreenBackground(MaterialTheme.colorScheme.background)
      .testTag("calculator_screen"),
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(bottom = bottomPadding),
    ) {
      // 1. Standard 68.dp Top Toolbar
      ToolboxTopBar(
        title = "Calculator",
        subtext = headerSubtext,
        icon = Icons.Default.Calculate,
        onNavigateBack = onNavigateBack,
        titleTestTag = "calculator_toolbar_title",
        subtextTestTag = "calculator_toolbar_subtext",
      )

      // 2. Pill Navigation Bar underneath the toolbar (DEG/RAD, f(x), Graph + floating Unit Converter, Copy, History)
      CalculatorPillNavigationBar(
        isDegreeMode = isDegreeMode,
        onToggleDegreeMode = { isDegreeMode = !isDegreeMode },
        isScientificExpanded = isScientificExpanded && !isGraphMode,
        onToggleScientific = {
          if (isGraphMode) {
            isGraphMode = false
            isScientificExpanded = true
          } else {
            isScientificExpanded = !isScientificExpanded
          }
        },
        isGraphMode = isGraphMode,
        onToggleGraphMode = {
          if (!isGraphMode && expression.isNotBlank() && CalculatorEngine.containsVariableX(expression)) {
            plotExpressionInGraph(expression)
          } else {
            isGraphMode = !isGraphMode
          }
        },
        historyCount = historyList.size,
        onOpenUnitConverter = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          openInUnitConverter()
        },
        onCopyResult = ::onCopyResult,
        onOpenHistory = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          showHistorySheet = true
        },
      )

      if (isGraphMode) {
        CalculatorGraphContent(
          functions = graphFunctions,
          selectedFunctionIndex = selectedGraphFnIndex.coerceIn(0, (graphFunctions.size - 1).coerceAtLeast(0)),
          onSelectFunction = { idx ->
            selectedGraphFnIndex = idx
          },
          onToggleFunctionVisibility = { idx ->
            if (idx in graphFunctions.indices) {
              val entry = graphFunctions[idx]
              graphFunctions[idx] = entry.copy(isVisible = !entry.isVisible)
            }
          },
          onEditFunction = { idx ->
            editingFunctionIndex = idx
            isAddingNewFunction = false
          },
          onAddFunctionRequest = {
            editingFunctionIndex = null
            isAddingNewFunction = true
          },
          onApplyPreset = { presetExpr ->
            val targetIdx = selectedGraphFnIndex.coerceIn(0, (graphFunctions.size - 1).coerceAtLeast(0))
            if (targetIdx in graphFunctions.indices) {
              graphFunctions[targetIdx] = graphFunctions[targetIdx].copy(
                expression = presetExpr,
                isVisible = true,
              )
            } else {
              graphFunctions.add(
                GraphFunctionEntry(
                  id = nextGraphFnId++,
                  expression = presetExpr,
                  colorIndex = 0,
                  isVisible = true,
                ),
              )
              selectedGraphFnIndex = 0
            }
          },
          isDegreeMode = isDegreeMode,
          xMin = graphXMin,
          xMax = graphXMax,
          yMin = graphYMin,
          yMax = graphYMax,
          onViewportChange = { newXMin, newXMax, newYMin, newYMax ->
            graphXMin = newXMin
            graphXMax = newXMax
            graphYMin = newYMin
            graphYMax = newYMax
            graphTraceX = graphTraceX.coerceIn(newXMin, newXMax)
          },
          traceX = graphTraceX,
          onTraceXChange = { newX ->
            graphTraceX = newX.coerceIn(graphXMin, graphXMax)
          },
          isTraceMode = isTraceInteractionMode,
          onToggleTraceMode = { isTraceInteractionMode = !isTraceInteractionMode },
          showKeyPoints = showGraphKeyPoints,
          onToggleKeyPoints = { showGraphKeyPoints = !showGraphKeyPoints },
          showTangent = showGraphTangent,
          onToggleTangent = { showGraphTangent = !showGraphTangent },
          activeAnalysisTab = activeAnalysisTab,
          onSelectAnalysisTab = { activeAnalysisTab = it },
          integralLower = integralLowerBound,
          integralUpper = integralUpperBound,
          onIntegralBoundsChange = { lo, hi ->
            integralLowerBound = lo
            integralUpperBound = hi
          },
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        )
      } else {
        // 3. Calculator Body (Display Card, Collapsible Scientific Keypad, Main Keypad)
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(horizontal = 16.dp, vertical = 8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          // Calculator Display Card
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(32.dp))
              .pointerInput(Unit) {
                detectTapGestures(onTap = { onCopyResult() })
              }
              .testTag("calculator_display_card"),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f),
            ),
            shape = RoundedCornerShape(32.dp),
            border = BorderStroke(
              width = 1.dp,
              color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
            ),
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(
                  horizontal = 22.dp,
                  vertical = if (isScientificExpanded) 12.dp else 18.dp,
                ),
              verticalArrangement = Arrangement.spacedBy(4.dp),
              horizontalAlignment = Alignment.End,
            ) {
              // Upper Expression String (scrollable horizontally)
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .horizontalScroll(exprScrollState),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                val displayText = CalculatorEngine.formatDisplay(expression.ifEmpty { "0" })
                val fontSize = when {
                  displayText.length <= 7 -> if (isScientificExpanded) 38.sp else 46.sp
                  displayText.length <= 12 -> 34.sp
                  else -> 28.sp
                }
                Text(
                  text = displayText,
                  style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = fontSize,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.5).sp,
                  ),
                  color = if (errorMessage != null) {
                    MaterialTheme.colorScheme.error
                  } else if (isJustEvaluated) {
                    MaterialTheme.colorScheme.primary
                  } else {
                    MaterialTheme.colorScheme.onSurface
                  },
                  maxLines = 1,
                  textAlign = TextAlign.End,
                  modifier = Modifier.testTag("calculator_display_expression"),
                )
              }

              // Lower Subtitle: Live preview (= 42), famous constant subtext, or Error message
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(if (isScientificExpanded) 24.dp else 30.dp),
                contentAlignment = Alignment.CenterEnd,
              ) {
                val previewSubtitleText = when {
                  errorMessage != null -> errorMessage!!
                  livePreview != null -> {
                    if (expressionHasVariableX) {
                      livePreview
                    } else if (isJustEvaluated && easterEggConstantSubtext != null) {
                      "= $livePreview • $easterEggConstantSubtext"
                    } else if (easterEggConstantSubtext != null) {
                      "= $livePreview ($easterEggConstantSubtext)"
                    } else {
                      "= $livePreview"
                    }
                  }
                  else -> ""
                }
                AnimatedContent(
                  targetState = previewSubtitleText,
                  transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(150)) },
                  label = "preview_animation",
                ) { text ->
                  if (text.isNotEmpty()) {
                    val hasEasterEgg = errorMessage == null && !expressionHasVariableX && easterEggConstantSubtext != null
                    Text(
                      text = text,
                      style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = if (expressionHasVariableX || hasEasterEgg) 14.sp else 20.sp,
                        fontWeight = FontWeight.Normal,
                      ),
                      color = if (errorMessage != null) {
                        MaterialTheme.colorScheme.error
                      } else if (expressionHasVariableX || hasEasterEgg) {
                        MaterialTheme.colorScheme.primary
                      } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                      },
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                      textAlign = TextAlign.End,
                      modifier = Modifier.testTag("calculator_display_preview"),
                    )
                  }
                }
              }
            }
          }

          // Scientific Keypad Drawer (Collapsible)
          AnimatedVisibility(
            visible = isScientificExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
          ) {
            ScientificKeypad(
              onInputChar = ::onInputChar,
              onInputFunction = { fn -> onInputChar("$fn(") },
              onMemoryClear = {
                memoryValue = 0.0
                Toast.makeText(context, "Memory cleared", Toast.LENGTH_SHORT).show()
              },
              onMemoryRecall = {
                onInputChar(CalculatorEngine.formatResult(memoryValue))
              },
              onMemoryAdd = {
                try {
                  val v = CalculatorEngine.evaluate(expression.ifEmpty { "0" }, isDegreeMode)
                  memoryValue += v
                  Toast.makeText(context, "M+ (${CalculatorEngine.formatResult(memoryValue)})", Toast.LENGTH_SHORT).show()
                } catch (_: Exception) {}
              },
              onMemorySubtract = {
                try {
                  val v = CalculatorEngine.evaluate(expression.ifEmpty { "0" }, isDegreeMode)
                  memoryValue -= v
                  Toast.makeText(context, "M- (${CalculatorEngine.formatResult(memoryValue)})", Toast.LENGTH_SHORT).show()
                } catch (_: Exception) {}
              },
              hasMemory = memoryValue != 0.0,
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 2.dp),
            )
          }

          // Main 4x5 Calculator Keypad
          MainCalculatorKeypad(
            onDigit = { onInputChar(it.toString()) },
            onOperator = { onInputChar(it) },
            onDecimal = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              errorMessage = null
              val tokens = expression.split(Regex("[+\\−×÷^()%]"))
              val lastToken = tokens.lastOrNull() ?: ""
              if (!lastToken.contains(".")) {
                if (lastToken.isEmpty()) {
                  expression += "0."
                } else {
                  expression += "."
                }
              }
            },
            onToggleSign = ::onToggleSign,
            onBackspace = ::onBackspace,
            onClear = ::onClear,
            onEquals = ::onEvaluate,
            onParenthesis = ::onSmartParenthesis,
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f),
          )
        }
      }
    }

    if (isAddingNewFunction || editingFunctionIndex != null) {
      val editIdx = editingFunctionIndex
      val existingEntry = editIdx?.let { graphFunctions.getOrNull(it) }
      val targetSlotIndex = editIdx ?: graphFunctions.size
      GraphFunctionEditorDialog(
        functionLabel = formatFunctionLabel(targetSlotIndex),
        initialExpression = existingEntry?.expression ?: "",
        isNewFunction = isAddingNewFunction,
        canDelete = existingEntry != null && graphFunctions.size > 1,
        isDegreeMode = isDegreeMode,
        onDismiss = {
          editingFunctionIndex = null
          isAddingNewFunction = false
        },
        onConfirm = { updatedExpr ->
          val cleaned = CalculatorEngine.normalizeGraphExpression(updatedExpr)
          if (existingEntry != null && editIdx != null && editIdx in graphFunctions.indices) {
            graphFunctions[editIdx] = existingEntry.copy(
              expression = cleaned,
              isVisible = true,
            )
            selectedGraphFnIndex = editIdx
          } else {
            val usedColors = graphFunctions.map { it.colorIndex }.toSet()
            val nextColor = (0 until 6).firstOrNull { it !in usedColors } ?: (graphFunctions.size % 6)
            graphFunctions.add(
              GraphFunctionEntry(
                id = nextGraphFnId++,
                expression = cleaned,
                colorIndex = nextColor,
                isVisible = true,
              ),
            )
            selectedGraphFnIndex = graphFunctions.lastIndex
          }
          editingFunctionIndex = null
          isAddingNewFunction = false
        },
        onDelete = {
          if (editIdx != null && editIdx in graphFunctions.indices && graphFunctions.size > 1) {
            graphFunctions.removeAt(editIdx)
            selectedGraphFnIndex = selectedGraphFnIndex.coerceIn(0, graphFunctions.lastIndex)
          }
          editingFunctionIndex = null
          isAddingNewFunction = false
        },
      )
    }

    // Scrim overlay behind history panel
    AnimatedVisibility(
      visible = showHistorySheet,
      enter = fadeIn(tween(250)),
      exit = fadeOut(tween(200)),
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.45f))
          .pointerInput(Unit) {
            detectTapGestures { showHistorySheet = false }
          },
      )
    }

    // History Panel popup from underneath the main toolbox navigation panel
    AnimatedVisibility(
      visible = showHistorySheet,
      enter = slideInVertically(
        initialOffsetY = { it },
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
      ) + fadeIn(tween(150)),
      exit = slideOutVertically(
        targetOffsetY = { it },
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
      ) + fadeOut(tween(150)),
      modifier = Modifier.align(Alignment.BottomCenter),
    ) {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .fillMaxHeight(0.72f)
          .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
          .pointerInput(Unit) {
            detectVerticalDragGestures { _, dragAmount ->
              if (dragAmount > 20f) {
                showHistorySheet = false
              }
            }
          },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 6.dp,
        shadowElevation = 12.dp,
      ) {
        Column(modifier = Modifier.fillMaxSize()) {
          // Compact drag handle
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(28.dp)
              .clickable { showHistorySheet = false }
              .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                  if (dragAmount > 10f) {
                    showHistorySheet = false
                  }
                }
              }
              .testTag("calculator_history_drag_handle")
              .semantics {
                contentDescription = "Collapse calculation history"
              },
            contentAlignment = Alignment.Center,
          ) {
            Box(
              modifier = Modifier
                .width(36.dp)
                .height(4.dp)
                .background(
                  color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                  shape = CircleShape,
                ),
            )
          }

          HistorySheetContent(
            history = historyList,
            onSelectHistoryItem = { item ->
              expression = item.result
              isDegreeMode = item.isDegreeMode
              showHistorySheet = false
              isJustEvaluated = true
            },
            onRestoreExpression = { item ->
              expression = item.expression
              isDegreeMode = item.isDegreeMode
              showHistorySheet = false
              isJustEvaluated = false
            },
            onClearHistory = {
              historyList.clear()
            },
            onClose = { showHistorySheet = false },
            bottomPadding = bottomPadding,
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f),
          )
        }
      }
    }
  }
}

@Composable
private fun CalculatorPillNavigationBar(
  isDegreeMode: Boolean,
  onToggleDegreeMode: () -> Unit,
  isScientificExpanded: Boolean,
  onToggleScientific: () -> Unit,
  isGraphMode: Boolean,
  onToggleGraphMode: () -> Unit,
  historyCount: Int,
  onOpenUnitConverter: () -> Unit,
  onCopyResult: () -> Unit,
  onOpenHistory: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val haptic = LocalHapticFeedback.current
  ToolboxPillNavigationBar(
    modifier = modifier,
    barTestTag = "calculator_pill_bar",
    floatingControlsTestTag = "calculator_pill_bar_floating_controls",
    floatingButtonsCount = 3,
    pills = {
      AngleModePill(
        isDegreeMode = isDegreeMode,
        onToggle = onToggleDegreeMode,
      )

      ScientificTogglePill(
        isExpanded = isScientificExpanded,
        onToggle = onToggleScientific,
      )

      ToolboxPillNavigationItem(
        selected = isGraphMode,
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          onToggleGraphMode()
        },
        label = "Graph",
        leadingIcon = Icons.AutoMirrored.Filled.ShowChart,
        modifier = Modifier.testTag("calculator_graph_toggle_chip"),
      )
    },
    floatingControls = {
      Surface(
        onClick = onOpenUnitConverter,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 3.dp,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = Modifier
          .size(34.dp)
          .testTag("calculator_unit_converter_button"),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.Straighten,
            contentDescription = "Open in Unit Converter",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
          )
        }
      }

      Surface(
        onClick = onCopyResult,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 3.dp,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = Modifier
          .size(34.dp)
          .testTag("calculator_copy_button"),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = "Copy result",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
          )
        }
      }

      Surface(
        onClick = onOpenHistory,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 3.dp,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = Modifier
          .size(34.dp)
          .testTag("calculator_history_button"),
      ) {
        Box(contentAlignment = Alignment.Center) {
          BadgedBox(
            badge = {
              if (historyCount > 0) {
                Badge(
                  containerColor = MaterialTheme.colorScheme.primary,
                  contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                  Text(historyCount.toString(), fontSize = 10.sp)
                }
              }
            },
          ) {
            Icon(
              imageVector = Icons.Default.History,
              contentDescription = "Calculation history",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(18.dp),
            )
          }
        }
      }
    },
  )
}

@Composable
private fun AngleModePill(
  isDegreeMode: Boolean,
  onToggle: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val haptic = LocalHapticFeedback.current
  val hexagonEnabled = currentHexagonIconButtons()
  val outerShape = if (hexagonEnabled) HorizontalHexagonShape else RoundedCornerShape(20.dp)
  val segmentShape = if (hexagonEnabled) HorizontalHexagonShape else RoundedCornerShape(16.dp)

  Surface(
    modifier = modifier
      .height(32.dp)
      .clip(outerShape)
      .clickable {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onToggle()
      }
      .testTag("calculator_angle_mode_chip"),
    shape = outerShape,
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 3.dp, vertical = 2.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      // DEG Segment
      val degBg by animateColorAsState(
        targetValue = if (isDegreeMode) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        animationSpec = tween(200),
        label = "deg_bg",
      )
      val degTextColor by animateColorAsState(
        targetValue = if (isDegreeMode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label = "deg_text",
      )
      Box(
        modifier = Modifier
          .clip(segmentShape)
          .background(degBg)
          .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = "DEG",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = if (isDegreeMode) FontWeight.Bold else FontWeight.Medium,
          color = degTextColor,
        )
      }

      // RAD Segment
      val radBg by animateColorAsState(
        targetValue = if (!isDegreeMode) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        animationSpec = tween(200),
        label = "rad_bg",
      )
      val radTextColor by animateColorAsState(
        targetValue = if (!isDegreeMode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label = "rad_text",
      )
      Box(
        modifier = Modifier
          .clip(segmentShape)
          .background(radBg)
          .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = "RAD",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = if (!isDegreeMode) FontWeight.Bold else FontWeight.Medium,
          color = radTextColor,
        )
      }
    }
  }
}

@Composable
private fun ScientificTogglePill(
  isExpanded: Boolean,
  onToggle: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val haptic = LocalHapticFeedback.current
  val hexagonEnabled = currentHexagonIconButtons()
  val chevronRotation by animateFloatAsState(
    targetValue = if (isExpanded) 180f else 0f,
    animationSpec = tween(durationMillis = 250),
    label = "chevron_rot",
  )

  FilterChip(
    selected = isExpanded,
    onClick = {
      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
      onToggle()
    },
    shape = if (hexagonEnabled) HorizontalHexagonShape else RoundedCornerShape(20.dp),
    leadingIcon = {
      Icon(
        imageVector = Icons.Default.Functions,
        contentDescription = null,
        modifier = Modifier.size(16.dp),
      )
    },
    label = {
      Text(
        text = "f(x)",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = if (isExpanded) FontWeight.Bold else FontWeight.Medium,
      )
    },
    trailingIcon = {
      Icon(
        imageVector = Icons.Default.KeyboardArrowDown,
        contentDescription = if (isExpanded) "Collapse scientific functions" else "Expand scientific functions",
        modifier = Modifier
          .size(16.dp)
          .graphicsLayer { rotationZ = chevronRotation },
      )
    },
    colors = FilterChipDefaults.filterChipColors(
      selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
      selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
      selectedLeadingIconColor = MaterialTheme.colorScheme.primary,
      selectedTrailingIconColor = MaterialTheme.colorScheme.primary,
    ),
    modifier = modifier.testTag("calculator_scientific_toggle_chip"),
  )
}

@Composable
private fun ScientificKeypad(
  onInputChar: (String) -> Unit,
  onInputFunction: (String) -> Unit,
  onMemoryClear: () -> Unit,
  onMemoryRecall: () -> Unit,
  onMemoryAdd: () -> Unit,
  onMemorySubtract: () -> Unit,
  hasMemory: Boolean,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.testTag("calculator_scientific_keypad"),
    verticalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    // Memory Row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      KeypadPillButton("MC", onClick = onMemoryClear, isEnabled = hasMemory, modifier = Modifier.weight(1f))
      KeypadPillButton("MR", onClick = onMemoryRecall, isEnabled = hasMemory, modifier = Modifier.weight(1f))
      KeypadPillButton("M+", onClick = onMemoryAdd, modifier = Modifier.weight(1f))
      KeypadPillButton("M-", onClick = onMemorySubtract, modifier = Modifier.weight(1f))
    }

    // Scientific Row 1: sin, cos, tan, ln, log
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      KeypadPillButton("sin", onClick = { onInputFunction("sin") }, modifier = Modifier.weight(1f))
      KeypadPillButton("cos", onClick = { onInputFunction("cos") }, modifier = Modifier.weight(1f))
      KeypadPillButton("tan", onClick = { onInputFunction("tan") }, modifier = Modifier.weight(1f))
      KeypadPillButton("ln", onClick = { onInputFunction("ln") }, modifier = Modifier.weight(1f))
      KeypadPillButton("log", onClick = { onInputFunction("log") }, modifier = Modifier.weight(1f))
    }

    // Scientific Row 2: √, x², xʸ, π, e
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      KeypadPillButton("√", onClick = { onInputFunction("sqrt") }, modifier = Modifier.weight(1f))
      KeypadPillButton("x²", onClick = { onInputChar("^2") }, modifier = Modifier.weight(1f))
      KeypadPillButton("xʸ", onClick = { onInputChar("^") }, modifier = Modifier.weight(1f))
      KeypadPillButton("π", onClick = { onInputChar("π") }, modifier = Modifier.weight(1f))
      KeypadPillButton("e", onClick = { onInputChar("e") }, modifier = Modifier.weight(1f))
    }

    // Scientific Row 3: %, n!, 1/x, |x|, x
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      KeypadPillButton("%", onClick = { onInputChar("%") }, modifier = Modifier.weight(1f))
      KeypadPillButton("n!", onClick = { onInputChar("!") }, modifier = Modifier.weight(1f))
      KeypadPillButton("1/x", onClick = { onInputChar("1/(") }, modifier = Modifier.weight(1f))
      KeypadPillButton("|x|", onClick = { onInputFunction("abs") }, modifier = Modifier.weight(1f))
      KeypadPillButton("x", onClick = { onInputChar("x") }, modifier = Modifier.weight(1f))
    }
  }
}

@Composable
private fun KeypadPillButton(
  label: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isEnabled: Boolean = true,
) {
  val haptic = LocalHapticFeedback.current
  Surface(
    modifier = modifier
      .height(36.dp)
      .clip(CircleShape)
      .clickable(enabled = isEnabled) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onClick()
      }
      .testTag("calculator_key_$label"),
    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(
      alpha = if (isEnabled) 1.0f else 0.4f,
    ),
    contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
      alpha = if (isEnabled) 1.0f else 0.4f,
    ),
    shape = CircleShape,
  ) {
    Box(contentAlignment = Alignment.Center) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
      )
    }
  }
}

@Composable
private fun MainCalculatorKeypad(
  onDigit: (Char) -> Unit,
  onOperator: (String) -> Unit,
  onDecimal: () -> Unit,
  onToggleSign: () -> Unit,
  onBackspace: () -> Unit,
  onClear: () -> Unit,
  onEquals: () -> Unit,
  onParenthesis: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.testTag("calculator_main_keypad"),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    // Row 1: C, ( ), ⌫, ÷
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      CalculatorButton(
        text = "C",
        onClick = onClear,
        type = ButtonType.Action,
        modifier = Modifier.weight(1f),
      )
      CalculatorButton(
        text = "( )",
        onClick = onParenthesis,
        type = ButtonType.Action,
        modifier = Modifier.weight(1f),
      )
      CalculatorButton(
        text = "⌫",
        onClick = onBackspace,
        onLongClick = onClear,
        type = ButtonType.Action,
        modifier = Modifier.weight(1f),
      )
      CalculatorButton(
        text = "÷",
        onClick = { onOperator("÷") },
        type = ButtonType.Operator,
        modifier = Modifier.weight(1f),
      )
    }

    // Row 2: 7, 8, 9, ×
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      CalculatorButton(text = "7", onClick = { onDigit('7') }, modifier = Modifier.weight(1f))
      CalculatorButton(text = "8", onClick = { onDigit('8') }, modifier = Modifier.weight(1f))
      CalculatorButton(text = "9", onClick = { onDigit('9') }, modifier = Modifier.weight(1f))
      CalculatorButton(
        text = "×",
        onClick = { onOperator("×") },
        type = ButtonType.Operator,
        modifier = Modifier.weight(1f),
      )
    }

    // Row 3: 4, 5, 6, −
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      CalculatorButton(text = "4", onClick = { onDigit('4') }, modifier = Modifier.weight(1f))
      CalculatorButton(text = "5", onClick = { onDigit('5') }, modifier = Modifier.weight(1f))
      CalculatorButton(text = "6", onClick = { onDigit('6') }, modifier = Modifier.weight(1f))
      CalculatorButton(
        text = "−",
        onClick = { onOperator("−") },
        type = ButtonType.Operator,
        modifier = Modifier.weight(1f),
      )
    }

    // Row 4: 1, 2, 3, +
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      CalculatorButton(text = "1", onClick = { onDigit('1') }, modifier = Modifier.weight(1f))
      CalculatorButton(text = "2", onClick = { onDigit('2') }, modifier = Modifier.weight(1f))
      CalculatorButton(text = "3", onClick = { onDigit('3') }, modifier = Modifier.weight(1f))
      CalculatorButton(
        text = "+",
        onClick = { onOperator("+") },
        type = ButtonType.Operator,
        modifier = Modifier.weight(1f),
      )
    }

    // Row 5: ±, 0, ., =
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      CalculatorButton(
        text = "±",
        onClick = onToggleSign,
        type = ButtonType.Number,
        modifier = Modifier.weight(1f),
      )
      CalculatorButton(text = "0", onClick = { onDigit('0') }, modifier = Modifier.weight(1f))
      CalculatorButton(text = ".", onClick = onDecimal, modifier = Modifier.weight(1f))
      CalculatorButton(
        text = "=",
        onClick = onEquals,
        type = ButtonType.Equals,
        modifier = Modifier.weight(1f),
      )
    }
  }
}

private enum class ButtonType {
  Number,
  Operator,
  Action,
  Equals,
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CalculatorButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  onLongClick: (() -> Unit)? = null,
  type: ButtonType = ButtonType.Number,
) {
  val haptic = LocalHapticFeedback.current
  val containerColor = when (type) {
    ButtonType.Number -> MaterialTheme.colorScheme.surfaceContainerHigh
    ButtonType.Operator -> MaterialTheme.colorScheme.secondaryContainer
    ButtonType.Action -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f)
    ButtonType.Equals -> MaterialTheme.colorScheme.primary
  }
  val contentColor = when (type) {
    ButtonType.Number -> MaterialTheme.colorScheme.onSurface
    ButtonType.Operator -> MaterialTheme.colorScheme.onSecondaryContainer
    ButtonType.Action -> MaterialTheme.colorScheme.onTertiaryContainer
    ButtonType.Equals -> MaterialTheme.colorScheme.onPrimary
  }

  Surface(
    modifier = modifier
      .fillMaxSize()
      .clip(CircleShape)
      .combinedClickable(
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          onClick()
        },
        onLongClick = onLongClick?.let {
          {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            it()
          }
        },
      )
      .testTag("calculator_key_$text")
      .semantics {
        role = Role.Button
        contentDescription = "Calculator button $text"
      },
    color = containerColor,
    contentColor = contentColor,
    shape = CircleShape,
    shadowElevation = if (type == ButtonType.Equals) 2.dp else 0.dp,
  ) {
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier.fillMaxSize(),
    ) {
      Text(
        text = text,
        style = when (type) {
          ButtonType.Number -> MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
          ButtonType.Equals -> MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold)
          else -> MaterialTheme.typography.titleMedium.copy(fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        },
        textAlign = TextAlign.Center,
      )
    }
  }
}

@Composable
private fun HistorySheetContent(
  history: List<CalculatorHistoryItem>,
  onSelectHistoryItem: (CalculatorHistoryItem) -> Unit,
  onRestoreExpression: (CalculatorHistoryItem) -> Unit,
  onClearHistory: () -> Unit,
  onClose: () -> Unit,
  bottomPadding: Dp = 0.dp,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp, vertical = 8.dp)
      .testTag("calculator_history_sheet"),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Text(
          text = "Calculation History",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
        )
      }

      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        if (history.isNotEmpty()) {
          IconButton(
            onClick = onClearHistory,
            modifier = Modifier.testTag("calculator_clear_history_button"),
          ) {
            Icon(
              imageVector = Icons.Default.DeleteOutline,
              contentDescription = "Clear all history",
              tint = MaterialTheme.colorScheme.error,
            )
          }
        }
        IconButton(onClick = onClose) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close history",
          )
        }
      }
    }

    HorizontalDivider()

    if (history.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .padding(bottom = bottomPadding),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = "No calculations in history yet",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = bottomPadding + 24.dp),
      ) {
        items(history, key = { it.id }) { item ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(24.dp))
              .clickable { onSelectHistoryItem(item) }
              .testTag("calculator_history_item_${item.id}"),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
            ),
            shape = RoundedCornerShape(24.dp),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
              ) {
                Text(
                  text = item.expression,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Text(
                  text = "= ${item.result}",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }

              OutlinedButton(
                onClick = { onRestoreExpression(item) },
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp),
              ) {
                Text(
                  "Edit",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun resolveGraphCurveColors(): List<Color> {
  val scheme = MaterialTheme.colorScheme
  return remember(scheme) {
    listOf(
      scheme.primary,
      scheme.tertiary,
      scheme.secondary,
      lerp(scheme.primary, scheme.tertiary, 0.5f),
      lerp(scheme.tertiary, scheme.error, 0.55f),
      lerp(scheme.secondary, scheme.primary, 0.45f),
    )
  }
}

internal fun computeNiceGridStep(span: Double, targetLines: Int = 6): Double {
  val safeSpan = span.coerceAtLeast(1e-6)
  val rawStep = safeSpan / targetLines.coerceAtLeast(2).toDouble()
  val exponent = floor(log10(rawStep))
  val powerOf10 = 10.0.pow(exponent)
  val fraction = rawStep / powerOf10
  val niceFraction = when {
    fraction < 1.5 -> 1.0
    fraction < 3.5 -> 2.0
    fraction < 7.5 -> 5.0
    else -> 10.0
  }
  return niceFraction * powerOf10
}

@Composable
private fun CalculatorGraphContent(
  functions: List<GraphFunctionEntry>,
  selectedFunctionIndex: Int,
  onSelectFunction: (Int) -> Unit,
  onToggleFunctionVisibility: (Int) -> Unit,
  onEditFunction: (Int) -> Unit,
  onAddFunctionRequest: () -> Unit,
  onApplyPreset: (String) -> Unit,
  isDegreeMode: Boolean,
  xMin: Double,
  xMax: Double,
  yMin: Double,
  yMax: Double,
  onViewportChange: (Double, Double, Double, Double) -> Unit,
  traceX: Double,
  onTraceXChange: (Double) -> Unit,
  isTraceMode: Boolean,
  onToggleTraceMode: () -> Unit,
  showKeyPoints: Boolean,
  onToggleKeyPoints: () -> Unit,
  showTangent: Boolean,
  onToggleTangent: () -> Unit,
  activeAnalysisTab: GraphAnalysisTab,
  onSelectAnalysisTab: (GraphAnalysisTab) -> Unit,
  integralLower: Double,
  integralUpper: Double,
  onIntegralBoundsChange: (Double, Double) -> Unit,
  modifier: Modifier = Modifier,
) {
  val haptic = LocalHapticFeedback.current
  val curveColors = resolveGraphCurveColors()
  val activeIdx = selectedFunctionIndex.coerceIn(0, (functions.size - 1).coerceAtLeast(0))
  val activeFunction = functions.getOrNull(activeIdx)
  val activeColor = curveColors[activeFunction?.colorIndex?.mod(curveColors.size) ?: 0]

  // Compute key feature points for the active function and intersections with other visible functions
  val allFeaturePoints = remember(functions.toList(), activeIdx, xMin, xMax, yMin, yMax, isDegreeMode) {
    val result = mutableListOf<GraphFeaturePoint>()
    val primary = functions.getOrNull(activeIdx)
    if (primary != null && primary.isVisible) {
      result.addAll(
        CalculatorEngine.findFunctionFeatures(
          expression = primary.expression,
          xMin = xMin,
          xMax = xMax,
          yMin = yMin,
          yMax = yMax,
          isDegreeMode = isDegreeMode,
          functionIndex = activeIdx,
        ),
      )
      functions.forEachIndexed { otherIdx, otherFn ->
        if (otherIdx != activeIdx && otherFn.isVisible) {
          result.addAll(
            CalculatorEngine.findIntersections(
              exprA = primary.expression,
              exprB = otherFn.expression,
              xMin = xMin,
              xMax = xMax,
              yMin = yMin,
              yMax = yMax,
              isDegreeMode = isDegreeMode,
              indexA = activeIdx,
              indexB = otherIdx,
            ),
          )
        }
      }
    }
    result.sortedBy { it.x }
  }

  val snappedFeature = remember(traceX, allFeaturePoints, xMin, xMax) {
    val snapTolerance = ((xMax - xMin) * 0.018).coerceAtLeast(1e-5)
    allFeaturePoints.minByOrNull { abs(it.x - traceX) }
      ?.takeIf { abs(it.x - traceX) <= snapTolerance }
  }

  fun zoomViewport(factor: Double) {
    val xCenter = 0.5 * (xMin + xMax)
    val yCenter = 0.5 * (yMin + yMax)
    val newHalfW = ((xMax - xMin) * 0.5 * factor).coerceIn(0.05, 5000.0)
    val newHalfH = ((yMax - yMin) * 0.5 * factor).coerceIn(0.05, 5000.0)
    onViewportChange(
      xCenter - newHalfW,
      xCenter + newHalfW,
      yCenter - newHalfH,
      yCenter + newHalfH,
    )
  }

  fun autoFitY() {
    val visible = functions.filter { it.isVisible }
    if (visible.isEmpty()) return
    var minVal = Double.POSITIVE_INFINITY
    var maxVal = Double.NEGATIVE_INFINITY
    val samples = 120
    val dx = (xMax - xMin) / samples.toDouble()
    for (fn in visible) {
      val compiled = CalculatorEngine.compileFunctionOrNull(fn.expression, isDegreeMode) ?: continue
      for (i in 0..samples) {
        val x = xMin + i * dx
        val y = compiled.evaluateAtOrNull(x) ?: continue
        if (y.isFinite() && abs(y) <= 1e5) {
          if (y < minVal) minVal = y
          if (y > maxVal) maxVal = y
        }
      }
    }
    if (minVal.isFinite() && maxVal.isFinite()) {
      val span = (maxVal - minVal).coerceAtLeast(2.0)
      val pad = span * 0.2
      onViewportChange(xMin, xMax, minVal - pad, maxVal + pad)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 4.dp)
      .testTag("calculator_graph_view"),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    // 1. Multi-Function Selector & Presets Bar
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        functions.forEachIndexed { idx, fnEntry ->
          val isSelected = idx == activeIdx
          val fnColor = curveColors[fnEntry.colorIndex.mod(curveColors.size)]
          val shape = if (currentHexagonIconButtons()) HorizontalHexagonShape else RoundedCornerShape(20.dp)
          Surface(
            modifier = Modifier
              .height(34.dp)
              .clip(shape)
              .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onSelectFunction(idx)
              }
              .testTag("calculator_graph_fn_chip_$idx"),
            shape = shape,
            color = if (isSelected) {
              MaterialTheme.colorScheme.primaryContainer
            } else {
              MaterialTheme.colorScheme.surfaceContainerHigh
            },
            border = BorderStroke(
              width = if (isSelected) 1.5.dp else 1.dp,
              color = if (isSelected) fnColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
            ),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              // Color dot + Visibility toggle
              Box(
                modifier = Modifier
                  .size(18.dp)
                  .clip(CircleShape)
                  .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggleFunctionVisibility(idx)
                  }
                  .testTag("calculator_graph_fn_visibility_$idx"),
                contentAlignment = Alignment.Center,
              ) {
                Box(
                  modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(
                      if (fnEntry.isVisible) fnColor else MaterialTheme.colorScheme.outlineVariant,
                    ),
                )
              }

              Text(
                text = "${formatFunctionLabel(idx)} = ${CalculatorEngine.formatDisplay(fnEntry.expression)}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (!fnEntry.isVisible) {
                  MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                } else if (isSelected) {
                  MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                  MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1,
              )

              Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit ${formatFunctionLabel(idx)}",
                tint = if (isSelected) {
                  MaterialTheme.colorScheme.primary
                } else {
                  MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier
                  .size(15.dp)
                  .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onEditFunction(idx)
                  }
                  .testTag("calculator_graph_edit_fn_$idx"),
              )
            }
          }
        }

        if (functions.size < 6) {
          ToolboxPillNavigationItem(
            selected = false,
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              onAddFunctionRequest()
            },
            label = "Add f(x)",
            leadingIcon = Icons.Default.Add,
            modifier = Modifier.testTag("calculator_graph_add_fn_button"),
          )
        }
      }

      // Quick Preset Functions Row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
          .testTag("calculator_graph_presets_row"),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "Presets:",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(end = 2.dp),
        )
        GRAPH_PRESETS.forEach { preset ->
          val isCurrentActive = activeFunction != null &&
            CalculatorEngine.normalizeGraphExpression(activeFunction.expression)
              .equals(preset.expression, ignoreCase = true)
          val pillShape = if (currentHexagonIconButtons()) HorizontalHexagonShape else RoundedCornerShape(16.dp)
          Surface(
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              onApplyPreset(preset.expression)
            },
            shape = pillShape,
            color = if (isCurrentActive) {
              MaterialTheme.colorScheme.secondaryContainer
            } else {
              MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            },
            border = BorderStroke(
              1.dp,
              if (isCurrentActive) {
                MaterialTheme.colorScheme.secondary
              } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
              },
            ),
            modifier = Modifier
              .height(26.dp)
              .testTag("calculator_graph_preset_${preset.expression}"),
          ) {
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier.padding(horizontal = 10.dp),
            ) {
              Text(
                text = preset.label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isCurrentActive) FontWeight.Bold else FontWeight.Medium,
                color = if (isCurrentActive) {
                  MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                  MaterialTheme.colorScheme.onSurfaceVariant
                },
              )
            }
          }
        }
      }
    }

    // 2. Interactive 2D Coordinate Graph Canvas Card
    GraphCanvasCard(
      functions = functions,
      activeFunctionIndex = activeIdx,
      curveColors = curveColors,
      isDegreeMode = isDegreeMode,
      xMin = xMin,
      xMax = xMax,
      yMin = yMin,
      yMax = yMax,
      onViewportChange = onViewportChange,
      traceX = traceX,
      onTraceXChange = { rawX ->
        val snapTolerance = ((xMax - xMin) * 0.022).coerceAtLeast(1e-5)
        val nearest = if (showKeyPoints) {
          allFeaturePoints.minByOrNull { abs(it.x - rawX) }
            ?.takeIf { abs(it.x - rawX) <= snapTolerance }
        } else {
          null
        }
        if (nearest != null) {
          if (abs(traceX - nearest.x) > 1e-6) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          }
          onTraceXChange(nearest.x)
        } else {
          onTraceXChange(rawX)
        }
      },
      isTraceMode = isTraceMode,
      onToggleTraceMode = {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onToggleTraceMode()
      },
      showKeyPoints = showKeyPoints,
      onToggleKeyPoints = {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onToggleKeyPoints()
      },
      showTangent = showTangent,
      onToggleTangent = {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onToggleTangent()
      },
      featurePoints = allFeaturePoints,
      showIntegralShade = activeAnalysisTab == GraphAnalysisTab.INTEGRAL,
      integralLower = integralLower,
      integralUpper = integralUpper,
      onZoomIn = {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        zoomViewport(0.7)
      },
      onZoomOut = {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        zoomViewport(1.4)
      },
      onAutoFitY = {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        autoFitY()
      },
      onResetViewport = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onViewportChange(-10.0, 10.0, -10.0, 10.0)
        onTraceXChange(0.0)
      },
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
    )

    // 3. Live Trace Readout, SlimDotSlider Scrubber & Analysis Card
    GraphAnalysisCard(
      functions = functions,
      activeFunctionIndex = activeIdx,
      activeColor = activeColor,
      isDegreeMode = isDegreeMode,
      xMin = xMin,
      xMax = xMax,
      traceX = traceX,
      onTraceXChange = onTraceXChange,
      snappedFeature = snappedFeature,
      featurePoints = allFeaturePoints,
      activeAnalysisTab = activeAnalysisTab,
      onSelectAnalysisTab = { tab ->
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onSelectAnalysisTab(tab)
      },
      integralLower = integralLower,
      integralUpper = integralUpper,
      onIntegralBoundsChange = onIntegralBoundsChange,
      onEditActiveFunction = { onEditFunction(activeIdx) },
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

@Composable
private fun GraphCanvasCard(
  functions: List<GraphFunctionEntry>,
  activeFunctionIndex: Int,
  curveColors: List<Color>,
  isDegreeMode: Boolean,
  xMin: Double,
  xMax: Double,
  yMin: Double,
  yMax: Double,
  onViewportChange: (Double, Double, Double, Double) -> Unit,
  traceX: Double,
  onTraceXChange: (Double) -> Unit,
  isTraceMode: Boolean,
  onToggleTraceMode: () -> Unit,
  showKeyPoints: Boolean,
  onToggleKeyPoints: () -> Unit,
  showTangent: Boolean,
  onToggleTangent: () -> Unit,
  featurePoints: List<GraphFeaturePoint>,
  showIntegralShade: Boolean,
  integralLower: Double,
  integralUpper: Double,
  onZoomIn: () -> Unit,
  onZoomOut: () -> Unit,
  onAutoFitY: () -> Unit,
  onResetViewport: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val scheme = MaterialTheme.colorScheme
  val activeFn = functions.getOrNull(activeFunctionIndex)
  val activeColor = curveColors[activeFn?.colorIndex?.mod(curveColors.size) ?: 0]

  // Sample all visible function curves
  val sampledCurves = remember(functions.toList(), xMin, xMax, yMin, yMax, isDegreeMode) {
    functions.mapIndexedNotNull { idx, fn ->
      if (!fn.isVisible) return@mapIndexedNotNull null
      val segments = CalculatorEngine.sampleFunctionSegments(
        expression = fn.expression,
        xMin = xMin,
        xMax = xMax,
        yMin = yMin,
        yMax = yMax,
        sampleCount = 300,
        isDegreeMode = isDegreeMode,
      )
      Triple(idx, fn, segments)
    }
  }

  val tracedY = remember(activeFn?.expression, traceX, isDegreeMode) {
    activeFn?.let { CalculatorEngine.evaluateAtOrNull(it.expression, traceX, isDegreeMode) }
  }
  val tracedSlope = remember(activeFn?.expression, traceX, isDegreeMode, showTangent) {
    if (showTangent && activeFn != null) {
      CalculatorEngine.numericalDerivative(activeFn.expression, traceX, isDegreeMode)
    } else {
      null
    }
  }

  Card(
    modifier = modifier
      .clip(RoundedCornerShape(24.dp))
      .testTag("calculator_graph_canvas_card"),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(
      containerColor = scheme.surfaceContainerHighest.copy(alpha = 0.45f),
    ),
    border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.45f)),
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      Canvas(
        modifier = Modifier
          .fillMaxSize()
          .testTag("calculator_graph_canvas")
          .pointerInput(isTraceMode, xMin, xMax, yMin, yMax) {
            detectTapGestures { tapOffset ->
              if (size.width > 0) {
                val tappedX = xMin + (tapOffset.x / size.width.toDouble()) * (xMax - xMin)
                onTraceXChange(tappedX.coerceIn(xMin, xMax))
              }
            }
          }
          .pointerInput(isTraceMode, xMin, xMax, yMin, yMax) {
            if (isTraceMode) {
              detectDragGestures { change, _ ->
                change.consume()
                if (size.width > 0) {
                  val draggedX = xMin + (change.position.x / size.width.toDouble()) * (xMax - xMin)
                  onTraceXChange(draggedX.coerceIn(xMin, xMax))
                }
              }
            } else {
              detectTransformGestures { _, pan, zoom, _ ->
                if (size.width <= 0 || size.height <= 0) return@detectTransformGestures
                val xSpan = (xMax - xMin).coerceAtLeast(1e-4)
                val ySpan = (yMax - yMin).coerceAtLeast(1e-4)
                val dx = -(pan.x / size.width.toDouble()) * xSpan
                val dy = (pan.y / size.height.toDouble()) * ySpan
                val scale = (1.0 / zoom.toDouble()).coerceIn(0.5, 2.0)

                val xCenter = 0.5 * (xMin + xMax) + dx
                val yCenter = 0.5 * (yMin + yMax) + dy
                val newHalfW = (xSpan * 0.5 * scale).coerceIn(0.05, 5000.0)
                val newHalfH = (ySpan * 0.5 * scale).coerceIn(0.05, 5000.0)
                onViewportChange(
                  xCenter - newHalfW,
                  xCenter + newHalfW,
                  yCenter - newHalfH,
                  yCenter + newHalfH,
                )
              }
            }
          },
      ) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f || xMax <= xMin || yMax <= yMin) return@Canvas

        val xSpan = xMax - xMin
        val ySpan = yMax - yMin

        fun toScreenX(mathX: Double): Float = (((mathX - xMin) / xSpan) * w).toFloat()
        fun toScreenY(mathY: Double): Float = (h - ((mathY - yMin) / ySpan) * h).toFloat()

        val xStep = computeNiceGridStep(xSpan, targetLines = 6)
        val yStep = computeNiceGridStep(ySpan, targetLines = 6)

        val minorGridColor = scheme.outlineVariant.copy(alpha = 0.18f)
        val majorGridColor = scheme.outlineVariant.copy(alpha = 0.38f)
        val axisColor = scheme.onSurface.copy(alpha = 0.65f)
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          color = scheme.onSurfaceVariant.copy(alpha = 0.85f).toArgb()
          textSize = 10.sp.toPx()
          typeface = Typeface.MONOSPACE
        }

        // 1. Minor & Major Vertical Grid Lines (X)
        val firstMajorX = floor(xMin / xStep) * xStep
        val minorXStep = xStep / 5.0
        val firstMinorX = floor(xMin / minorXStep) * minorXStep
        var mx = firstMinorX
        var minorCountX = 0
        while (mx <= xMax && minorCountX < 200) {
          if (mx >= xMin) {
            val sx = toScreenX(mx)
            drawLine(
              color = minorGridColor,
              start = Offset(sx, 0f),
              end = Offset(sx, h),
              strokeWidth = 1f,
            )
          }
          mx += minorXStep
          minorCountX++
        }

        // 2. Minor & Major Horizontal Grid Lines (Y)
        val firstMajorY = floor(yMin / yStep) * yStep
        val minorYStep = yStep / 5.0
        val firstMinorY = floor(yMin / minorYStep) * minorYStep
        var my = firstMinorY
        var minorCountY = 0
        while (my <= yMax && minorCountY < 200) {
          if (my >= yMin) {
            val sy = toScreenY(my)
            drawLine(
              color = minorGridColor,
              start = Offset(0f, sy),
              end = Offset(w, sy),
              strokeWidth = 1f,
            )
          }
          my += minorYStep
          minorCountY++
        }

        val yAxisScreenX = toScreenX(0.0).coerceIn(28f, w - 28f)
        val xAxisScreenY = toScreenY(0.0).coerceIn(20f, h - 22f)

        var gx = firstMajorX
        var majorCountX = 0
        while (gx <= xMax && majorCountX < 40) {
          if (gx >= xMin) {
            val sx = toScreenX(gx)
            drawLine(
              color = majorGridColor,
              start = Offset(sx, 0f),
              end = Offset(sx, h),
              strokeWidth = 1.dp.toPx(),
            )
            if (abs(gx) > xStep * 1e-4 && sx in 22f..(w - 22f)) {
              val label = CalculatorEngine.formatResult(gx)
              val textW = labelPaint.measureText(label)
              drawContext.canvas.nativeCanvas.drawText(
                label,
                sx - textW * 0.5f,
                (xAxisScreenY + 13.dp.toPx()).coerceAtMost(h - 6.dp.toPx()),
                labelPaint,
              )
            }
          }
          gx += xStep
          majorCountX++
        }

        var gy = firstMajorY
        var majorCountY = 0
        while (gy <= yMax && majorCountY < 40) {
          if (gy >= yMin) {
            val sy = toScreenY(gy)
            drawLine(
              color = majorGridColor,
              start = Offset(0f, sy),
              end = Offset(w, sy),
              strokeWidth = 1.dp.toPx(),
            )
            if (abs(gy) > yStep * 1e-4 && sy in 18f..(h - 18f)) {
              val label = CalculatorEngine.formatResult(gy)
              drawContext.canvas.nativeCanvas.drawText(
                label,
                (yAxisScreenX + 5.dp.toPx()).coerceAtMost(w - labelPaint.measureText(label) - 6.dp.toPx()),
                sy + 3.5.dp.toPx(),
                labelPaint,
              )
            }
          }
          gy += yStep
          majorCountY++
        }

        // 3. Coordinate Axes (x = 0 and y = 0)
        if (0.0 in xMin..xMax) {
          val sx0 = toScreenX(0.0)
          drawLine(
            color = axisColor,
            start = Offset(sx0, 0f),
            end = Offset(sx0, h),
            strokeWidth = 1.8.dp.toPx(),
          )
        }
        if (0.0 in yMin..yMax) {
          val sy0 = toScreenY(0.0)
          drawLine(
            color = axisColor,
            start = Offset(0f, sy0),
            end = Offset(w, sy0),
            strokeWidth = 1.8.dp.toPx(),
          )
        }

        // 4. Definite Integral Shaded Region (when Integral analysis tab is active)
        if (showIntegralShade && activeFn != null && activeFn.isVisible) {
          val lo = minOf(integralLower, integralUpper).coerceIn(xMin, xMax)
          val hi = maxOf(integralLower, integralUpper).coerceIn(xMin, xMax)
          if (hi > lo) {
            val compiled = CalculatorEngine.compileFunctionOrNull(activeFn.expression, isDegreeMode)
            if (compiled != null) {
              val shadeSteps = 80
              val step = (hi - lo) / shadeSteps.toDouble()
              val baselineY = toScreenY(0.0).coerceIn(0f, h)
              val shadePath = Path()
              var started = false
              for (i in 0..shadeSteps) {
                val xVal = lo + i * step
                val yVal = compiled.evaluateAtOrNull(xVal) ?: continue
                val sx = toScreenX(xVal)
                val sy = toScreenY(yVal).coerceIn(-h, h * 2f)
                if (!started) {
                  shadePath.moveTo(sx, baselineY)
                  shadePath.lineTo(sx, sy)
                  started = true
                } else {
                  shadePath.lineTo(sx, sy)
                }
              }
              if (started) {
                shadePath.lineTo(toScreenX(hi), baselineY)
                shadePath.close()
                drawPath(
                  path = shadePath,
                  brush = Brush.verticalGradient(
                    colors = listOf(
                      activeColor.copy(alpha = 0.28f),
                      activeColor.copy(alpha = 0.08f),
                    ),
                  ),
                )
              }
            }
          }
        }

        // 5. Plot Function Curves
        sampledCurves.forEach { (fnIdx, fnEntry, segments) ->
          val isActiveCurve = fnIdx == activeFunctionIndex
          val curveColor = curveColors[fnEntry.colorIndex.mod(curveColors.size)]
          val strokePx = if (isActiveCurve) 3.dp.toPx() else 2.2.dp.toPx()

          segments.forEach { seg ->
            if (seg.size >= 2) {
              val path = Path()
              seg.forEachIndexed { ptIdx, pt ->
                val sx = toScreenX(pt.x)
                val sy = toScreenY(pt.y)
                if (ptIdx == 0) {
                  path.moveTo(sx, sy)
                } else {
                  path.lineTo(sx, sy)
                }
              }
              drawPath(
                path = path,
                color = if (isActiveCurve) curveColor else curveColor.copy(alpha = 0.8f),
                style = Stroke(
                  width = strokePx,
                  cap = StrokeCap.Round,
                  join = StrokeJoin.Round,
                ),
              )
            }
          }
        }

        // 6. Key Feature Points (Roots, Y-Intercept, Extrema, Intersections)
        if (showKeyPoints) {
          featurePoints.forEach { fp ->
            if (fp.x in xMin..xMax && fp.y in yMin..yMax) {
              val sx = toScreenX(fp.x)
              val sy = toScreenY(fp.y)
              val markerColor = when (fp.type) {
                GraphFeatureType.ROOT -> scheme.primary
                GraphFeatureType.Y_INTERCEPT -> scheme.secondary
                GraphFeatureType.LOCAL_MAX, GraphFeatureType.LOCAL_MIN -> scheme.tertiary
                GraphFeatureType.INTERSECTION -> scheme.error
              }
              drawCircle(
                color = scheme.surface,
                radius = 5.5.dp.toPx(),
                center = Offset(sx, sy),
              )
              drawCircle(
                color = markerColor,
                radius = 4.dp.toPx(),
                center = Offset(sx, sy),
              )
            }
          }
        }

        // 7. Tangent Line at Traced Point
        if (showTangent && tracedY != null && tracedSlope != null) {
          val yAtLeft = tracedY + tracedSlope * (xMin - traceX)
          val yAtRight = tracedY + tracedSlope * (xMax - traceX)
          drawLine(
            color = scheme.tertiary.copy(alpha = 0.85f),
            start = Offset(toScreenX(xMin), toScreenY(yAtLeft)),
            end = Offset(toScreenX(xMax), toScreenY(yAtRight)),
            strokeWidth = 1.6.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f),
          )
        }

        // 8. Interactive Trace Crosshair
        if (traceX in xMin..xMax) {
          val traceScreenX = toScreenX(traceX)
          drawLine(
            color = activeColor.copy(alpha = 0.55f),
            start = Offset(traceScreenX, 0f),
            end = Offset(traceScreenX, h),
            strokeWidth = 1.4.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f),
          )
          if (tracedY != null && tracedY.isFinite() && tracedY in (yMin - ySpan)..(yMax + ySpan)) {
            val traceScreenY = toScreenY(tracedY)
            drawLine(
              color = activeColor.copy(alpha = 0.35f),
              start = Offset(0f, traceScreenY),
              end = Offset(w, traceScreenY),
              strokeWidth = 1.dp.toPx(),
              pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f),
            )
            if (traceScreenY in 0f..h) {
              drawCircle(
                color = activeColor.copy(alpha = 0.25f),
                radius = 11.dp.toPx(),
                center = Offset(traceScreenX, traceScreenY),
              )
              drawCircle(
                color = scheme.surface,
                radius = 6.5.dp.toPx(),
                center = Offset(traceScreenX, traceScreenY),
              )
              drawCircle(
                color = activeColor,
                radius = 4.8.dp.toPx(),
                center = Offset(traceScreenX, traceScreenY),
              )
            }
          }
        }
      }

      // Top-Left Viewport Coordinate Badge
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = scheme.surface.copy(alpha = 0.86f),
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = Modifier
          .align(Alignment.TopStart)
          .padding(10.dp)
          .testTag("calculator_graph_viewport_badge"),
      ) {
        Text(
          text = "x:[${CalculatorEngine.formatResult(xMin)}, ${CalculatorEngine.formatResult(xMax)}]  y:[${CalculatorEngine.formatResult(yMin)}, ${CalculatorEngine.formatResult(yMax)}]",
          style = MaterialTheme.typography.labelSmall,
          fontFamily = FontFamily.Monospace,
          color = scheme.onSurfaceVariant,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
      }

      // Top-Right Mode Toggle Pills (Trace vs Pan, Key Points, Tangent)
      Row(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        GraphCanvasOverlayChip(
          label = if (isTraceMode) "Trace" else "Pan",
          selected = isTraceMode,
          onClick = onToggleTraceMode,
          testTag = "calculator_graph_trace_toggle",
        )
        GraphCanvasOverlayChip(
          label = "Points",
          selected = showKeyPoints,
          onClick = onToggleKeyPoints,
          testTag = "calculator_graph_keypoints_toggle",
        )
        GraphCanvasOverlayChip(
          label = "Tangent",
          selected = showTangent,
          onClick = onToggleTangent,
          testTag = "calculator_graph_tangent_toggle",
        )
      }

      // Bottom-Right Floating Zoom & Viewport Controls
      Row(
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        GraphFloatingIconButton(
          icon = Icons.Default.Add,
          contentDescription = "Zoom in",
          onClick = onZoomIn,
          testTag = "calculator_graph_zoom_in",
        )
        GraphFloatingIconButton(
          icon = Icons.Default.Remove,
          contentDescription = "Zoom out",
          onClick = onZoomOut,
          testTag = "calculator_graph_zoom_out",
        )
        GraphFloatingIconButton(
          icon = Icons.Default.FitScreen,
          contentDescription = "Auto-fit Y axis",
          onClick = onAutoFitY,
          testTag = "calculator_graph_autofit_y",
        )
        GraphFloatingIconButton(
          icon = Icons.Default.CenterFocusStrong,
          contentDescription = "Reset viewport",
          onClick = onResetViewport,
          testTag = "calculator_graph_reset_viewport",
        )
      }
    }
  }
}

@Composable
private fun GraphCanvasOverlayChip(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  testTag: String,
) {
  val shape = if (currentHexagonIconButtons()) HorizontalHexagonShape else RoundedCornerShape(16.dp)
  Surface(
    onClick = onClick,
    shape = shape,
    color = if (selected) {
      MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
    } else {
      MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
    },
    border = BorderStroke(
      1.dp,
      if (selected) {
        MaterialTheme.colorScheme.primary
      } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
      },
    ),
    modifier = Modifier
      .height(28.dp)
      .testTag(testTag),
  ) {
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier.padding(horizontal = 10.dp),
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        color = if (selected) {
          MaterialTheme.colorScheme.onPrimaryContainer
        } else {
          MaterialTheme.colorScheme.onSurfaceVariant
        },
      )
    }
  }
}

@Composable
private fun GraphFloatingIconButton(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  contentDescription: String,
  onClick: () -> Unit,
  testTag: String,
) {
  Surface(
    onClick = onClick,
    shape = CircleShape,
    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f),
    tonalElevation = 3.dp,
    shadowElevation = 3.dp,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
    modifier = Modifier
      .size(32.dp)
      .testTag(testTag),
  ) {
    Box(contentAlignment = Alignment.Center) {
      Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.size(16.dp),
      )
    }
  }
}

@Composable
private fun GraphAnalysisCard(
  functions: List<GraphFunctionEntry>,
  activeFunctionIndex: Int,
  activeColor: Color,
  isDegreeMode: Boolean,
  xMin: Double,
  xMax: Double,
  traceX: Double,
  onTraceXChange: (Double) -> Unit,
  snappedFeature: GraphFeaturePoint?,
  featurePoints: List<GraphFeaturePoint>,
  activeAnalysisTab: GraphAnalysisTab,
  onSelectAnalysisTab: (GraphAnalysisTab) -> Unit,
  integralLower: Double,
  integralUpper: Double,
  onIntegralBoundsChange: (Double, Double) -> Unit,
  onEditActiveFunction: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val haptic = LocalHapticFeedback.current
  val scheme = MaterialTheme.colorScheme
  val activeFn = functions.getOrNull(activeFunctionIndex)
  val yValue = remember(activeFn?.expression, traceX, isDegreeMode) {
    activeFn?.let { CalculatorEngine.evaluateAtOrNull(it.expression, traceX, isDegreeMode) }
  }
  val slopeValue = remember(activeFn?.expression, traceX, isDegreeMode) {
    activeFn?.let { CalculatorEngine.numericalDerivative(it.expression, traceX, isDegreeMode) }
  }

  Card(
    modifier = modifier.testTag("calculator_graph_readout_card"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = scheme.surfaceVariant.copy(alpha = 0.45f),
    ),
    border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.45f)),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      // Top Row: Active function expression + Traced coordinates (x, y, dy/dx)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier
            .weight(1f)
            .clickable { onEditActiveFunction() },
        ) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(activeColor),
          )
          Text(
            text = if (activeFn != null) {
              "${formatFunctionLabel(activeFunctionIndex)} = ${CalculatorEngine.formatDisplay(activeFn.expression)}"
            } else {
              "No active function"
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.testTag("calculator_graph_active_expression"),
          )
        }

        if (snappedFeature != null) {
          val badgeShape = if (currentHexagonIconButtons()) HorizontalHexagonShape else RoundedCornerShape(12.dp)
          Surface(
            shape = badgeShape,
            color = scheme.primaryContainer,
            modifier = Modifier.testTag("calculator_graph_snapped_badge"),
          ) {
            Text(
              text = snappedFeature.label,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = scheme.onPrimaryContainer,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            )
          }
        }
      }

      // Coordinate Readout Pills (x, f(x), dy/dx)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        GraphReadoutMetricPill(
          label = "x",
          value = CalculatorEngine.formatResult(traceX),
          modifier = Modifier
            .weight(1f)
            .testTag("calculator_graph_readout_x"),
        )
        GraphReadoutMetricPill(
          label = formatFunctionLabel(activeFunctionIndex),
          value = yValue?.let { CalculatorEngine.formatResult(it) } ?: "undef",
          highlightColor = activeColor,
          modifier = Modifier
            .weight(1f)
            .testTag("calculator_graph_readout_y"),
        )
        GraphReadoutMetricPill(
          label = "dy/dx",
          value = slopeValue?.let { CalculatorEngine.formatResult(it) } ?: "—",
          modifier = Modifier
            .weight(1f)
            .testTag("calculator_graph_readout_slope"),
        )
      }

      // SlimDotSlider Trace Scrubber
      val sliderFraction = remember(traceX, xMin, xMax) {
        if (xMax > xMin) {
          ((traceX - xMin) / (xMax - xMin)).toFloat().coerceIn(0f, 1f)
        } else {
          0.5f
        }
      }
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Text(
          text = "Trace x",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Medium,
          color = scheme.onSurfaceVariant,
        )
        SlimDotSlider(
          value = sliderFraction,
          onValueChange = { frac ->
            val newX = xMin + frac.toDouble() * (xMax - xMin)
            onTraceXChange(newX)
          },
          accentColor = activeColor,
          modifier = Modifier
            .weight(1f)
            .testTag("calculator_graph_trace_slider"),
        )
        Surface(
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onTraceXChange(0.0.coerceIn(xMin, xMax))
          },
          shape = CircleShape,
          color = scheme.surfaceContainerHigh,
          border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.5f)),
          modifier = Modifier
            .height(26.dp)
            .testTag("calculator_graph_trace_zero_button"),
        ) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 8.dp),
          ) {
            Text(
              text = "x=0",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.SemiBold,
              color = scheme.onSurface,
            )
          }
        }
      }

      // Analysis Tab Selector (Key Points | Table | Integral)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        GraphAnalysisTab.entries.forEach { tab ->
          val selected = tab == activeAnalysisTab
          ToolboxPillNavigationItem(
            selected = selected,
            onClick = { onSelectAnalysisTab(tab) },
            label = tab.label,
            modifier = Modifier.testTag("calculator_graph_analysis_tab_${tab.name.lowercase()}"),
          )
        }
      }

      // Analysis Tab Content
      when (activeAnalysisTab) {
        GraphAnalysisTab.KEY_POINTS -> {
          if (featurePoints.isEmpty()) {
            Text(
              text = "No roots, extrema, or intersections in current viewport",
              style = MaterialTheme.typography.bodySmall,
              color = scheme.onSurfaceVariant,
              modifier = Modifier
                .padding(vertical = 4.dp)
                .testTag("calculator_graph_keypoints_empty"),
            )
          } else {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .testTag("calculator_graph_keypoints_list"),
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              featurePoints.forEachIndexed { idx, fp ->
                val isAtTrace = abs(fp.x - traceX) < 1e-4
                val pillShape = if (currentHexagonIconButtons()) HorizontalHexagonShape else RoundedCornerShape(16.dp)
                Surface(
                  onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onTraceXChange(fp.x)
                  },
                  shape = pillShape,
                  color = if (isAtTrace) scheme.primaryContainer else scheme.surfaceContainerHigh,
                  border = BorderStroke(
                    1.dp,
                    if (isAtTrace) scheme.primary else scheme.outlineVariant.copy(alpha = 0.5f),
                  ),
                  modifier = Modifier
                    .height(28.dp)
                    .testTag("calculator_graph_feature_chip_$idx"),
                ) {
                  Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 10.dp),
                  ) {
                    Text(
                      text = fp.label,
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = if (isAtTrace) FontWeight.Bold else FontWeight.Medium,
                      color = if (isAtTrace) scheme.onPrimaryContainer else scheme.onSurface,
                    )
                  }
                }
              }
            }
          }
        }

        GraphAnalysisTab.TABLE -> {
          val step = computeNiceGridStep(xMax - xMin, targetLines = 8)
          val startX = ceil(xMin / step) * step
          val sampleXs = remember(xMin, xMax, step, startX) {
            val list = mutableListOf<Double>()
            var curr = startX
            while (curr <= xMax && list.size < 15) {
              list.add(if (abs(curr) < 1e-11) 0.0 else curr)
              curr += step
            }
            list
          }
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState())
              .testTag("calculator_graph_table_list"),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            sampleXs.forEachIndexed { idx, sx ->
              val sy = activeFn?.let { CalculatorEngine.evaluateAtOrNull(it.expression, sx, isDegreeMode) }
              val isSelected = abs(sx - traceX) < step * 0.15
              Surface(
                onClick = {
                  haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  onTraceXChange(sx)
                },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) scheme.primaryContainer else scheme.surfaceContainerHigh,
                border = BorderStroke(
                  1.dp,
                  if (isSelected) scheme.primary else scheme.outlineVariant.copy(alpha = 0.45f),
                ),
                modifier = Modifier.testTag("calculator_graph_table_cell_$idx"),
              ) {
                Column(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                  Text(
                    text = "x = ${CalculatorEngine.formatResult(sx)}",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = if (isSelected) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
                  )
                  Text(
                    text = sy?.let { CalculatorEngine.formatResult(it) } ?: "undef",
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) scheme.primary else scheme.onSurface,
                  )
                }
              }
            }
          }
        }

        GraphAnalysisTab.INTEGRAL -> {
          val integralValue = remember(activeFn?.expression, integralLower, integralUpper, isDegreeMode) {
            activeFn?.let {
              CalculatorEngine.definiteIntegral(
                expression = it.expression,
                a = integralLower,
                b = integralUpper,
                isDegreeMode = isDegreeMode,
              )
            }
          }
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState())
              .testTag("calculator_graph_integral_row"),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "∫ [${CalculatorEngine.formatResult(integralLower)}, ${CalculatorEngine.formatResult(integralUpper)}] ${formatFunctionLabel(activeFunctionIndex)} dx = ${integralValue?.let { CalculatorEngine.formatResult(it) } ?: "undef"}",
              style = MaterialTheme.typography.labelMedium,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              color = scheme.primary,
              modifier = Modifier.testTag("calculator_graph_integral_result"),
            )

            val boundsPresets = listOf(
              "0 → x" to (0.0 to traceX),
              "0 → 1" to (0.0 to 1.0),
              "−1 → 1" to (-1.0 to 1.0),
              "0 → π" to (0.0 to Math.PI),
            )
            boundsPresets.forEach { (label, pair) ->
              Surface(
                onClick = {
                  haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  onIntegralBoundsChange(pair.first, pair.second)
                },
                shape = CircleShape,
                color = scheme.surfaceContainerHigh,
                border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.height(26.dp),
              ) {
                Box(
                  contentAlignment = Alignment.Center,
                  modifier = Modifier.padding(horizontal = 8.dp),
                ) {
                  Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = scheme.onSurface,
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun GraphReadoutMetricPill(
  label: String,
  value: String,
  modifier: Modifier = Modifier,
  highlightColor: Color? = null,
) {
  val scheme = MaterialTheme.colorScheme
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(12.dp),
    color = scheme.surface.copy(alpha = 0.7f),
    border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.4f)),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "$label:",
        style = MaterialTheme.typography.labelSmall,
        color = scheme.onSurfaceVariant,
      )
      Text(
        text = value,
        style = MaterialTheme.typography.labelMedium,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        color = highlightColor ?: scheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun GraphFunctionEditorDialog(
  functionLabel: String,
  initialExpression: String,
  isNewFunction: Boolean,
  canDelete: Boolean,
  isDegreeMode: Boolean,
  onDismiss: () -> Unit,
  onConfirm: (String) -> Unit,
  onDelete: () -> Unit,
) {
  val haptic = LocalHapticFeedback.current
  val focusRequester = remember { FocusRequester() }
  var textFieldValue by remember {
    mutableStateOf(
      TextFieldValue(
        text = initialExpression,
        selection = TextRange(0, initialExpression.length),
      ),
    )
  }

  LaunchedEffect(Unit) {
    focusRequester.requestFocus()
  }

  val normalizedInput = remember(textFieldValue.text) {
    CalculatorEngine.normalizeGraphExpression(textFieldValue.text)
  }
  val compiled = remember(normalizedInput, isDegreeMode) {
    CalculatorEngine.compileFunctionOrNull(normalizedInput, isDegreeMode)
  }
  val isValid = normalizedInput.isNotBlank() && compiled != null
  val samplePreviewText = remember(compiled) {
    if (compiled == null) {
      null
    } else {
      val samplePoints = listOf(-1.0, 0.0, 1.0, 2.0).mapNotNull { x ->
        compiled.evaluateAtOrNull(x)?.let { y ->
          "f(${CalculatorEngine.formatResult(x)})=${CalculatorEngine.formatResult(y)}"
        }
      }
      if (samplePoints.isNotEmpty()) samplePoints.joinToString(" • ") else "Valid function expression"
    }
  }

  fun insertToken(token: String) {
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    val current = textFieldValue.text
    val selStart = textFieldValue.selection.min.coerceIn(0, current.length)
    val selEnd = textFieldValue.selection.max.coerceIn(0, current.length)
    val updated = current.substring(0, selStart) + token + current.substring(selEnd)
    val newCursor = selStart + token.length
    textFieldValue = TextFieldValue(text = updated, selection = TextRange(newCursor))
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    modifier = Modifier.testTag("calculator_graph_fn_dialog"),
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ShowChart,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column {
          Text(
            text = if (isNewFunction) "Add Function $functionLabel" else "Edit Function $functionLabel",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Enter an expression in terms of variable x",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        // Context Preview Card
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          ),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Text(
              text = "$functionLabel = ${CalculatorEngine.formatDisplay(normalizedInput.ifBlank { "…" })}",
              style = MaterialTheme.typography.bodyLarge,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = when {
                normalizedInput.isBlank() -> "Type a mathematical function such as sin(x) or x^2 - 4"
                samplePreviewText != null -> samplePreviewText
                else -> "Invalid syntax — check parentheses or operators"
              },
              style = MaterialTheme.typography.bodySmall,
              fontFamily = FontFamily.Monospace,
              color = if (normalizedInput.isNotBlank() && !isValid) {
                MaterialTheme.colorScheme.error
              } else {
                MaterialTheme.colorScheme.onSurfaceVariant
              },
            )
          }
        }

        // Rounded Input Field
        OutlinedTextField(
          value = textFieldValue,
          onValueChange = { textFieldValue = it },
          label = { Text("$functionLabel =") },
          placeholder = { Text("e.g. sin(x) + 0.5*x") },
          singleLine = true,
          isError = normalizedInput.isNotBlank() && !isValid,
          shape = RoundedCornerShape(14.dp),
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Functions,
              contentDescription = null,
            )
          },
          trailingIcon = {
            if (textFieldValue.text.isNotEmpty()) {
              IconButton(
                onClick = {
                  textFieldValue = TextFieldValue("")
                },
              ) {
                Icon(
                  imageVector = Icons.Default.Clear,
                  contentDescription = "Clear expression",
                )
              }
            }
          },
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
          keyboardActions = KeyboardActions(
            onDone = {
              if (isValid) onConfirm(normalizedInput)
            },
          ),
          supportingText = {
            if (normalizedInput.isNotBlank() && !isValid) {
              Text(
                text = "Enter a valid mathematical expression in x",
                color = MaterialTheme.colorScheme.error,
              )
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .testTag("calculator_graph_fn_input"),
        )

        // Quick-Insert Math Tokens Row
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          val quickTokens = listOf("x", "^2", "^", "sin(", "cos(", "tan(", "sqrt(", "ln(", "exp(", "abs(", "π", "e")
          quickTokens.forEach { token ->
            Surface(
              onClick = { insertToken(token) },
              shape = CircleShape,
              color = MaterialTheme.colorScheme.surfaceContainerHighest,
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
              modifier = Modifier
                .height(30.dp)
                .testTag("calculator_graph_quick_token_$token"),
            ) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(horizontal = 10.dp),
              ) {
                Text(
                  text = token,
                  style = MaterialTheme.typography.labelMedium,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface,
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          onConfirm(normalizedInput)
        },
        enabled = isValid,
        shape = CircleShape,
        modifier = Modifier.testTag("calculator_graph_fn_confirm"),
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(if (isNewFunction) "Plot" else "Save")
      }
    },
    dismissButton = {
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (canDelete) {
          TextButton(
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              onDelete()
            },
            shape = CircleShape,
            modifier = Modifier.testTag("calculator_graph_fn_delete"),
          ) {
            Text("Delete", color = MaterialTheme.colorScheme.error)
          }
        }
        TextButton(
          onClick = onDismiss,
          shape = CircleShape,
          modifier = Modifier.testTag("calculator_graph_fn_cancel"),
        ) {
          Text("Cancel")
        }
      }
    },
  )
}

@Preview(showBackground = true)
@Composable
fun CalculatorScreenPreview() {
  ToolboxTheme {
    CalculatorScreen()
  }
}
