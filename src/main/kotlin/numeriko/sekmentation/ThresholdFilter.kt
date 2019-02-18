package numeriko.sekmentation

import tomasvolker.numeriko.core.interfaces.array1d.integer.IntArray1D
import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D
import tomasvolker.numeriko.core.interfaces.array2d.double.MutableDoubleArray2D
import kotlin.math.max

class ThresholdFilter(override val nBins: Int): PipelineFilter2D, ImageHistogram(nBins) {
    override fun filter(input: DoubleArray2D, destination: MutableDoubleArray2D) =
            estimateOptimalThreshold(estimateHistogram(input)).let { threshold ->
                for (i in 0 until input.shape0) {
                    for (j in 0 until input.shape1) {
                        destination[i,j] = if (input[i,j] > threshold) input[i,j] else 0.0
                    }
                }
            }

    private fun estimateOptimalThreshold(histogram: IntArray1D): Int {
        val maxHist = histogram.max() ?: 0
        var area = 0
        val percentDiff = mutableListOf<Double>()

        for (i in 0 until maxHist) {
            if (area == 0)
                percentDiff.add(0.0)
            else
                percentDiff.add(histogram[i] / area.toDouble())
            area += histogram[i]
        }

        return percentDiff.max()?.toInt() ?: 0
    }

    private fun histogramArea(histogram: IntArray1D, threshold: Int) =
            histogram.count { it < threshold }
}