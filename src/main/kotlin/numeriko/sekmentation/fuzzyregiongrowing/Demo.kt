package numeriko.sekmentation.fuzzyregiongrowing

import numeriko.sekmentation.Resources
import org.openrndr.application
import org.openrndr.configuration
import kotlin.math.roundToInt

fun main() = runFuzzyRegionGrowingDemo()

fun runFuzzyRegionGrowingDemo() {

    val image = Resources.image("P1_Image_originale.png").equalized()

    application(
        configuration = configuration {
            title = "Fuzzy Region Growing"
            width = 1000
            height = 800
            windowResizable = true
        },
        program = RegionGrowing3DProgram(
            FuzzyConnectedness(
                image = image,
                seed = Point((0.6 * image.shape0).roundToInt(), (0.4 * image.shape1).roundToInt())
            ),
            verticalFactor = 30.0
        )
    )

}



