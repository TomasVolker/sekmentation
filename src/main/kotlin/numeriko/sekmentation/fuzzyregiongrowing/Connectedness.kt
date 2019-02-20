package numeriko.sekmentation.fuzzyregiongrowing

import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D
import tomasvolker.numeriko.core.interfaces.array2d.double.MutableDoubleArray2D
import tomasvolker.numeriko.core.interfaces.factory.doubleZeros
import tomasvolker.numeriko.core.primitives.squared
import java.util.*
import kotlin.math.*

data class Statistic(
    val mean: Double,
    val deviation: Double
)

data class Point(
    val x: Int,
    val y: Int
)

fun Point.neighborhood(): List<Point> =
    (x-1..x+1).flatMap { u ->
        (y-1..y+1).map { v ->
            Point(u, v)
        }
    }

fun Point.isNeighbor(other: Point) =
    max((other.x - this.x).absoluteValue, (other.y - this.y).absoluteValue) <= 1

fun DoubleArray2D.isValidPoint(point: Point) = point.x in 0 until shape0 && point.y in 0 until shape1

operator fun DoubleArray2D.get(point: Point) = this[point.x, point.y]
operator fun MutableDoubleArray2D.set(point: Point, value: Double) { this[point.x, point.y] = value }

fun <T> List<T>.allPairs(): List<Pair<T, T>> =
    this.mapIndexed { i, value ->
        (i+1 until size).map { j -> value to this[j] }
    }.flatten()

fun List<Double>.deviation(): Double {
    val mean = average()
    return sqrt(sumByDouble { (it - mean).squared() } / (size - 1))
}

fun List<Double>.computeStatistics() = Statistic(
    mean = average(),
    deviation = deviation()
)

fun DoubleArray2D.estimateConnectivityStatistic(
    point: Point,
    connectivityFunction: (a: Double, b: Double)->Double
): Statistic = point.neighborhood()
        .filter { isValidPoint(it) }
        .allPairs()
        .filter { it.first.isNeighbor(it.second) }
        .map { connectivityFunction(this[it.first], this[it.second]) }
        .computeStatistics()


class FuzzyConnectednessAlgorithm(
    val image: DoubleArray2D,
    seed: Point
) {

    val connectivityMap = doubleZeros(image.shape0, image.shape1).asMutable()

    val queue: Queue<Point> = ArrayDeque()

    init {
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

