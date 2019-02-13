package numeriko.sekmentation.levelset

import numeriko.sekmentation.visualization.Grid2D
import numeriko.som.PanZoom
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.colorBuffer
import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D
import tomasvolker.numeriko.core.interfaces.array2d.double.elementWise
import tomasvolker.numeriko.core.interfaces.array2d.generic.forEachIndex
import tomasvolker.numeriko.core.interfaces.factory.doubleArray2D
import tomasvolker.numeriko.core.primitives.indicator
import kotlin.random.Random

fun main() {

    val image = doubleArray2D(400, 400) { i0, i1 -> (i0 >= 50+ Random.nextInt(-2, 2)).indicator() }

    val phi = doubleArray2D(400, 400) { i0, i1 -> (i1 >= 50).indicator() }

    val (phix, phiy) = phi.computeGradients()

    plotImage(phi)

}

fun DoubleArray2D.normalizeContrast(): DoubleArray2D {
    val min = min() ?: 0.0
    val delta = (max() ?: 1.0) - min
    return elementWise { (it - min) / delta }
}

fun plotImage(image: DoubleArray2D) {

    val normalized = image.normalizeContrast()

    application {
        configure {
            width = image.shape0
            height = image.shape1
            windowResizable = true
        }
        program {
            extend(PanZoom())
            extend(Grid2D())

            val buffer = colorBuffer(image.shape0, image.shape1)

            val shadow = buffer.shadow

            shadow.buffer.rewind()
            normalized.forEachIndex { i0, i1 ->
                shadow[i0, i1] = ColorRGBa.WHITE.shade(image[i0, i1])
            }
            shadow.upload()

            extend {
                drawer.image(buffer)
            }
        }
    }

}
