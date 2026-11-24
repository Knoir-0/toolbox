package com.example.toolbox.ui.tools

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.example.toolbox.ui.theme.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.toolbox.ui.theme.toolboxScreenBackground
import kotlin.random.Random

const val GOOD_PREFS_NAME = "good_tool_prefs"
const val PREF_KEY_GOOD_CURRENT_MESSAGE = "good_current_message"
const val GOOD_TAP_TO_REFRESH_TEXT = "tap to refresh"
const val GOOD_WORD_BLACKLIST_PROBABILITY = 0.8f

private val GOOD_WORD_SPLIT_REGEX = Regex("""[^\p{L}\p{N}']+""")

/**
 * Extracts normalized (lowercase) words from [message] so messages sharing words can be identified.
 */
fun extractGoodMessageWords(message: String): Set<String> {
  if (message.isBlank()) return emptySet()
  return message
    .lowercase()
    .split(GOOD_WORD_SPLIT_REGEX)
    .map { it.trim('\'') }
    .filter { it.isNotEmpty() }
    .toSet()
}

val GOOD_POSITIVE_MESSAGES: List<String> = listOf(
  "do good today",
  "please be kind",
  "with love",
  "you're doing great",
  "glad you're here",
  "drink some water",
  "be gentle",
  "you matter",
  "sending good vibes",
  "thanks for being you",
  "take a break",
  "rest easy",
  "one step at a time",
  "keep going",
  "proud of you",
  "stay hydrated",
  "you got this",
  "made for you",
  "do your best",
  "be you today",
  "be good",
  "help someone today",
  "share a smile",
  "check on a friend",
  "say something kind",
  "spread some joy",
  "listen more",
  "be patient",
  "clean a bit, it helps",
  "choose kindness",
  "hug someone",
  "shine bright",
  "be a light",
  "love is everywhere",
  "choose love",
  "be kind",
  "forgive",
  "leave the past",
  "don't worry",
  "it'll be okay",
  "take care",
  "be safe",
  "act with love",
  "act with kindness",
  "take action",
  "be you",
  "breathe",
  "let it go",
  "peace within",
  "be peaceful",
  "slow down",
  "find good",
  "enjoy life",
  "be present",
  "do rest sometimes",
  "look how far we've come",
  "act with grace",
  "be brave",
  "be bold",
  "start small",
  "stay curious",
  "be wise",
  "make something",
  "keep learning",
  "always try",
  "spread kindness",
  "spread love",
  "you are not alone",
  "you are loved",
  "always hope",
  "it's okay",
  "be alright",
  "this too shall pass",
  "endure",
  "please forgive",
  "know that you are loved",
  "love you",
  "explore today",
  "forget the past",
  "it's okay to drift",
  "make someone happy",
  "be happy",
  "together we build",
  "together we do better",
  "unite",
  "break the chains",
  "be free",
  "love",
  "you are human",
  "sometimes, plans change",
  "roll with it",
  "reach out",
  "forget about it",
  "make it happen",
  "stop waiting",
  "you know what to do",
  "i am as i am",
  "do you",
  "spread positivity",
  "it's okay to feel",
  "sometimes we fall",
  "get back up",
  "be strong",
  "please rest",
  "eat well",
  "better every day",
  "one day",
  "you are",
  "be",
  "be adventurous",
  "explore",
  "create something",
  "you are okay",
  "touch some grass",
  "rest your eyes",
  "sunlight, please",
  "stay weird",
  "step outside",
  "learn from mistakes",
  "learn today",
  "learn something new",
  "care",
  "laugh at your worries",
  "it's not so big anymore",
  "you can do it",
  "do it",
  "do well",
  "no need to rush",
  "still here",
  "good things take time",
  "go easy on yourself",
  "good enough",
  "grow a bit",
  "you are enough",
  "trust yourself",
  "take your time",
  "touch grass",
  "thank you",
  "get a nap in",
  "boop",
  "live well",
  "love well",
  "laugh about it",
  "sometimes life sucks",
  "you don't have to",
  "bad days end too",
  "tired is fine",
  "sleep well",
  "laugh often",
  "speak kindly",
  "stay warm",
  "keep shining",
  "hold on",
  "cheers",
  "hope it gets better",
  "shrug it off",
  "shake it off",
  "have some fun",
  "vibing",
  "still kicking",
  "hang in there",
  "keep it up",
  "stay gold, ponyboy",
  "stay gold",
  "be real",
  "have good thoughts",
  "peace and love",
  "no rush",
  "kick back",
  "it happens",
  "say no sometimes",
  "make your own choices",
  "think for yourself",
  "look at you",
  "bump some music",
  "deep breath",
  "unify",
  "be creative",
  "take a deep breath",
  "put down the phone",
  "less screens",
  "let it be",
  "start a cult",
  "do a favor",
  "do something good today",
  "rest a little",
  "remain calm",
  "be calm",
  "focus on yourself",
  "nothing to prove",
  "all in good time",
  "float for a bit",
  "pace yourself",
  "smile a little",
  "treat yourself",
  "sing along",
  "walk a mile",
  "just vibing",
  "question authority",
  "make the world better",
  "make your world better",
  "this world is ours",
  "share something",
  "don't forget to eat",
  "shape your world",
  "endless possibilities",
  "endless paths",
  "walk your own path",
  "forge ahead",
  "make your own path",
  "inspire someone",
  "get inspired",
  "find inspiration",
  "you're not stuck",
  "you good?",
  "you okay?",
  "you alright?",
  "holding up?",
  "feeling okay?",
  "feeling alright?",
  "what's the hurry?",
  "did you eat today?",
  "had enough water?",
  "need a hug?",
)

