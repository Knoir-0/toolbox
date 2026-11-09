package com.example.toolbox.ui.tools

import android.widget.Toast
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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toolbox.ui.theme.ToolboxTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
  modifier: Modifier = Modifier,
  bottomPadding: Dp = 0.dp,
) {
  var expression by rememberSaveable { mutableStateOf("") }
  var isDegreeMode by rememberSaveable { mutableStateOf(true) }
  var isScientificExpanded by rememberSaveable { mutableStateOf(false) }
  var showHistorySheet by rememberSaveable { mutableStateOf(false) }
  var isJustEvaluated by rememberSaveable { mutableStateOf(false) }
  var memoryValue by rememberSaveable { mutableDoubleStateOf(0.0) }
  var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

  val historyList = remember { mutableStateListOf<CalculatorHistoryItem>() }
  val haptic = LocalHapticFeedback.current
  val clipboardManager = LocalClipboardManager.current
  val context = LocalContext.current
  val exprScrollState = rememberScrollState()

  // Live preview evaluation
  val livePreview = remember(expression, isDegreeMode) {
    if (errorMessage != null || expression.isBlank()) {
      null
    } else {
      when (val res = CalculatorEngine.evaluateSafe(expression, isDegreeMode)) {
        is EvaluationResult.Success -> res.formatted
        is EvaluationResult.Error -> null
        EvaluationResult.Incomplete -> null
      }
    }
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
    val textToCopy = if (errorMessage != null) errorMessage!! else expression.ifEmpty { "0" }
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

    val canClose = lastChar in '0'..'9' || lastChar == ')' || lastChar == 'π' || lastChar == 'e' || lastChar == '!' || lastChar == '%'

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

  Box(
    modifier = modifier
      .fillMaxSize()
      .testTag("calculator_screen"),
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .padding(bottom = bottomPadding),
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
            .padding(horizontal = 22.dp, vertical = 18.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
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
              displayText.length <= 7 -> 46.sp
              displayText.length <= 12 -> 36.sp
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

          // Lower Subtitle: Live preview (= 42) or Error message
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(30.dp),
            contentAlignment = Alignment.CenterEnd,
          ) {
            AnimatedContent(
              targetState = errorMessage ?: livePreview?.let { "= $it" } ?: "",
              transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(150)) },
              label = "preview_animation",
            ) { text ->
              if (text.isNotEmpty()) {
                Text(
                  text = text,
                  style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                  ),
                  color = if (errorMessage != null) {
                    MaterialTheme.colorScheme.error
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

      // Controls Bar (Toolbar): DEG/RAD, Scientific fx toggle, Copy, History
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          // Degree / Radian mode toggle pill
          AngleModePill(
            isDegreeMode = isDegreeMode,
            onToggle = { isDegreeMode = !isDegreeMode },
          )

          // Scientific fx toggle button
          ScientificTogglePill(
            isExpanded = isScientificExpanded,
            onToggle = { isScientificExpanded = !isScientificExpanded },
          )
        }

        // History & Copy buttons
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Surface(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .clickable(onClick = { onCopyResult() })
              .testTag("calculator_copy_button"),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy result",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(17.dp),
              )
            }
          }

          Surface(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .clickable(onClick = { showHistorySheet = true })
              .testTag("calculator_history_button"),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
          ) {
            Box(contentAlignment = Alignment.Center) {
              BadgedBox(
                badge = {
                  if (historyList.isNotEmpty()) {
                    Badge(
                      containerColor = MaterialTheme.colorScheme.primary,
                      contentColor = MaterialTheme.colorScheme.onPrimary,
                    ) {
                      Text(historyList.size.toString(), fontSize = 10.sp)
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
            .padding(bottom = 4.dp),
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
              .padding(top = 12.dp, bottom = 4.dp),
            contentAlignment = Alignment.Center,
          ) {
            Surface(
              modifier = Modifier
                .width(36.dp)
                .height(4.dp),
              shape = CircleShape,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            ) {}
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
private fun AngleModePill(
  isDegreeMode: Boolean,
  onToggle: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val haptic = LocalHapticFeedback.current

  Surface(
    modifier = modifier
      .height(34.dp)
      .clip(CircleShape)
      .clickable {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onToggle()
      }
      .testTag("calculator_angle_mode_chip"),
    shape = CircleShape,
    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
  ) {
    Row(
      modifier = Modifier.padding(3.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      // DEG Segment
      val degBg by animateColorAsState(
        targetValue = if (isDegreeMode) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(200),
        label = "deg_bg",
      )
      val degTextColor by animateColorAsState(
        targetValue = if (isDegreeMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        animationSpec = tween(200),
        label = "deg_text",
      )
      Box(
        modifier = Modifier
          .clip(CircleShape)
          .background(degBg)
          .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = "DEG",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = if (isDegreeMode) FontWeight.ExtraBold else FontWeight.SemiBold,
            fontSize = 11.sp,
          ),
          color = degTextColor,
        )
      }

      // RAD Segment
      val radBg by animateColorAsState(
        targetValue = if (!isDegreeMode) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(200),
        label = "rad_bg",
      )
      val radTextColor by animateColorAsState(
        targetValue = if (!isDegreeMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        animationSpec = tween(200),
        label = "rad_text",
      )
      Box(
        modifier = Modifier
          .clip(CircleShape)
          .background(radBg)
          .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = "RAD",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = if (!isDegreeMode) FontWeight.ExtraBold else FontWeight.SemiBold,
            fontSize = 11.sp,
          ),
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
  val chevronRotation by animateFloatAsState(
    targetValue = if (isExpanded) 180f else 0f,
    animationSpec = tween(durationMillis = 250),
    label = "chevron_rot",
  )
  val containerColor by animateColorAsState(
    targetValue = if (isExpanded) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f),
    animationSpec = tween(200),
    label = "fx_container",
  )
  val contentColor by animateColorAsState(
    targetValue = if (isExpanded) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
    animationSpec = tween(200),
    label = "fx_content",
  )
  val borderColor by animateColorAsState(
    targetValue = if (isExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
    animationSpec = tween(200),
    label = "fx_border",
  )

  Surface(
    modifier = modifier
      .height(34.dp)
      .clip(CircleShape)
      .clickable {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onToggle()
      }
      .testTag("calculator_scientific_toggle_chip"),
    shape = CircleShape,
    color = containerColor,
    contentColor = contentColor,
    border = BorderStroke(1.dp, borderColor),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Text(
        text = "f(x)",
        style = MaterialTheme.typography.labelMedium.copy(
          fontStyle = FontStyle.Italic,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          fontFamily = FontFamily.Serif,
        ),
      )
      Icon(
        imageVector = Icons.Default.KeyboardArrowDown,
        contentDescription = if (isExpanded) "Collapse scientific functions" else "Expand scientific functions",
        modifier = Modifier
          .size(16.dp)
          .graphicsLayer { rotationZ = chevronRotation },
      )
    }
  }
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

    // Scientific Row 3: %, n!, 1/x, |x|
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      KeypadPillButton("%", onClick = { onInputChar("%") }, modifier = Modifier.weight(1f))
      KeypadPillButton("n!", onClick = { onInputChar("!") }, modifier = Modifier.weight(1f))
      KeypadPillButton("1/x", onClick = { onInputChar("1/(") }, modifier = Modifier.weight(1f))
      KeypadPillButton("|x|", onClick = { onInputFunction("abs") }, modifier = Modifier.weight(1f))
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
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
          imageVector = Icons.Default.History,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
        )
        Text(
          text = "Calculation History",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
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

@Preview(showBackground = true)
@Composable
fun CalculatorScreenPreview() {
  ToolboxTheme {
    CalculatorScreen()
  }
}
