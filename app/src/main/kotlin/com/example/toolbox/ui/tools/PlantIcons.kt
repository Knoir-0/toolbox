package com.example.toolbox.ui.tools

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Returns the custom [ImageVector] icon matching this [PlantItem]'s specific plant type.
 */
fun PlantItem.iconVector(): ImageVector = iconType.imageVector()

/**
 * Returns the representative [ImageVector] icon for this [PlantCategory].
 */
fun PlantCategory.iconVector(): ImageVector = defaultIconType.imageVector()

/**
 * Returns the [ImageVector] corresponding to this [PlantIconType].
 */
fun PlantIconType.imageVector(): ImageVector = when (this) {
  PlantIconType.TOMATO -> PlantIcons.Tomato
  PlantIconType.BELL_PEPPER -> PlantIcons.BellPepper
  PlantIconType.CHILI_PEPPER -> PlantIcons.ChiliPepper
  PlantIconType.CUCUMBER_SQUASH -> PlantIcons.CucumberSquash
  PlantIconType.PUMPKIN -> PlantIcons.Pumpkin
  PlantIconType.PEA_POD -> PlantIcons.PeaPod
  PlantIconType.CORN -> PlantIcons.Corn
  PlantIconType.EGGPLANT -> PlantIcons.Eggplant
  PlantIconType.BROCCOLI -> PlantIcons.Broccoli
  PlantIconType.CABBAGE -> PlantIcons.Cabbage
  PlantIconType.STALK -> PlantIcons.Stalk
  PlantIconType.ARTICHOKE_OKRA -> PlantIcons.ArtichokeOkra
  PlantIconType.LETTUCE_HEAD -> PlantIcons.LettuceHead
  PlantIconType.LEAFY_GREEN -> PlantIcons.LeafyGreen
  PlantIconType.CARROT_ROOT -> PlantIcons.CarrotRoot
  PlantIconType.ROUND_ROOT -> PlantIcons.RoundRoot
  PlantIconType.POTATO_TUBER -> PlantIcons.PotatoTuber
  PlantIconType.ALLIUM_BULB -> PlantIcons.AlliumBulb
  PlantIconType.SCALLION_LEEK -> PlantIcons.ScallionLeek
  PlantIconType.HERB_LEAF -> PlantIcons.HerbLeaf
  PlantIconType.HERB_NEEDLE -> PlantIcons.HerbNeedle
  PlantIconType.STRAWBERRY -> PlantIcons.Strawberry
  PlantIconType.MELON -> PlantIcons.Melon
  PlantIconType.BERRY -> PlantIcons.Berry
  PlantIconType.GRAPE -> PlantIcons.Grape
  PlantIconType.FLOWER_BLOOM -> PlantIcons.FlowerBloom
}

/**
 * Collection of 24x24 vector icons representing specific garden plant types.
 */
object PlantIcons {

  val Tomato: ImageVector by lazy {
    ImageVector.Builder(
      name = "PlantTomato",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      // Plump tomato body with glossy highlight cutout
      path(
        fill = SolidColor(Color.Black),
        pathFillType = PathFillType.EvenOdd,
      ) {
        moveTo(12f, 7.2f)
        curveTo(16.8f, 6.8f, 20.5f, 9.8f, 20.5f, 14.3f)
        curveTo(20.5f, 18.6f, 16.9f, 21.5f, 12f, 21.5f)
        curveTo(7.1f, 21.5f, 3.5f, 18.6f, 3.5f, 14.3f)
        curveTo(3.5f, 9.8f, 7.2f, 6.8f, 12f, 7.2f)
        close()
        // Highlight crescent
        moveTo(7.2f, 11.2f)
        curveTo(6.1f, 12.4f, 5.8f, 14.2f, 6.4f, 15.8f)
        curveTo(6.6f, 16.3f, 7.3f, 16.4f, 7.6f, 15.9f)
        curveTo(7.2f, 14.5f, 7.4f, 12.9f, 8.3f, 11.8f)
        curveTo(8.6f, 11.4f, 7.7f, 10.7f, 7.2f, 11.2f)
        close()
      }
      // Stem & 5-pointed star calyx
      path(
        fill = SolidColor(Color.Black),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.6f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(12f, 2.5f)
        lineTo(12f, 6.8f)
        moveTo(12f, 5.8f)
        lineTo(7.5f, 4.8f)
        moveTo(12f, 5.8f)
        lineTo(16.5f, 4.8f)
        moveTo(12f, 6.2f)
        lineTo(9.2f, 8.2f)
        moveTo(12f, 6.2f)
        lineTo(14.8f, 8.2f)
      }
    }.build()
  }

