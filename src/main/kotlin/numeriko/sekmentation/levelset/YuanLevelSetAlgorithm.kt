package numeriko.sekmentation.levelset

import numeriko.sekmentation.graph.Grid2DGaussianWeightedGraph
import numeriko.sekmentation.visualization.Grid2D
import numeriko.sekmentation.visualization.write
import numeriko.som.PanZoom
import org.openrndr.KEY_SPACEBAR
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.configuration
import org.openrndr.draw.colorBuffer
import tomasvolker.numeriko.core.dsl.D
import tomasvolker.numeriko.core.functions.filter2D
import tomasvolker.numeriko.core.functions.transpose
import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D
import tomasvolker.numeriko.core.interfaces.array2d.double.elementWise
import tomasvolker.numeriko.core.interfaces.array2d.generic.forEachIndex
import tomasvolker.numeriko.core.interfaces.factory.doubleArray2D
import tomasvolker.numeriko.core.interfaces.factory.doubleZeros
import tomasvolker.numeriko.core.interfaces.factory.nextGaussian
import tomasvolker.numeriko.core.primitives.indicator
import tomasvolker.numeriko.core.primitives.squared
import kotlin.math.*
import kotlin.random.Random

fun main() {

    application(
        configuration = configuration {
            width = 1000
            height = 800
            windowResizable = true
        },
        program = LevelSetProgram(
            YuanLevelSetAlgorithm(
                image = doubleArray2D(100, 100) { x, y ->
                    255 * tanh((y - 50) / 5.0) + Random.nextGaussian() * 2.0
                },
                kernelDeviation = 3.0,
                deltaT = 1e-1,
                alpha = 20.0,
                epsilon = 1.0,
                lambda0 = 1.03,
                lambda1 = 1.0,
                nu = 0.001 * 255.squared(),
                mu = 1.0
            )
        )
    )


}

class YuanLevelSetAlgorithm(
    override val image: DoubleArray2D,
    val kernelDeviation: Double,
    val deltaT: Double,
    val alpha: Double,
    val epsilon: Double,
    val lambda0: Double,
    val lambda1: Double,
    val nu: Double,
    val mu: Double
): LevelSetAlgorithm {

    val width = image.shape0
    val height = image.shape1

    override val finished: Boolean
        get() = false

    override var step: Int = 0
        private set

    val lattice = Grid2DGaussianWeightedGraph(
        width = image.shape0,
        height = image.shape1,
        deviation = kernelDeviation
    )

    val mean0Image = doubleZeros(width, height).asMutable()
    val dev0Image = doubleZeros(width, height).asMutable()
    val mean1Image = doubleZeros(width, height).asMutable()
    val dev1Image = doubleZeros(width, height).asMutable()

    fun meanImage(set: Int) = when(set) {
        0 -> mean0Image
        1 -> mean1Image
        else -> error("invalid set")
    }

    fun devImage(set: Int) = when(set) {
        0 -> dev0Image
        1 -> dev1Image
        else -> error("invalid set")
    }

    override val phi = doubleArray2D(width, height) { x, y ->
        if (hypot(x - width / 2.0, y - height / 2.0 - 20) < 10.0) 1.0 else -1.0
    }.asMutable()

    var phiLaplacian = doubleZeros(image.shape0, image.shape1)

    var phiGradientX = doubleZeros(image.shape0, image.shape1)
    var phiGradientY = doubleZeros(image.shape0, image.shape1)
    var phiGradientDirectionDiv = doubleZeros(image.shape0, image.shape1)

    fun node(x: Int, y: Int) = lattice.node(x, y)

    inline fun weightedSum(
        x: Int,
        y: Int,
        selector: (u: Int, v: Int)->Double
    ): Double {

        var sum = 0.0
        lattice.forEachOnSupport(x, y) { u, v ->
            sum += lattice.weight(node(x, y), node(u, v)) * selector(u, v)
        }
        return sum

    }

    fun membership(set: Int, x: Int, y: Int): Double =
        when(set) {
            0 -> softHeaviside(phi[x, y])
            1 -> 1 - softHeaviside(phi[x, y])
            else -> error("Illegal set")
        }

    fun softHeaviside(x: Double): Double =
        0.5 * (1 + atan(x / epsilon) * 2 / PI )

    fun softDelta(x: Double): Double =
        epsilon / (PI * (epsilon.squared() + x.squared()))

    fun estimateDistributions(
        set: Int,
        x: Int,
        y: Int
    ) {

        val normalization = weightedSum(x, y) { u, v -> membership(set, u, v) }

        val mean = weightedSum(x, y) { u, v -> image[u, v] * membership(set, u, v) } / normalization
        val dev = weightedSum(x, y) { u, v -> (image[u, v] - mean).squared() * membership(set, u, v) } / normalization

        meanImage(set)[x, y] = mean
        devImage(set)[x, y] = dev + 1e-8
    }

    fun information(
        set: Int,
        x: Int,
        y: Int,
        u: Int,
        v: Int
    ): Double {
        val mean = meanImage(set)[x, y]
        val dev2 = devImage(set)[x, y]
        return 0.5 * ln(2 * PI * dev2) + (image[u, v] - mean).squared() / (2 * dev2)
    }

    fun updatePhiLaplacian() {

        phiLaplacian = phi.computeSecondD0() + phi.computeSecondD1()

    }

    fun updatePhiGradientDirectionDiv() {

        val gradients = phi.computeGradients()

        phiGradientX = gradients.x / (gradients.norm() + 1e-8)
        phiGradientY = gradients.y / (gradients.norm() + 1e-8)

        phiGradientDirectionDiv = phiGradientX.computeGradient0() + phiGradientY.computeGradient1()
    }

    fun dPhiDt(x: Int, y: Int): Double {

        val e0 = lambda0 * weightedSum(x, y) { u, v -> information(0, x, y, u, v).squared() }
        val e1 = lambda1 * weightedSum(x, y) { u, v -> information(1, x, y, u, v).squared() }
        val e2 = (lambda0 - lambda1) * weightedSum(x, y) { u, v ->
            information(0, x, y, u, v) * information(1, x, y, u, v)
        }

        val delta = softDelta(phi[x, y])

        return - alpha * delta * (e0 - e1 + e2) +
                nu * delta * phiGradientDirectionDiv[x, y] +
                mu * (phiLaplacian[x, y] - phiGradientDirectionDiv[x, y])
    }

    fun estimateDistributions() {

        image.forEachIndex { x, y ->
            estimateDistributions(0, x, y)
            estimateDistributions(1, x, y)
        }

    }

    override fun step() {

        estimateDistributions()

        updatePhiGradientDirectionDiv()
        updatePhiLaplacian()

        phi.forEachIndex { x, y ->
            phi[x, y] += deltaT * dPhiDt(x, y)
        }

        step++

    }

}


fun DoubleArray2D.normalizeContrast(): DoubleArray2D {
    val min = min() ?: 0.0
    val delta = (max() ?: 1.0) - min
    return elementWise { (it - min) / delta }
}

