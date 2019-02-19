package numeriko.sekmentation.levelset

import tomasvolker.numeriko.core.dsl.D
import tomasvolker.numeriko.core.functions.filter2D
import tomasvolker.numeriko.core.index.All
import tomasvolker.numeriko.core.interfaces.array1d.double.DoubleArray1D
import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D
import tomasvolker.numeriko.core.interfaces.array2d.double.elementWise
import tomasvolker.numeriko.core.interfaces.array2d.generic.lastIndex0
import tomasvolker.numeriko.core.interfaces.array2d.generic.lastIndex1
import tomasvolker.numeriko.core.interfaces.arraynd.double.DoubleArrayND
import tomasvolker.numeriko.core.interfaces.factory.doubleArray2D
import tomasvolker.numeriko.core.interfaces.slicing.get
import tomasvolker.numeriko.core.primitives.sumDouble
import kotlin.math.hypot
/*
fun DoubleArray2D.filterSame(
    filter: DoubleArray2D
): DoubleArray2D {

    val filterCenter0 = filter.shape0 / 2
    val filterCenter1 = filter.shape1 / 2

    val filterShape0 = filter.shape0
    val filterShape1 = filter.shape1

    val resultShape0 = this.shape0
    val resultShape1 = this.shape1

    return doubleArray2D(resultShape0, resultShape1) { i0, i1 ->
        sumDouble(0 until filterShape0, 0 until filterShape1) { j0, j1 ->
            val k0 = i0 + j0 - filterCenter0
            val k1 = i1 + j1 - filterCenter1
            if (k0 in 0 until resultShape0 && k1 in 0 until resultShape1) {
                this[k0, k1]
            } else {
                padding
            } * filter[j0, j1]
        }
    }

}
*/
fun DoubleArray2D.laplacian(): DoubleArray2D {

    val filter = D[D[  0,-1, 0 ],
                   D[ -1, 4,-1 ],
                   D[  0,-1, 0 ]]

    return this.filter2D(filter)
}

fun DoubleArray2D.computeGradient0(): DoubleArray2D =
        doubleArray2D(shape0, shape1) { i0, i1 ->
            when(i0) {
                0 -> this[1, i1] - this[0, i1]
                lastIndex0 -> this[i0, i1] - this[i0 - 1, i1]
                else -> (this[i0 + 1, i1] - this[i0 - 1, i1]) / 2.0
            }
        }

fun DoubleArray2D.computeGradient1(): DoubleArray2D =
    doubleArray2D(shape0, shape1) { i0, i1 ->
        when(i1) {
            0 -> this[i0, 1] - this[i0, 0]
            lastIndex1 -> this[i0, i1] - this[i0, i1 - 1]
            else -> (this[i0, i1 + 1] - this[i0, i1 - 1]) / 2.0
        }
    }

fun DoubleArray2D.computeSecondD0(): DoubleArray2D =
    doubleArray2D(shape0, shape1) { i0, i1 ->
        when(i0) {
            0 -> this[0, i1] + 2 * this[1, i1] + this[2, i1]
            lastIndex0 -> this[i0 - 2, i1] + 2 * this[i0 - 1, i1] + this[i0, i1]
            else -> this[i0 - 1, i1] + 2 * this[i0, i1] + this[i0 + 1, i1]
        }
    }

fun DoubleArray2D.computeSecondD1(): DoubleArray2D =
    doubleArray2D(shape0, shape1) { i0, i1 ->
        when(i1) {
            0 -> this[i0, 0] + 2 * this[i0, 1] + this[i0, 2]
            lastIndex0 -> this[i0, i1 - 2] + 2 * this[i0, i1 - 1] + this[i0, i1]
            else -> this[i0, i1 - 1] + 2 * this[i0, i1] + this[i0, i1 + 1]
        }
    }

fun DoubleArray2D.computeGradients(): Pair<DoubleArray2D, DoubleArray2D> =
    Pair(computeGradient0(), computeGradient1())

fun DoubleArray2D.gradientNorm(): DoubleArray2D {

    val (gradx, grady) = this.computeGradients()

    return elementWise(gradx, grady) { x, y ->
        hypot(x, y)
    }

}

class ImageVectorField(
    val data: DoubleArrayND
) {

    init {

        require(data.rank == 3)
        require(data.shape(2) == 2)

    }

    val x = data.arrayAlongAxis(axis = 2, index = 0)
    val y = data.arrayAlongAxis(axis = 2, index = 1)

    operator fun get(i0: Int, i1: Int): DoubleArray1D =
        data.get(i0, i1, All).as1D()

    operator fun component1() = x
    operator fun component2() = y

}
