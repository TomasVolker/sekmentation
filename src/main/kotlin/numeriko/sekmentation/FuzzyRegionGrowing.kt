package numeriko.sekmentation

import tomasvolker.numeriko.core.functions.average
import tomasvolker.numeriko.core.interfaces.array1d.integer.IntArray1D
import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D
import tomasvolker.numeriko.core.interfaces.array2d.double.MutableDoubleArray2D
import tomasvolker.numeriko.core.interfaces.factory.toDoubleArray1D
import tomasvolker.numeriko.core.operations.stack
import tomasvolker.numeriko.core.primitives.productDouble
import tomasvolker.numeriko.core.primitives.sqrt
import tomasvolker.numeriko.core.primitives.squared
import tomasvolker.numeriko.core.primitives.sumDouble
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.exp
import kotlin.math.sqrt

data class PixelCoordinates(val x: Int, val y: Int)

data class Pixel(val intensity: Double, val coordinates: PixelCoordinates)

data class ProtoRegion(val mainPixel: Pixel, val pixelList: List<Pixel>) {

    val mean get() = pixelList.fold(pixelList[0].intensity) { acc, pixel -> acc + pixel.intensity } / pixelList.size
    val std get() = mean.let { mu -> pixelList.fold((pixelList[0].intensity - mu).squared()) { acc, pixel ->
        acc + (pixel.intensity - mu).squared()
    } / pixelList.size }

    fun addRegionValues(other: List<Pixel>): List<Pixel> =
            List(pixelList.size) { i -> Pixel(pixelList[i].intensity + other[i].intensity, other[i].coordinates) }

    fun absDiffRegionValues(other: List<Pixel>): List<Pixel> =
            List(pixelList.size) { i -> Pixel((pixelList[i].intensity - other[i].intensity).absoluteValue, other[i].coordinates) }

    fun add(other: ProtoRegion) =
        ProtoRegion(
            mainPixel = Pixel(mainPixel.intensity + other.mainPixel.intensity, other.mainPixel.coordinates),
            pixelList = addRegionValues(other.pixelList)
        )

    fun absDifference(other: ProtoRegion) =
            ProtoRegion(
                mainPixel = Pixel((mainPixel.intensity - other.mainPixel.intensity).absoluteValue, other.mainPixel.coordinates),
                pixelList = absDiffRegionValues(other.pixelList)
            )
}

class FuzzyRegionGrowing(val seed: PixelCoordinates, val neighboorhoodSize: Int): PipelineFilter2D {
    override fun filter(input: DoubleArray2D, destination: MutableDoubleArray2D) {
        // TODO
    }

    private fun spelAffinity(pixelRegion: ProtoRegion, other: ProtoRegion): Double {
        val regionSum: ProtoRegion = pixelRegion.add(other)
        val regionDiff: ProtoRegion = pixelRegion.absDifference(other)

        return (gaussian(regionSum.mainPixel.intensity, regionSum.mean, regionSum.std) + gaussian(regionDiff.mainPixel.intensity, regionDiff.mean, regionDiff.std)) / 2.0
    }

    private fun gaussian(x: Double, mean: Double, std: Double) = exp(-(x - mean).squared() / (2.0 * std.squared()))

    private fun getProtoAdjacent(pixel: Pixel, input: DoubleArray2D) =
            ProtoRegion(pixel,
                List(neighboorhoodSize) { i ->
                    Pixel(
                        intensity = input[i + pixel.coordinates.x - sqrt(neighboorhoodSize).toInt(),i + pixel.coordinates.y - sqrt(neighboorhoodSize).toInt()],
                        coordinates = PixelCoordinates(x = i + pixel.coordinates.x - sqrt(neighboorhoodSize).toInt(),
                            y = i + pixel.coordinates.y - sqrt(neighboorhoodSize).toInt())
                    )
                }
            )

}