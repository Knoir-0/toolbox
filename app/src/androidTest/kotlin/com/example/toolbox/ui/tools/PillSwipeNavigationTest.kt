package com.example.toolbox.ui.tools

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PillSwipeNavigationTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @OptIn(ExperimentalFoundationApi::class)
  @Test
  fun pillHorizontalPagerPaginatesAcrossClickableCardsDiagonalSwipesAndChipRows() {
    val tabs = listOf("First", "Second", "Third")
    var activeTab by mutableStateOf("First")
    var cardClicks by mutableIntStateOf(0)

    composeTestRule.setContent {
      MaterialTheme {
        Column(modifier = Modifier.size(400.dp, 700.dp)) {
          ToolboxPillNavigationBar {
            tabs.forEach { tab ->
              ToolboxPillNavigationItem(
                selected = activeTab == tab,
                onClick = { activeTab = tab },
                label = tab,
                testTag = "pill_$tab",
              )
            }
          }

          ToolboxPillHorizontalPager(
            items = tabs,
            selectedItem = activeTab,
            onSelectItem = { activeTab = it },
            modifier = Modifier
              .weight(1f)
              .fillMaxWidth(),
          ) { pageTab ->
            val isActive = pageTab == activeTab
            LazyColumn(modifier = Modifier.fillMaxSize()) {
              item {
                Card(
                  onClick = { cardClicks++ },
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .then(if (isActive) Modifier.testTag("clickable_card") else Modifier),
                ) {
                  Text("Clickable Card ($pageTab)")
                }
              }
              item {
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .combinedClickable(
                      onClick = { cardClicks++ },
                      onLongClick = { cardClicks += 10 },
                    )
                    .then(if (isActive) Modifier.testTag("combined_clickable_row") else Modifier),
                ) {
                  Text("Combined Clickable Row ($pageTab)")
                }
              }
              item {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .horizontalScroll(rememberScrollState())
                    .then(if (isActive) Modifier.testTag("non_scrolling_chip_row") else Modifier),
                ) {
                  Button(onClick = { cardClicks++ }) {
                    Text("Short Chip ($pageTab)")
                  }
                }
              }
              item {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .horizontalScroll(rememberScrollState())
                    .then(if (isActive) Modifier.testTag("scrolling_chip_row") else Modifier),
                ) {
                  repeat(8) { index ->
                    Box(modifier = Modifier.width(140.dp).height(48.dp)) {
                      Text("Wide Item $index")
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    // 1. Diagonal-arc horizontal swipe on a clickable Card inside a LazyColumn paginates First -> Second
    composeTestRule.onNodeWithTag("clickable_card").performTouchInput {
      down(Offset(width * 0.85f, height * 0.5f))
      moveTo(Offset(width * 0.15f, height * 0.5f + 24f))
      up()
    }
    composeTestRule.waitForIdle()
    assertEquals("Second", activeTab)
    assertEquals(0, cardClicks)

    // 2. Swiping left on a combinedClickable row paginates Second -> Third without clicking
    composeTestRule.onNodeWithTag("combined_clickable_row").performTouchInput { swipeLeft() }
    composeTestRule.waitForIdle()
    assertEquals("Third", activeTab)
    assertEquals(0, cardClicks)

    // 3. Swiping right on a non-scrolling horizontalScroll row bubbles into HorizontalPager (Third -> Second)
    composeTestRule.onNodeWithTag("non_scrolling_chip_row").performTouchInput { swipeRight() }
    composeTestRule.waitForIdle()
    assertEquals("Second", activeTab)
    assertEquals(0, cardClicks)

    // 4. Tapping a pill animates the pager to that page
    composeTestRule.onNodeWithTag("pill_First").performClick()
    composeTestRule.waitForIdle()
    assertEquals("First", activeTab)
  }
}
