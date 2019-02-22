package numeriko.sekmentation.visualization

import numeriko.sekmentation.levelset.normalizeContrast
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.ColorBuffer
import org.openrndr.draw.colorBuffer
import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D
import tomasvolker.numeriko.core.interfaces.array2d.generic.forEachIndex

fun plotImage(image: DoubleArray2D) {

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

            buffer.write(image.normalizeContrast())

            extend {
                drawer.image(buffer)
            }
        }
    }

}

fun ColorBuffer.write(image: DoubleArray2D) {

    shadow.buffer.rewind()
    image.forEachIndex { i0, i1 ->
        shadow[i0, i1] = ColorRGBa.WHITE.shade(image[i0, i1])
    }
    shadow.upload()

}
