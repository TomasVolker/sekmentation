package numeriko.sekmentation.fuzzyregiongrowing

import numeriko.sekmentation.*
import tomasvolker.kyplot.dsl.showImage
import tomasvolker.kyscript.KyScriptConfig
import tomasvolker.numeriko.core.dsl.D
import tomasvolker.numeriko.core.dsl.I
import tomasvolker.numeriko.core.functions.normalized
import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D
import tomasvolker.numeriko.core.interfaces.array2d.double.MutableDoubleArray2D
import tomasvolker.numeriko.core.interfaces.array2d.double.elementWise
import tomasvolker.numeriko.core.interfaces.array2d.generic.toListOfLists
import tomasvolker.numeriko.core.interfaces.factory.doubleArray2D
import tomasvolker.numeriko.core.interfaces.factory.doubleZeros
import tomasvolker.numeriko.core.primitives.squared
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.pow


fun DoubleArray2D.normalized() =
        sum().let { sum -> this.elementWise { it / sum } }

class FuzzyRegionGrowing(val kernel: FilterKernel<DoubleArray2D>,
                         val nBins: Int = 256,
                         val pixelSeed: PixelCoordinates,
                         val neighborhoodSize: Int = 8): PipelineFilter2D {

    override fun filter(input: DoubleArray2D, destination: MutableDoubleArray2D) {
        val auxMatrix: MutableDoubleArray2D = destination.copy()

        HistogramEqualizationFilter().filter(input, auxMatrix).also { println("Histogram eq done") }
        showImage { data = auxMatrix.toListOfLists() }
        MedianFilter2D(kernel).filter(auxMatrix, destination).also { println("Median filter done") }
        showImage { data = destination.toListOfLists() }
        FuzzyConnectedness(pixelSeed, neighborhoodSize).filter(destination, auxMatrix).also { println("Fuzzy done") }
        showImage { data = auxMatrix.toListOfLists() }
        ThresholdFilter(nBins).filter(auxMatrix, destination).also { println("Threshold done") }
    }
}


fun loadImage(filename: String): DoubleArray2D =
        ImageIO.read(File(filename)).let { image ->
            doubleArray2D(image.height, image.width) { i1, i0 ->
                image.getRGB(i0, i1) / ((-256.0).pow(3))
            }
        }


fun main() {
    KyScriptConfig.defaultPythonPath = "python"

    val testImage = loadImage("data/P1_Image_originale.png")
    val pixelSeed = PixelCoordinates(testImage.shape0 - 362, testImage.shape1 - 240)
    val filteredImage: MutableDoubleArray2D = doubleZeros(testImage.shape0, testImage.shape1).asMutable()
    val medianKernel = FilterKernel(D[D[1,1,1],D[1,2,1],D[1,1,1]], I[3,3])

    FuzzyRegionGrowing(medianKernel, pixelSeed = pixelSeed).filter(testImage, filteredImage)

    showImage { data = filteredImage.toListOfLists() }
}