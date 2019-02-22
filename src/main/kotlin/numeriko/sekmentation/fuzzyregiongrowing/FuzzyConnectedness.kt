package numeriko.sekmentation.fuzzyregiongrowing

import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D
import tomasvolker.numeriko.core.interfaces.array2d.double.MutableDoubleArray2D
import tomasvolker.numeriko.core.interfaces.factory.doubleZeros
import tomasvolker.numeriko.core.primitives.squared
import java.util.*
import kotlin.math.*


class FuzzyConnectedness(
    val image: DoubleArray2D,
    seed: Point
) {

    val connectivityMap = doubleZeros(image.shape0, image.shape1).asMutable()

    val queue: Queue<Point> = ArrayDeque()

    init {
        // Initialization
        seed.neighborhood()
            .onEach { connectivityMap[it] = 1.0 }
            .let { queue.addAll(it) }
    }

    val sumStats = image.estimateConnectivityStatistic(seed) { a, b -> a + b }
    val diffStats = image.estimateConnectivityStatistic(seed) { a, b -> (a - b).absoluteValue }

    fun Statistic.gaussian(x: Double) =
            exp(-(x - mean).squared() / (2 * deviation.squared()))

    fun pixelAffinity(
        center: Point,
        neighbor: Point
    ): Double =
        (sumStats.gaussian(image[center] + image[neighbor]) +
        diffStats.gaussian((image[center] - image[neighbor]).absoluteValue)) / 2


    fun finished() = queue.isEmpty()

    fun step() {

        val current = queue.poll() ?: return

        for(neighbor in current.neighborhood().filter { image.isValidPoint(it) }) {

            val potentialAffinity = min(connectivityMap[current], pixelAffinity(current, neighbor))

            if (potentialAffinity > connectivityMap[neighbor]) {
                queue.add(neighbor)
                connectivityMap[neighbor] = potentialAffinity
            }

        }

    }

    fun run() {

        while (!finished()) {
            step()
        }

    }

}

