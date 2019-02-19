package numeriko.sekmentation.levelset

import com.github.tomasvolker.parallel.parallelContext
import kotlinx.coroutines.launch
import org.openrndr.application
import org.openrndr.configuration
import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D
import tomasvolker.numeriko.core.interfaces.factory.doubleArray2D
import tomasvolker.numeriko.core.primitives.squared
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
            LevelSet(
                image = image,
                deltaT = 1e-1,
                speed = 0.5
            ),
            verticalFactor = 10.0
        )
    )

}



class LevelSet(
    override val image: DoubleArray2D,
    val deltaT: Double,
    val speed: Double = 1.0
): LevelSetAlgorithm {

    override var step: Int = 0
        private set

    override val finished: Boolean
        get() = false

    val width = image.shape0
    val height = image.shape1

    val force: DoubleArray2D
    val forceGradient: ImageVectorField

    init {

        val (gradientX, gradientY) = image.computeGradients()

        force = doubleArray2D(width, height) { x, y ->
            1.0 / (1.0  + gradientX[x, y].squared() + gradientY[x, y].squared())
        }
        forceGradient = force.computeGradients()

    }

    override val phi = doubleArray2D(width, height) { x, y ->
        if (hypot(x - width / 2.0 - 30, y - height / 2.0 + 100) < 60.0) 1.0 else -1.0
    }.asMutable()

    override fun step() {

        val phiCopy = phi.copy()

        parallelContext {

            (0 until width).inIntervalsOf(100).forEach { interval ->

                launch {
                    for(i0 in interval) {
                        for (i1 in 0 until height) {

                            phi[i0, i1] += deltaT * (
                                force[i0, i1] * (phiCopy.gradientNormAt(i0, i1) * speed + 2 * phiCopy.laplacianAt(i0, i1))
                            )

                        }
                    }
                }

            }

        }

        step++
    }

    fun DoubleArray2D.gradientNormAt(i0: Int, i1: Int): Double =
            hypot(gradient0At(i0, i1), gradient1At(i0, i1))

}

fun IntRange.inIntervalsOf(size: Int): List<IntRange> =
    ((this step size) + (last + 1))
        .zipWithNext { current, next ->
            current until next
        }
