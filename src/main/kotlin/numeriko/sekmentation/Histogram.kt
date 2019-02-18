package numeriko.sekmentation

import tomasvolker.numeriko.core.functions.differences
import tomasvolker.numeriko.core.interfaces.array1d.double.DoubleArray1D
import tomasvolker.numeriko.core.interfaces.array1d.double.elementWise
import tomasvolker.numeriko.core.interfaces.array1d.integer.IntArray1D
import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D
import tomasvolker.numeriko.core.interfaces.factory.doubleArray2D
import tomasvolker.numeriko.core.interfaces.factory.intArray1D
import tomasvolker.numeriko.core.interfaces.factory.toDoubleArray1D

fun IntArray1D.cumulativeSum() = map { reduce { acc, i -> acc + it } }.also { println(it) }.toDoubleArray1D()

interface Histogram<T> {
    val nBins: Int
    fun estimateHistogram(input: T): IntArray1D
}

open class ImageHistogram(override val nBins: Int): Histogram<DoubleArray2D> {

    override fun estimateHistogram(input: DoubleArray2D): IntArray1D =
            intArray1D(nBins) { i ->
                input.count { it.toInt() == i }
            }

    fun floatTo8Bits(image: DoubleArray2D) =
        doubleArray2D(image.shape0, image.shape1) { i0, i1 -> (image[i0,i1] * 255.0).toInt() }

    fun equalizedHistogram(image: DoubleArray2D): DoubleArray1D =
            estimateHistogram(image).let {
                it.cumulativeSum()
                    .elementWise { it * (image.max()!! - image.min()!!) + image.min()!! } // nasty !! action going on
                    .differences()
            }
}