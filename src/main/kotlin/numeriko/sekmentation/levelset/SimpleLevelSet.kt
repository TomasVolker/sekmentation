package numeriko.sekmentation.levelset

import com.github.tomasvolker.parallel.parallelContext
import kotlinx.coroutines.launch
import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D
import tomasvolker.numeriko.core.interfaces.factory.doubleArray2D

class SimpleLevelSet(
    override val image: DoubleArray2D,
    val deltaT: Double,
    val speed: Double = 1.0,
    initializer: (x: Int, y: Int)->Boolean
): LevelSetAlgorithm {

    override var step: Int = 0
        private set

    override val finished: Boolean
        get() = false

    val width = image.shape0
    val height = image.shape1

    val force = doubleArray2D(width, height) { x, y ->
        1.0 / (1.0 + image.gradientNormSquaredAt(x, y))
    }

    override val phi = doubleArray2D(width, height) { x, y ->
        if (initializer(x, y)) 1.0 else -1.0
    }.asMutable()

    override fun step() {

        val phiCopy = phi.copy()

        parallelContext {

            (0 until width).inIntervalsOf(50).forEach { interval ->

                launch {

                    for (i0 in interval) {
                        for (i1 in 0 until height) {

                            phi[i0, i1] += deltaT * (
                                    force[i0, i1] * (phiCopy.gradientNormAt(i0, i1) *
                                    speed + 2 * phiCopy.laplacianAt(i0, i1))
                                    )

                        }
                    }

                }

            }

        }

        for (i0 in 0 until width) {
            phi[i0, 0] = -1.0
            phi[i0, height-1] = -1.0
        }
        for (i1 in 0 until height) {
            phi[0, i1] = -1.0
            phi[width-1, i1] = -1.0
        }

        step++
    }

}

fun IntRange.inIntervalsOf(size: Int): List<IntRange> =
    ((this step size) + (last + 1))
        .zipWithNext { current, next ->
            current until next
        }