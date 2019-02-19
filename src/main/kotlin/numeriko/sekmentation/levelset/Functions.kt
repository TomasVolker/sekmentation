package numeriko.sekmentation.levelset

import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D
import tomasvolker.numeriko.core.interfaces.array2d.double.MutableDoubleArray2D
import tomasvolker.numeriko.core.interfaces.array2d.generic.forEachIndex
import tomasvolker.numeriko.core.primitives.sumDouble

fun DoubleArray2D.filter2DTo(
    filter: DoubleArray2D,
    destination: MutableDoubleArray2D,
    padding: Double = 0.0
) {

    val filterCenter0 = filter.shape0 / 2
    val filterCenter1 = filter.shape1 / 2

    val filterShape0 = filter.shape0
    val filterShape1 = filter.shape1

    val resultShape0 = this.shape0
    val resultShape1 = this.shape1

    forEachIndex { i0, i1 ->
        destination[i0, i1] = sumDouble(0 until filterShape0, 0 until filterShape1) { j0, j1 ->
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