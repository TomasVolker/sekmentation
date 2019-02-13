package numeriko.sekmentation

import tomasvolker.numeriko.core.index.All
import tomasvolker.numeriko.core.interfaces.array1d.double.DoubleArray1D
import tomasvolker.numeriko.core.interfaces.array1d.generic.lastIndex
import tomasvolker.numeriko.core.interfaces.array1d.integer.IntArray1D
import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D
import tomasvolker.numeriko.core.interfaces.array2d.double.MutableDoubleArray2D
import tomasvolker.numeriko.core.interfaces.arraynd.double.DoubleArrayND
import tomasvolker.numeriko.core.interfaces.factory.doubleArray2D
import tomasvolker.numeriko.core.interfaces.factory.doubleZeros
import tomasvolker.numeriko.core.interfaces.factory.toDoubleArray1D

interface Filter<T> {
    fun filter(input: T): T
}

interface PipelineFilter2D: Filter<DoubleArray2D> {
    override fun filter(input: DoubleArray2D): DoubleArray2D {
        val destination = doubleZeros(input.shape).as2D().asMutable()
        filter(input, destination)
        return destination
    }
    fun filter(input: DoubleArray2D, destination: MutableDoubleArray2D): Unit
}

data class FilterKernel<T: DoubleArrayND>(val window: T, val shape: IntArray1D)

private fun DoubleArray2D.flatten() = List(shape0) { i -> this[i,All] }.flatten().toDoubleArray1D()

private fun DoubleArray1D.median() = sorted()[lastIndex / 2]

class MedianFilter2D(val kernel: FilterKernel<DoubleArray2D>): PipelineFilter2D {

    override fun filter(input: DoubleArray2D, destination: MutableDoubleArray2D): Unit {
        val edgeX = input.shape0 - kernel.shape[0]
        val edgeY = input.shape1 - kernel.shape[1]

        for (i in 0 until input.shape0) {
            for (j in 0 until input.shape1) {
                if (i < kernel.shape[0] || i > edgeX || j < kernel.shape[1] || j > edgeY)
                    destination[i,j] = input[i,j]
                else
                    destination[i,j] = convolveWithKernel(input, PixelPosition(i, j)).estimateMedian()
            }
        }
    }

    private fun convolveWithKernel(image: DoubleArray2D, position: PixelPosition) =
            doubleArray2D(kernel.shape[0], kernel.shape[1]) { i0, i1 ->
                image[i0 + position.x,i1 + position.y] * kernel.window[i0,i1]
            }

    private fun DoubleArray2D.estimateMedian() = flatten().median()

    inner class PixelPosition(val x: Int, val y: Int)
}

class HistogramEquilizationFilter(override val nBins: Int): PipelineFilter2D, ImageHistogram(nBins) {
    override fun filter(input: DoubleArray2D, destination: MutableDoubleArray2D) {
        val grayScale = equalizedHistogram(input)

        for (i in 0 until input.shape0) {
            for (j in 0 until input.shape1) {
                destination[i,j] = grayScale[input[i,j].toInt()]
            }
        }
    }
}