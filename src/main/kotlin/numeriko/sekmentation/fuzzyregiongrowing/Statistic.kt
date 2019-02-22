package numeriko.sekmentation.fuzzyregiongrowing

import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D
import tomasvolker.numeriko.core.primitives.squared
import kotlin.math.sqrt

data class Statistic(
    val mean: Double,
    val deviation: Double
)

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