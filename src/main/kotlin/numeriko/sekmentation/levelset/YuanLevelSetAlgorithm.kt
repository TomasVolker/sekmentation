package numeriko.sekmentation.levelset

import numeriko.sekmentation.graph.Grid2DGaussianWeightedGraph
import numeriko.sekmentation.visualization.Grid2D
import numeriko.sekmentation.visualization.write
import numeriko.som.PanZoom
import org.openrndr.KEY_SPACEBAR
import org.openrndr.application
import org.openrndr.color.ColorRGBa
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

    val image = doubleArray2D(100, 100) { i0, i1 ->
        255 * (i1 >= 50 + Random.nextInt(-2, 2)).indicator() + Random.nextGaussian() * 5
    }

    val deltaT = 0.1
    val alpha = 20.0
    val lambda0 = 1.03
    val lambda1 = 1.0
    val sigma = 3.0
    val nu = 0.001 * 255.0.squared()
    val mu = 1.0
    val epsilon = 1.0

    val levelSet = YuanLevelSetAlgorithm(
        image = image,
        kernelDeviation = sigma,
        deltaT = deltaT,
        alpha = alpha,
        epsilon = epsilon,
        lambda0 = lambda0,
        lambda1 = lambda1,
        nu = nu,
        mu = mu
    )

    application {
        configure {
            width = 800
            height = 600
            windowResizable = true
        }
        program {

            backgroundColor = ColorRGBa.BLUE.shade(0.2)

            extend(PanZoom())
            extend(Grid2D())

            val buffer = colorBuffer(image.shape0, image.shape1)
            val bufferPhi = colorBuffer(image.shape0, image.shape1)

            buffer.write(image.normalizeContrast())

            keyboard.keyUp.listen {
                if (it.key == KEY_SPACEBAR) {
                    levelSet.step()
                    println("step: ${levelSet.step}")
                }
            }

            extend {

                drawer.image(buffer)

                bufferPhi.write(levelSet.phi.normalizeContrast())
                drawer.image(bufferPhi, image.shape0.toDouble(), 0.0)

                bufferPhi.write(levelSet.phiGradientX.normalizeContrast())
                drawer.image(bufferPhi, 0.0, image.shape1.toDouble())

                bufferPhi.write(levelSet.phiGradientY.normalizeContrast())
                drawer.image(bufferPhi, image.shape0.toDouble(), image.shape1.toDouble())


            }
        }
    }


}

class YuanLevelSetAlgorithm(
    val image: DoubleArray2D,
    val kernelDeviation: Double,
    val deltaT: Double,
    val alpha: Double,
    val epsilon: Double,
    val lambda0: Double,
    val lambda1: Double,
    val nu: Double,
    val mu: Double
) {

    val width = image.shape0
    val height = image.shape1

    var step: Int = 0
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

    val phi = doubleArray2D(width, height) { i0, i1 ->
        if (i1 > height / 2)
            -1.0
        else
            1.0
    }.asMutable()

    val phiLaplacian = doubleZeros(image.shape0, image.shape1).asMutable()

    val phiGradientX = doubleZeros(image.shape0, image.shape1).asMutable()
    val phiGradientY = doubleZeros(image.shape0, image.shape1).asMutable()
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
        devImage(set)[x, y] = dev
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

        val filter = D[D[  0,-1, 0 ],
                       D[ -1, 4,-1 ],
                       D[  0,-1, 0 ]] / 8

        phi.filter2DTo(
            filter = filter,
            destination = phiLaplacian
        )

    }

    fun updatePhiGradientDirectionDiv() {

        val sobelY = D[D[ -1, 0, 1 ],
                       D[ -2, 0, 2 ],
                       D[ -1, 0, 1 ]] / 8

        val sobelX = sobelY.transpose().copy()

        phi.filter2DTo(
            filter = sobelX,
            destination = phiGradientX
        )

        phi.filter2DTo(
            filter = sobelY,
            destination = phiGradientY
        )

        phi.forEachIndex { i0, i1 ->
            val norm = hypot(phiGradientX[i0, i1], phiGradientY[i0, i1]) + 0.01
            phiGradientX[i0, i1] /= norm
            phiGradientY[i0, i1] /= norm
        }

        phiGradientDirectionDiv = phiGradientX.filter2D(sobelX) + phiGradientY.filter2D(sobelY)

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

    fun step() {

        estimateDistributions()

        updatePhiGradientDirectionDiv()
        updatePhiLaplacian()

        phi.forEachIndex { x, y ->
            phi[x, y] -= deltaT * dPhiDt(x, y)
        }

        step++

    }

}


fun DoubleArray2D.normalizeContrast(): DoubleArray2D {
    val min = min() ?: 0.0
    val delta = (max() ?: 1.0) - min
    return elementWise { (it - min) / delta }
}

