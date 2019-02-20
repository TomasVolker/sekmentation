package numeriko.sekmentation.fuzzyregiongrowing

import org.openrndr.application
import org.openrndr.configuration
import tomasvolker.numeriko.core.interfaces.array2d.double.elementWise
import kotlin.math.hypot

fun main() {

    val image = loadImage("data/P1_Image_originale.png").equalized()

    application(
        configuration = configuration {
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