val DEFAULT_GOOD_MESSAGE: String = GOOD_POSITIVE_MESSAGES.first()

/**
 * Picks a new positive message from [messages], guaranteed to be different from [currentMessage]
 * whenever [messages] has more than 1 distinct entry.
 *
 * To make consecutive messages feel more random to humans, [wordBlacklistProbability] (80% by
 * default) of the time all words present in [currentMessage] are blacklisted when filtering
 * candidate messages (falling back to any distinct message if every candidate shares a word).
 */
fun pickNextGoodMessage(
  currentMessage: String? = null,
  random: Random = Random.Default,
  messages: List<String> = GOOD_POSITIVE_MESSAGES,
  wordBlacklistProbability: Float = GOOD_WORD_BLACKLIST_PROBABILITY,
): String {
  if (messages.isEmpty()) return ""
  if (messages.size == 1) return messages.first()
  val distinctCandidates = messages.filter { it != currentMessage }
  val basePool = if (distinctCandidates.isNotEmpty()) distinctCandidates else messages
  val blacklistedWords = currentMessage?.let(::extractGoodMessageWords).orEmpty()
  val shouldBlacklistCurrentWords = blacklistedWords.isNotEmpty() &&
    wordBlacklistProbability > 0f &&
    (wordBlacklistProbability >= 1f || random.nextFloat() < wordBlacklistProbability)
  val pool = if (shouldBlacklistCurrentWords) {
    val nonOverlapping = basePool.filter { candidate ->
      extractGoodMessageWords(candidate).none { it in blacklistedWords }
    }
    if (nonOverlapping.isNotEmpty()) nonOverlapping else basePool
  } else {
    basePool
  }
  return pool[random.nextInt(pool.size)]
}

/**
 * Repository interface for persisting the active message in the **Good** tool.
 */
interface GoodRepository {
  fun getCurrentMessage(): String
  fun setCurrentMessage(message: String)
  fun refreshMessage(random: Random = Random.Default): String {
    val next = pickNextGoodMessage(
      currentMessage = getCurrentMessage(),
      random = random,
    )
    setCurrentMessage(next)
    return next
  }
}

/**
 * [SharedPreferences]-backed implementation of [GoodRepository].
 */
