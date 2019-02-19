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
import tomasvolker.numeriko.core.operations.stack
import tomasvolker.numeriko.core.primitives.sumDouble
import kotlin.math.hypot

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

fun DoubleArray2D.computeGradients(): ImageVectorField =
    ImageVectorField(
        listOf(computeGradient0(), computeGradient1()).stack(axis = 2)
    )

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

    val width get() = data.shape(0)
    val height get() = data.shape(1)

    val x = data.arrayAlongAxis(axis = 2, index = 0).as2D()
    val y = data.arrayAlongAxis(axis = 2, index = 1).as2D()

    operator fun get(i0: Int, i1: Int): DoubleArray1D =
        data.get(i0, i1, All).as1D()

    operator fun component1() = x
    operator fun component2() = y

}

fun ImageVectorField.norm() =
        doubleArray2D(width, height) { x, y ->
            hypot(this.x[x, y], this.y[x, y])
        }
