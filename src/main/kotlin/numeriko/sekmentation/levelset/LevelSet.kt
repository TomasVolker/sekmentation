package numeriko.sekmentation.levelset

import com.github.tomasvolker.parallel.parallelContext
import kotlinx.coroutines.async
import org.openrndr.application
import org.openrndr.configuration
import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D
import tomasvolker.numeriko.core.interfaces.array2d.double.elementWise
import tomasvolker.numeriko.core.interfaces.array2d.double.times
import tomasvolker.numeriko.core.interfaces.array2d.generic.forEachIndex
import tomasvolker.numeriko.core.interfaces.array2d.generic.lastIndex0
import tomasvolker.numeriko.core.interfaces.factory.doubleArray2D
import tomasvolker.numeriko.core.primitives.squared
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.absoluteValue
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
                v = 0.5
            ),
            verticalFactor = 10.0
        )
    )

}



class LevelSet(
    override val image: DoubleArray2D,
    val deltaT: Double,
    val v: Double = 1.0
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
        val phiGradients = phi.computeGradients()

        val gradientNorm = elementWise(phiGradients.x, phiGradients.y) { x, y ->
            hypot(x, y)
        }

        val laplacian = phi.computeSecondD0() + phi.computeSecondD1()

        phi.forEachIndex { x, y ->
            phi[x, y] += deltaT * force[x, y] * (gradientNorm[x, y] * v + 2 * laplacian[x, y])
        }

        step++
    }

}

/*
fun DoubleArray2D.computeGradientDirectionDivergence(): DoubleArray2D {
    doubleArray2D(shape0, shape1) { i0, i1 ->
        when (i0) {
            0 -> TODO()
            lastIndex0 -> TODO()
            else -> {

                when(i1) {
                    0 -> TODO()
                    lastIndex0 -> TODO()
                    else -> {


                        val grad0p = this[i0+1, i1] - this[i0, i1]
                        val grad0m = this[i0, i1] - this[i0-1, i1]
                        val grad0c = (grad0p + grad0m) / 2

                        val grad1p = this[i0, i1+1] - this[i0, i1]
                        val grad1m = this[i0, i1] - this[i0, i1-1]
                        val grad1c = (grad1p + grad1m) / 2

                        val gradNorm = hypot(grad0c, grad1c)

                        grad0p / gradNorm
                    }
                }

            }
        }
    }
}
*/