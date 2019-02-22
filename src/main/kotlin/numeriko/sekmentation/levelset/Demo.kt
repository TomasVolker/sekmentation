package numeriko.sekmentation.levelset

import numeriko.sekmentation.Resources
import numeriko.sekmentation.io.loadImage
import org.openrndr.application
import org.openrndr.configuration
import kotlin.math.hypot

fun main() = runLevelSetDemo()

fun runLevelSetDemo() {

    val image = Resources.image("P1_Image_originale.png")

    application(
        configuration = configuration {
            title = "Level Set"
            width = 1000
            height = 800
            windowResizable = true
        },
        program = LevelSet3DProgram(
            SimpleLevelSet(
                image = image,
                deltaT = 1e-1,
                speed = 0.5,
                initializer = { x, y ->
                    hypot(x - image.shape0 / 2.0 - 30, y - image.shape1 / 2.0 + 100) < 30.0
                }
            ),
            verticalFactor = 10.0
        )
    )

}