class SharedPreferencesGoodRepository(
  private val sharedPreferences: SharedPreferences,
  private val appContext: Context? = null,
) : GoodRepository {

  constructor(context: Context) : this(
    sharedPreferences = context.getSharedPreferences(GOOD_PREFS_NAME, Context.MODE_PRIVATE),
    appContext = context.applicationContext ?: context,
  )

  override fun getCurrentMessage(): String {
    val saved = sharedPreferences.getString(PREF_KEY_GOOD_CURRENT_MESSAGE, null)
      ?.trim()
      ?.takeIf { it.isNotEmpty() }
    return saved ?: DEFAULT_GOOD_MESSAGE
  }

  override fun setCurrentMessage(message: String) {
    val sanitized = message.trim().ifEmpty { DEFAULT_GOOD_MESSAGE }
    sharedPreferences.edit()
      .putString(PREF_KEY_GOOD_CURRENT_MESSAGE, sanitized)
      .apply()
    GoodWidgets.updateAllWidgets(appContext)
  }
}

/**
 * In-memory implementation of [GoodRepository] for testing and Compose previews.
 */
class InMemoryGoodRepository(
  initialMessage: String = DEFAULT_GOOD_MESSAGE,
) : GoodRepository {
  private var currentMessage: String = initialMessage.trim().ifEmpty { DEFAULT_GOOD_MESSAGE }

  override fun getCurrentMessage(): String = currentMessage

  override fun setCurrentMessage(message: String) {
    currentMessage = message.trim().ifEmpty { DEFAULT_GOOD_MESSAGE }
  }
}

@Composable
fun rememberGoodRepository(): GoodRepository {
  val isInspection = LocalInspectionMode.current
  val context = LocalContext.current
  return remember(isInspection, context) {
    if (isInspection) {
      InMemoryGoodRepository()
    } else {
      SharedPreferencesGoodRepository(context.applicationContext ?: context)
    }
  }
}

/**
 * Minimalist full-canvas screen for the **Good** tool.
 * Displays the current positive message centered on screen; tapping anywhere refreshes to a new
 * positive message and remembers the state across launches.
 */
@Composable
fun GoodScreen(
  modifier: Modifier = Modifier,
  repository: GoodRepository = rememberGoodRepository(),
  random: Random = Random.Default,
) {
  val haptic = LocalHapticFeedback.current
  val isInspection = LocalInspectionMode.current
  val lifecycleOwner = LocalLifecycleOwner.current
  var currentMessage by rememberSaveable {
    mutableStateOf(repository.getCurrentMessage())
  }
  DisposableEffect(lifecycleOwner, repository, isInspection) {
    if (isInspection) {
      onDispose {}
    } else {
      val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
          currentMessage = repository.getCurrentMessage()
        }
      }
      lifecycleOwner.lifecycle.addObserver(observer)
      onDispose {
        lifecycleOwner.lifecycle.removeObserver(observer)
      }
    }
  }
  val interactionSource = remember { MutableInteractionSource() }

  Box(
    modifier = modifier
      .fillMaxSize()
      .toolboxScreenBackground(MaterialTheme.colorScheme.background)
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        onClickLabel = "Refresh message",
      ) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        currentMessage = repository.refreshMessage(random)
      }
      .padding(horizontal = 32.dp, vertical = 24.dp)
      .testTag("good_screen"),
    contentAlignment = Alignment.Center,
  ) {
    AnimatedContent(
      targetState = currentMessage,
      transitionSpec = {
        (fadeIn(tween(220)) + scaleIn(initialScale = 0.94f, animationSpec = tween(220)))
          .togetherWith(fadeOut(tween(160)) + scaleOut(targetScale = 0.94f, animationSpec = tween(160)))
      },
      label = "good_message_transition",
    ) { message ->
      Text(
        text = message,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = Modifier.testTag("good_message_text"),
      )
    }

    TextButton(
      onClick = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        currentMessage = repository.refreshMessage(random)
      },
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 16.dp)
        .testTag("good_tap_to_refresh_button"),
    ) {
      Icon(
        imageVector = Icons.Default.Refresh,
        contentDescription = null,
        modifier = Modifier.size(18.dp),
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = GOOD_TAP_TO_REFRESH_TEXT,
        modifier = Modifier.testTag("good_tap_to_refresh_text"),
      )
    }
  }
}
