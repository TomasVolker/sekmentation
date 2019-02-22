package numeriko.sekmentation.fuzzyregiongrowing

import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D
import tomasvolker.numeriko.core.interfaces.array2d.generic.forEachIndex
import tomasvolker.numeriko.core.interfaces.factory.doubleZeros

fun DoubleArray2D.equalized(): DoubleArray2D {

    class ImageValue(val value: Double, val x: Int, val y: Int)

    val list = mutableListOf<ImageValue>()

    forEachIndex { i0, i1 ->
        list.add(ImageValue(this[i0,i1], i0, i1))
    }

    list.sortBy { it.value }

    val result = doubleZeros(shape0, shape1).asMutable()

    list.forEachIndexed { index, value ->
        result[value.x, value.y] = index.toDouble() / (list.size - 1)
    }

    return result
}
