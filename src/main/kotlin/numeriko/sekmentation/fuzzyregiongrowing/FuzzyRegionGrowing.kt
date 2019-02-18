package numeriko.sekmentation.fuzzyregiongrowing

import numeriko.sekmentation.*
import tomasvolker.numeriko.core.interfaces.array1d.double.MutableDoubleArray1D
import tomasvolker.numeriko.core.interfaces.array1d.double.applyElementWise
import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D
import tomasvolker.numeriko.core.interfaces.array2d.double.MutableDoubleArray2D
import tomasvolker.numeriko.core.interfaces.factory.doubleArray2D
import tomasvolker.numeriko.core.interfaces.factory.doubleZeros
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO


class FuzzyRegionGrowing(val kernel: FilterKernel<DoubleArray2D>,
                         val nBins: Int = 256,
                         val pixelSeed: PixelCoordinates,
                         val neighborhoodSize: Int = 8): PipelineFilter2D {

    override fun filter(input: DoubleArray2D, destination: MutableDoubleArray2D) {
        val auxMatrix: MutableDoubleArray2D = destination.copy()

        HistogramEquilizationFilter(nBins).filter(input, auxMatrix)
        MedianFilter2D(kernel).filter(auxMatrix, destination)
        FuzzyConnectedness(pixelSeed, neighborhoodSize).filter(destination, auxMatrix)
        ThresholdFilter(nBins).filter(auxMatrix, destination)
    }
}


fun loadImage(filename: String): DoubleArray2D {
    val image = ImageIO.read(File(filename)).data
    val imageArray: MutableDoubleArray1D = doubleZeros(image.height * image.width).asMutable()
    image.getPixels(0,0, image.width, image.height, imageArray.toDoubleArray())

    return doubleArray2D(image.width, image.height) { i0, i1 -> imageArray[i0*i1] }
}


fun medianKernel(input: DoubleArray2D) =
        doubleArray2D(input.shape0, input.shape1) { i0, i1 -> input[i0,i1] }


fun main() {
    val testImage = loadImage("data/P1_Image_originale.png")
//    val pixelSeed = PixelCoordinates()
}