package numeriko.sekmentation.fuzzyregiongrowing

import numeriko.sekmentation.fuzzyregiongrowing.legacy.loadImage
import org.openrndr.application
import org.openrndr.configuration

fun main() = runFuzzyRegionGrowingDemo()

fun runFuzzyRegionGrowingDemo() {

    val image = loadImage("data/P1_Image_originale.png").equalized()

    application(
        configuration = configuration {
            title = "Fuzzy Region Growing"
            width = 1000
            height = 800
            windowResizable = true
        },
        program = RegionGrowing3DProgram(
            FuzzyConnectednessAlgorithm(
                image = image,
                seed = Point(image.shape0 / 2 - 20, image.shape1 / 2 + 50)
            ),
            verticalFactor = 30.0
        )
    )

}



