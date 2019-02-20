package numeriko.sekmentation.fuzzyregiongrowing

import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D
import tomasvolker.numeriko.core.interfaces.array2d.double.MutableDoubleArray2D
import tomasvolker.numeriko.core.interfaces.array2d.double.applyElementWise
import tomasvolker.numeriko.core.primitives.squared
import tomasvolker.numeriko.core.primitives.sumDouble
import java.util.*
import kotlin.math.*

data class PixelCoordinates(val x: Int, val y: Int)

data class Pixel(val intensity: Double, val coordinates: PixelCoordinates)

data class ProtoRegion(val mainPixel: Pixel, val pixelList: List<Pixel>) {

    val position get() = mainPixel.coordinates
    val mean get() = pixelList.map { it.intensity }.average()
    val std get() = mean.let { mu -> sqrt(sumDouble(0 until pixelList.size) { i ->
        pixelList.map { (it.intensity - mu).squared() }[i] } / (pixelList.size - 1))
    }
    val sumMean get() = getSumPairs().average()
    val sumStd get() = getSumPairs().let { pairs ->
        sqrt(sumDouble(0 until pairs.size) { i ->
            pairs.map { (it - pairs.average()).squared() }[i] } / (pairs.size - 1))
    }
    val diffMean get() = getDiffPairs().average()
    val diffStd get() = getDiffPairs().let { pairs ->
        sqrt(sumDouble(0 until pairs.size) { i ->
            pairs.map { (it - pairs.average()).squared() }[i]} / (pairs.size - 1))
    }

    fun getSumPairs(): List<Double> {
        val sumPairs = mutableListOf<Double>()

        pixelList.forEach { pixel ->
            sumPairs.addAll(pixelList.filter { it != pixel }
                .map { it.intensity + pixel.intensity }
            )
        }

        return sumPairs.toList()
    }

    fun getDiffPairs(): List<Double> {
        val diffPairs = mutableListOf<Double>()

        pixelList.forEach { pixel ->
            diffPairs.addAll(pixelList.filter { it != pixel }
                .map { (it.intensity - pixel.intensity).absoluteValue }
            )
        }

        return diffPairs.toList()
    }
}

class FuzzyConnectedness(val seed: PixelCoordinates):
    PipelineFilter2D {
    override fun filter(input: DoubleArray2D, destination: MutableDoubleArray2D) {
        val seedPixel = Pixel(input[seed.x, seed.y], seed)
        val pixelQueue: Queue<Pixel> = ArrayDeque()

        // Initialization
        var currRegion = getProtoAdjacent(seedPixel, input)
        pixelQueue.addAll(currRegion.pixelList.filter { it != seedPixel })
        destination.applyElementWise { it * 0.0 }
        pixelQueue.forEach { pixel ->
            destination[pixel.coordinates.x, pixel.coordinates.y] = 1.0
        }

        while (pixelQueue.isNotEmpty()) {
            val nextPixel = pixelQueue.poll()
            currRegion = getProtoAdjacent(nextPixel, input)
            val currAffinity = min(destination[currRegion.position.x, currRegion.position.y],spelAffinity(currRegion, nextPixel))

            if (currAffinity > destination[nextPixel.coordinates.x, nextPixel.coordinates.y]) {
                pixelQueue.add(nextPixel)
                destination[nextPixel.coordinates.x, nextPixel.coordinates.y] = currAffinity
            }
        }
    }

    private fun spelAffinity(pixelRegion: ProtoRegion, pixel: Pixel): Double =
        (gaussian(pixelRegion.mainPixel.intensity + pixel.intensity, pixelRegion.sumMean, pixelRegion.sumStd) +
                gaussian((pixelRegion.mainPixel.intensity - pixel.intensity).absoluteValue, pixelRegion.diffMean, pixelRegion.diffMean)) / 2.0

    private fun gaussian(x: Double, mean: Double, std: Double) = exp(-(x - mean).squared() / (2.0 * std.squared()))

    private fun getProtoAdjacent(pixel: Pixel, input: DoubleArray2D) =
        ProtoRegion(pixel,
            List(9) { i ->
                val coordX: Int = if (i > 2 || i < input.shape0 - 2)
                    (i + pixel.coordinates.x - 1) %
                            (pixel.coordinates.x + 1)
                else
                    0
                val coordY: Int = if (i > 2 || i < input.shape0 - 2)
                    (i + pixel.coordinates.y - 1) /
                            (pixel.coordinates.y + 1)
                else
                    0
                Pixel(
                    intensity = input[coordX, coordY],
                    coordinates = PixelCoordinates(x = coordX, y = coordY)
                )
            }
        )

}