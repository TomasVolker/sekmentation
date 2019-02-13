package numeriko.sekmentation.graph

import tomasvolker.numeriko.core.primitives.modulo
import tomasvolker.numeriko.core.primitives.squared
import kotlin.math.*

interface GaussianWeightedGraph: WeightedGraph {

    var deviation: Double

    fun distanceSquared(from: Int, to: Int): Double = distance(from, to).squared()

    fun distance(from: Int, to: Int): Double

    override fun weight(from: Int, to: Int): Double =
        exp(-distanceSquared(from, to) / (2 * deviation.squared()))

}

class CircleGaussianWeightedGraph(
    override val size: Int,
    override var deviation: Double
): GaussianWeightedGraph, Graph {

    override fun distanceSquared(from: Int, to: Int): Double {
        val angleFrom = 2 * PI * from.toDouble() / size
        val angleTo = 2 * PI * to.toDouble() / size

        val deltaX = size * (cos(angleFrom) - cos(angleTo))
        val deltaY = size * (sin(angleFrom) - sin(angleTo))
        return deltaX.squared() + deltaY.squared()
    }

    override fun distance(from: Int, to: Int): Double = sqrt(distanceSquared(from, to))

    override fun areConnected(from: Int, to: Int): Boolean =
        ((from - to) modulo size).absoluteValue < 2

    override fun neighbors(nodeIndex: Int): Iterable<Int> =
        listOf(
            (nodeIndex-1) modulo size,
            (nodeIndex+1) modulo size
        )

}


class Grid2DGaussianWeightedGraph(
    val width: Int,
    val height: Int,
    override var deviation: Double
): GaussianWeightedGraph, Graph {

    override val size: Int
        get() = width * height

    fun xOf(index: Int) = index % width
    fun yOf(index: Int) = index / width

    fun deltaX(from: Int, to: Int): Int = xOf(to) - xOf(from)
    fun deltaY(from: Int, to: Int): Int = yOf(to) - yOf(from)

    override fun distanceSquared(from: Int, to: Int): Double =
        (deltaX(from, to).squared() + deltaY(from, to).squared()).toDouble()

    override fun distance(from: Int, to: Int): Double = sqrt(distanceSquared(from, to))

    override fun areConnected(from: Int, to: Int): Boolean =
        (deltaX(from, to).absoluteValue + deltaY(from, to).absoluteValue) == 1

    override fun neighbors(nodeIndex: Int): Iterable<Int> =
        mutableListOf<Int>().apply {
            val x = xOf(nodeIndex)
            val y = yOf(nodeIndex)

            if (0 < x) add(toLinear(x-1, y))
            if (x < width-1) add(toLinear(x+1, y))
            if (0 < y) add(toLinear(x, y-1))
            if (y < height-1) add(toLinear(x, y+1))

        }

    fun toLinear(x: Int, y: Int) = width * y + x

}


class Grid3DGaussianWeightedGraph(
    val width: Int,
    val height: Int,
    val depth: Int,
    override var deviation: Double
): GaussianWeightedGraph, Graph {

    override val size: Int
        get() = width * height * depth

    fun xOf(index: Int) = index % width
    fun yOf(index: Int) = (index % (width * height)) / width
    fun zOf(index: Int) = index / (width * height)

    fun toLinear(x: Int, y: Int, z: Int) = width * height * z + width * y + x

    fun deltaX(from: Int, to: Int): Int = xOf(to) - xOf(from)
    fun deltaY(from: Int, to: Int): Int = yOf(to) - yOf(from)
    fun deltaZ(from: Int, to: Int): Int = zOf(to) - zOf(from)

    override fun distanceSquared(from: Int, to: Int): Double =
        (deltaX(from, to).squared() +
         deltaY(from, to).squared() +
         deltaZ(from, to).squared()
        ).toDouble()

    override fun distance(from: Int, to: Int): Double = sqrt(distanceSquared(from, to))

    override fun areConnected(from: Int, to: Int): Boolean =
        (deltaX(from, to).absoluteValue + deltaY(from, to).absoluteValue + deltaZ(from, to).absoluteValue) == 1

    override fun neighbors(nodeIndex: Int): Iterable<Int> =
        mutableListOf<Int>().apply {
            val x = xOf(nodeIndex)
            val y = yOf(nodeIndex)
            val z = zOf(nodeIndex)

            if (0 < x) add(toLinear(x-1, y, z))
            if (x < width-1) add(toLinear(x+1, y, z))

            if (0 < y) add(toLinear(x, y-1, z))
            if (y < height-1) add(toLinear(x, y+1, z))

            if (0 < z) add(toLinear(x, y, z-1))
            if (z < depth-1) add(toLinear(x, y, z+1))

        }

}