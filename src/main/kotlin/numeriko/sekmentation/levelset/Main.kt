package numeriko.sekmentation.levelset

import org.openrndr.application
import org.openrndr.configuration
import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D
import tomasvolker.numeriko.core.interfaces.factory.doubleArray2D
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.hypot

val Int.gray get(): Double {
    val r = (this and 0x00FF0000) shr 16
    val g = (this and 0x0000FF00) shr 8
    val b = (this and 0x000000FF)
    return (r + g + b) / (255.0 * 3.0)
}

fun loadImage(path: String): DoubleArray2D {

    val image = ImageIO.read(File(path))
    return doubleArray2D(image.width, image.height) { x, y ->
        image.getRGB(x, y).gray * 255.0
    }
}

fun main() {

    val image = loadImage("data/P1_Image_originale.png")

    application(
        configuration = configuration {
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



