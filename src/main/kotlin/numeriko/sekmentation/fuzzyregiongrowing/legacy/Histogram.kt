package numeriko.sekmentation.fuzzyregiongrowing.legacy

import tomasvolker.numeriko.core.interfaces.array1d.double.DoubleArray1D
import tomasvolker.numeriko.core.interfaces.array1d.integer.IntArray1D
import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D
import tomasvolker.numeriko.core.interfaces.factory.doubleArray2D
import tomasvolker.numeriko.core.interfaces.factory.intArray1D
import tomasvolker.numeriko.core.interfaces.factory.toDoubleArray1D

fun IntArray1D.cumulativeSum() = map { reduce { acc, i -> acc + it } }.toDoubleArray1D()
fun IntArray1D.normalized() = map { it / this.sum() }.toDoubleArray1D()

interface Histogram<T> {
    val nBins: Int
    fun estimateHistogram(input: T): IntArray1D
}

class ImageHistogram(override val nBins: Int):
    Histogram<DoubleArray2D> {

    fun floatTo8Bits(image: DoubleArray2D): DoubleArray2D =
        doubleArray2D(image.shape0, image.shape1) { i0, i1 -> (image[i0,i1] * 255.0).toInt() }

    override fun estimateHistogram(input: DoubleArray2D): IntArray1D {
        val bins = getBins(input.linearView()).sorted()
        val maxBins = bins.last()
        val minBins = bins.first()
        val stepSize = (maxBins - minBins) / nBins

        return if (bins.size == nBins)
            intArray1D(nBins) { i ->
                input.count { it == bins[i] }
            }
        else
            intArray1D(nBins) { i ->
                input.filter { it < i * stepSize + minBins && it > (i - 1) * stepSize + minBins }.count()
            }
    }

    fun normalizedHistogram(input: DoubleArray2D): DoubleArray1D =
        estimateHistogram(input).let { it.toDoubleArray1D() / it.sum() }

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
}