  val BellPepper: ImageVector by lazy {
    ImageVector.Builder(
      name = "PlantBellPepper",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      // Curved stem
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2.0f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(12f, 6.5f)
        curveTo(12f, 4.2f, 13.2f, 2.6f, 15.2f, 2.6f)
      }
      // Three-lobed bell pepper body with vertical groove cutouts
      path(
        fill = SolidColor(Color.Black),
        pathFillType = PathFillType.EvenOdd,
      ) {
        moveTo(8.2f, 6.2f)
        curveTo(9.5f, 5.9f, 10.8f, 6.2f, 12f, 6.6f)
        curveTo(13.2f, 6.2f, 14.5f, 5.9f, 15.8f, 6.2f)
        curveTo(18.6f, 6.6f, 20.2f, 8.8f, 19.8f, 12.2f)
        lineTo(19.0f, 18.2f)
        curveTo(18.7f, 20.1f, 17.0f, 21.3f, 15.2f, 20.8f)
        curveTo(14.2f, 21.4f, 13.1f, 21.6f, 12f, 21.6f)
        curveTo(10.9f, 21.6f, 9.8f, 21.4f, 8.8f, 20.8f)
        curveTo(7.0f, 21.3f, 5.3f, 20.1f, 5.0f, 18.2f)
        lineTo(4.2f, 12.2f)
        curveTo(3.8f, 8.8f, 5.4f, 6.6f, 8.2f, 6.2f)
        close()
        // Left lobe groove
        moveTo(8.6f, 8.5f)
        lineTo(9.8f, 8.5f)
        lineTo(10.1f, 19.0f)
        lineTo(8.9f, 19.0f)
        close()
        // Right lobe groove
        moveTo(14.2f, 8.5f)
        lineTo(15.4f, 8.5f)
        lineTo(15.1f, 19.0f)
        lineTo(13.9f, 19.0f)
        close()
      }
    }.build()
  }

  val ChiliPepper: ImageVector by lazy {
    ImageVector.Builder(
      name = "PlantChiliPepper",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      // Curved stem
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
      ) {
        moveTo(17.2f, 6.2f)
        curveTo(18.2f, 4.5f, 19.5f, 3.3f, 21.0f, 3.0f)
      }
      // Tapered curved chili pod
      path(
        fill = SolidColor(Color.Black),
        pathFillType = PathFillType.EvenOdd,
      ) {
        moveTo(14.0f, 5.8f)
        curveTo(16.5f, 5.0f, 19.2f, 6.8f, 19.2f, 9.6f)
        curveTo(19.2f, 14.8f, 13.5f, 20.8f, 4.5f, 20.8f)
        curveTo(3.3f, 20.8f, 2.8f, 19.5f, 3.8f, 18.8f)
        curveTo(9.8f, 16.5f, 12.2f, 12.2f, 12.6f, 8.2f)
        curveTo(12.7f, 6.9f, 13.2f, 6.1f, 14.0f, 5.8f)
        close()
        // Inner shine line
        moveTo(15.4f, 9.2f)
        curveTo(15.0f, 12.8f, 12.5f, 16.0f, 8.5f, 17.6f)
        curveTo(9.2f, 18.3f, 10.2f, 18.0f, 11.2f, 17.3f)
        curveTo(14.5f, 15.2f, 16.6f, 12.2f, 16.8f, 9.2f)
        close()
      }
    }.build()
  }

  val CucumberSquash: ImageVector by lazy {
    ImageVector.Builder(
      name = "PlantCucumberSquash",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      // Stem nub
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.8f,
        strokeLineCap = StrokeCap.Round,
      ) {
        moveTo(17.5f, 5.2f)
        lineTo(19.8f, 3.0f)
      }
      // Elongated curved squash/cucumber body with texture dots
      path(
        fill = SolidColor(Color.Black),
        pathFillType = PathFillType.EvenOdd,
      ) {
        moveTo(14.8f, 4.5f)
        curveTo(17.2f, 4.0f, 19.5f, 6.2f, 19.0f, 8.8f)
        curveTo(17.8f, 14.6f, 13.2f, 19.2f, 7.6f, 20.5f)
        curveTo(4.8f, 21.1f, 2.8f, 18.8f, 3.6f, 16.2f)
        curveTo(5.2f, 11.2f, 9.6f, 5.6f, 14.8f, 4.5f)
        close()
        // Texture dots
        moveTo(14.0f, 8.5f)
        arcToRelative(1.0f, 1.0f, 0f, true, true, 2.0f, 0f)
        arcToRelative(1.0f, 1.0f, 0f, true, true, -2.0f, 0f)
        close()
        moveTo(10.8f, 12.2f)
        arcToRelative(1.0f, 1.0f, 0f, true, true, 2.0f, 0f)
        arcToRelative(1.0f, 1.0f, 0f, true, true, -2.0f, 0f)
        close()
        moveTo(7.2f, 15.8f)
        arcToRelative(1.0f, 1.0f, 0f, true, true, 2.0f, 0f)
        arcToRelative(1.0f, 1.0f, 0f, true, true, -2.0f, 0f)
        close()
      }
    }.build()
  }

  val Pumpkin: ImageVector by lazy {
    ImageVector.Builder(
      name = "PlantPumpkin",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      // Woody curved stem
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2.0f,
        strokeLineCap = StrokeCap.Round,
      ) {
        moveTo(12f, 6.8f)
        curveTo(12f, 4.4f, 13.4f, 2.8f, 15.0f, 2.8f)
      }
      // Ribbed pumpkin gourd with curved rib cutouts
      path(
        fill = SolidColor(Color.Black),
        pathFillType = PathFillType.EvenOdd,
      ) {
        moveTo(12f, 6.8f)
        curveTo(17.8f, 6.4f, 21.5f, 9.6f, 21.5f, 14.0f)
        curveTo(21.5f, 18.4f, 17.8f, 21.2f, 12f, 20.8f)
        curveTo(6.2f, 21.2f, 2.5f, 18.4f, 2.5f, 14.0f)
        curveTo(2.5f, 9.6f, 6.2f, 6.4f, 12f, 6.8f)
        close()
        // Left rib groove
        moveTo(9.2f, 8.2f)
        curveTo(7.4f, 11.5f, 7.4f, 16.5f, 9.2f, 19.5f)
        lineTo(10.4f, 19.5f)
        curveTo(8.8f, 16.5f, 8.8f, 11.5f, 10.4f, 8.2f)
        close()
        // Right rib groove
        moveTo(13.6f, 8.2f)
        curveTo(15.2f, 11.5f, 15.2f, 16.5f, 13.6f, 19.5f)
        lineTo(14.8f, 19.5f)
        curveTo(16.6f, 16.5f, 16.6f, 11.5f, 14.8f, 8.2f)
        close()
      }
    }.build()
  }

  val PeaPod: ImageVector by lazy {
    ImageVector.Builder(
      name = "PlantPeaPod",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      // Curved pod with 3 round peas inside
      path(
        fill = SolidColor(Color.Black),
        pathFillType = PathFillType.EvenOdd,
      ) {
        moveTo(19.8f, 4.2f)
        curveTo(20.6f, 9.8f, 18.2f, 15.4f, 13.8f, 18.4f)
        curveTo(10.2f, 20.8f, 6.2f, 21.0f, 3.5f, 19.8f)
        curveTo(4.6f, 13.8f, 8.8f, 8.4f, 14.5f, 5.4f)
        curveTo(16.4f, 4.4f, 18.2f, 4.0f, 19.8f, 4.2f)
        close()
        // Pea 1
        moveTo(7.2f, 15.4f)
        arcToRelative(2.1f, 2.1f, 0f, true, true, 4.2f, 0f)
        arcToRelative(2.1f, 2.1f, 0f, true, true, -4.2f, 0f)
        close()
        // Pea 2
        moveTo(10.5f, 12.1f)
        arcToRelative(2.1f, 2.1f, 0f, true, true, 4.2f, 0f)
        arcToRelative(2.1f, 2.1f, 0f, true, true, -4.2f, 0f)
        close()
        // Pea 3
        moveTo(13.8f, 8.8f)
        arcToRelative(2.1f, 2.1f, 0f, true, true, 4.2f, 0f)
        arcToRelative(2.1f, 2.1f, 0f, true, true, -4.2f, 0f)
        close()
      }
    }.build()
  }

  val Corn: ImageVector by lazy {
    ImageVector.Builder(
      name = "PlantCorn",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      // Central cob with kernel cutouts
      path(
        fill = SolidColor(Color.Black),
        pathFillType = PathFillType.EvenOdd,
      ) {
        moveTo(12f, 2.5f)
        curveTo(14.6f, 2.5f, 16.0f, 5.2f, 16.0f, 9.8f)
        lineTo(15.5f, 16.5f)
        lineTo(8.5f, 16.5f)
        lineTo(8.0f, 9.8f)
        curveTo(8.0f, 5.2f, 9.4f, 2.5f, 12f, 2.5f)
        close()
        // Kernel grid lines
        moveTo(11.4f, 4.8f)
        lineTo(12.6f, 4.8f)
        lineTo(12.6f, 14.5f)
        lineTo(11.4f, 14.5f)
        close()
        moveTo(9.4f, 7.2f)
        lineTo(14.6f, 7.2f)
        lineTo(14.6f, 8.3f)
        lineTo(9.4f, 8.3f)
        close()
        moveTo(9.4f, 10.6f)
        lineTo(14.6f, 10.6f)
        lineTo(14.6f, 11.7f)
        lineTo(9.4f, 11.7f)
        close()
      }
      // Peeled husk leaves on left and right
      path(fill = SolidColor(Color.Black)) {
        moveTo(12f, 21.5f)
        curveTo(7.5f, 21.5f, 4.2f, 17.0f, 4.2f, 11.5f)
        curveTo(6.8f, 12.2f, 9.2f, 15.2f, 11.2f, 18.2f)
        lineTo(12f, 17.2f)
        lineTo(12.8f, 18.2f)
        curveTo(14.8f, 15.2f, 17.2f, 12.2f, 19.8f, 11.5f)
        curveTo(19.8f, 17.0f, 16.5f, 21.5f, 12f, 21.5f)
        close()
      }
    }.build()
  }

  val Eggplant: ImageVector by lazy {
    ImageVector.Builder(
      name = "PlantEggplant",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      // Stem & calyx cap at top-right
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.8f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(16.8f, 5.5f)
        lineTo(19.5f, 2.8f)
        moveTo(13.2f, 6.0f)
        lineTo(16.8f, 5.5f)
        lineTo(17.5f, 9.2f)
      }
      // Plump teardrop eggplant body
      path(
        fill = SolidColor(Color.Black),
        pathFillType = PathFillType.EvenOdd,
      ) {
        moveTo(14.2f, 6.2f)
        curveTo(16.2f, 6.0f, 17.6f, 7.5f, 17.4f, 9.6f)
        curveTo(17.1f, 13.8f, 14.5f, 20.8f, 9.0f, 20.8f)
        curveTo(5.2f, 20.8f, 3.5f, 17.8f, 4.4f, 14.2f)
        curveTo(5.4f, 10.6f, 9.8f, 8.8f, 12.6f, 6.8f)
        curveTo(13.2f, 6.4f, 13.7f, 6.2f, 14.2f, 6.2f)
        close()
        // Gloss highlight
        moveTo(7.2f, 13.6f)
        curveTo(6.4f, 15.2f, 6.6f, 16.8f, 7.8f, 17.8f)
        curveTo(8.3f, 18.2f, 9.0f, 17.5f, 8.5f, 17.0f)
        curveTo(7.8f, 16.2f, 7.6f, 15.0f, 8.3f, 14.0f)
        close()
      }
    }.build()
  }

  val Broccoli: ImageVector by lazy {
    ImageVector.Builder(
      name = "PlantBroccoli",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      // Fluffy floret crown
      path(fill = SolidColor(Color.Black)) {
        moveTo(7.2f, 13.5f)
        curveTo(4.8f, 13.5f, 3.2f, 11.8f, 3.2f, 9.6f)
        curveTo(3.2f, 7.8f, 4.4f, 6.3f, 6.1f, 5.9f)
        curveTo(6.8f, 4.0f, 9.2f, 2.8f, 12.0f, 2.8f)
        curveTo(14.8f, 2.8f, 17.2f, 4.0f, 17.9f, 5.9f)
        curveTo(19.6f, 6.3f, 20.8f, 7.8f, 20.8f, 9.6f)
        curveTo(20.8f, 11.8f, 19.2f, 13.5f, 16.8f, 13.5f)
        close()
      }
      // Branching stalk
      path(
        fill = SolidColor(Color.Black),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2.0f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(10.2f, 13.2f)
        lineTo(10.2f, 20.5f)
        lineTo(13.8f, 20.5f)
        lineTo(13.8f, 13.2f)
        close()
        moveTo(10.5f, 15.8f)
        lineTo(7.5f, 12.8f)
        moveTo(13.5f, 15.8f)
        lineTo(16.5f, 12.8f)
      }
    }.build()
  }

  val Cabbage: ImageVector by lazy {
    ImageVector.Builder(
      name = "PlantCabbage",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      // Round layered brassica head with wrapping leaf seams
      path(
        fill = SolidColor(Color.Black),
        pathFillType = PathFillType.EvenOdd,
      ) {
        moveTo(12f, 3.5f)
        curveTo(17.2f, 3.5f, 21.0f, 7.3f, 21.0f, 12.2f)
        curveTo(21.0f, 17.1f, 17.2f, 20.8f, 12f, 20.8f)
        curveTo(6.8f, 20.8f, 3.0f, 17.1f, 3.0f, 12.2f)
        curveTo(3.0f, 7.3f, 6.8f, 3.5f, 12f, 3.5f)
        close()
        // Overlapping leaf wrap seams
        moveTo(7.2f, 6.5f)
        curveTo(10.2f, 9.5f, 11.5f, 13.8f, 11.2f, 18.8f)
        lineTo(12.6f, 18.8f)
        curveTo(12.8f, 13.5f, 11.4f, 8.8f, 8.2f, 5.6f)
        close()
        moveTo(16.8f, 6.8f)
        curveTo(14.5f, 9.2f, 13.0f, 12.0f, 12.4f, 15.2f)
        lineTo(13.7f, 15.5f)
        curveTo(14.3f, 12.5f, 15.6f, 9.8f, 17.8f, 7.6f)
        close()
      }
    }.build()
  }

  val Stalk: ImageVector by lazy {
    ImageVector.Builder(
      name = "PlantStalk",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      // Three upright spears/stalks with pointed tips and tie band
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2.2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        // Left stalk
        moveTo(7.2f, 20.5f)
        lineTo(7.2f, 5.5f)
        moveTo(5.6f, 7.5f)
        lineTo(7.2f, 4.5f)
        lineTo(8.8f, 7.5f)
        // Center stalk
        moveTo(12.0f, 20.5f)
        lineTo(12.0f, 3.5f)
        moveTo(10.2f, 6.0f)
        lineTo(12.0f, 3.0f)
        lineTo(13.8f, 6.0f)
        // Right stalk
        moveTo(16.8f, 20.5f)
        lineTo(16.8f, 5.5f)
        moveTo(15.2f, 7.5f)
        lineTo(16.8f, 4.5f)
        lineTo(18.4f, 7.5f)
        // Bundle tie band
        moveTo(5.2f, 15.5f)
        lineTo(18.8f, 15.5f)
      }
    }.build()
  }

  val ArtichokeOkra: ImageVector by lazy {
    ImageVector.Builder(
      name = "PlantArtichokeOkra",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      // Tiered artichoke bud scales + stem
      path(
        fill = SolidColor(Color.Black),
        pathFillType = PathFillType.EvenOdd,
      ) {
        moveTo(12f, 2.8f)
        lineTo(15.6f, 7.2f)
        lineTo(19.2f, 9.2f)
        curveTo(19.5f, 14.5f, 16.5f, 18.2f, 13.4f, 18.6f)
        lineTo(13.4f, 21.5f)
        lineTo(10.6f, 21.5f)
        lineTo(10.6f, 18.6f)
        curveTo(7.5f, 18.2f, 4.5f, 14.5f, 4.8f, 9.2f)
        lineTo(8.4f, 7.2f)
        close()
        // Inner scale cutouts
        moveTo(12f, 7.5f)
        lineTo(9.2f, 11.2f)
        lineTo(12f, 12.8f)
        lineTo(14.8f, 11.2f)
        close()
        moveTo(8.5f, 13.5f)
        lineTo(12f, 15.8f)
        lineTo(15.5f, 13.5f)
        lineTo(15.5f, 14.8f)
        lineTo(12f, 17.2f)
        lineTo(8.5f, 14.8f)
        close()
      }
    }.build()
  }

  val LettuceHead: ImageVector by lazy {
    ImageVector.Builder(
      name = "PlantLettuceHead",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      // Scalloped rosette of leafy greens tapering to a crisp base
      path(
        fill = SolidColor(Color.Black),
        pathFillType = PathFillType.EvenOdd,
      ) {
        moveTo(8.5f, 20.8f)
        curveTo(5.2f, 18.2f, 3.2f, 14.5f, 3.5f, 10.5f)
        curveTo(3.6f, 8.2f, 5.2f, 6.8f, 7.2f, 7.0f)
        curveTo(8.2f, 4.6f, 10.0f, 3.5f, 12.0f, 3.5f)
        curveTo(14.0f, 3.5f, 15.8f, 4.6f, 16.8f, 7.0f)
        curveTo(18.8f, 6.8f, 20.4f, 8.2f, 20.5f, 10.5f)
        curveTo(20.8f, 14.5f, 18.8f, 18.2f, 15.5f, 20.8f)
        close()
        // Midrib and side veins
        moveTo(11.3f, 8.0f)
        lineTo(12.7f, 8.0f)
        lineTo(12.7f, 19.0f)
        lineTo(11.3f, 19.0f)
        close()
        moveTo(8.0f, 11.2f)
        lineTo(12.0f, 14.2f)
        lineTo(16.0f, 11.2f)
        lineTo(16.8f, 12.3f)
        lineTo(12.0f, 15.8f)
        lineTo(7.2f, 12.3f)
        close()
      }
    }.build()
  }

  val LeafyGreen: ImageVector by lazy {
    ImageVector.Builder(
      name = "PlantLeafyGreen",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      // Broad spinach/kale leaf with midrib and veins
      path(
        fill = SolidColor(Color.Black),
        pathFillType = PathFillType.EvenOdd,
      ) {
        moveTo(19.5f, 4.5f)
        curveTo(19.5f, 12.8f, 15.2f, 18.5f, 7.5f, 18.5f)
        lineTo(4.5f, 21.5f)
        lineTo(3.2f, 20.2f)
        lineTo(6.2f, 17.2f)
        curveTo(5.8f, 9.5f, 11.2f, 4.5f, 19.5f, 4.5f)
        close()
        // Diagonal leaf vein cutout
        moveTo(7.8f, 15.2f)
        lineTo(15.8f, 7.2f)
        lineTo(16.8f, 8.2f)
        lineTo(8.8f, 16.2f)
        close()
      }
    }.build()
  }

  val CarrotRoot: ImageVector by lazy {
    ImageVector.Builder(
      name = "PlantCarrotRoot",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      // Leafy top tuft
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.8f,
        strokeLineCap = StrokeCap.Round,
      ) {
        moveTo(12f, 7.5f)
        lineTo(12f, 2.5f)
        moveTo(12f, 7.2f)
        lineTo(8.5f, 3.5f)
        moveTo(12f, 7.2f)
        lineTo(15.5f, 3.5f)
      }
      // Conical tapered carrot root with horizontal root creases
      path(
        fill = SolidColor(Color.Black),
        pathFillType = PathFillType.EvenOdd,
      ) {
        moveTo(8.2f, 7.5f)
        lineTo(15.8f, 7.5f)
        curveTo(16.8f, 7.5f, 17.4f, 8.5f, 17.0f, 9.6f)
        lineTo(13.0f, 21.0f)
        curveTo(12.6f, 22.0f, 11.4f, 22.0f, 11.0f, 21.0f)
        lineTo(7.0f, 9.6f)
        curveTo(6.6f, 8.5f, 7.2f, 7.5f, 8.2f, 7.5f)
        close()
        // Root creases
        moveTo(8.0f, 10.6f)
        lineTo(11.8f, 10.6f)
        lineTo(11.8f, 11.8f)
        lineTo(8.0f, 11.8f)
        close()
        moveTo(12.2f, 13.8f)
        lineTo(15.2f, 13.8f)
        lineTo(15.2f, 15.0f)
        lineTo(12.2f, 15.0f)
        close()
      }
    }.build()
  }

  val RoundRoot: ImageVector by lazy {
    ImageVector.Builder(
      name = "PlantRoundRoot",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      // Leafy stems on top
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.8f,
        strokeLineCap = StrokeCap.Round,
      ) {
        moveTo(12f, 7.8f)
        lineTo(12f, 2.5f)
        moveTo(12f, 7.5f)
        lineTo(8.2f, 3.8f)
        moveTo(12f, 7.5f)
        lineTo(15.8f, 3.8f)
      }
      // Round radish/beet/turnip bulb tapering to a taproot tail
      path(
        fill = SolidColor(Color.Black),
        pathFillType = PathFillType.EvenOdd,
      ) {
        moveTo(12f, 7.5f)
        curveTo(15.6f, 7.5f, 18.2f, 10.0f, 18.2f, 13.2f)
        curveTo(18.2f, 16.2f, 15.2f, 18.2f, 13.0f, 19.5f)
        lineTo(12.0f, 22.0f)
        lineTo(11.0f, 19.5f)
        curveTo(8.8f, 18.2f, 5.8f, 16.2f, 5.8f, 13.2f)
        curveTo(5.8f, 10.0f, 8.4f, 7.5f, 12f, 7.5f)
        close()
        // Highlight arc
        moveTo(8.4f, 11.2f)
        curveTo(7.6f, 12.4f, 7.6f, 14.0f, 8.5f, 15.2f)
        lineTo(9.6f, 14.5f)
        curveTo(9.0f, 13.6f, 9.0f, 12.5f, 9.5f, 11.8f)
        close()
      }
    }.build()
  }

  val PotatoTuber: ImageVector by lazy {
    ImageVector.Builder(
      name = "PlantPotatoTuber",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      // Organic potato tuber silhouette with eye dimples
      path(
        fill = SolidColor(Color.Black),
        pathFillType = PathFillType.EvenOdd,
      ) {
        moveTo(15.2f, 5.2f)
        curveTo(18.8f, 5.2f, 21.0f, 7.8f, 20.8f, 11.2f)
        curveTo(20.6f, 15.2f, 17.2f, 19.2f, 11.2f, 19.5f)
        curveTo(6.2f, 19.8f, 3.2f, 17.2f, 3.2f, 13.5f)
        curveTo(3.2f, 9.5f, 6.5f, 7.2f, 10.2f, 6.2f)
        curveTo(12.0f, 5.6f, 13.6f, 5.2f, 15.2f, 5.2f)
        close()
        // Tuber eyes
        moveTo(8.2f, 11.5f)
        arcToRelative(1.1f, 1.1f, 0f, true, true, 2.2f, 0f)
        arcToRelative(1.1f, 1.1f, 0f, true, true, -2.2f, 0f)
        close()
        moveTo(14.2f, 9.5f)
        arcToRelative(1.1f, 1.1f, 0f, true, true, 2.2f, 0f)
        arcToRelative(1.1f, 1.1f, 0f, true, true, -2.2f, 0f)
        close()
        moveTo(11.8f, 14.8f)
        arcToRelative(1.1f, 1.1f, 0f, true, true, 2.2f, 0f)
        arcToRelative(1.1f, 1.1f, 0f, true, true, -2.2f, 0f)
        close()
      }
    }.build()
  }

  val AlliumBulb: ImageVector by lazy {
    ImageVector.Builder(
      name = "PlantAlliumBulb",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      // Onion / garlic bulb with pointed neck, root whiskers, and clove ribs
      path(
        fill = SolidColor(Color.Black),
        pathFillType = PathFillType.EvenOdd,
      ) {
        moveTo(12f, 2.5f)
        curveTo(13.2f, 5.5f, 14.5f, 7.5f, 16.8f, 9.5f)
        curveTo(19.0f, 11.4f, 19.8f, 13.6f, 19.5f, 16.0f)
        curveTo(19.0f, 18.8f, 16.2f, 20.2f, 12f, 20.2f)
        curveTo(7.8f, 20.2f, 5.0f, 18.8f, 4.5f, 16.0f)
        curveTo(4.2f, 13.6f, 5.0f, 11.4f, 7.2f, 9.5f)
        curveTo(9.5f, 7.5f, 10.8f, 5.5f, 12f, 2.5f)
        close()
        // Vertical tunic ribs
        moveTo(9.8f, 9.5f)
        curveTo(8.4f, 12.2f, 8.4f, 15.8f, 9.8f, 18.5f)
        lineTo(10.9f, 18.5f)
        curveTo(9.6f, 15.8f, 9.6f, 12.2f, 10.9f, 9.5f)
        close()
        moveTo(13.1f, 9.5f)
        curveTo(14.4f, 12.2f, 14.4f, 15.8f, 13.1f, 18.5f)
        lineTo(14.2f, 18.5f)
        curveTo(15.6f, 15.8f, 15.6f, 12.2f, 14.2f, 9.5f)
        close()
      }
      // Root whiskers at base
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Round,
      ) {
        moveTo(10f, 20.0f)
        lineTo(9.2f, 21.8f)
        moveTo(12f, 20.0f)
        lineTo(12f, 22.0f)
        moveTo(14f, 20.0f)
        lineTo(14.8f, 21.8f)
      }
    }.build()
  }

  val ScallionLeek: ImageVector by lazy {
    ImageVector.Builder(
      name = "PlantScallionLeek",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      // White shaft at bottom + fanning green leaf blades at top
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2.2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        // Lower stalk
        moveTo(10.8f, 19.8f)
        lineTo(10.8f, 12.0f)
        moveTo(13.2f, 19.8f)
        lineTo(13.2f, 12.0f)
        // Fanning leaf blades
        moveTo(10.8f, 12.0f)
        curveTo(10.0f, 8.2f, 8.2f, 5.2f, 5.8f, 3.2f)
        moveTo(12.0f, 12.0f)
        lineTo(12.0f, 2.8f)
        moveTo(13.2f, 12.0f)
        curveTo(14.0f, 8.2f, 15.8f, 5.2f, 18.2f, 3.2f)
      }
      // Root whiskers
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.4f,
        strokeLineCap = StrokeCap.Round,
      ) {
        moveTo(10.2f, 20.0f)
        lineTo(9.4f, 21.8f)
        moveTo(12.0f, 20.0f)
        lineTo(12.0f, 22.0f)
        moveTo(13.8f, 20.0f)
        lineTo(14.6f, 21.8f)
      }
    }.build()
  }

  val HerbLeaf: ImageVector by lazy {
    ImageVector.Builder(
      name = "PlantHerbLeaf",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      // Central herb stem
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.8f,
        strokeLineCap = StrokeCap.Round,
      ) {
        moveTo(12f, 21.2f)
        lineTo(12f, 6.5f)
      }
      // Terminal leaf + 2 opposite pairs of aromatic basil/mint leaves
      path(fill = SolidColor(Color.Black)) {
        // Top terminal leaf
        moveTo(12f, 2.2f)
        curveTo(14.2f, 4.0f, 14.5f, 6.8f, 12f, 8.5f)
        curveTo(9.5f, 6.8f, 9.8f, 4.0f, 12f, 2.2f)
        close()
        // Upper-left leaf
        moveTo(11.5f, 11.2f)
        curveTo(8.2f, 11.2f, 5.2f, 9.2f, 4.5f, 6.2f)
        curveTo(7.5f, 6.0f, 10.5f, 7.8f, 11.5f, 11.2f)
        close()
        // Upper-right leaf
        moveTo(12.5f, 11.2f)
        curveTo(15.8f, 11.2f, 18.8f, 9.2f, 19.5f, 6.2f)
        curveTo(16.5f, 6.0f, 13.5f, 7.8f, 12.5f, 11.2f)
        close()
        // Lower-left leaf
        moveTo(11.5f, 16.8f)
        curveTo(7.8f, 16.8f, 4.6f, 14.6f, 3.8f, 11.5f)
        curveTo(7.2f, 11.2f, 10.4f, 13.2f, 11.5f, 16.8f)
        close()
        // Lower-right leaf
        moveTo(12.5f, 16.8f)
        curveTo(16.2f, 16.8f, 19.4f, 14.6f, 20.2f, 11.5f)
        curveTo(16.8f, 11.2f, 13.6f, 13.2f, 12.5f, 16.8f)
        close()
      }
    }.build()
  }

  val HerbNeedle: ImageVector by lazy {
    ImageVector.Builder(
      name = "PlantHerbNeedle",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      // Woody rosemary/thyme/dill sprig with angled needle pairs
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(12f, 21.5f)
        lineTo(12f, 3.0f)
        // Needle pairs
        moveTo(12f, 7.5f)
        lineTo(7.5f, 4.2f)
        moveTo(12f, 7.5f)
        lineTo(16.5f, 4.2f)

        moveTo(12f, 11.5f)
        lineTo(6.8f, 8.0f)
        moveTo(12f, 11.5f)
        lineTo(17.2f, 8.0f)

        moveTo(12f, 15.5f)
        lineTo(6.8f, 12.0f)
        moveTo(12f, 15.5f)
        lineTo(17.2f, 12.0f)

        moveTo(12f, 19.0f)
        lineTo(7.8f, 16.0f)
        moveTo(12f, 19.0f)
        lineTo(16.2f, 16.0f)
      }
    }.build()
  }

  val Strawberry: ImageVector by lazy {
    ImageVector.Builder(
      name = "PlantStrawberry",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      // Stem & leafy hull crown
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.7f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(12f, 2.5f)
        lineTo(12f, 6.2f)
        moveTo(7.5f, 5.2f)
        lineTo(12f, 6.5f)
        lineTo(16.5f, 5.2f)
      }
      // Heart-shaped strawberry body with seed cutouts
      path(
        fill = SolidColor(Color.Black),
        pathFillType = PathFillType.EvenOdd,
      ) {
        moveTo(12f, 6.8f)
        curveTo(16.8f, 6.2f, 19.5f, 8.8f, 19.0f, 12.5f)
        curveTo(18.4f, 16.5f, 14.8f, 21.2f, 12.0f, 21.5f)
        curveTo(9.2f, 21.2f, 5.6f, 16.5f, 5.0f, 12.5f)
        curveTo(4.5f, 8.8f, 7.2f, 6.2f, 12f, 6.8f)
        close()
        // Seeds
        moveTo(9.0f, 10.5f)
        arcToRelative(0.9f, 0.9f, 0f, true, true, 1.8f, 0f)
        arcToRelative(0.9f, 0.9f, 0f, true, true, -1.8f, 0f)
        close()
        moveTo(13.2f, 10.5f)
        arcToRelative(0.9f, 0.9f, 0f, true, true, 1.8f, 0f)
        arcToRelative(0.9f, 0.9f, 0f, true, true, -1.8f, 0f)
        close()
        moveTo(11.1f, 13.8f)
        arcToRelative(0.9f, 0.9f, 0f, true, true, 1.8f, 0f)
        arcToRelative(0.9f, 0.9f, 0f, true, true, -1.8f, 0f)
        close()
        moveTo(8.2f, 14.8f)
        arcToRelative(0.9f, 0.9f, 0f, true, true, 1.8f, 0f)
        arcToRelative(0.9f, 0.9f, 0f, true, true, -1.8f, 0f)
        close()
        moveTo(14.0f, 14.8f)
        arcToRelative(0.9f, 0.9f, 0f, true, true, 1.8f, 0f)
        arcToRelative(0.9f, 0.9f, 0f, true, true, -1.8f, 0f)
        close()
        moveTo(11.1f, 17.5f)
        arcToRelative(0.9f, 0.9f, 0f, true, true, 1.8f, 0f)
        arcToRelative(0.9f, 0.9f, 0f, true, true, -1.8f, 0f)
        close()
      }
    }.build()
  }

  val Melon: ImageVector by lazy {
    ImageVector.Builder(
      name = "PlantMelon",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      // Watermelon / melon slice wedge with rind arc and seeds
      path(
        fill = SolidColor(Color.Black),
        pathFillType = PathFillType.EvenOdd,
      ) {
        moveTo(3.0f, 9.5f)
        lineTo(21.0f, 9.5f)
        curveTo(21.0f, 15.8f, 16.8f, 20.2f, 12.0f, 20.2f)
        curveTo(7.2f, 20.2f, 3.0f, 15.8f, 3.0f, 9.5f)
        close()
        // Rind separator arc
        moveTo(5.4f, 15.5f)
        curveTo(7.2f, 17.4f, 9.5f, 18.4f, 12.0f, 18.4f)
        curveTo(14.5f, 18.4f, 16.8f, 17.4f, 18.6f, 15.5f)
        lineTo(17.6f, 14.6f)
        curveTo(16.1f, 16.2f, 14.1f, 17.1f, 12.0f, 17.1f)
        curveTo(9.9f, 17.1f, 7.9f, 16.2f, 6.4f, 14.6f)
        close()
        // Seeds
        moveTo(8.5f, 12.2f)
        arcToRelative(0.9f, 0.9f, 0f, true, true, 1.8f, 0f)
        arcToRelative(0.9f, 0.9f, 0f, true, true, -1.8f, 0f)
        close()
        moveTo(11.1f, 13.8f)
        arcToRelative(0.9f, 0.9f, 0f, true, true, 1.8f, 0f)
        arcToRelative(0.9f, 0.9f, 0f, true, true, -1.8f, 0f)
        close()
        moveTo(13.7f, 12.2f)
        arcToRelative(0.9f, 0.9f, 0f, true, true, 1.8f, 0f)
        arcToRelative(0.9f, 0.9f, 0f, true, true, -1.8f, 0f)
        close()
      }
    }.build()
  }

  val Berry: ImageVector by lazy {
    ImageVector.Builder(
      name = "PlantBerry",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      // Stem & leaf at top
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.7f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
      ) {
        moveTo(12f, 6.5f)
        lineTo(12f, 2.8f)
        moveTo(12f, 4.8f)
        lineTo(16.2f, 3.2f)
        moveTo(12f, 4.8f)
        lineTo(7.8f, 3.2f)
      }
      // Three clustered berries with crown dimples
      path(
        fill = SolidColor(Color.Black),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // Top berry
        moveTo(8.2f, 10.2f)
        arcToRelative(3.8f, 3.8f, 0f, true, true, 7.6f, 0f)
        arcToRelative(3.8f, 3.8f, 0f, true, true, -7.6f, 0f)
        close()
        moveTo(11.0f, 10.2f)
        arcToRelative(1.0f, 1.0f, 0f, true, true, 2.0f, 0f)
        arcToRelative(1.0f, 1.0f, 0f, true, true, -2.0f, 0f)
        close()
        // Bottom-left berry
        moveTo(4.2f, 16.5f)
        arcToRelative(4.0f, 4.0f, 0f, true, true, 8.0f, 0f)
        arcToRelative(4.0f, 4.0f, 0f, true, true, -8.0f, 0f)
        close()
        moveTo(7.2f, 16.5f)
        arcToRelative(1.0f, 1.0f, 0f, true, true, 2.0f, 0f)
        arcToRelative(1.0f, 1.0f, 0f, true, true, -2.0f, 0f)
        close()
        // Bottom-right berry
        moveTo(11.8f, 16.5f)
        arcToRelative(4.0f, 4.0f, 0f, true, true, 8.0f, 0f)
        arcToRelative(4.0f, 4.0f, 0f, true, true, -8.0f, 0f)
        close()
        moveTo(14.8f, 16.5f)
        arcToRelative(1.0f, 1.0f, 0f, true, true, 2.0f, 0f)
        arcToRelative(1.0f, 1.0f, 0f, true, true, -2.0f, 0f)
        close()
      }
    }.build()
  }

  val Grape: ImageVector by lazy {
    ImageVector.Builder(
      name = "PlantGrape",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      // Vine stem and tendril
      path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.7f,
        strokeLineCap = StrokeCap.Round,
      ) {
        moveTo(12f, 6.2f)
        lineTo(12f, 2.5f)
        moveTo(12f, 4.2f)
        curveTo(14.5f, 3.8f, 16.5f, 2.8f, 17.5f, 4.5f)
      }
      // Pyramid bunch of 6 grapes (3-2-1)
      path(fill = SolidColor(Color.Black)) {
        // Top row of 3
        moveTo(4.8f, 9.2f)
        arcToRelative(2.5f, 2.5f, 0f, true, true, 5.0f, 0f)
        arcToRelative(2.5f, 2.5f, 0f, true, true, -5.0f, 0f)
        close()
        moveTo(9.5f, 9.2f)
        arcToRelative(2.5f, 2.5f, 0f, true, true, 5.0f, 0f)
        arcToRelative(2.5f, 2.5f, 0f, true, true, -5.0f, 0f)
        close()
        moveTo(14.2f, 9.2f)
        arcToRelative(2.5f, 2.5f, 0f, true, true, 5.0f, 0f)
        arcToRelative(2.5f, 2.5f, 0f, true, true, -5.0f, 0f)
        close()
        // Middle row of 2
        moveTo(7.1f, 14.0f)
        arcToRelative(2.5f, 2.5f, 0f, true, true, 5.0f, 0f)
        arcToRelative(2.5f, 2.5f, 0f, true, true, -5.0f, 0f)
        close()
        moveTo(11.9f, 14.0f)
        arcToRelative(2.5f, 2.5f, 0f, true, true, 5.0f, 0f)
        arcToRelative(2.5f, 2.5f, 0f, true, true, -5.0f, 0f)
        close()
        // Bottom grape
        moveTo(9.5f, 18.8f)
        arcToRelative(2.5f, 2.5f, 0f, true, true, 5.0f, 0f)
        arcToRelative(2.5f, 2.5f, 0f, true, true, -5.0f, 0f)
        close()
      }
    }.build()
  }

  val FlowerBloom: ImageVector by lazy {
    ImageVector.Builder(
      name = "PlantFlowerBloom",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      // 8-petal flower blossom with center seed disc cutout
      path(
        fill = SolidColor(Color.Black),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // Top & bottom petals
        moveTo(9.2f, 5.5f)
        arcToRelative(2.8f, 2.8f, 0f, true, true, 5.6f, 0f)
        arcToRelative(2.8f, 2.8f, 0f, true, true, -5.6f, 0f)
        close()
        moveTo(9.2f, 18.5f)
        arcToRelative(2.8f, 2.8f, 0f, true, true, 5.6f, 0f)
        arcToRelative(2.8f, 2.8f, 0f, true, true, -5.6f, 0f)
        close()
        // Left & right petals
        moveTo(2.7f, 12.0f)
        arcToRelative(2.8f, 2.8f, 0f, true, true, 5.6f, 0f)
        arcToRelative(2.8f, 2.8f, 0f, true, true, -5.6f, 0f)
        close()
        moveTo(15.7f, 12.0f)
        arcToRelative(2.8f, 2.8f, 0f, true, true, 5.6f, 0f)
        arcToRelative(2.8f, 2.8f, 0f, true, true, -5.6f, 0f)
        close()
        // 4 Diagonal petals
        moveTo(4.6f, 7.4f)
        arcToRelative(2.8f, 2.8f, 0f, true, true, 5.6f, 0f)
        arcToRelative(2.8f, 2.8f, 0f, true, true, -5.6f, 0f)
        close()
        moveTo(13.8f, 7.4f)
        arcToRelative(2.8f, 2.8f, 0f, true, true, 5.6f, 0f)
        arcToRelative(2.8f, 2.8f, 0f, true, true, -5.6f, 0f)
        close()
        moveTo(4.6f, 16.6f)
        arcToRelative(2.8f, 2.8f, 0f, true, true, 5.6f, 0f)
        arcToRelative(2.8f, 2.8f, 0f, true, true, -5.6f, 0f)
        close()
        moveTo(13.8f, 16.6f)
        arcToRelative(2.8f, 2.8f, 0f, true, true, 5.6f, 0f)
        arcToRelative(2.8f, 2.8f, 0f, true, true, -5.6f, 0f)
        close()
        // Center seed disc ring
        moveTo(8.5f, 12.0f)
        arcToRelative(3.5f, 3.5f, 0f, true, true, 7.0f, 0f)
        arcToRelative(3.5f, 3.5f, 0f, true, true, -7.0f, 0f)
        close()
        moveTo(10.2f, 12.0f)
        arcToRelative(1.8f, 1.8f, 0f, true, true, 3.6f, 0f)
        arcToRelative(1.8f, 1.8f, 0f, true, true, -3.6f, 0f)
        close()
      }
    }.build()
  }
}
