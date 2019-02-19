package numeriko.sekmentation.levelset

import org.openrndr.application
import org.openrndr.configuration
import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D
import tomasvolker.numeriko.core.interfaces.array2d.double.elementWise
import tomasvolker.numeriko.core.interfaces.array2d.generic.forEachIndex
import tomasvolker.numeriko.core.interfaces.factory.doubleArray2D
import tomasvolker.numeriko.core.primitives.squared
import kotlin.math.absoluteValue
import kotlin.math.hypot
import kotlin.math.tanh

fun main() {

    application(
        configuration = configuration {
            width = 1000
            height = 800
            windowResizable = true
        },
        program = LevelSetProgram(
            LevelSet(
                image = doubleArray2D(100, 100) { x, y ->
                    255 * tanh((y - 50) / 5.0)
                },
                deltaT = 1e-1
            )
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
        if (hypot(x - width / 2.0, y - height / 2.0 - 20) < 10.0) 1.0 else -1.0
    }.asMutable()

    override fun step() {

        val phiGradients = phi.computeGradients()

        val gradientNorm = elementWise(phiGradients.x, phiGradients.y) { x, y ->
            hypot(x, y)
        }

        val gradientDir0 = phiGradients.x / (gradientNorm + 1e-8)
        val gradientDir1 = phiGradients.y / (gradientNorm + 1e-8)

        val laplacian = phi.computeSecondD0() + phi.computeSecondD1()
        //val curvature = 2 * laplacian / (gradientNorm + 1e-8)

        val curvature = gradientDir0.computeGradient0() + gradientDir1.computeGradient1()

        phi.forEachIndex { x, y ->
            phi[x, y] += deltaT * (
                    gradientNorm[x, y] * (curvature[x, y] + force[x, y] * v) +
                    if(phi[x, y].absoluteValue > 1.1) -phi[x, y] else 0.0
            )
        }

        step++
    }

}