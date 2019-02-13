package numeriko.sekmentation.graph

import tomasvolker.numeriko.core.primitives.squared
import kotlin.math.*

interface GaussianWeightedGraph: WeightedGraph {

    var deviation: Double

    fun distanceSquared(from: Int, to: Int): Double = distance(from, to).squared()

    fun distance(from: Int, to: Int): Double

    override fun weight(from: Int, to: Int): Double =
        exp(-distanceSquared(from, to) / (2 * deviation.squared())) / (sqrt(2 * PI) * deviation)

}

class Grid2DGaussianWeightedGraph(
    val width: Int,
    val height: Int,
    override var deviation: Double
): GaussianWeightedGraph {

    override val size: Int
        get() = width * height

    val Int.x get() = xOf(this)
    val Int.y get() = yOf(this)

    fun xOf(index: Int) = index % width
    fun yOf(index: Int) = index / width

    override fun distanceSquared(from: Int, to: Int): Double =
        distance(from.x, from.y, to.x, to.y)

    override fun distance(from: Int, to: Int): Double =
        sqrt(distanceSquared(from, to))

    fun distanceSquared(x0: Int, y0: Int, x1: Int, y1: Int): Double =
        ((x1 - x0).squared() + (y1 - y0).squared()).toDouble()

    fun distance(x0: Int, y0: Int, x1: Int, y1: Int): Double =
        sqrt(distanceSquared(x0, y0, x1, y1))

    fun weight(x0: Int, y0: Int, x1: Int, y1: Int): Double =
        weight(node(x0, y0), node(x1, y1))

    fun node(x: Int, y: Int) = width * y + x

    override fun support(node: Int): Iterable<Int> = mutableListOf<Int>().apply {
        forEachOnSupport(node) { add(it) }
    }

    override fun forEachOnSupport(node: Int, block: (node: Int)->Unit) {
        forEachOnSupport(node.x, node.y) { x, y -> block(node(x, y)) }
    }

    inline fun forEachOnSupport(x: Int, y: Int, block: (Int, Int)->Unit) {

        val window = ceil(deviation * 3).toInt()

        val minX = (x-window).coerceIn(0, width-1)
        val maxX = (x+window).coerceIn(0, width-1)

        val minY = (y-window).coerceIn(0, height-1)
        val maxY = (y+window).coerceIn(0, height-1)

        for (i in minX..maxX) {
            for (j in minY.. maxY) {
                block(x, y)
            }
        }

    }

}
