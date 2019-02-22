package numeriko.sekmentation.fuzzyregiongrowing

import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D
import tomasvolker.numeriko.core.interfaces.array2d.double.MutableDoubleArray2D
import kotlin.math.absoluteValue
import kotlin.math.max

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