package numeriko.sekmentation.levelset

import tomasvolker.numeriko.core.dsl.D
import tomasvolker.numeriko.core.functions.filter2D
import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D
import tomasvolker.numeriko.core.interfaces.array2d.double.elementWise
import kotlin.math.hypot

fun DoubleArray2D.laplacian(): DoubleArray2D {

    val filter = D[D[  0,-1, 0 ],
                   D[ -1, 4,-1 ],
                   D[  0,-1, 0 ]]

    return this.filter2D(filter)
}

fun DoubleArray2D.computeGradientX(): DoubleArray2D {

    val filter = D[D[ -1, 0, 1 ],
                   D[ -2, 0, 2 ],
                   D[ -1, 0, 1 ]]

    return this.filter2D(filter)
}

fun DoubleArray2D.computeGradientY(): DoubleArray2D {

    val filter = D[D[ -1, -2, -1 ],
                   D[  0,  0,  0 ],
                   D[  1,  2,  1 ]]

    return this.filter2D(filter)
}

fun DoubleArray2D.computeGradients(): Pair<DoubleArray2D, DoubleArray2D> =
    Pair(computeGradientX(), computeGradientY())

fun DoubleArray2D.computeEdges(): DoubleArray2D {

    val (gradx, grady) = this.computeGradients()

    return elementWise(gradx, grady) { x, y ->
        hypot(x, y)
    }

}
