package numeriko.sekmentation

import tomasvolker.kyplot.dsl.showImage
import tomasvolker.kyplot.dsl.showStem
import tomasvolker.numeriko.core.functions.differences
import tomasvolker.numeriko.core.interfaces.array1d.double.DoubleArray1D
import tomasvolker.numeriko.core.interfaces.array1d.double.elementWise
import tomasvolker.numeriko.core.interfaces.array1d.integer.IntArray1D
import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D
import tomasvolker.numeriko.core.interfaces.factory.doubleArray2D
import tomasvolker.numeriko.core.interfaces.factory.intArray1D
import tomasvolker.numeriko.core.interfaces.factory.toDoubleArray1D
import tomasvolker.numeriko.core.linearalgebra.linearSpace

fun IntArray1D.cumulativeSum() = map { reduce { acc, i -> acc + it } }.toDoubleArray1D()

interface Histogram<T> {
    val nBins: Int
    fun estimateHistogram(input: T): IntArray1D
}

open class ImageHistogram(override val nBins: Int): Histogram<DoubleArray2D> {

    override fun estimateHistogram(input: DoubleArray2D): IntArray1D =
            intArray1D(nBins) { i ->
                input.count { it.toInt() == i }
            }

    fun floatTo8Bits(image: DoubleArray2D): DoubleArray2D =
        doubleArray2D(image.shape0, image.shape1) { i0, i1 -> (image[i0,i1] * 255.0).toInt() }

    fun getEvenSpread(input: DoubleArray2D): List<Double> {
        val bins = getBins(input.linearView())

        if (bins.size <= nBins)
            return bins
        else {
            TODO("do the means in each bin")
        }
    }

    private fun getBins(input: DoubleArray1D): List<Double> {
        return if (input.size == 1)
            listOf(input[0])
        else
            getBins(input.filter { it == input[0] }.toDoubleArray1D()) + input[0]
    }

    private fun countTotalBins(input: DoubleArray1D): Int {
        return if (input.size == 1)
            1
        else
            countTotalBins(input.filter { it == input[0] }.toDoubleArray1D()) + input.count { it == input[0] }
    }


    fun equalizedHistogram(image: DoubleArray2D): DoubleArray1D =
            estimateHistogram(image).let {hist ->
                hist.cumulativeSum()
                    .elementWise { it * (hist.max()!! - hist.min()!!) + hist.min()!! } // nasty !! action going on
                    .differences()
            }
}